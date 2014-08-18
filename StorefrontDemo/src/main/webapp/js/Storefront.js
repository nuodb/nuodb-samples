/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * This file contains Storefront controller logic. It's all encapsulated in the "Storefront" global namespace.
 */

var Storefront = {
    init: function(cfg, showHeader) {
        var me = this;

        // Set basic app properties
        me.currency = cfg.currency;
        me.appInstanceUuid = cfg.appInstanceUuid;
        me.regions = me.aggregateRegions(cfg.appInstances);

        // Initialize elements shared across pages
        me.initSearchBox();
        if (window.self === window.top) {
            $('#admin-link').show();

            if (showHeader && window.NuoHeader) {
                NuoHeader.render({
                    appTitle: 'NuoDB Storefront Demo',
                    homeUrl: './',
                    sidebarClick: function() {
                        document.location.href = $('#admin-link a').attr('href');
                    },
                    sidebarTip: 'Show control panel',
                    username: cfg.customer.displayName
                });

                // Don't show username, because it's in the shared header
                $('#top-bar .username, #top-bar .divider-vertical').hide();
            }
        }

        $('.alert .btn').click(function() {
            var buttons = $('.btn', $(this).closest('form'));
            setTimeout(function() {
                buttons.attr('disabled', 'disabled');
            }, 0);
        });

        // Initialize page-specific elements
        switch (cfg.pageName) {
            case "control-panel":
                me.initControlPanelPage(cfg);
                break;

            case "store/products":
                me.initProductsPage(cfg.pageData.products, cfg.pageData.categories, cfg.pageData.filter);
                break;

            case "store/product":
                me.initProductPage(cfg.pageData, cfg.customer);
                break;

            case "store/cart":
                me.initCartPage(cfg.pageData);
                break;
        }

        // Show accumulated messages
        me.TemplateMgr.applyTemplate('tpl-messages', '#messages', cfg.messages);
    },

    initSearchBox: function() {
        var me = this;
        $('.search').click(function(event) {
            var txt = $('.search-query', this);
            if ($(event.target).hasClass('search-clear')) {
                // "X" button was clicked to clear out search box
                txt.val('').trigger('change').trigger('clear');
            } else {
                txt.select();
            }
            txt.focus();
        }).change(function() {
            // Toggle "X" icon in search box based on whether there's content in the box
            var txt = $('.search-query', this);
            var ico = $('.search-icon', this);
            ico[txt.val() ? 'addClass' : 'removeClass']('search-icon-clear');
        });
    },

    aggregateRegions: function(appInstances) {
        var me = this;

        // Aggregate instances to region level
        var regionMap = {};
        var regions = [];
        for ( var i = 0; i < appInstances.length; i++) {
            var instance = appInstances[i];
            var regionObj;
            if (!(regionObj = regionMap[instance.region])) {
                regionMap[instance.region] = regionObj = {
                    storeName: instance.name,
                    regionName: instance.region,
                    currency: instance.currency,
                    instances: [],
                };
                regions.push(regionObj);
            }
            regionObj.instances.push(instance);
            if (Storefront.appInstanceUuid == instance.uuid) {
                regionObj.selected = true;
            } else if (instance.currency != regionObj.currency) {
                instance.currency = 'MIXED';
            }
        }

        // Sort regions by name
        regions.sort(function(a, b) {
            return (a.name < b.name) ? -1 : (a.name == b.name) ? 0 : 1;
        });

        // Initialize region dropdown
        me.initRegionSelectorMenu(regions, regionMap);

        return regions;
    },

    initRegionSelectorMenu: function(regions, regionMap) {
        var me = this;

        if ($('#region-menu').length == 0) {
            // No menu on this page
            return;
        }

        // Render menu template
        me.TemplateMgr.applyTemplate('tpl-region-menu', '#region-menu', {
            regions: regions
        });

        // Hook menu clicks
        $('#region-menu').on('click', 'li > a', function() {
            // Get region
            var region = regionMap[$(this).attr('data-region')];

            if (!region) {
                setTimeout(function() {
                    var buff = [];
                    buff.push('To run the Storefront across multiple regions, start additional instances of the Storefront with connection strings pointed to NuoDB brokers running in other regions.');
                    buff.push('Use NuoDB version 2.0 (or greater) to take advantage of this feature.\n\n');
                    buff.push('See the NuoDB documentation for more information.');
                    alert(buff.join(''));
                }, 0);
                return;
            }

            // Choose a random instance to navigate to
            var instance = region.instances[Math.floor(region.instances.length * Math.random())];
            document.location.href = instance.url;
        });
    },

    initProductsPage: function(products, categories, filter) {
        var me = this;
        me.filter = filter;

        // Render category list
        me.TemplateMgr.applyTemplate('tpl-category-nav', '#category-nav', categories);

        // Render product list
        me.TemplateMgr.applyTemplate('tpl-product-list', '#product-list', products);

        // Handle infinite scrolling
        me.paginator = $('#paginator');
        $(window).scroll(function() {
            if ($(window).scrollTop() + $(window).height() >= $('#product-list').offset().top + $('#product-list').height() - 500) {
                if (!me.paginator.is(':visible') && me.paginator.hasClass('loading')) {
                    me.paginator.show();
                    me.filter.page++;
                    me.updateProductList(true);
                }
            }
        });

        // Handle sort events
        $('#product-sort').on('click', 'a', function(event) {
            event.preventDefault();
            me.filter.sort = $(this).attr('data-sort');
            $('#product-sort-label').html($(this).html());
            me.updateProductList();
        });

        // Handle category clicks
        $('#category-nav').on('click', 'a', function() {
            // Toggle selection of this category
            var category = $(this).parent().attr('data-category');
            var categories = me.filter.categories || [];
            var idx = $.inArray(category, categories);
            if (idx >= 0) {
                //categories.splice(idx, 1);
                categories = [];
            } else {
                //categories.push(category);
                categories = [category];
            }
            me.filter.categories = categories;
            me.updateProductList();
        });

        // Avoid POST for search on this page -- use AJAX instead
        $('form.search').submit(function(e) {
            $('.search').change();
            return false;
        });
        $('.search').change(function() {
            var txt = $('.search-query', this);
            if (me.filter.matchText != txt.val()) {
                me.filter.matchText = txt.val();
                me.updateProductList();
            }
        });

        // Initialize UI elements on page outside the templates
        me.syncProductsPage(products);
    },

    initProductPage: function(product, customer) {
        var me = this;

        me.TemplateMgr.applyTemplate('tpl-product', '#product', product);

        // Handle "Add to Cart" form submit
        $('form.add-to-cart').submit(function(event) {
            event.preventDefault();
            $.ajax('api/customer/cart', {
                cache: false,
                data: {
                    productId: product.id,
                    quantity: parseInt($('[name=quantity]', this).val())
                },
                dataType: 'json',
                type: 'PUT'
            }).done(function(responseData) {
                document.location.href = "cart";
            });
        });

        // Initialize review form fields
        $('form.add-review [name=emailAddress]').val(customer.emailAddress);

        // Focus on first field when review dialog opens
        $('#dlg-add-review').on('shown', function() {
            $('[name=title]').focus();
        });

        // Handle "Add Review" form submit
        $('form.add-review').submit(function(event) {
            event.preventDefault();

            // Validate form
            var rating = $('[name=rating]', this).val();
            if (!rating) {
                alert("Please select a star rating first.");
                return;
            }

            $.ajax('api/products/' + encodeURIComponent(product.id) + '/reviews', {
                cache: false,
                data: {
                    title: $('[name=title]', this).val(),
                    comments: $('[name=comments]', this).val(),
                    emailAddress: $('[name=emailAddress]', this).val(),
                    rating: rating
                },
                dataType: 'json',
                type: 'POST'
            }).done(function(responseData) {
                document.location.reload();
            });
        });
    },

    initCartPage: function(cart) {
        var me = this;
        me.TemplateMgr.applyTemplate('tpl-cart', '#cart', cart);
    },

    syncProductsPage: function(data) {
        var me = this;

        // Reset paginator
        var hasNext = (me.filter.page < Math.floor(data.totalCount / me.filter.pageSize));
        me.paginator.hide();
        me.paginator[hasNext ? 'addClass' : 'removeClass']('loading');

        // Update search box 
        if (me.filter.matchText) {
            $('#search').val(me.filter.matchText).trigger('change');
        }

        // Update sort selection
        if (me.filter.sort) {
            var sortLabel = $('#product-sort [data-sort=' + me.filter.sort + ']').html();
            if (sortLabel) {
                $('#product-sort-label').html(sortLabel);
            }
        }

        // Update "all items" label
        $('#lbl-all-items').html(((data.totalCount != 1) ? data.totalCount.format(0) + ' products' : '1 product'));

        // Select active categories
        $('#category-nav li').removeClass('active');
        var categories = me.filter.categories;
        if (categories) {
            for ( var i = 0; i < categories.length; i++) {
                $('#category-nav li[data-category="' + categories[i] + '"]').addClass('active');
            }
        }

        delete me.updateRequest;
    },

    updateProductList: function(append) {
        var me = this;
        if (me.updateRequest) {
            if (append) {
                return;
            }
            me.updateRequest.abort();
        }
        if (!append) {
            me.filter.page = 1;
        }
        me.updateRequest = me.TemplateMgr.autoFillTemplate('product-list', 'api/products', me.filter, $.proxy(me.syncProductsPage, me), append);
    }
};

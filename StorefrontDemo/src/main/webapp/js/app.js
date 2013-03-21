var Storefront = {
    init: function() {
        var me = this;
        me.params = $.parseParams(document.location.search);
        me.Products.filter.matchText = me.params.search;
        if (me.Products.filter.matchText) {
            $('#search').attr('value', me.Products.filter.matchText);
        }

        me.TemplateMgr.autoFillTemplate('category-nav', 'api/categories');
        me.TemplateMgr.autoFillTemplate('cart', 'api/customer/cart', null, $.proxy(me.initCartPage, me));
        me.TemplateMgr.autoFillTemplate('product', 'api/products/' + encodeURIComponent(me.params.productId), null, $.proxy(me.initProductPage, me));
        me.autoFillProductList();
    },
    
    initProductListPage: function(data) {
        var me = this;
        if (!me.paginator) {
            me.paginator = $('#paginator');
    
            $(window).scroll(function() {
                if ($(window).scrollTop() >= $(document).height() - $(window).height() - 100) {
                    if (me.paginator.is(':visible')) {
                        me.paginator.hide();
                        me.Products.filter.page++;
                        me.autoFillProductList(true);
                    }
                }
            });
    
            $('#product-sort').on('click', 'a', function(event) {
                event.preventDefault();
                me.Products.filter.sort = $(this).attr('data-sort');
                $('#product-sort-label').html($(this).html());
                me.autoFillProductList();
            });
        }
        
        // Reset paginator
        var filter = me.Products.filter;
        var hasNext = (filter.page < Math.floor(data.totalCount / filter.pageSize));
        me.paginator[hasNext ? 'show' : 'hide']();        
    },

    initProductPage: function() {
        var me = this;
        $('form.add-to-cart').submit(function(event) {
            event.preventDefault();
            $.ajax('api/customer/cart', {
                cache: false,
                data: {
                    productId: me.params.productId,
                    quantity: parseInt($('[name=quantity]', this).val())
                },
                dataType: 'json',
                type: 'PUT'
            }).done(function(responseData) {
                document.location.href = "cart.jsp";
            });
        });
        
        $('form.add-review').submit(function(event) {
            event.preventDefault();
            $.ajax('api/products/' + encodeURIComponent(me.params.productId) + '/reviews', {
                cache: false,
                data: {
                    title: $('[name=title]', this).val(),
                    comments: $('[name=comments]', this).val(),
                    emailAddress: $('[name=emailAddress]', this).val(),
                    rating: $('[name=rating]', this).val()
                },
                dataType: 'json',
                type: 'POST'
            }).done(function(responseData) {
                document.location.reload();
            });
        });
    },
    
    initCartPage: function(data) {
        var total 
    },
    
    autoFillProductList: function(append) {
        var me = this;
        var filter = me.Products.filter;
        return me.TemplateMgr.autoFillTemplate('product-list', 'api/products', filter, $.proxy(me.initProductListPage, me), append);
    }
};

Storefront.Products = {
    filter: {
        matchText: null,
        categories: null,
        page: 1,
        pageSize: 30,
        sort: 'RELEVANCE'
    }
};

Storefront.TemplateMgr = {
    templates: {},

    autoFillTemplate: function(name, url, qsData, callback, append) {
        var me = this;
        var templateName = 'tpl-' + name;
        var el = '#' + name;

        if (!me.hasTemplate(templateName)) {
            return false;
        }

        $.ajax(url, {
            cache: false,
            data: qsData,
            dataType: 'json'
        }).done(function(responseData) {
            me.applyTemplate(templateName, el, responseData, append);

            // Initialize star ratings
            $('.rateit', el).rateit();

            if (callback) {
                callback(responseData);
            }
        });
        return true;
    },

    hasTemplate: function(name) {
        return !!this.getTemplate(name);
    },

    getTemplate: function(name) {
        var me = this;
        template = me.templates[name];
        if (template === undefined) {
            var template = $('#' + name);
            if (!template.length) {
                template = null;
            } else {
                template = Handlebars.compile(template.html());
            }
        }
        me.templates[name] = template;
        return template;
    },

    applyTemplate: function(name, selector, data, append) {
        var template = this.getTemplate(name);
        var html = template(data);
        if (append) {
            $(selector).append(html);
        } else {
            $(selector).html(html);
        }
    }
}

Handlebars.registerHelper('priceFormat', function(price) {
    return '$' + price.toFixed(2);
});

Handlebars.registerHelper('dateFormat', function(date) {
    return new Date(date);
});

(function($) {
    var re = /([^&=]+)=?([^&]*)/g;
    var decode = function(str) {
        return decodeURIComponent(str.replace(/\+/g, ' '));
    };
    $.parseParams = function(query) {
        var params = {}, e;
        if (query) {
            if (query.substr(0, 1) == '?') {
                query = query.substr(1);
            }

            while (e = re.exec(query)) {
                var k = decode(e[1]);
                var v = decode(e[2]);
                if (params[k] !== undefined) {
                    if (!$.isArray(params[k])) {
                        params[k] = [params[k]];
                    }
                    params[k].push(v);
                } else {
                    params[k] = v;
                }
            }
        }
        return params;
    };
})(jQuery);

$(document).ready(function() {
    Storefront.init();
});

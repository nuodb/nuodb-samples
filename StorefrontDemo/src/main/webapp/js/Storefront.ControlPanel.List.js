/* Copyright (c) 2013 NuoDB, Inc. */

(function() {
    "use strict";

    var LIST_UPDATE_INTERVAL_MS = 1000;

    var g_app;
    var g_lastItems = null;

    Storefront.initControlPanelProductsPage = function(productInfo) {
        g_app = this;
        g_app.TemplateMgr.applyTemplate('tpl-product-info', '#product-info', productInfo);
        $('#lbl-products').text((productInfo.productCount || 0).format(0));
    }

    Storefront.initControlPanelProcessesPage = function(processes) {
        g_app = this;

        // Hook shutdown events
        $('#list').on('click', '.btn-danger', function() {
            if (!confirm('Are you sure you want to shut down this node?')) {
                return;
            }
            var row$ = $(this).closest('tr');
            var uid = row$.attr('data-uid');
            $.ajax({
                method: 'DELETE',
                url: 'api/processes/' + uid
            }).fail(function(xhr, status, statusMsg) {
                if (xhr.status == 200) {
                    // Not actually an error, jQuery just couldn't parse the empty response
                    row$.fadeOut();
                } else {
                    var msg;
                    try {
                        msg = JSON.parse(xhr.responseText).message;
                    } catch (e) {
                    }
                    alert(msg || statusMsg);
                }
            });
        });

        var transformProcessList = function(processes) {
            // Sort by region, then host, then type
            processes.sort(function(a, b) {
                return compare(a.region, b.region) || compare(a.address, b.address) || compare(a.type, b.type);
            });

            // Apply icon based on type
            for ( var i = 0; i < processes.length; i++) {
                var process = processes[i];
                switch (process.type) {
                    case 'SM':
                        process.typeName = 'Storage manager';
                        process.icon = 'icon-hdd';
                        break;

                    case 'TE':
                        process.typeName = 'Transaction engine';
                        process.icon = 'icon-cog';
                        break;

                    default:
                        process.typeName = node.type;
                        process.icon = 'icon-question-sign';
                        break;
                }
            }

            return processes;
        }

        renderList(processes, 'api/processes', transformProcessList);
    }

    Storefront.initControlPanelRegionsPage = function(regions) {
        g_app = this;
        renderList(regions, 'api/stats/regions', null);
    }

    function renderList(origItems, updateUrl, transformFunc) {
        var render = function(items) {
            if (transformFunc) {
                items = transformFunc(items);
            }
            if (!areListsEqual(items, g_lastItems)) {
                g_lastItems = items;
                g_app.TemplateMgr.applyTemplate('tpl-list', '#list', items);
            }
        }

        var autoUpdate = function() {
            $.ajax({
                method: 'GET',
                url: updateUrl,
                cache: false
            }).done(function(items) {
                render(items);
            }).always(function() {
                setTimeout(autoUpdate, LIST_UPDATE_INTERVAL_MS);
            });
        };

        render(origItems);
        autoUpdate();
    }

    function areListsEqual(list1, list2) {
        if (!list1 || !list2 || list1.length != list2.length) {
            return false;
        }
        for ( var i = 0; i < list1.length; i++) {
            var item1 = list1[i];
            var item2 = list2[i];
            for ( var key in item1) {
                if (item1[key] != item2[key]) {
                    return false;
                }
            }
        }
        return true;
    }
})();

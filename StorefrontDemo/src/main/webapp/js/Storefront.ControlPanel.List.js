/* Copyright (c) 2013-2015 NuoDB, Inc. */

(function() {
    "use strict";

    var LIST_UPDATE_INTERVAL_MS = 1000;

    var g_app;
    var g_lastItems = null;

    Storefront.initControlPanelProductsPage = function(stats) {
        g_app = this;
        g_app.TemplateMgr.applyTemplate('tpl-product-info', '#product-info', stats);
        $('#btn-delete').click(function() {
            return confirm('Are you sure you want to delete all product and customer data?\n\nIf you proceed, you\'ll be able to recreate the product catalog from several sources.');
        });
    };

    Storefront.initControlPanelDatabasePage = function(data) {
        g_app = this;

        var dbName = data.dbConnInfo.dbName;
        data.apiUrl += '/databases/' + encodeURIComponent(dbName);
        if (data.db) {
            data.dbStatusColor = (data.db.status == 'RUNNING') ? 'success' : '';
            $('#btn-console').attr('href', g_app.fixupHostname(data.adminConsoleUrl) + '#databases/' + encodeURIComponent(dbName));
            $('#btn-explorer').attr('href', g_app.fixupHostname(data.sqlExplorerUrl) + "?db=" + encodeURIComponent(dbName));
        } else {
            data.dbStatusColor = 'important';
            $('#btn-console, #btn-explorer').hide();
        }
        $('#ddl').val(data.ddl);

        g_app.TemplateMgr.applyTemplate('tpl-db-info', '#db-info', data);
    };

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
                    var msg = null;
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
                        process.typeName = 'Storage Manager';
                        process.icon = 'icon-hdd';
                        break;

                    case 'TE':
                        process.typeName = 'Transaction Engine';
                        process.icon = 'icon-cog';
                        break;

                    default:
                        process.typeName = node.type;
                        process.icon = 'icon-question-sign';
                        break;
                }
            }

            return processes;
        };

        renderList(processes, 'api/processes', transformProcessList);
    };

    Storefront.initControlPanelRegionsPage = function(regions) {
        g_app = this;
        for ( var i = 0; i < regions.length; i++) {
            var region = g_app.regionMap[regions[i].region];
            if (region) {
                regions[i].instances = region.instances;
                regions[i].multiInstance = region.instances.length > 1;
            } else {
                regions[i].instances = [];
            }
        }
        renderList(regions, 'api/stats/regions', null);
    };

    function renderList(origItems, updateUrl, transformFunc) {
        var render = function(items) {
            if (transformFunc) {
                items = transformFunc(items);
            }
            if (!areListsEqual(items, g_lastItems)) {
                g_lastItems = items;
                g_app.TemplateMgr.applyTemplate('tpl-list', '#list', items);
            }
        };

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
                    if (!$.isArray(item1[key]) || !areListsEqual(item1[key], item2[key])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
})();

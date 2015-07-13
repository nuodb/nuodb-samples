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
        $('#ddl').html(ddlColor(data.ddl));

        g_app.TemplateMgr.applyTemplate('tpl-db-info', '#db-info', data);

        $('#btn-copy-ddl').click(function() {
            copyToClipboard($('#ddl')[0]);
        });
    };

    Storefront.initControlPanelProcessesPage = function(processes) {
        g_app = this;

        hookListItemShutdown('process', 'api/processes/');

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

                    case 'SSM':
                        process.typeName = 'Snapshot Storage Manager';
                        process.icon = 'icon-camera';
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

    Storefront.initControlPanelTenantsPage = function(tenants) {
        g_app = this;

        hookListItemShutdown('tenant', 'api/tenants/');

        renderList(tenants, 'api/tenants', null);
    };

    function hookListItemShutdown(noun, apiPath) {
        $('#list').on('click', '.btn-danger', function() {
            if (!confirm('Are you sure you want to shut down this ' + noun + '?')) {
                return;
            }

            var row$ = $(this).closest('tr');
            var uid = row$.attr('data-uid');

            function onSuccess() {
                row$.fadeOut();
                if (uid == Storefront.tenant) {
                    window.top.location.href = "./admin";
                }
            }

            $.ajax({
                method: 'DELETE',
                url: apiPath + uid + '?tenant=' + encodeURIComponent(Storefront.tenant)
            })
            .done(onSuccess)
            .fail(function(xhr, status, statusMsg) {
                if (xhr.status == 200) {
                    // Not actually an error, jQuery just couldn't parse the empty response
                    onSuccess();
                } else {
                    var msg = null;
                    try {
                        msg = JSON.parse(xhr.responseText).message;
                    } catch (e) {
                    }
                    alert(msg || statusMsg || 'Unable to shut down ' + noun);
                }
            });
        });
    }
    ;

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
                url: updateUrl + '?tenant=' + encodeURIComponent(Storefront.tenant),
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
            if (!areObjectsEqual(list1[i], list2[i])) {
                return false;
            }
        }
        return true;
    }

    function areObjectsEqual(item1, item2) {
        if (!$.isPlainObject(item1)) {
            return item1 == item2;
        }
        for ( var key in item1) {
            var item1v = item1[key];
            var item2v = item2[key];
            if (item1v != item2v) {
                if ($.isArray(item1v)) {
                    if (!areListsEqual(item1v, item2v)) {
                        return false;
                    }
                } else if ($.isPlainObject(item1v)) {
                    if (!areObjectsEqual(item1v, item2v)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    function ddlColor(ddl) {
        // Keywords
        ddl = ddl.replace(/([\s])((create|alter|drop) (table|index)|generated by default as identity|if exists|not|primary key|references|foreign key|add constraint|on)(?=[\s;])/gi, '$1<span class="ddl-keyword">$2</span>');

        // Types
        ddl = ddl.replace(/([\s])(bigint|timestamp|character varying|boolean|integer|float|numeric|null)(?=[\s\(,])/gi, '$1<span class="ddl-type">$2</span>');

        // Numbers
        ddl = ddl.replace(/([^A-Z_])([0-9]+)(?=[^A-Z_])/gi, '$1<span class="ddl-number">$2</span>');

        // Comments
        ddl = ddl.replace(/(\n|^)(--.+?)(\n|$)/gi, '$1<span class="ddl-comment">$2</span>$3');

        return ddl;
    }
})();

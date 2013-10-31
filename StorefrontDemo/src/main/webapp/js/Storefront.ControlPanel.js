/* Copyright (c) 2013 NuoDB, Inc. */

(function() {
    "use strict";

    var app;
    var regionData = null;
    var minHeavyCpuUtilizationPct = 90;

    Storefront.initControlPanelPage = function(cfg) {
        var pageData = cfg.pageData;
        app = this;        
        regionData = initRegionData(app.regions, pageData.stats);

        if (!jQuery.support.cors && regionData.instanceCount > 1) {
            cfg.messages.push({
                severity: 'WARNING',
                message: 'Your browser does not support CORS, which is needed for this control panel to communicate with other Storefront instances.  The statistics you see here may be incomplete or inaccurate, and you may not be able to control all instances.  Please use Internet Explorer 10+ or a newer version of Chrome, Firefox, Safari, or Opera.'
            });
        }

        initCustomersTab();
        initProductsTab(pageData.productInfo);
        initNodesTab(pageData.dbNodes);

        refreshStats(pageData.stats);
    }

    function initCustomersTab() {
        // Render regions table
        app.TemplateMgr.applyTemplate('tpl-regions', '#regions', regionData);

        // Handle "Change" and "Hide" workload details buttons
        $('#regions').on('click', '.btn-change', function() {
            var row$ = $(this).closest('tr').addClass('active');
            row$.next().fadeIn();
        }).on('click', '.btn-hide', function() {
            var row$ = $(this).closest('tr').removeClass('active');
            row$.next().hide();
        });

        // Handle currency change dropdown
        $('#regions').on('click', '.currency .dropdown-menu a', function(e) {
            e.preventDefault();
            var regionName = $(this).closest('.region-overview').attr('data-region');
            var btn$ = $(this).closest('.currency').find('.btn-change-currency');
            changeCurrency(getRegionByName(regionName), $(this).attr('data-currency'), btn$);
        });

        // Handle workload "Update" button
        $('#regions').on('click', '.btn-update', function(e) {
            var regionDetails$ = $(this).closest('.region-details');
            var regionName = regionDetails$.attr('data-region');
            var inputs$ = regionDetails$.find('input');

            // Validate inputs
            for ( var i = 0; i < inputs$.length; i++) {
                var f = $(inputs$[i]);
                var max = parseInt(f.attr('max'));
                var name = f.attr('data-name');
                if (isNaN(f.val()) || (f[0].validity && !f[0].validity.valid)) {
                    f.focus();
                    alert('Please enter a number.');
                    return false;
                }
                if (!isNaN(max) && f.val() > max) {
                    f.focus();
                    alert('User count for "' + name + '" cannot exceed ' + max + '.');
                    e.preventDefault();
                    return false;
                }
                if (f.val() < 0) {
                    alert('User count for "' + name + '" cannot be negative.');
                    e.preventDefault();
                    f.focus();
                    return false;
                }
            }

            // Serialize data
            var data = inputs$.serializeObject();
            for ( var key in data) {
                data[key] = parseInt(data[key]);
            }

            // Make changes
            updateWorkloadUsers(getRegionByName(regionName), data, $(this));
        });

        // Handle <Enter> key on input field as an "Update" click
        $('#regions').on('keypress', 'input', function(e) {
            if (e.which == 13) {
                $(this).closest('.details-box').find('.btn-update').trigger('click');
            }
        });

        // Handle "Stop all" button
        $('#btn-stop-all').click(function() {
            var data = {};
            for ( var i = 0; i < regionData.workloads.length; i++) {
                data['workload-' + regionData.workloads[i].workload.name] = 0;
            }
            updateWorkloadUsers(null, data, $('.btn-update'));
        });

        // Handle refresh
        $('#btn-refresh').click(function() {
            document.location.reload();
        });

        // Handle tooltips
        $('div[data-toggle="tooltip"]').tooltip();

        // Select quantity upon focus
        $('input[type=number]').on('click', function(e) {
            $(this).select();
            $(this).focus();
        });

        // Enable HTML5 form features in browsers that don't support it
        $('form').form();
    }

    function initProductsTab(productInfo) {
        app.TemplateMgr.applyTemplate('tpl-product-info', '#product-info', productInfo);
        $('#lbl-products').text((productInfo.productCount || 0).format(0));
    }

    function initNodesTab(dbNodes) {
        // Sort by region, then address, then type
        dbNodes.sort(function(a, b) {
            var diff = compare(a.region, b.region);
            if (diff == 0) {
                diff = compare(a.address, b.address);
                if (diff == 0) {
                    diff = compare(a.type, b.type);
                }
            }
            return diff;
        });

        // Apply icon based on type
        for ( var i = 0; i < dbNodes.length; i++) {
            var node = dbNodes[i];
            node.icon = (node.type == 'Storage') ? 'icon-hdd' : 'icon-cog';
        }

        // Build list
        app.TemplateMgr.applyTemplate('tpl-node-list', '#node-list', dbNodes);
        $('#lbl-nodes').text(dbNodes.length.format(0));
    }

    function initRegionData(regions, stats) {
        var workloadTemplates = convertWorkloadMapToSortedList(stats.workloadStats);
        var workloadList = [];
        var instanceCount = 0;
        for ( var i = 0; i < regions.length; i++) {
            var region = regions[i];
            region.workloads = [];
            region.instanceCountLabel = pluralize(region.instances.length, "instance");
            region.webCustomerCount = 0;
            instanceCount += region.instances.length;

            // Initialize workload data
            for ( var j = 0; j < workloadTemplates.length; j++) {
                var workload = workloadTemplates[j];
                var workloadCopy = {
                    activeWorkerCount: 0,
                    workload: $.extend({}, workload.workload)
                };
                region.workloads.push(workloadCopy);
                if (i == 0) {
                    workloadList.push($.extend({}, workloadCopy));
                }
            }
        }

        return {
            regions: regions,
            workloads: workloadList,
            instanceCount: instanceCount,
            regionSummaryLabel: pluralize(instanceCount, "Storefront instance") + ' across ' + pluralize(regions.length, 'region')
        };
    }

    function refreshStats(localStats) {
        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];

            for ( var j = 0; j < region.instances.length; j++) {
                var instance = region.instances[j];
                if (instance.isRefreshing) {
                    break;
                }

                if (localStats && instance.uuid == Storefront.appInstanceUuid) {
                    // We already have the local stats on hand, so don't bother doing an AJAX request to re-fetch them
                    refreshInstanceStatsComplete(region, instance, localStats);
                } else {
                    refreshInstanceStats(region, instance);
                }
            }
        }
    }

    function syncInstanceStatusIndicator(region, instance) {
        syncStatusIndicator($('#regions [data-region="' + region.regionName + '"] .dropdown > a .label-status'), region);
        syncStatusIndicator($('#regions [data-instance="' + instance.uuid + '"] .label-status'), instance);
    }

    function refreshInstanceStats(region, instance) {
        instance.isRefreshing = true;
        syncInstanceStatusIndicator(region, instance);

        $.ajax({
            url: instance.url + '/api/stats?includeStorefront=true',
            cache: false
        }).done(function(stats) {
            instance.notResponding = false;
            refreshInstanceStatsComplete(region, instance, stats)
        }).fail(function() {
            instance.notResponding = true;
            refreshInstanceStatsComplete(region, instance, {
                storefrontStats: {},
                workloadStats: {}
            })
        });
    }

    function refreshInstanceStatsComplete(region, instance, stats) {
        // Update instance
        instance.isRefreshing = false;
        if (stats.storefrontStats) {
            var regStats = stats.storefrontStats[region.regionName];
            if (regStats) {
                region.webCustomerCount = regStats.activeWebCustomerCount;
            }
        }
        if (stats.appInstance) {
            instance.heavyLoad = stats.appInstance.cpuUtilization >= minHeavyCpuUtilizationPct;
        }
        if (stats.workloadStats) {
            instance.workloadStats = stats.workloadStats;
        }

        // Update region
        region.isRefreshing = false;
        region.heavyLoad = false;
        region.notResponding = false;
        for ( var i = 0; i < region.instances.length; i++) {
            var otherInstance = region.instances[i];

            if (otherInstance.isRefreshing) {
                region.isRefreshing = true;
            }
            if (otherInstance.notResponding) {
                region.notResponding = true;
            }
            if (otherInstance.heavyLoad) {
                region.heavyLoad = true;
            }
        }

        // Update status indicator at region and instance levels
        syncInstanceStatusIndicator(region, instance);

        // Update global region stats
        recalcRegionStats();
        recalcCustomerStats();
    }

    function recalcRegionStats() {
        var activeInstances = 0;
        var heavyLoadInstances = 0;
        var notRespondingInstances = 0;

        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];

            for ( var j = 0; j < region.instances.length; j++) {
                var instance = region.instances[j];
                if (instance.notResponding) {
                    notRespondingInstances++;
                } else if (instance.heavyLoad) {
                    heavyLoadInstances++;
                } else {
                    activeInstances++;
                }
            }
        }

        $('#label-active').html(activeInstances);
        $('#label-heavy-load').html(heavyLoadInstances);
        $('#label-not-responding').html(notRespondingInstances);
    }

    function recalcCustomerStats() {
        var maxRegionUserCount = 0;
        var totalSimulatedUserCount = 0;
        var totalWebCustomerCount = 0;

        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];

            // Accumulate real users (reported at region level)
            totalWebCustomerCount += region.webCustomerCount;

            // Accumulate simulated users (reported at instance level)
            var regionUserCount = region.webCustomerCount;
            for ( var j = 0; j < region.workloads.length; j++) {
                var workload = region.workloads[j];
                workload.activeWorkerCount = 0;

                for ( var k = 0; k < region.instances.length; k++) {
                    var instance = region.instances[k];

                    // Find corresponding workload in this instance
                    if (instance.workloadStats) {
                        var workloadStats = instance.workloadStats[workload.workload.name];
                        if (workloadStats) {
                            var instanceUserCount = Math.min(workloadStats.activeWorkerCount, workloadStats.activeWorkerLimit);
                            workload.activeWorkerCount += instanceUserCount;
                            regionUserCount += instanceUserCount;
                            totalSimulatedUserCount += instanceUserCount;
                        }
                    }
                }
            }

            if (regionUserCount > maxRegionUserCount) {
                maxRegionUserCount = regionUserCount;
            }
        }

        if (maxRegionUserCount == 0) {
            maxRegionUserCount = 1; // to avoid divide by 0 NaN's
        }

        // Update bar charts
        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];
            var regionOverview$ = $('.region-overview[data-region="' + region.regionName + '"]');
            var bars$ = regionOverview$.find('.progress').children();
            var detailedBars$ = $('.region-details[data-region="' + region.regionName + '"] .progress .bar');
            var label$ = regionOverview$.find('.lbl-users');
            var regionUserCount = 0;

            for ( var j = 0; j < region.workloads.length; j++) {
                var workloadUserCount = region.workloads[j].activeWorkerCount;

                // Region bar
                $(bars$[j]).css('width', (workloadUserCount / maxRegionUserCount * 100) + '%').attr('title', formatTooltipWithCount(region.workloads[j].workload.name, workloadUserCount));

                // Detailed workload bar
                $(detailedBars$[j * 2]).css('width', (regionUserCount / maxRegionUserCount * 100) + '%');
                $(detailedBars$[j * 2 + 1]).css('width', (workloadUserCount / maxRegionUserCount * 100) + '%').attr('title', formatTooltipWithCount(region.workloads[j].workload.name, workloadUserCount));

                // Detailed label
                regionUserCount += workloadUserCount;
                $(detailedBars$[j * 2]).closest('tr').find('.lbl-users').html(workloadUserCount.format(0));

                // Input field
                $(detailedBars$[j * 2]).closest('tr').find('input').val(workloadUserCount);
            }
            $(bars$[j]).css('width', (region.webCustomerCount / maxRegionUserCount * 100) + '%').attr('title', formatTooltipWithCount('Web browser user', region.webCustomerCount));

            // Region label
            regionUserCount += region.webCustomerCount;
            label$.html(regionUserCount.format(0));
        }

        // Update global workload labels
        for ( var j = 0; j < regionData.workloads.length; j++) {
            var workload = regionData.workloads[j];
            var count = 0;
            for ( var i = 0; i < regionData.regions.length; i++) {
                count += regionData.regions[i].workloads[j].activeWorkerCount;
            }
            $('.customer-summary [data-workload="' + workload.workload.name + '"]').html(count);
        }
        $('#summary-users-simulated').html(pluralize(totalSimulatedUserCount, 'simulated customer'));
        $('#summary-users-real').html(pluralize(totalWebCustomerCount, 'real customer'));
        $('#label-web-user-count .label').html(totalWebCustomerCount);

        // Update tab label
        $('#lbl-customers').text((totalSimulatedUserCount + totalWebCustomerCount).format(0));
    }

    function syncStatusIndicator(status$, obj) {
        status$.removeClass('label-refreshing label-important label-warning label-success');
        if (obj.isRefreshing) {
            status$.addClass('label-refreshing');
        } else if (obj.notResponding) {
            status$.addClass('label-important');
        } else if (obj.heavyLoad) {
            status$.addClass('label-warning');
        } else {
            status$.addClass('label-success');
        }
    }

    function convertWorkloadMapToSortedList(workloads) {
        var workloadList = [];
        for ( var key in workloads) {
            workloadList.push(workloads[key]);
        }
        workloadList.sort(function(a, b) {
            return (a.workload.name < b.workload.name) ? -1 : (a.workload.name == b.workload.name) ? 0 : 1;
        });
        return workloadList;
    }

    function formatTooltipWithCount(label, count) {
        return label + ' (' + count.format(0) + ')';
    }

    function getRegionByName(name) {
        for ( var i = 0; i < regionData.regions.length; i++) {
            if (regionData.regions[i].regionName == name) {
                return regionData.regions[i];
            }
        }
        return null;
    }

    function changeCurrency(region, currency, btn$) {
        var changeCount = region.instances.length;
        var failedInstances = [];

        btn$.attr('disabled', 'disabled').find('span').text('Changing...');

        for ( var i = 0; i < region.instances.length; i++) {
            var instance = region.instances[i];

            (function(instance) {
                $.ajax({
                    method: 'PUT',
                    url: instance.url + '/api/app-instances',
                    data: {
                        currency: currency
                    },
                    cache: false
                }).fail(function() {
                    failedInstances.push(instance.url);
                }).always(function() {
                    if (--changeCount == 0) {
                        btn$.removeAttr('disabled').find('span').text(Handlebars.helpers.currencyFormat(currency));

                        if (failedInstances.length) {
                            alert('Unable to change currency on one or more instances:\n\n - ' + failedInstances.join('\n - '));
                        }
                    }
                });
            })(instance);
        }
    }

    /**
     * Updates the simulated user counts in the specified region or all regions.
     * 
     * @param targetRegion  The region to update, or null to update all regions.  The share of users is split evently across all instances in the region, with 
     *                      those appearing earlier in the list getting the leftovers when an even split is not possible.
     * @param  data         The new user counts.  The keys should be in the format of "workload-{workloadName"}.  Not all workloads need to specified.
     * @param btn$          The button to disable while the update is in progress.  The global "Stop all" button is also disabled during the update.
     */
    function updateWorkloadUsers(targetRegion, data, btn$) {
        var failedInstances = [];
        var pendingUpdates = false;
        var changeCounts = {};

        $('#btn-stop-all').attr('disabled', 'disabled');
        btn$.attr('disabled', 'disabled').text('Updating...');

        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];
            if (targetRegion && region != targetRegion) {
                continue;
            }
            changeCounts[region.regionName] = region.instances.length;

            for ( var j = 0; j < region.instances.length; j++) {
                var instance = region.instances[j];
                var instanceData = {};

                // Give the instance a proportion of the total workload
                for ( var key in data) {
                    var value = Math.ceil(data[key] / (region.instances.length - j));
                    instanceData[key] = value;
                    data[key] -= value;
                }

                (function(region, instance) {
                    $.ajax({
                        method: 'PUT',
                        url: instance.url + '/api/simulator/workloads',
                        data: instanceData,
                        cache: false
                    }).success(function(data) {
                        instance.workloadStats = data.workloadStats;
                        recalcCustomerStats();
                    }).fail(function() {
                        failedInstances.push(instance.url);
                    }).always(function() {
                        if (--changeCounts[region.regionName] == 0) {
                            btn$.removeAttr('disabled').text('Update');

                            if ($('.btn-update[disabled]').length == 0) {
                                $('#btn-stop-all').removeAttr('disabled');
                            }

                            if (failedInstances.length) {
                                alert('Unable to change currency on one or more instances:\n\n - ' + failedInstances.join('\n - '));
                            }
                        }
                    });
                })(region, instance);
            }
        }
    }
})();

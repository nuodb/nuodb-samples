/* Copyright (c) 2013 NuoDB, Inc. */

(function() {
    "use strict";

    var app;
    var regionData = null;

    Storefront.initControlPanelPage = function(pageData) {
        app = this;
        regionData = initRegionData(app.regions, pageData.stats);

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
            var data = regionDetails$.find('input').serializeObject();
            for ( var key in data) {
                data[key] = parseInt(data[key]);
            }
            updateWorkloadUsers(getRegionByName(regionName), data, $(this));
        });

        // Handle refresh
        $('#btn-refresh').click(function() {
            document.location.reload();
        });

        // Render product info
        app.TemplateMgr.applyTemplate('tpl-product-info', '#product-info', pageData.productInfo);

        // Select quantity upon focus
        $('input[type=number]').on('click', function(e) {
            $(this).select();
            $(this).focus();
        });

        // Handle reset button
        $('#btn-reset').click(function() {
            $('input[type=number]').val('0');
            $(this).closest('form').submit();
        });

        // Handle tooltips
        $('a[data-toggle="tooltip"]').tooltip();

        // Enable HTML5 form features in browsers that don't support it
        $('form').form();

        // Validate min/max users per workload
        $('#workload-form').submit(function(e) {
            var numberFields = $('input[type=number]');
            for ( var i = 0; i < numberFields.length; i++) {
                var f = $(numberFields[i]);
                var max = parseInt(f.attr('max'));
                var name = f.attr('data-name');
                if (!isNaN(max) && f.val() > max) {
                    f.focus();
                    alert('User count for "' + name + '" cannot exceed ' + max + '.');
                    e.preventDefault();
                    return false;
                } else if (f.val() < 0) {
                    alert('User count for "' + name + '" cannot be negative.');
                    e.preventDefault();
                    f.focus();
                    return false;
                }
            }
        });

        refreshStats(false);
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
                    activeWorkerLimit: 0,
                    workload: $.extend({}, workload.workload)
                };
                region.workloads.push(workloadCopy);
                if (i == 0) {
                    workloadList.push($.extend({}, workloadCopy));
                }
            }
        }

        regions.sort(function(a, b) {
            return (a)
        })

        return {
            regions: regions,
            workloads: workloadList,
            regionSummaryLabel: pluralize(instanceCount, "Storefront instance") + ' across ' + pluralize(regions.length, 'region')
        };
    }

    function refreshStats(includeLocalInstance) {
        for ( var i = 0; i < regionData.regions.length; i++) {
            var region = regionData.regions[i];

            for ( var j = 0; j < region.instances.length; j++) {
                var instance = region.instances[j];
                if (instance.isRefreshing) {
                    break;
                }

                instance.isRefreshing = false;
                $('#regions [data-region="' + region.regionName + '"] .label-status').addClass('label-refreshing');

                refreshInstanceStats(region, instance);
            }
        }
    }

    function refreshInstanceStats(region, instance) {
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
            instance.heavyLoad = stats.appInstance.cpuUtilization >= 90;
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
        syncStatusIndicator($('#regions [data-region="' + region.regionName + '"] .dropdown > a .label-status'), region);
        syncStatusIndicator($('#regions [data-instance="' + instance.uuid + '"] .label-status'), instance);

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
                workload.activeWorkerLimit = 0;

                for ( var k = 0; k < region.instances.length; k++) {
                    var instance = region.instances[k];

                    // Find corresponding workload in this instance
                    if (instance.workloadStats) {
                        var workloadStats = instance.workloadStats[workload.workload.name];
                        if (workloadStats) {
                            workload.activeWorkerLimit += workloadStats.activeWorkerLimit;
                            regionUserCount += workloadStats.activeWorkerLimit;
                            totalSimulatedUserCount += workloadStats.activeWorkerLimit;
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
                var workloadUserCount = region.workloads[j].activeWorkerLimit;

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
                count += regionData.regions[i].workloads[j].activeWorkerLimit;
            }
            $('.customer-summary [data-workload="' + workload.workload.name + '"]').html(count);
        }
        $('#summary-users-simulated').html(pluralize(totalSimulatedUserCount, 'simulated customer'));
        $('#summary-users-real').html(pluralize(totalWebCustomerCount, 'real customer'));
        $('#label-web-user-count .label').html(totalWebCustomerCount);
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
                            alert('Unable to change currency on one or more instances:\n\n - ' + failedInstances.join('\n -'));
                        }
                    }
                });
            })(instance);
        }
    }

    function updateWorkloadUsers(region, data, btn$) {
        var failedInstances = [];
        var pendingUpdates = false;

        $('#btn-reset').attr('disabled', 'disabled');
        btn$.attr('disabled', 'disabled').text('Updating...');

        for ( var i = 0; i < region.instances.length; i++) {
            var instance = region.instances[i];
            var instanceData = {};

            // Give the instance a proportion of the total workload
            for ( var key in data) {
                var value = Math.ceil(data[key] / region.instances.length);
                instanceData[key] = value;
                data[key] -= value;
            }

            (function(instance) {
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
                    if (--changeCount == 0) {
                        btn$.removeAttr('disabled').text('Update');
                        
                        if ($('.btn-update[disabled]').length == 0) {
                            $('#btn-reset').removeAttr('disabled');
                        }

                        if (failedInstances.length) {
                            alert('Unable to change currency on one or more instances:\n\n - ' + failedInstances.join('\n -'));
                        }
                    }                    
                });
            })(instance);
        }
    }
})();

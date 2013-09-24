/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.controller.Storefront
 * 
 * Communicates with the Storefront API to maintain the Metrics store, Workloads store, and the dynamically generated metric history stores.
 */
Ext.define('App.controller.Storefront', {
    extend: 'Ext.app.Controller',

    stores: ['Metrics'],
    lastTimestamp: null,
    outstandingRequestCount: 0,

    /** @Override */
    init: function() {
        var me = this;
        me.callParent(arguments);
        me.statsHistory = [];
        me.regionStats = {}; // updated by RemoteStorefront
        me.seenInstanceUuidMap = {};        
    },

    /** @Override */
    destroy: function() {
        var me = this;
        clearInterval(me.refreshInterval);
        this.callParent(arguments);
    },

    /** @Override */
    onLaunch: function() {
        var me = this;

        // Refresh stats periodically
        me.refreshInterval = setInterval(Ext.bind(me.onRefreshStats, me), App.app.refreshFrequencyMs);

        this.callParent(arguments);
    },

    getMetricHistoryStore: function(metric, categoryIdx) {
        var me = this;
        var storeKey = 'historyStore' + (categoryIdx == null ? 0 : categoryIdx);
        var store = metric.get(storeKey);
        if (!store) {
            metric.set(storeKey, store = me.createMetricHistoryStore(metric, categoryIdx));
        }
        return store;
    },

    getFieldConfigs: function(stats) {
        var fields = [{
            name: 'timestamp',
            type: 'date'
        }];
        for ( var seriesName in stats) {
            fields.push({
                name: seriesName,
                type: 'int'
            });
        }
        fields.sort(function(a, b) {
            return (a.name < b.name) ? -1 : (a.name == b.name) ? 0 : 1;
        })
        return fields;
    },

    isModelMissingStatField: function(model, stats) {
        var fields = model.getFields();
        var fieldNames = {};
        for ( var i = 0; i < fields.length; i++) {
            fieldNames[fields[i].name] = true;
        }
        for ( var stat in stats) {
            if (!fieldNames[stat]) {
                // we found a missing stat
                return true;
            }
        }
        return false;
    },

    createMetricHistoryStore: function(metric, categoryIdx) {
        var me = this;
        var category = metric.get('category' + (categoryIdx == null ? 0 : categoryIdx));
        var metricName = metric.get('name');
        var modelName = 'DynamicMetricModel_' + category;
        var storeName = 'DynamicMetricStore_' + category;

        if (!App.store[storeName]) {
            if (me.statsHistory.length == 0) {
                // No data yet -- can't create store
                return null;
            }

            // Create model
            var stats = me.statsHistory[0][category];
            var model = Ext.define('App.model.' + modelName, {
                extend: 'Ext.data.Model',
                fields: me.getFieldConfigs(stats)
            });
            Ext.override(model, {
                get: function(name) {
                    return this.callOverridden(arguments) || 0;
                }
            });

            // Create store
            Ext.define('App.store.' + storeName, {
                extend: 'Ext.data.Store',
                model: 'App.model.' + modelName
            });
        }

        var records = [];
        for ( var i = 0; i < me.statsHistory.length; i++) {
            var stats = me.statsHistory[i][category];
            var record = {
                timestamp: new Date(me.statsHistory[i].timestamp)
            };
            for ( var seriesName in stats) {
                record[seriesName] = stats[seriesName][metricName];
            }
            records.push(record);
        }

        var store = new App.store[storeName]();
        store.loadData(records);
        return store;
    },

    getLatestValue: function(path) {
        var me = this;
        var source = me.statsHistory[me.statsHistory.length - 1];

        path = path.split('.');
        for ( var pathIdx = 0; source && pathIdx < path.length; pathIdx++) {
            source = source[path[pathIdx]];
        }
        return source;
    },

    resetStats: function() {
        var me = this;
        me.statsHistory = [];
        me.lastTimestamp = null;
    },

    /** @private interval handler */
    onRefreshStats: function() {
        var me = this;
        if (me.outstandingRequestCount >= App.app.maxOutstandingRequestCount) {
            return;
        }

        me.outstandingRequestCount++;

        Ext.Ajax.request({
            url: App.app.apiBaseUrl + '/api/stats?includeStorefront=true',
            method: 'GET',
            scope: this,
            callback: function() {
                me.outstandingRequestCount--;
            },
            success: function(response) {
                var stats;
                try {
                    stats = Ext.decode(response.responseText);
                } catch (e) {
                }
                if (!stats) {
                    return;
                }

                // If we're talking to a new Storefront instance (maybe service was bounced), throw away old data so deltas aren't bogus
                if (me.instancesAvailableHaveChanged(stats)) {
                    me.resetStats();
                }

                if (me.lastTimestamp && stats.timestamp < me.lastTimestamp) {
                    // We received a response out-of-sequence.  Ignore it since deltas were already calculated.
                    return;
                }
                me.lastTimestamp = stats.timestamp;

                // Add stats from regions
                stats.regionTransactionStats = {};
                stats.regionWorkloadStats = {};
                stats.regionWorkloadStepStats = {};
                me.aggregateInstanceStats(stats.appInstance.region, stats, stats); // this region first
                for ( var regionName in me.regionStats) {
                    var region = me.regionStats[regionName];
                    for ( var uuid in region) {
                        me.aggregateInstanceStats(regionName, region[uuid], stats);
                        me.seenInstanceUuidMap[uuid] = true;
                    }
                }

                // Calculate deltas
                if (me.statsHistory.length > 0) {
                    var oldStats = me.statsHistory[me.statsHistory.length - 1];
                    for ( var category in stats) {
                        for ( var series in stats[category]) {
                            var oldSeries = oldStats[category][series];
                            for ( var metric in stats[category][series]) {
                                if (!metric.endsWith('Delta')) {
                                    stats[category][series][metric + 'Delta'] = stats[category][series][metric] - ((oldSeries) ? oldSeries[metric] : 0);
                                }
                            }
                        }
                    }
                }

                // Create "all" aggregate for each category
                for ( var category in stats) {
                    var all = {};
                    for ( var series in stats[category]) {
                        for ( var metric in stats[category][series]) {
                            if (metric == 'uptimeMs') {
                                all[metric] = Math.max(all[metric] || 0, stats[category][series][metric]);
                            } else {
                                all[metric] = (all[metric] || 0) + stats[category][series][metric];
                            }
                        }
                    }
                    stats[category].all = all;
                }

                // Record to history
                me.statsHistory.splice(0, me.statsHistory.length - App.app.maxStatsHistory);
                me.statsHistory.push(stats);

                // Update metric history stores with data
                var timestamp = new Date(stats.timestamp);
                me.getStore('Metrics').each(function(metric) {
                    for ( var i = 0; i < 2; i++) {
                        var store = metric.get('historyStore' + i);
                        if (store) {
                            var catStats = stats[metric.get('category' + i)];

                            if (me.isModelMissingStatField(store.model, catStats)) {
                                // Reconfigure fields 
                                store.model.setFields(me.getFieldConfigs(catStats));
                                store.fireEvent('metachange');
                            }

                            var metricName = metric.get('name');
                            var record = {
                                timestamp: timestamp
                            };
                            for ( var seriesName in catStats) {
                                record[seriesName] = catStats[seriesName][metricName];
                            }
                            store.add(record);
                            while (store.getCount() > App.app.maxStatsHistory) {
                                store.removeAt(0);
                            }
                        }
                    }
                });

                // Notify other listeners of the change
                me.application.fireEvent('statschange', me);
            },
            failure: function(response) {
                me.application.fireEvent('statsfail', response);
            }
        });
    },

    instancesAvailableHaveChanged: function(stats) {
        var me = this;

        if (me.localInstanceUuid != stats.appInstance.uuid) {
            // This is the first data, or local instance has been restarted
            me.localInstanceUuid = stats.appInstance.uuid;
            return true;
        }

        for ( var region in me.regionStats) {
            for ( var uuid in me.regionStats[region]) {
                if (!me.seenInstanceUuidMap[uuid]) {
                    // Instance is new
                    return true;
                }
            }
        }

        return false;
    },
    
    aggregateInstanceStats: function(regionName, instanceStats, stats) {
        var me = this;

        if (!stats.regionTransactionStats[regionName]) {
            // Create stats containers for this region
            stats.regionTransactionStats[regionName] = {};
            stats.regionWorkloadStats[regionName] = {};
            stats.regionWorkloadStepStats[regionName] = {};
        }

        if (instanceStats != stats) {
            // Aggregate stats (unless we're accumulating local stats, which are already part of stats)
            me.aggregateStats(instanceStats.transactionStats, stats.transactionStats);
            me.aggregateStats(instanceStats.workloadStats, stats.workloadStats);
            me.aggregateStats(instanceStats.workloadStepStats, stats.workloadStepStats);
        }

        me.rollupAndAggregateStats(instanceStats.transactionStats, stats.regionTransactionStats[regionName]);
        me.rollupAndAggregateStats(instanceStats.workloadStats, stats.regionWorkloadStats[regionName]);
        me.rollupAndAggregateStats(instanceStats.workloadStepStats, stats.regionWorkloadStepStats[regionName]);
    },

    rollupAndAggregateStats: function(src, dest) {
        for ( var key in src) {
            this.aggregateStats(src[key], dest);
        }
    },

    aggregateStats: function(src, dest) {
        for ( var key in src) {
            var srcVal = src[key];
            if (!srcVal) {
                continue;
            }

            if (!dest[key]) {
                dest[key] = (typeof (srcVal) == "object") ? $.extend({}, srcVal) : srcVal;
            } else if (typeof (dest[key]) == "object") {
                this.aggregateStats(srcVal, dest[key]);
            } else {
                dest[key] += srcVal;
            }
        }
    }
});

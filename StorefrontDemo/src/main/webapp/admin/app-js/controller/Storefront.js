/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.controller.Storefront
 * 
 * Communicates with the Storefront API to maintain the Metrics store, Workloads store, and the dynamically generated metric history stores.
 */
Ext.define('App.controller.Storefront', {
    extend: 'Ext.app.Controller',

    stores: ['Metrics', 'Workloads'],
    defaultStorefrontName: 'Default Storefront',
    lastTimestamp: null,
    outstandingRequestCount: 0,

    /** @Override */
    init: function() {
        var me = this;
        me.callParent(arguments);
        me.statsHistory = [];
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

        // Initialize workload store
        me.getStore('Workloads').on('update', Ext.bind(me.onWorkloadChange, me));
        me.reloadWorkloadStore();

        // Refresh stats periodically
        me.refreshInterval = setInterval(Ext.bind(me.onRefreshStats, me), App.app.refreshFrequencyMs);

        this.callParent(arguments);
    },

    getMetricHistoryStore: function(metric) {
        var me = this;
        var store = metric.get('historyStore');
        if (!store) {
            metric.set('historyStore', store = me.createMetricHistoryStore(metric));
        }
        return store;
    },

    createMetricHistoryStore: function(metric) {
        var me = this;
        var category = metric.get('category');
        var metricName = metric.get('name');
        var modelName = 'DynamicMetricModel_' + category;
        var storeName = 'DynamicMetricStore_' + category;

        if (!App.store[storeName]) {
            if (me.statsHistory.length == 0) {
                // No data yet -- can't create store
                return null;
            }
            
            var fields = [{
                name: 'timestamp',
                type: 'date'
            }];
            var stats = me.statsHistory[0][category];
            for ( var seriesName in stats) {
                fields.push({
                    name: seriesName,
                    type: 'int'
                });
            }

            // Create model
            Ext.define('App.model.' + modelName, {
                extend: 'Ext.data.Model',
                fields: fields
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
                timestamp: new Date(me.statsHistory[i].storefrontStats.all.timestamp)
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
                var instanceId = stats.storefrontStats.instanceId;
                if (me.instanceId !== instanceId) {
                    me.statsHistory = [];
                    me.lastTimestamp = null;
                }
                me.instanceId = instanceId;
                
                if (me.lastTimestamp && stats.storefrontStats.timestamp < me.lastTimestamp) {
                    // We received a response out-of-sequence.  Ignore it since deltas were already calculated.
                    return;
                }
                me.lastTimestamp = stats.storefrontStats.timestamp;

                // Convert storefront stats into series form (consistent with other categories for coding ease)
                var storefrontStats = stats.storefrontStats;
                stats.storefrontStats = {};
                stats.storefrontStats[me.defaultStorefrontName] = storefrontStats;

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
                            all[metric] = (all[metric] || 0) + stats[category][series][metric];
                        }
                    }
                    stats[category].all = all;
                }

                // Record to history
                me.statsHistory.splice(0, me.statsHistory.length - App.app.maxStatsHistory);
                me.statsHistory.push(stats);

                // Update metric history stores with data
                var timestamp = new Date(stats.storefrontStats.all.timestamp);
                me.getStore('Metrics').each(function(metric) {
                    var store = metric.get('historyStore');
                    if (store) {
                        var metricName = metric.get('name');
                        var record = {
                            timestamp: timestamp
                        };
                        var catStats = stats[metric.get('category')];
                        for ( var seriesName in catStats) {
                            record[seriesName] = catStats[seriesName][metricName];
                        }
                        store.add(record);
                        if (store.getCount() > App.app.maxStatsHistory) {
                            store.removeAt(0);
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

    reloadWorkloadStore: function() {
        var me = this;
        Ext.Ajax.request({
            url: App.app.apiBaseUrl + '/api/simulator/workloads',
            method: 'GET',
            scope: this,
            success: function(response) {
                var data;
                try {
                    data = Ext.decode(response.responseText);
                } catch (e) {
                    Ext.log(Ext.String.format('Unable to decode workload response:  {0} ({1})', response.responseText, e));
                    return;
                }

                var workloads = [];
                for ( var i = 0; i < data.length; i++) {
                    var stats = data[i];
                    workloads.push(Ext.apply(stats, stats.workload));
                }
                me.getStore('Workloads').loadData(workloads);
            },
            failure: function(response) {
                Ext.log('Unable to query Storefront API: ' + response.status)
            }
        });
    },

    onWorkloadChange: function(store, record, op, modifiedFieldNames) {
        var me = this;

        if (!Ext.Array.contains(modifiedFieldNames, 'activeWorkerCount')) {
            return;
        }

        var workloadName = record.get('name');
        var activeWorkerCount = record.get('activeWorkerCount');

        Ext.Ajax.request({
            url: Ext.String.format('{0}/api/simulator/workloads/{1}/workers', App.app.apiBaseUrl, workloadName),
            method: 'PUT',
            params: {
                minWorkers: activeWorkerCount,
                limit: activeWorkerCount
            },
            scope: this,
            success: function(response) {
            },
            failure: function(response) {
                Ext.log('Unable to update workload: ' + response.status)
            }
        });
    }
});

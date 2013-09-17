/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.controller.Storefront
 * 
 * Communicates with the Storefront API to maintain the Metrics store, Workloads store, and the dynamically generated metric history stores.
 */
Ext.define('App.controller.Storefront', {
    extend: 'Ext.app.Controller',

    stores: ['Metrics'],
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
                var instanceId = stats.appInstance.uuid;
                if (me.instanceId !== instanceId) {
                    me.statsHistory = [];
                    me.lastTimestamp = null;
                }
                me.instanceId = instanceId;

                if (me.lastTimestamp && stats.timestamp < me.lastTimestamp) {
                    // We received a response out-of-sequence.  Ignore it since deltas were already calculated.
                    return;
                }
                me.lastTimestamp = stats.timestamp;

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
                    var store = metric.get('historyStore');
                    if (store) {
                        var catStats = stats[metric.get('category')];

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
                });

                // Notify other listeners of the change
                me.application.fireEvent('statschange', me);
            },
            failure: function(response) {
                me.application.fireEvent('statsfail', response);
            }
        });
    }
});

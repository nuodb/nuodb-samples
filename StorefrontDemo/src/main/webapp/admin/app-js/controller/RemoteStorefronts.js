/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.controller.RemoteStorefronts
 * 
 * Communicates with the remote Storefront instances to supplement the data stores.
 */
Ext.define('App.controller.RemoteStorefronts', {
    extend: 'Ext.app.Controller',

    minHeavyCpuUtilizationPct: 90,

    /** @Override */
    init: function() {
        var me = this;
        me.callParent(arguments);
        me.appInstances = [];
    },

    /** @Override */
    destroy: function() {
        var me = this;
        clearInterval(me.refreshInterval);
        clearInterval(me.instanceRefreshInterval);
        this.callParent(arguments);
    },

    /** @Override */
    onLaunch: function() {
        var me = this;

        // Get a reference to the controller we'll be feeding data
        me.storefrontController = me.application.getController('Storefront');

        // Refresh stats periodically
        me.refreshInterval = setInterval(Ext.bind(me.onRefreshStats, me), App.app.refreshFrequencyMs);

        // Refresh instance list now and periodically
        me.instanceRefreshInterval = setInterval(Ext.bind(me.onRefreshInstanceList, me), App.app.instanceListRefreshFrequencyMs);
        me.onRefreshInstanceList();

        me.callParent(arguments);
    },

    /** @private interval handler for refreshing app instance list */
    onRefreshInstanceList: function() {
        var me = this;
        Ext.Ajax.request({
            url: App.app.apiBaseUrl + '/api/app-instances',
            method: 'GET',
            scope: this,
            success: function(response) {
                // Get old instance request counts
                var instanceRequestCountMap = {};
                for ( var i = 0; i < me.appInstances.length; i++) {
                    var instance = me.appInstances[i];
                    instanceRequestCountMap[instance.uuid] = instance.outstandingRequestCount;
                }
                try {
                    me.appInstances = Ext.decode(response.responseText);
                } catch (e) {
                    return;
                }

                // Discover valid IDs
                var knownUuidMap = (me.storefrontController.stats || {}).regionWorkloadStats || {};
                var uuidMap = {};
                for ( var i = 0; i < me.appInstances.length; i++) {
                    var uuid = me.appInstances[i].uuid;
                    uuidMap[uuid] = true;
                    me.appInstances[i].outstandingRequestCount = instanceRequestCountMap[uuid] || 0;
                }

                // Delete instances that are no longer alive
                var removeCount = 0;
                var regionStats = me.storefrontController.regionStats;
                for ( var regionName in regionStats) {
                    var region = regionStats[regionName];
                    for ( var uuid in region) {
                        if (!uuidMap[uuid]) {
                            delete region[uuid];
                            delete me.storefrontController.seenInstanceUuidMap[uuid];
                            removeCount++;
                        }
                    }
                }

                // Reset stats if something has changed so our deltas aren't messed up
                if (removeCount > 0) {
                    me.storefrontController.resetStats();
                }

                // Signal instances under heavy load
                for ( var i = 0; i < me.appInstances.length; i++) {
                    var instance = me.appInstances[i];
                    if (instance.cpuUtilization >= me.minHeavyCpuUtilizationPct) {
                        me.application.fireEvent('heavyload', {
                            status: 500,
                            responseJson: {
                                message: "Instance " + instance.url + " is under heavy load.  Reduce simulated users here or add instances to this region.",
                                ttl: App.app.instanceListRefreshFrequencyMs + 2000
                            }
                        }, instance);
                    }
                }
            },
            failure: function(response) {
                me.application.fireEvent('statsfail', response, null);
            }
        });
    },

    /** @private interval handler */
    onRefreshStats: function() {
        var me = this;

        for ( var i = 0; i < me.appInstances.length; i++) {
            var instance = me.appInstances[i];
            if (!instance.local && instance.outstandingRequestCount < App.app.maxOutstandingRequestCount) {
                me.refreshInstanceStats(instance);
            }
        }
    },

    refreshInstanceStats: function(instance) {
        var me = this;
        instance.outstandingRequestCount++;

        try {
            Ext.Ajax.request({
                url: instance.url + '/api/stats?includeStorefront=false',
                method: 'GET',
                callback: function() {
                    instance.outstandingRequestCount--;
                },
                success: function(response) {
                    var stats;
                    try {
                        stats = Ext.decode(response.responseText);
                    } catch (e) {
                        return;
                    }

                    var regionStats = me.storefrontController.regionStats[instance.region];
                    if (!regionStats) {
                        me.storefrontController.regionStats[instance.region] = regionStats = {};
                    } else {
                        var lastTimestamp = regionStats[instance.uuid].timestamp;
                        if (lastTimestamp && stats.timestamp < lastTimestamp) {
                            // We received a response out-of-sequence.  Ignore it since deltas were already calculated.
                            return;
                        }
                    }
                    regionStats[instance.uuid] = stats;
                },
                failure: function(response) {
                    me.application.fireEvent('statsfail', response, instance);
                }
            });
        } catch (e) {
            instance.outstandingRequestCount--;
            me.application.fireEvent('statsfail', {
                status: 500,
                responseJson: {
                    message: "Your browser does not support CORS, which is required to collect statistics from this instance."
                }
            }, instance);
        }
    }
});

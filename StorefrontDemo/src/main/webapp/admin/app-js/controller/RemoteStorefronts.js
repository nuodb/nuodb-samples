/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.controller.RemoteStorefronts
 * 
 * Communicates with the remote Storefront instances to supplement the data stores.
 */
Ext.define('App.controller.RemoteStorefronts', {
    extend: 'Ext.app.Controller',

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
                try {
                    me.appInstances = Ext.decode(response.responseText);
                } catch (e) {
                    return;
                }

                // Discover valid IDs
                var uuidMap = {};
                for ( var i = 0; i < me.appInstances.length; i++) {
                    uuidMap[me.appInstances[i].uuid] = true;
                }

                // Delete instances that are no longer alive
                var regionStats = me.storefrontController.regionStats;
                for ( var regionName in regionStats) {
                    var region = regionStats[regionName];
                    for ( var uuid in region) {
                        if (!uuidMap[uuid]) {
                            delete region[uuid];
                        }
                    }
                }
            },
            failure: function(response) {
                me.application.fireEvent('statsfail', response, instance);
            }
        });
    },

    /** @private interval handler */
    onRefreshStats: function() {
        var me = this;

        for ( var i = 0; i < me.appInstances.length; i++) {
            var instance = me.appInstances[i];
            if (!instance.local) {
                me.refreshInstanceStats(instance);
            }
        }
    },

    refreshInstanceStats: function(instance) {
        var me = this;

        Ext.Ajax.request({
            url: instance.url + '/api/stats?includeStorefront=false',
            method: 'GET',
            scope: this,
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
                }
                regionStats[instance.uuid] = stats;

                //me.storefrontController.aggregateStats(stats, regionStats);
            },
            failure: function(response) {
                me.application.fireEvent('statsfail', response, instance);
            }
        });
    }
});

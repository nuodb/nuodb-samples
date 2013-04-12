/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.Viewport
 * 
 * Viewport for the admin application
 */
Ext.define('App.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: ['App.view.HeaderBar', 'App.view.FooterBar', 'App.view.ControlPanel', 'App.view.MetricDashboard', 'Ext.ux.IFrame'],

    layout: 'border',

    initComponent: function() {
        var me = this;

        me.frameMap = {};

        me.items = [{
            region: 'north',
            xtype: 'headerbar',
            listeners: {
                viewchange: me.onViewChange.bind(me)
            }
        }, {
            region: 'center',
            layout: 'card',
            itemId: 'center',
            items: [{
                region: 'center',
                layout: 'card',
                itemId: 'metricsView',
                padding: '20'
            }]
        }, {
            region: 'south',
            xtype: 'footerbar'
        }];

        me.callParent(arguments);

        me.center = me.down('[itemId=center]');
        me.metricsView = me.down('[itemId=metricsView]');

        var viewName = me.down('headerbar').getActiveViewName();
        if (viewName) {
            me.onViewChange(viewName);
        }
    },

    onViewChange: function(viewName) {
        var me = this;
        var centerLayout = me.center.getLayout();
        var viewUrl = me.getViewUrl(viewName);
        
        if (viewUrl) {
            // Show URL of the view in an iframe
            
            if (!me.frameMap[viewName]) {
                me.frameMap[viewName] = me.center.add({
                    xtype: 'uxiframe',
                    itemId: 'storefrontView',
                    src: viewUrl
                });
            }
            centerLayout.setActiveItem(me.frameMap[viewName]);
        } else {
            // Show metrics associated with the view
            
            var view = me.metricsView.items.get(viewName);
            if (!view) {
                view = {
                    xtype: 'metricdashboard',
                    itemId: viewName,
                    metrics: me.getViewMetrics(viewName)
                };
                view = me.metricsView.add(view);
            }
            centerLayout.setActiveItem(0);
            me.metricsView.getLayout().setActiveItem(view);
        }
    },

    getViewUrl: function(viewName) {
        switch (viewName) {
            case 'welcome':
                return '../welcome';

            case 'storefront':
                return '../products;'

            default:
                return null;
        }
    },

    getViewMetrics: function(viewName) {
        var store = Ext.getStore('Metrics');
        var metrics = [];
        store.each(function(metric) {
            if (metric.get('view') == viewName) {
                metrics.push(metric);
            }
        });
        return metrics;
    }
});

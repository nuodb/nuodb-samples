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
                xtype: 'uxiframe',
                itemId: 'storefrontView'
            }, {
                border: false,
                padding: 20,
                layout: 'border',
                items: [{
                    region: 'west',
                    xtype: 'controlpanel',
                    width: 320,
                    minWidth: 250,
                    split: true,
                    collapsible: true,
                    collapseMode: 'mini'
                }, {
                    region: 'center',
                    layout: 'card',
                    itemId: 'metricsView'
                }]
            }]
        }, {
            region: 'south',
            xtype: 'footerbar'
        }];

        me.callParent(arguments);

        me.center = me.down('[itemId=center]');
        me.storefrontView = me.down('[itemId=storefrontView]');
        me.metricsView = me.down('[itemId=metricsView]');

        var viewName = me.down('headerbar').getActiveViewName();
        if (viewName) {
            me.onViewChange(viewName);
        }
    },

    onViewChange: function(viewName) {
        var me = this;
        var showStore = (viewName == 'storefront');
        me.center.getLayout().setActiveItem((showStore) ? 0 : 1);

        if (showStore) {
            var frame = me.storefront;
            if (!me.storefrontView.isStoreLoaded) {
                me.storefrontView.load('../products');
                me.storefrontView.isStoreLoaded = true;
            }
        } else {
            var view = me.metricsView.items.get(viewName);
            if (!view) {
                view = {
                    xtype: 'metricdashboard',
                    itemId: viewName,
                    metrics: me.getViewMetrics(viewName)
                };
                view = me.metricsView.add(view);
            }
            me.metricsView.getLayout().setActiveItem(view);
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

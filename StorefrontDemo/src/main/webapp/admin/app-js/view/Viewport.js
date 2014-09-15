/* Copyright (c) 2013-2014 NuoDB, Inc. */

/**
 * @class App.view.Viewport
 * 
 * Viewport for the admin application
 */
Ext.define('App.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: ['App.view.HeaderBar', 'App.view.MessageBar', 'App.view.NavBar', 'App.view.FooterBar', 'App.view.MetricDashboard', 'Ext.ux.IFrame'],

    layout: 'border',

    initComponent: function() {
        var me = this;

        if (window.NuoHeader) {
            me.padding = '50 0 0 0';
            NuoHeader.render({
                appTitle: 'NuoDB Storefront Demo',
                homeUrl: './',
                sidebarTip: 'Hide control panel'
            });
        }

        me.items = [{
            region: 'north',
            xtype: 'messagebar'
        }, {
            region: 'north',
            xtype: 'headerbar'
        }, {
            region: 'center',
            layout: 'card',
            itemId: 'center',
            items: [{
                xtype: 'uxiframe',
                itemId: 'frameView',
                src: '../welcome',
                listeners: {
                    load: function() {
                        try {
                            var url = this.getWin().document.location.href.split('/');
                            App.app.fireEvent('viewchange', '/' + url[url.length - 1], false);
                        } catch (e) {                            
                        }
                    }
                }
            }, {
                layout: 'card',
                itemId: 'metricsView',
                padding: '20'
            }]
        }, {
            region: 'west',
            xtype: 'navbar'
        }];

        me.callParent(arguments);

        me.center = me.down('[itemId=center]');
        me.metricsView = me.down('[itemId=metricsView]');
        me.frameView = me.down('[itemId=frameView]');

        App.app.on('viewchange', Ext.bind(me.onViewChange, me));
    },

    onViewChange: function(viewName, isUserInitiated) {
        var me = this;
        var centerLayout = me.center.getLayout();
        
        url = me.getViewUrl(viewName);

        if (url) {
            // Show URL of the view in an iframe
            if (isUserInitiated !== false) {
                me.frameView.load(url);
            }
            centerLayout.setActiveItem(me.frameView);
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
            centerLayout.setActiveItem(me.metricsView);
            me.metricsView.getLayout().setActiveItem(view);
        }
    },

    getViewUrl: function(viewName) {
        if (viewName[0] == '/') {
            return '..' + viewName;
        }
        
        switch (viewName) {
            case 'welcome':
                return '../welcome';

            case 'metrics-hosts':
                return '../control-panel-processes';

            case 'metrics-regions':
                return '../control-panel-regions';
                
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

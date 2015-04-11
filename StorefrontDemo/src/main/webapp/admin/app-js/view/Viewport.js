/* Copyright (c) 2013-2015 NuoDB, Inc. */

/**
 * @class App.view.Viewport
 * 
 * Viewport for the admin application
 */
Ext.define('App.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: ['App.view.HeaderBar', 'App.view.MessageBar', 'App.view.NavBar', 'App.view.ChartControlBar', 'App.view.MetricDashboard', 'Ext.ux.IFrame'],

    layout: {
        type: 'border',
        regionWeights: {
            north: 0,
            west: 0,
            east: 0,
            south: 0
        }        
    },

    initComponent: function() {
        var me = this;

        if (window.NuoHeader) {
            me.padding = '50 0 0 0';
            NuoHeader.render({
                appTitle: 'NuoDB Storefront Demo' + ((App.app.tenant) ? ' [' + Ext.String.htmlEncode(App.app.tenant) + ']' : ''),
                homeUrl: './',
                sidebarTip: 'Hide control panel'
            });
        }
        
        me.items =  [{
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
                listeners: { load: me.onIFrameLoad }
            }, {
                xtype: 'uxiframe',
                itemId: 'userView',
                listeners: { load: me.onIFrameLoad }
            }, {
                layout: 'card',
                itemId: 'metricsView',
                padding: '20'
            }]
        }, {
            region: 'west',
            xtype: 'navbar'
        }, {
            region: 'south',
            xtype: 'chartcontrolbar',
            id: 'chartcontrolbar',
            itemId: 'chartControlView',
            hidden: true
        }];

        me.callParent(arguments);

        me.center = me.down('[itemId=center]');
        me.metricsView = me.down('[itemId=metricsView]');
        me.frameView = me.down('[itemId=frameView]');
        me.userView = me.down('[itemId=userView]');
        me.chartControlView = me.down('[itemId=chartControlView]');

        App.app.on('viewchange', Ext.bind(me.onViewChange, me));
    },
    
    afterRender: function() {
        var me = this;
        me.callParent(arguments);
        App.app.fireEvent('viewchange', '/welcome', true, null);
    },

    onIFrameLoad: function() {
        try {
            this.lastLoadTime = new Date();
            var url = this.getWin().document.location.href.split('/');
            App.app.fireEvent(this.loadEvent || 'viewchange', '/' + url[url.length - 1], false, null);
            delete this.loadEvent;
        } catch (e) {
        }
    },

    onViewChange: function(viewName, isUserInitiated, loadEvent) {
        var me = this;
        var centerLayout = me.center.getLayout();

        url = me.getViewUrl(viewName);

        if (url) {
            // Show URL of the view in an iframe
            var isUserView = /[^?]*/.exec(url)[0] == '../control-panel-users';
            var targetView = (isUserView) ? me.userView : me.frameView;
            if (isUserInitiated !== false) {
                targetView.loadEvent = loadEvent;
                targetView.load(url + '?tenant=' + encodeURIComponent(App.app.tenant));
            }
            if (!loadEvent) {
                centerLayout.setActiveItem(targetView);
                me.chartControlView.setVisible(false);
            }
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
            me.chartControlView.setVisible(true);
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

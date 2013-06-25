/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.HeaderBar
 */
Ext.define('App.view.HeaderBar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.headerbar',
    requires: ['App.view.MetricWell'],

    border: false,
    cls: 'nuo-header',
    enableOverflow: true,

    /** @Override */
    initComponent: function() {
        var me = this;
        var clickHandler = Ext.bind(me.onViewButtonClick, me);

        me.items = ['->', {
            xtype: 'container',
            id: 'appTitle',
            html: App.app.title,
            margin: '10 0 0 0'
        }, {
            xtype: 'container',
            layout: 'fit',
            items: {
                xtype: 'button',
                margin: '0 5 0 5',
                itemId: 'welcome',
                cls: 'btn-header',
                text: 'Control Panel',
                scale: 'medium',
                iconCls: 'ico-panel-32',
                tooltip: 'Describes the purpose of the Storefront demo and lets you configure simulated workloads',
                iconAlign: 'bottom',
                scale: 'large',
                enableToggle: true,
                allowDepress: false,
                pressed: true,
                handler: clickHandler,
                width: 110
            }
        }, {
            xtype: 'container',
            layout: 'fit',
            items: {
                xtype: 'splitbutton',
                tooltip: 'Shows the storefront website&mdash;where you can go shopping!',
                margin: '0 5 0 5',
                itemId: 'storefront',
                cls: 'btn-header',
                iconAlign: 'bottom',
                iconCls: 'ico-store-32',
                text: 'Shopping Site',
                scale: 'large',
                width: 80,
                enableToggle: true,
                allowDepress: false,
                ui: 'default',
                handler: clickHandler,
                width: 110,
                menu: {
                    showSeparator: false,
                    items: [{
                        text: '<b>Show Default Storefront</b>',
                        handler: clickHandler,
                        itemId: 'storefront'
                    }, {
                        text: 'Show Default Storefront in New Tab',
                        href: '../products',
                        hrefTarget: 'blank'
                    }]
                }
            }
        }, /*
             * { xtype: 'metricwell', text: '<b>NuoDB</b> transactions/sec', tooltip: 'Shows NuoDB\'s performance metrics',
             * graphVisible: false, metric: 'dbStats.tps', format: 'TBD', itemId: 'metrics-db', listeners: { click:
             * clickHandler } },
             */{
            xtype: 'metricwell',
            text: '<b>Service</b> calls/sec',
            tooltip: 'Shows application\'s service layer metrics',
            format: ',.0',
            metric: 'transactionStats.all.totalCountDelta',
            itemId: 'metrics-service',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Store</b> items in carts',
            tooltip: 'Shows store metrics',
            metric: 'storefrontStats.all.cartItemCount',
            itemId: 'metrics-storefront',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Simulator</b> active users',
            tooltip: 'Shows simulated activity metrics',
            metric: 'workloadStats.all.activeWorkerCount',
            itemId: 'metrics-simulator',
            listeners: {
                click: clickHandler
            }
        }, '->'];

        me.callParent(arguments);
        me.btnShowStore = me.down('#btnShowStore');
        me.viewButtons = Ext.ComponentQuery.query('button, metricwell', me);
    },

    getActiveViewName: function() {
        var me = this;
        for ( var i = 0; i < me.viewButtons.length; i++) {
            if (me.viewButtons[i].pressed) {
                return me.viewButtons[i].getItemId();
            }
        }
    },

    /** @private event handler */
    onViewButtonClick: function(btnActive) {
        var me = this;
        var viewName = btnActive.getItemId();
        for ( var i = 0; i < me.viewButtons.length; i++) {
            var btn = me.viewButtons[i];
            if (btn != btnActive) {
                btn.toggle(false, true);
            }
        }
        me.fireEvent('viewchange', viewName);
    }
});

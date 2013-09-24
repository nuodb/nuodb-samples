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
            itemId: 'welcome',
            margin: '10 0 0 0',
            items: {
                xtype: 'button',
                margin: '0 5 0 5',
                id: 'welcome',
                itemId: 'welcome',
                cls: 'btn-header',
                text: 'Welcome',
                scale: 'medium',
                iconCls: 'ico-logo',
                tooltip: "Demo overview, DDL, and important links",
                iconAlign: 'bottom',
                scale: 'large',
                enableToggle: true,
                allowDepress: false,
                handler: clickHandler,
                width: 110
            }
        }, {
            xtype: 'container',
            layout: 'fit',
            items: {
                xtype: 'button',
                margin: '0 5 0 5',
                itemId: 'control-panel',
                cls: 'btn-header',
                text: 'Control Panel',
                scale: 'medium',
                iconCls: 'ico-panel-32',
                tooltip: 'Storefront instances and workload configuration',
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
                xtype: 'button',
                tooltip: 'Storefront website&mdash;where you can go shopping!',
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
                width: 110
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Service</b> calls/sec',
            tooltip: 'Service layer metrics',
            format: ',.0',
            metric: 'transactionStats.all.totalCountDelta',
            itemId: 'metrics-service',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Storefront</b> items in carts',
            tooltip: 'Storefront metrics',
            metric: 'storefrontStats.all.cartItemCount',
            itemId: 'metrics-storefront',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Simulator</b> active users',
            tooltip: 'Simulated activity metrics',
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

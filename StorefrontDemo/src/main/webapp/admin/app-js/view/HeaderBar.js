/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.HeaderBar
 */
Ext.define('App.view.HeaderBar', {
    extend: 'Ext.container.Container',
    alias: 'widget.headerbar',
    requires: ['App.view.MetricWell'],

    border: false,
    id: 'headerbar',

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    /** @Override */
    initComponent: function() {
        var me = this;
        var clickHandler = Ext.bind(me.onViewButtonClick, me);

        me.items = [{
            xtype: 'metricwell',
            text: '<b>Users</b>',
            icon: 'ico-users.png',            
            metric: 'workloadStats.all.activeWorkerCount',
            itemId: 'metrics-users',
            input: 'spinner',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Hosts</b><br />per region',
            icon: 'ico-process.png',            
            metric: 'workloadStats.all.activeWorkerCount',
            itemId: 'metrics-hosts',
            input: 'slider',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Regions</b>',
            icon: 'ico-pin.png',            
            metric: 'workloadStats.all.activeWorkerCount',
            itemId: 'metrics-regions',
            input: 'slider',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Throughput</b><br />transactions/sec',
            icon: 'ico-dashboard.png',            
            format: ',.0',
            metric: 'transactionStats.all.totalCountDelta',
            itemId: 'metrics-throughput',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Latency</b><br />ms/transactions',
            icon: 'ico-dashboard.png',
            format: ',.0',
            metric: 'transactionStats.all.avgDurationCalc',
            itemId: 'metrics-latency',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Storefront</b><br />items in carts',
            icon: 'ico-product.png',
            metric: 'storefrontStats.all.cartItemCount',
            itemId: 'metrics-storefront',
            listeners: {
                click: clickHandler
            }
        }];

        me.callParent(arguments);
        me.btnShowStore = me.down('#btnShowStore');
        me.viewButtons = Ext.ComponentQuery.query('button, metricwell', me);

        App.app.on('viewchange', function(viewName) {
            for ( var i = 0; i < me.viewButtons.length; i++) {
                var btn = me.viewButtons[i];
                btn.toggle(btn.getItemId() == viewName, true);
            }
        });
    },

    /** @private event handler */
    onViewButtonClick: function(btnActive) {
        var me = this;
        var viewName = btnActive.getItemId();
        App.app.fireEvent('viewchange', viewName);
    }
});

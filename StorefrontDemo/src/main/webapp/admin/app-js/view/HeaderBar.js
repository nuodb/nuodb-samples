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
        var changeHandler = Ext.bind(me.onChange, me);

        me.items = [{
            xtype: 'metricwell',
            text: '<b>Users</b>',
            icon: 'ico-users.png',
            metric: 'workloadStats.all.activeWorkerCount',
            itemId: 'metrics-users',
            input: 'spinner',
            flex: 0.7,
            listeners: {
                click: clickHandler,
                change: changeHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Hosts</b><br />per region',
            icon: 'ico-process.png',
            metric: 'dbStats.usedHostCount',
            itemId: 'metrics-hosts',
            input: 'slider',
            inputMaxMetric: 'dbStats.hostCount',
            flex: 0.7,
            listeners: {
                click: clickHandler,
                change: changeHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Regions</b>',
            icon: 'ico-pin.png',
            metric: 'dbStats.usedRegionCount',
            itemId: 'metrics-regions',
            input: 'slider',
            inputMaxMetric: 'dbStats.regionCount',
            flex: 0.7,
            listeners: {
                click: clickHandler,
                change: changeHandler
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
            flex: 0.7,
            listeners: {
                click: clickHandler
            }
        }];

        me.callParent(arguments);
        me.btnHosts = me.down('[itemId=metrics-hosts]');
        me.btnRegions = me.down('[itemId=metrics-regions]');
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
    },

    onChange: function(btn, value) {
        var me = this;
        switch (btn.itemId) {
            case 'metrics-users':
                d = Ext.ComponentQuery.query('uxiframe')[0].getDoc()
                $('input[type=number]:not([readonly])', d).each(function() {
                    var currentVal = Math.max(0, parseInt($(this).val()));
                    $(this).val((value > 0) ? currentVal + 10 : (value < 0) ? Math.max(0, currentVal - 10) : 0);
                });
                $('.btn-update', d).click();
                
                break;

            case 'metrics-hosts':
            case 'metrics-regions':
                Ext.Ajax.request({
                    url: App.app.apiBaseUrl + '/api/stats/db?numRegions=' + me.btnRegions.getInputValue() + "&numHosts=" + me.btnHosts.getInputValue(),
                    method: 'PUT',
                    scope: this,
                    failure: function(response) {
                        App.app.fireEvent('error', response, null);
                    }
                });
                break;

            default:
                break;
        }
    }
});

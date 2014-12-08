/* Copyright (c) 2013-2014 NuoDB, Inc. */

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
            href: '/control-panel-processes',
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
            href: '/control-panel-regions',
            listeners: {
                click: clickHandler,
                change: changeHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Throughput</b><br />transactions/sec',
            icon: 'ico-dashboard.png',
            format: ',.0',
            displayAvg: true,
            metric: 'transactionStats.all.totalCountDelta',
            itemId: 'metrics-throughput',
            listeners: {
                click: clickHandler
            }
        }, {
            xtype: 'metricwell',
            text: '<b>Latency</b><br />ms/transaction',
            icon: 'ico-dashboard.png',
            format: ',.0',
            displayAvg: true,
            metric: 'transactionStats.all.avgDurationCalc',
            itemId: 'metrics-latency',
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
                btn.toggle(btn.getItemId() == viewName || btn.href == viewName, true);
            }
        });
    },

    /** @private event handler */
    onViewButtonClick: function(btnActive) {
        var viewName = btnActive.getItemId();
        App.app.fireEvent('viewchange', viewName, true, null);
    },

    onChange: function(btn, value) {
        var me = this;
        switch (btn.itemId) {
            case 'metrics-users':
                if (!me.adjustUserLoad(value)) {
                    App.app.fireEvent('viewchange', '/control-panel-users', true, 'viewload');
                    App.app.on('viewload', function(viewName) {
                        me.adjustUserLoad(value);
                    }, me, {
                        single: true
                    });
                }
                break;

            case 'metrics-hosts':
            case 'metrics-regions':
                if (btn.activeRequest) {
                    Ext.Ajax.abort(btn.activeRequest);
                }
                btn.noInputSyncUntil = new Date().getTime() + 1000 * 60;
                btn.setWait(true);
                var thisRequest;
                btn.activeRequest = thisRequest = Ext.Ajax.request({
                    url: App.app.apiBaseUrl + '/api/stats/db?numRegions=' + me.btnRegions.getInputValue() + "&numHosts=" + me.btnHosts.getInputValue(),
                    method: 'PUT',
                    scope: this,
                    success: function() {
                        btn.noInputSyncUntil = new Date().getTime() + 1000 * 3;
                    },
                    failure: function(response) {
                        App.app.fireEvent('error', response, null);
                        btn.noInputSyncUntil = 0;
                    },
                    callback: function() {
                        if (btn.activeRequest == thisRequest) {
                            delete btn.activeRequest;
                        }
                    }
                });
                break;

            default:
                break;
        }
    },

    adjustUserLoad: function(value) {
        try {
            var frame = Ext.ComponentQuery.query('[itemId=userView]')[0];
            if (new Date() - frame.lastLoadTime > App.app.simulatedUserPageExpiryMs) {
                return false;
            }

            var doc = frame.getDoc();
            if ($('#table-regions', doc).length == 0) {
                return false;
            }

            $('input[type=number]:not([readonly])', doc).each(function() {
                var currentVal = Math.max(0, parseInt($(this).val()));
                
                // Unless we're stopping all, adjust non-analyst workloads only
                if (value == 0 || !/analyst/.test($(this).attr('name'))) {
                    $(this).val((value > 0) ? currentVal + 10 : (value < 0) ? Math.max(0, currentVal - 10) : 0);
                }
            });

            $('.btn-update', doc).click();
            frame.lastLoadTime = new Date();
            return true;
        } catch (e) {
            return false;
        }
    }
});

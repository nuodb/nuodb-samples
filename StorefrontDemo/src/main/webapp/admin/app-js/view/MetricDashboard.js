/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.MetricDashboard
 */

Ext.define('App.view.MetricDashboard', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metricdashboard',
    requires: ['App.view.MetricChart'],

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    /** @Override */
    initComponent: function() {
        var me = this;

        // Divide charts into two columns
        var col1EndIdx = Math.ceil(me.metrics.length / 2);
        var col1 = me.createColumnConfig(me.metrics, 0, col1EndIdx);
        me.items = (me.metrics.length == 1) ? col1 : [col1, me.createColumnConfig(me.metrics, col1EndIdx, me.metrics.length)];

        me.callParent(arguments);
    },

    createColumnConfig: function(metrics, startIdx, endIdx) {
        var col = {
            flex: 1,
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 20 0 20',
            items: []
        };

        for ( var i = startIdx; i < endIdx; i++) {
            col.items.push({
                flex: 1,
                xtype: 'metricchart',
                metric: metrics[i],
                header: false,
                padding: (i == endIdx - 1) ? '0 0 20 0' : '0 0 60 0',
            })
        }

        return col;
    }
});

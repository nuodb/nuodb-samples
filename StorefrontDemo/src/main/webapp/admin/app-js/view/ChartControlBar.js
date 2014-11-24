/* Copyright (c) 2013-2014 NuoDB, Inc. */

/**
 * @class App.view.ChartControlBar
 */
Ext.define('App.view.ChartControlBar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.chartcontrolbar',
    padding: '10 0',
    border: 0,

    items: ['->', '<b>History:</b>', ' ', {
        text: '1 min',
        toggleGroup: 'x',
        pressed: true
    }, {
        text: '2 min',
        toggleGroup: 'x'
    }, {
        text: '5 min',
        toggleGroup: 'x'
    }, {
        text: '10 min',
        toggleGroup: 'x'
    }, {
        text: '15 min',
        toggleGroup: 'x'
    }, ' ', '-', ' ', '<b>Y AXIS:</b>', ' ', {
        text: 'Auto-adjust',
        toggleGroup: 'y',
        pressed: true,
        tooltip: 'Adjusts the maximum value of the y axis to the largest value currently visible.'
    }, {
        text: 'Lock to peak',
        toggleGroup: 'y',
        tooltip: 'Fixes the maximum value of the y axis to the largest value encountered, even if that value is no longer visible.'
    }, '->'],

    onClickHandler: function(btn) {
        switch (btn.toggleGroup) {
            case 'x':
                App.app.maxStatsHistory = parseInt(btn.text) * 60 + 1;
                break;

            case 'y':
                App.app.lockStatsYAxisToMax = btn.text.indexOf('Lock') >= 0;
                break;
                
            default:
                return;
        }
        
        App.app.fireEvent('chartconfigchange');
    },

    /** @Override */
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.items.each(function() {
            this.on('click', me.onClickHandler);
        });
    }
});

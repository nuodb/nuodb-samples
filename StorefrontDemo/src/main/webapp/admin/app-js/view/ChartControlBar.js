/* Copyright (c) 2013-2015 NuoDB, Inc. */

/**
 * @class App.view.ChartControlBar
 */
Ext.define('App.view.ChartControlBar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.chartcontrolbar',
    padding: '10 0',
    border: 0,

    /** @Override */
    initComponent: function() {
        var me = this;

        me.items = ['->', '<b>HISTORY:</b>', ' ', {
            text: '1 min',
            handler: me.onClickHandler,
            toggleGroup: 'x',
            pressed: true,
            allowDepress: false
        }, {
            text: '2 min',
            handler: me.onClickHandler,
            toggleGroup: 'x',
            allowDepress: false
        }, {
            text: '5 min',
            handler: me.onClickHandler,
            toggleGroup: 'x',
            allowDepress: false
        }, {
            text: '10 min',
            handler: me.onClickHandler,
            toggleGroup: 'x',
            allowDepress: false
        }, {
            text: '15 min',
            handler: me.onClickHandler,
            toggleGroup: 'x',
            allowDepress: false
        }, ' ', '-', ' ', '<b>Y AXIS:</b>', ' ', {
            text: 'Auto-adjust',
            handler: me.onClickHandler,
            toggleGroup: 'y',
            pressed: !App.app.lockStatsYAxisToMax,
            tooltip: 'Adjusts the maximum value of the y axis to the largest value currently visible.',
            tooltipType: 'title',
            allowDepress: false
        }, {
            text: 'Lock to peak',
            handler: me.onClickHandler,
            toggleGroup: 'y',
            pressed: App.app.lockStatsYAxisToMax,
            tooltip: 'Fixes the maximum value of the y axis to the largest value encountered, even if that value is no longer visible.',
            tooltipType: 'title',
            allowDepress: false
        }, '->'];

        me.callParent(arguments);
    },

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
    }
});

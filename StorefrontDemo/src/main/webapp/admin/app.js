/* Copyright (c) 2013-2015 NuoDB, Inc. */

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

/**
 * Defines the admin application.
 */
Ext.application({
    name: 'App',
    title: 'Storefront Demo',
    appFolder: 'app-js',
    autoCreateViewport: true,
    enableQuickTips: true,

    refreshFrequencyMs: 1000 * 1,
    refreshGracePeriodMs: 2000 * 1, 
    instanceListRefreshFrequencyMs: 1000 * 30,
    maxStatsHistory: 61,
    lockStatsYAxisToMax: true,
    apiBaseUrl: '..',
    maxOutstandingRequestCount: 2,
    minHeavyCpuUtilizationPct: 90,
    msgDefaultDisplayTimeMs: 5 * 1000,
    simulatedUserPageExpiryMs: 5 * 1000,

    defaultLineColor: '#c0cd30',
    defaultFillColor: '#e3e7a7',

    controllers: ['Storefront', 'RemoteStorefronts'],

    paths: {
        'Ext.ux': 'ext-js-ux'
    },

    constructor: function() {
        document.title = 'NuoDB ' + this.title;

        Ext.define('Ext.chart.theme.AppTheme', {
            extend: 'Ext.chart.theme.Base',

            constructor: function(config) {
                var valueFont = '11px Tahoma';
                var gray = '#958979';

                this.callParent([Ext.apply({
                    colors: ['#26a9e0', '#24e0cc', '#4be123', '#d7db22', '#cca61b', '#c76424', '#e5624d', '#ff4b5c', '#e650af', '#bd50e5', '#8e6892', '#6084a9', '#88979d', '#97aea0', '#b0c3c9'],
                    
                    axis: {
                        stroke: gray
                    },
                    axisLabelLeft: {
                        fill: gray,
                        font: valueFont
                    },
                    axisTitleLeft: {
                        fill: gray,
                        font: valueFont
                    },
                    axisLabelBottom: {
                        fill: gray,
                        font: valueFont
                    }
                }, config)]);
            }
        });

        this.callParent(arguments);
    }
});

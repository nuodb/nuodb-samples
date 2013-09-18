/* Copyright (c) 2013 NuoDB, Inc. */

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
    instanceListRefreshFrequencyMs: 1000 * 30,
    maxStatsHistory: 60,
    apiBaseUrl: '..',
    maxOutstandingRequestCount: 2,

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
                var titleFont = '11px Tahoma bold';
                var gray = '#958979';
                var dkGray = '#444';

                this.callParent([Ext.apply({
                    colors: ['#95b452', '#c3cf21', '#cca61b', '#b78776', '#b28697', '#8894a5', '#a2998b', '#66644a', '#828a16', '#886f12', '#6d4a46', '#684963', '#44556f', '#58595b'],
                    axis: {
                        stroke: gray
                    },
                    axisLabelLeft: {
                        fill: gray,
                        font: valueFont
                    },
                    axisTitleLeft: {
                        fill: gray,
                        font: titleFont
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

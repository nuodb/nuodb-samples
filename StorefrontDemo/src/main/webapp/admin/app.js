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
                var titleFont = '11px Tahoma bold';
                var gray = '#958979';
                var dkGray = '#444';

                this.callParent([Ext.apply({
                    //colors: ['#c3cf21', '#7d8321', '#26a9e0', '#246e8d', '#a2998b', '#58595b', '#8783a4', '#002856', '#bd3632', '#5f172d', '#ffb202', '#9e730f'],
                    //colors: ['#26a9e0', '#24e0cc', '#4be123', '#c3cf21', '#cca61b', '#b78776', '#a2998b', '#e23d22', '#e2229c', '#ae22e2', '#222be2', '#262d48', '#c0c0c0'],
                    //colors: ['#26a9e0', '#b0c3c9', '#135e83', '#687c83', '#c3cf21', '#8fb528', '#e7d396', '#b1a265', '#dea5b8', '#b78297', '#efba9a', '#e0875a', '#d8630c', '#bb590f'],
                    colors: ['#26a9e0', '#24e0cc', '#4be123', '#d7db22', '#cca61b', '#c76424', '#e5624d', '#e650af', '#bd50e5', '#8e6892', '#6084a9', '#88979d', '#97aea0', '#b0c3c9'],
                    
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

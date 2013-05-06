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

    refreshFrequencyMs: 1000,
    maxStatsHistory: 60,
    apiBaseUrl: '..',

    controllers: ['Storefront'],
    models: ['Host', 'Metric', 'Workload'],
    stores: ['Hosts', 'Metrics', 'Workloads'],

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
                    colors: ['#95b452', '#c3cf21', '#cca61b', '#b78776', '#b28697', '#8894a5', '#a2998b', '#58595b', '#44556f', '#6d4a46', '#684963', '#66644a'],
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

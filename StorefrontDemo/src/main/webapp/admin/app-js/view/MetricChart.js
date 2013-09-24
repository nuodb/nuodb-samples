/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.view.MetricChart', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metricchart',

    layout: 'card',

    /** @Override */
    initComponent: function() {
        var me = this;

        me.tbar = ['->', {
            xtype: 'container',
            html: '<b>' + me.metric.get('title').toUpperCase() + '</b> &nbsp;&nbsp;'
        }, ' ', {
            xtype: 'button',
            text: 'Total',
            handler: me.onChangeGrouping,
            scope: me,
            categoryIdx: null
        }, {
            xtype: 'button',
            text: me.metric.get('groupBy0'),
            handler: me.onChangeGrouping,
            scope: me,
            categoryIdx: 0
        }];

        if (me.metric.get('groupBy1')) {
            me.tbar.push({
                xtype: 'button',
                text: me.metric.get('groupBy1'),
                handler: me.onChangeGrouping,
                scope: me,
                categoryIdx: 1
            });
        }
        me.tbar.push('->');

        me.showMetric(me.metric);
        me.callParent(arguments);
    },

    showMetric: function(metric, categoryIdx) {
        var me = this;
        
        if (categoryIdx === undefined) {
            categoryIdx = metric.get('defaultCategoryIdx');
        }
        
        me.metric = metric;
        me.categoryIdx = categoryIdx;

        var store = App.app.getController('Storefront').getMetricHistoryStore(metric, categoryIdx);
        if (store == null) {
            // Metrics aren't available yet.  Wait for the store to become available and try again.
            App.app.on('statschange', function() {
                me.showMetric(me.metric, me.categoryIdx);
            }, null, {
                single: true
            });
            return null;
        }

        var chartConfig = me.createChartConfig(store, metric, categoryIdx);

        store.on('metachange', me.onStoreMetaChange, me);

        if (me.rendered) {
            me.removeAll();
            me.add(chartConfig);
        } else {
            me.items = chartConfig;
        }
    },

    onStoreMetaChange: function() {
        var me = this;
        me.showMetric(me.metric, me.categoryIdx);
    },

    onChangeGrouping: function(src) {
        var me = this;
        me.showMetric(me.metric, src.categoryIdx);
    },

    createChartConfig: function(store, metric, categoryIdx) {
        var me = this;
        var metricName = metric.get('name');
        var aggregate = categoryIdx == null;
        var unitName = metric.get('unit');
        var unitNameLcase = unitName.toLowerCase();

        if (Ext.isArray(me.tbar)) {
            me.tbar[3].pressed = aggregate;
            me.tbar[4].pressed = (categoryIdx == 0);
            if (me.tbar.length >= 6) {
                me.tbar[5].pressed = (categoryIdx === 1);
            }
        } else {
            var buttons = me.dockedItems.get(0).items;
            buttons.get(3).toggle(aggregate);
            buttons.get(4).toggle(categoryIdx == 0);
            if (buttons.get(5).toggle) {
                buttons.get(5).toggle(categoryIdx == 1);
            }
        }

        // Detect series
        var seriesNames = [];
        var fields = store.model.getFields();
        var actualSeriesCount = 0;
        for ( var i = 0; i < fields.length; i++) {
            if (!fields[i].name) {
                // Ignore series without names
                continue;
            }

            switch (fields[i].name) {
                case 'timestamp':
                case 'id':
                    // Ignore
                    break;

                case 'all':
                    if (aggregate) {
                        seriesNames.push(fields[i].name);
                    }
                    break;

                default:
                    actualSeriesCount++;
                    if (!aggregate) {
                        seriesNames.push(fields[i].name);
                    }
                    break;
            }
        }

        // Build series configs
        var series = [];
        var hasMultiSeries = (!aggregate);
        var tooltipFormat = (hasMultiSeries) ? '{0}:<br />{1} {2}' : '{1} {2}';
        series.push({
            type: 'area',
            smooth: true,
            showMarkers: false,
            markerConfig: {
                radius: 2
            },
            axis: 'left',
            xField: 'timestamp',
            yField: seriesNames,
            highlight: true,
            fill: true,
            style: {
                'stroke-width': 1,
                stroke: '#eee',
                opacity: 0.8
            },
            tips: {
                trackMouse: true,
                minWidth: 150,
                renderer: function(record, ctx) {
                    var seriesName = ctx.storeField || seriesNames[0];
                    this.setTitle(Ext.String.format(tooltipFormat, seriesName, record.get(seriesName), unitNameLcase));
                }
            }
        });

        if (aggregate) {
            series[0].type = 'line';
            series[0].highlight = false;
            series[0].style.fill = App.app.defaultFillColor;
            series[0].style.stroke = App.app.defaultLineColor;
            series[0].style['stroke-width'] = 4;
            series[0].yField = series[0].yField[0];
        }

        // Build chart config
        return {
            xtype: 'chart',
            theme: 'AppTheme',
            store: store,
            padding: '5 0 0 0',
            legend: {
                position: 'right',
                boxFill: 'none',
                boxStrokeWidth: 0,
                visible: hasMultiSeries,
                itemSpacing: 0,
                labelFont: '11px Tahoma',
                labelColor: '#958979'
            },
            axes: [{
                type: 'Numeric',
                minimum: 0,
                position: 'left',
                fields: seriesNames,
                title: unitName,
                grid: {
                    odd: {
                        opacity: 1,
                        fill: '#f8f8f8'
                    }
                }
            }, {
                type: 'Time',
                dateFormat: 'h:i:s a',
                fromDate: me.calcFromDate(store),
                toDate: me.calcToDate(store),
                position: 'bottom',
                fields: 'timestamp',
                step: [Ext.Date.SECOND, 60],
                minorTickSteps: 0
            }],
            series: series,
            listeners: {
                beforerefresh: function() {
                    var yAxis = this.axes.getAt(1);
                    var store = this.getStore();
                    yAxis.fromDate = me.calcFromDate(store);
                    yAxis.toDate = me.calcToDate(store);
                }
            }
        };
    },

    calcToDate: function(store) {
        return store.getAt(store.getCount() - 1).get('timestamp');
    },

    calcFromDate: function(store) {
        var me = this;
        var fromDate = store.getAt(0).get('timestamp');
        var numPoints = store.getCount();
        if (numPoints < App.app.maxStatsHistory) {
            fromDate = new Date(fromDate.getTime() - (App.app.maxStatsHistory - numPoints) * App.app.refreshFrequencyMs);
        }
        return fromDate;
    }
});

/* Copyright (c) 2013-2014 NuoDB, Inc. */

Ext.define('App.view.MetricChart', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metricchart',

    layout: 'card',

    seriesConfigMap: {
        line: {
            type: 'line',
            seriesPerField: true,
            smooth: false,
            showMarkers: true,
            markerConfig: {
                type: 'circle',
                radius: 3
            },
            highlight: {
                size: 6,
                radius: 6
            },
            fill: false,
            style: {
                'stroke-width': 3
            }
        },
        column: {
            type: 'column',
            gutter: 0,
            groupGutter: 0,
            renderer: function(sprite, record, attr, index, store) {
                attr.width += 1;
                return attr;
            }
        },
        area: {
            type: 'area',
            highlight: {
                stroke: '#000',
                opacity: 1
            },
            style: {
                'stroke-width': 0,
                stroke: '#eee',
                opacity: 0.5
            },
            tips: {}
        },
        common: {
            xField: 'timestamp',
            smooth: true,
            showMarkers: false,
            stacked: true,
            axis: 'left',
            fill: true,
            tooltipFormat: '{0}:<br />{1} {2}',
            tips: {
                trackMouse: true,
                minWidth: 150,
                renderer: function(record, ctx) {
                    var seriesName = ctx.storeField || ctx.yField || ctx.series.yField;
                    this.setTitle(Ext.String.format(ctx.series.tooltipFormat, seriesName, Ext.util.Format.number(record.get(seriesName), '0.0'), ctx.series.tooltipUnit));
                }

            }
        }
    },

    /** @Override */
    initComponent: function() {
        var me = this;

        me.tbar = ['->', '<b>' + me.metric.get('title').toUpperCase() + '</b> &nbsp;&nbsp;', ' ', {
            xtype: 'button',
            text: 'Overall',
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

        App.app.on('chartconfigchange', function() {
            me.items.each(function(chart) {
                me.updateChartAxes(chart);
                //chart.redraw();  <-- too slow, just let the chart update itself at the next store update
            });
        });
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
        var aggregate = categoryIdx == null;
        var unitName = metric.get('unit');

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
        var seriesType = metric.get('chartType') || 'area';
        var seriesConfig = $.extend(true, {
            tooltipUnit: unitName.toLowerCase()
        }, me.seriesConfigMap.common, me.seriesConfigMap[seriesType]);

        if (aggregate) {
            series.push($.extend(seriesConfig, {
                type: 'line',
                yField: seriesNames[0],
                showMarkers: false,
                style: {
                    fill: App.app.defaultFillColor,
                    stroke: App.app.defaultLineColor,
                    'stroke-width': 3
                },
                tooltipFormat: '{1} {2}'
            }));
        } else if (seriesConfig.seriesPerField) {
            for ( var i = 0; i < seriesNames.length; i++) {
                series.push($.extend(true, {
                    yField: seriesNames[i]
                }, seriesConfig));
            }
        } else {
            seriesConfig.yField = seriesNames;
            series.push(seriesConfig);
        }

        var dateRange = me.calcDateRange(store);

        // Build chart config
        return {
            xtype: 'chart',
            theme: 'AppTheme',
            store: store,
            padding: '5 0 0 0',
            shadow: false,
            legend: {
                position: 'right',
                boxFill: 'none',
                boxStrokeWidth: 0,
                visible: !aggregate,
                itemSpacing: 0,
                labelFont: '11px Tahoma',
                labelColor: '#958979'
            },
            axes: [{
                type: 'Numeric',
                constrain: false,
                minimum: 0,
                maximum: me.calcYAxisMax(),
                position: 'left',
                fields: seriesNames,
                title: unitName,
                adjustMaximumByMajorUnit: true,
                grid: {
                    odd: {
                        opacity: 1,
                        fill: '#f8f8f8'
                    }
                }
            }, {
                type: 'Time',
                dateFormat: 'h:i:s a',
                fromDate: dateRange[0],
                toDate: dateRange[1],
                position: 'bottom',
                fields: 'timestamp',
                minorTickSteps: 0
            }],
            series: series,
            listeners: {
                beforerefresh: function() {
                    me.updateChartAxes(this);
                },
                show: function() {
                    alert(0);
                }
            }
        };
    },

    updateChartAxes: function(chart) {
        var me = this;
        var store = chart.getStore();

        // Configure x axis
        var xAxis = chart.axes.getAt(1);
        var dateRange = me.calcDateRange(store);
        xAxis.fromDate = dateRange[0];
        xAxis.toDate = dateRange[1];

        // Configure y axis
        var yAxis = chart.axes.getAt(0);
        yAxis.maximum = me.calcYAxisMax();

    },

    calcYAxisMax: function() {
        var me = this;
        if (App.app.lockStatsYAxisToMax) {
            max = me.metric.get(me.categoryIdx != null && me.categoryIdx >= 0 ? 'maxStackedValue' + me.categoryIdx: 'maxValue');
            if (max > 0) {
                return max;
            }
        }
        return undefined;
    },

    calcDateRange: function(store) {
        var toDate = store.getAt(store.getCount() - 1).get('timestamp');
        var fromDate = new Date(toDate.getTime() - App.app.refreshFrequencyMs * (App.app.maxStatsHistory - 1));
        return [fromDate, toDate];
    }
});

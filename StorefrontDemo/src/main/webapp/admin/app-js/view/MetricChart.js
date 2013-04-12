/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.view.MetricChart', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metricchart',

    layout: 'card',

    /** @Override */
    initComponent: function() {
        var me = this;

        me.tools = [{
            type: 'plus',
            itemId: 'btnShowSeries',
            hidden: true,
            handler: function() {
                me.setToolVisible('plus', false);
                me.setToolVisible('minus', true);
                me.showMetric(me.metric, false);
            }
        }, {
            type: 'minus',
            itemId: 'btnHideSeries',
            hidden: true,
            handler: function() {
                me.setToolVisible('plus', true);
                me.setToolVisible('minus', false);
                me.showMetric(me.metric, true);
            }
        }];
        
        if (me.metric) {
            me.showMetric(me.metric);
        }
        
        me.callParent(arguments);
    },

    showMetric: function(metric, aggregate) {
        var me = this;
        me.metric = metric;
        me.aggregate = aggregate;

        var store = App.app.getController('Storefront').getMetricHistoryStore(metric);
        if (store == null) {
            // Metrics aren't available yet.  Wait for the store to become available and try again.
            App.app.on('statschange', function() {
                me.showMetric(me.metric, me.aggregate);
            }, null, {
                single: true
            });
            return null;
        }

        var chartConfig = me.createChartConfig(store, metric, aggregate);

        if (me.rendered) {
            me.removeAll();
            me.add(chartConfig);
        } else {
            me.items = chartConfig;
        }
    },

    createChartConfig: function(store, metric, aggregate) {
        var me = this;
        var metricName = metric.get('name');
        var aggregate = (aggregate === undefined) ? metric.get('aggregate') : aggregate;
        var unitName = metric.get('unit');
        var unitNameLcase = unitName.toLowerCase();
        
        // Detect series
        var seriesNames = [];
        var fields = store.model.getFields();
        var actualSeriesCount = 0;
        for ( var i = 0; i < fields.length; i++) {
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
        var hasMultiSeries = (seriesNames.length > 1);
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
        
        if (seriesNames.length == 1) {
            series[0].type = 'line';
            series[0].highlight = false;
            series[0].style.fill = '#edf0cf';
            series[0].style.stroke = '#bfc48e';
            series[0].style['stroke-width'] = 4;
            series[0].yField = series[0].yField[0];
        }
        
        me.setToolVisible('plus', actualSeriesCount > 1 && aggregate);
        me.setToolVisible('minus', actualSeriesCount > 1 && !aggregate);

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
    },
    
    setToolVisible: function(toolType, visible) {
        var me = this;
        for (var i = 0; i < me.tools.length; i++) {
            var tool = me.tools[i];
            if (tool.type == toolType) {
                if (me.rendered) {
                    tool.setVisible(visible);
                } else {
                    tool.hidden = !visible;
                }
                break;
            }
        }
    }
});

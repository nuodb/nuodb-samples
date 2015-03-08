/* Copyright (c) 2013-2015 NuoDB, Inc. */

Ext.define('App.view.MetricWell', {
    extend: 'Ext.container.Container',
    alias: 'widget.metricwell',
    requires: ['Ext.ux.column.Sparkline'],

    /** {@cfg} {Number} value */
    value: 0,

    flex: 1,
    margin: '0 8',
    minWidth: 150,
    displayAvg: false,

    maxHistory: 20,
    valueSum: 0,
    valueCount: 0,
    noInputSyncUntil: 0,

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    cls: 'metric-well',

    /** {@cfg} {String} metric */

    /** {@cfg} {Function} formatter */

    /** {@cfg} {String} format */

    /** @Override */
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            graphId: me.getId() + '-graph',
            formatter: Ext.util.Format.number,
            format: ',',
            value: 0,
            border: 1
        });

        me.valueHistory = [];

        if (me.input) {
            me.cls += ' well-with-input';
        }

        for ( var i = 0; i < me.maxHistory; i++) {
            me.valueHistory.push(null);
        }

        me.items = [{
            layout: 'absolute',
            cls: 'btn',
            itemId: 'btn',
            height: 80,
            flex: 1,
            items: [{
                xtype: 'container',
                itemId: 'graphContainer',
                cls: 'graph-container',
                html: '<div id="' + me.graphId + '"></div>',
                hidden: me.graphVisible === false
            }, {
                xtype: 'label',
                html: '<img src="img/' + me.icon + '" width="16" height="16" />' + me.text,
                cls: 'label-metric'
            }, {
                width: '100%',
                xtype: 'label',
                html: '&nbsp;',
                cls: 'label-value',
                itemId: 'value'
            }]
        }];

        switch (me.input) {
            case 'slider':
                me.items.push({
                    xtype: 'container',
                    layout: 'fit',
                    cls: 'btn-input',
                    items: {
                        xtype: 'slider',
                        padding: '7 3 7 3',
                        vertical: true,
                        increment: 1,
                        minValue: 1,
                        maxValue: 1,
                        disabled: true,
                        layout: 'fit',
                        listeners: {
                            changecomplete: function(slider, newValue) {
                                me.fireEvent('change', me, newValue);
                            }
                        }
                    }
                });
                break;

            case 'spinner':
                me.items.push({
                    xtype: 'toolbar',
                    cls: 'btn-input',
                    vertical: true,
                    items: [{
                        iconCls: 'ico-up',
                        tooltip: 'Increase simulated users by 10 per workload, per region',
                        listeners: {
                            click: function() {
                                me.fireEvent('change', me, 1);
                            }
                        }
                    }, {
                        iconCls: 'ico-down',
                        tooltip: 'Decrease simulated users by 10 per workload, per region',
                        listeners: {
                            click: function() {
                                me.fireEvent('change', me, -1);
                            }
                        }
                    }, ' ', {
                        iconCls: 'ico-cancel',
                        tooltip: 'Stop all simulated users',
                        listeners: {
                            click: function() {
                                me.fireEvent('change', me, 0);
                            }
                        }
                    }],
                    padding: '2 2 0 2'
                });
                break;
        }

        me.callParent(arguments);

        me.lblValue = me.down('[itemId=value]');
        me.graphContainer = me.down('[itemId=graphContainer]');
        me.btn = me.down('[itemId=btn]');
        me.btn.on('afterrender', function() {
            var el = me.btn.getEl();
            el.on('mouseenter', function() {
                el.addCls('hover');
            });
            el.on('mouseleave', function() {
                el.removeCls('hover');
            });
            el.on('mousedown', function() {
                me.getEl().addCls('active');
            });
            el.on('mouseup', function() {
                if (!me.pressed) {
                    me.toggle(!me.pressed);
                }
            });
        });

        me.inputSlider = me.down('[xtype=slider]');

        me.setValue(me.value);
        App.app.on('statschange', me.onStatsChange, me);
    },

    /** @Override */
    destroy: function() {
        var me = this;
        App.app.un('statschange', me.onStatsChange, me);
        this.callParent(arguments);
    },

    getValue: function() {
        return this.value;
    },

    getInputValue: function() {
        return !this.inputSlider ? null : this.inputSlider.getValue();
    },

    setValue: function(value) {
        var me = this;
        me.value = value || 0;
        var removedValues = me.valueHistory.splice(0, me.valueHistory.length - me.maxHistory);
        me.valueHistory.push(me.value);

        // Adjust sum & count for averages
        if (me.displayAvg) {
            for ( var i = 0; i < removedValues.length; i++) {
                if (removedValues[i] != null) {
                    me.valueSum -= removedValues[i];
                    me.valueCount--;
                }
            }
            me.valueCount++;
            me.valueSum += value || 0;
            me.lblValue.setText(me.formatter(me.valueSum / me.valueCount, me.format));
        } else {
            me.lblValue.setText(me.formatter(value, me.format));
        }

        if (me.rendered) {
            me.syncGraph();
        }
    },

    /** @Override */
    afterLayout: function() {
        var me = this;
        me.callParent(arguments);
        me.syncGraph();
    },

    toggle: function(state, suppressEvent) {
        var me = this;
        if (me.pressed != state) {
            me.pressed = state;
            if (me.pressed) {
                me.getEl().addCls('active');
            } else {
                me.getEl().removeCls('active');
            }
            if (suppressEvent !== true) {
                me.fireEvent('click', me);
            }
        }
    },

    onStatsChange: function(stats) {
        var me = this;

        val = stats.getLatestValue(me.metric);
        me.setValue(val);
        if (me.inputSlider) {
            if (me.noInputSyncUntil == 0 || me.noInputSyncUntil <= new Date().getTime()) {
                var max = stats.getLatestValue(me.inputMaxMetric);
                me.inputSlider.setMaxValue(max);
                me.inputSlider.setDisabled(max <= 1);
                me.inputSlider.setValue(val);
                me.setWait(false);
            }
        }
    },

    syncGraph: function() {
        var me = this;
        if (!me.graphContainer.isVisible()) {
            return;
        }

        $('#' + me.graphId).sparkline(me.valueHistory, {
            type: 'line',
            chartRangeMin: 0,
            spotColor: '',
            maxSpotColor: '',
            minSpotColor: '',
            height: '35',
            width: '100%',
            highlightLineColor: '#000',
            highlightSpotColor: '#000',
            lineWidth: 3,
            lineColor: App.app.defaultLineColor,
            fillColor: App.app.defaultFillColor,
            disableInteraction: true
        });
    },

    setWait: function(wait) {
        var me = this;
        me.getEl().query('img')[0].src = 'img/' + ((wait) ? 'wait.gif' : me.icon);
    }
});

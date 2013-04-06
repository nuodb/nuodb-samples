Ext.define('App.view.MetricWell', {
    extend: 'Ext.container.Container',
    alias: 'widget.metricwell',
    requires: ['Ext.ux.column.Sparkline'],

    /** {@cfg} {Number} value */
    value: 0,

    margin: '0 5',

    maxHistory: 10,

    cls: 'metric-well x-btn x-btn-default-medium',
    width: 150,
    allowDepress: false,

    /*
     * width: 150, height: 55,
     */

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
        for ( var i = 0; i < me.maxHistory; i++) {
            me.valueHistory.push(null);
        }
        
        if (me.pressed) {
            me.cls += ' x-btn-default-medium-pressed';
        }

        me.items = {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'label',
                html: me.text,
                cls: 'label-metric'
            }, {
                layout: 'absolute',
                height: 40,
                items: [{
                    xtype: 'container',
                    itemId: 'graphContainer',
                    cls: 'graph-container',
                    html: '<div id="' + me.graphId + '"></div>',
                    hidden: me.graphVisible === false
                }, {
                    width: '100%',
                    xtype: 'label',
                    html: '&nbsp;',
                    cls: 'label-value',
                    itemId: 'value'
                }]
            }]
        };

        me.callParent(arguments);
        me.on('afterrender', function() {
            var el = me.getEl();
            el.on('mouseenter', function() {
                el.addCls('x-btn-default-medium-over');
            });
            el.on('mouseleave', function() {
                el.removeCls('x-btn-default-medium-over');
                if (!me.pressed) {
                    el.removeCls('x-btn-default-medium-pressed');
                }
            });
            el.on('mousedown', function() {
                el.addCls('x-btn-default-medium-pressed');
            });
            el.on('mouseup', function() {
                if (!me.pressed || me.allowDepress) {
                    me.toggle(!me.pressed);
                }
            });            
        });

        me.lblValue = me.down('[itemId=value]');
        me.graphContainer = me.down('[itemId=graphContainer]');
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

    setValue: function(value) {
        var me = this;
        me.value = value || 0;
        me.valueHistory.splice(0, me.valueHistory.length - me.maxHistory);
        me.valueHistory.push(me.value);
        me.lblValue.setText(me.formatter(me.value, me.format));
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
                me.getEl().addCls('x-btn-default-medium-pressed');
            } else {
                me.getEl().removeCls('x-btn-default-medium-pressed');
            }
            if (suppressEvent !== true) {
                me.fireEvent('click', me);
            }
        }
    },

    onStatsChange: function(stats) {
        var me = this;
        me.setValue(stats.getLatestValue(me.metric));
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
            height: '40',
            width: '100%',
            highlightLineColor: '#000',
            highlightSpotColor: '#000',
            lineWidth: 3,
            lineColor: '#bfc48e',
            fillColor: '#edf0cf',
            disableInteraction: true
        });
    }
});

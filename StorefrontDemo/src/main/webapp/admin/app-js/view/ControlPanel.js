/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.ControlPanel
 * 
 * This view is not currently used, but may be available in the future.
 */
Ext.define('App.view.ControlPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.controlpanel',

    title: 'Control Panel',
    layout: 'fit',

    initComponent: function() {
        var me = this;
        
        // FIXME: Temporary code to seed store
        var hostStore = Ext.getStore("Hosts");
        for ( var i = 0; i < 1; i++) {
            hostStore.add({
                shortName: 'Host' + i,
                name: 'Host' + i,
                state: 'DOWN'
            });
        }

        var hostList = {
            xtype: 'panel',
            title: 'Hosts',
            id: 'images-view',
            autoScroll: true,
            items: {
                xtype: 'dataview',
                store: 'Hosts',
                tpl: ['<tpl for=".">', '<div class="thumb-wrap" id="{name}">', '<tpl if="state == ' + "'UP'" + '">', '<div class="thumb"><img src="images/app/host_up.png"></div>',
                // '<div class="thumb"><img
                // src="resources/images/app/server-up.png"></div>',
                '</tpl>', '<tpl if="state == ' + "'DOWN'" + '">', '<div class="thumb"><img src="resources/images/app/host_down.png"></div>',
                // '<div class="thumb"><img
                // src="resources/images/app/server-down.png"></div>',
                '</tpl>', '<span class="x-editable">{shortName}</span></div>', '</tpl>', '<div class="x-clear"></div>'],
                trackOver: true,
                overItemCls: 'x-item-over',
                itemSelector: 'div.thumb-wrap',
                emptyText: '<div style="width:auto; padding-top:10px; padding-left:10px;">No hosts to display</div>',
                prepareData: function(data) {
                    Ext.apply(data, {
                        shortName: Ext.util.Format.ellipsis(data.name, 15)
                    });
                    return data;
                },
                listeners: {
                    render: function(view) {
                        view.tip = Ext.create('Ext.tip.ToolTip', {
                            target: view.el,
                            delegate: view.itemSelector,
                            trackMouse: true,
                            // minWidth: 200,
                            // maxWidth: 400,
                            dismissDelay: 0,
                            showDelay: 800,
                            renderTo: Ext.getBody(),
                            listeners: {
                                beforeshow: function updateTipBody(tip) {
                                    var record = view.getRecord(tip.triggerElement);

                                    tip.update("Name: <b>" + record.get('name') + "</b><br>" + "Address: " + record.get('address') + "<br>" + 'State: ' + Ext.util.Format.nuoAppState(record.get('state')) + "<br>" + "Type: " + Ext.util.Format.nuoHost(record.get('type')) + "<br>" + "OS: " + Ext.util.Format.nuoOs(record.get('os')) + "<br>" + "Cores: "
                                            + record.get('cores') + "<br");
                                }
                            }
                        });
                    }
                }
            }
        };

        me.items = [{
            xtype: 'tabpanel',
            flex: 1,
            plain: true,
            activeTab: 2,
            items: [{
                xtype: 'gridpanel',
                title: 'Hosts',
                store: 'Hosts',
                tbar: {
                    items: [{

                    }]
                },
                viewConfig: {
                    markDirty: false
                },
                columns: [{
                    text: 'Hostname',
                    dataIndex: 'shortName',
                    flex: 1
                }, {
                    text: 'App History',
                    xtype: 'sparklinecolumn',
                    dataIndex: 'AppHistory',
                    width: 120,
                    globalMaxDataIndex: 'App',
                    sparklineConfig: {
                        type: 'line',
                        chargeRangeMin: 0,
                        spotColor: '',
                        maxSpotColor: '',
                        minSpotColor: '',
                        height: '19',
                        width: '100%',
                        highlightLineColor: '#000',
                        highlightSpotColor: '#000',
                        lineColor: '#5c6218',
                        fillColor: '#c0cd30',
                        tooltipOffsetY: -30,
                        tooltipOffsetX: 0,
                        tooltipFormatter: function(sparkline, options, fields) {
                            return (!Ext.isNumber(fields.y)) ? '' : Ext.util.Format.number(fields.y, ',') + ' App on ' + fields.x;
                        }
                    }
                }, {
                    text: 'App',
                    tooltip: 'Transactions per second',
                    dataIndex: 'App',
                    align: 'right',
                    width: 75,
                    renderer: function(value) {
                        return Ext.util.Format.number(value, ',');
                    }
                }]
            }, {
                title: 'Stores'
            }, {
                xtype: 'workloadgrid'
            }]
        }];

        me.fbar = {
            xtype: 'label',
            html: 'This pane will explain the selected tab and the value it demonstrates.',
            height: 60,
            resizable: true,
            resizeHandles: 'n'
        }

        me.callParent(arguments);
    }
});

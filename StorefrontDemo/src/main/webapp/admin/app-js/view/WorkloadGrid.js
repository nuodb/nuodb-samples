Ext.define('App.view.WorkloadGrid', {
    extend: 'Ext.grid.Panel',
    requires: ['App.model.Workload'],
    alias: 'widget.workloadgrid',
    
    title: 'Workloads',
    viewConfig: {
        markDirty: false
    },
    columns: [{
        text: 'Workload',
        dataIndex: 'name',
        flex: 1
    }, {
        text: 'Simulated Users',
        dataIndex: 'activeWorkerCount',
        width: 100,
        editor: {
            xtype: 'numberfield',
            minValue: 0,
            maxValue: 1000,
            allowBlank: false,
            selectOnFocus: true
        }
    }],
    store: 'Workloads',
    selModel: 'cellmodel',
    
    /** @Override */
    initComponent: function() {
        var me = this;
        
        me.plugins = [Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        })];
        
        me.callParent(arguments);
    }
});

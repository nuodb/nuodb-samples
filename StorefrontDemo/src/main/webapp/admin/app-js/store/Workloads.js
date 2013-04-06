Ext.define('App.store.Workloads', {
    extend: 'Ext.data.Store',
    model: 'App.model.Workload',
    proxy: 'memory'
});

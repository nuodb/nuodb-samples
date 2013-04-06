Ext.define('App.store.Hosts', {
    extend: 'Ext.data.Store',
    
    model: 'App.model.Host',
    proxy: 'memory'
});

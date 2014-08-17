/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.Sidebar
 */
Ext.define('App.view.HeaderBar', {
    extend: 'Ext.Component',
    alias: 'widget.navbar',

    border: false,
    cls: 'nuo-navbar',

    /** @Override */
    initComponent: function() {
        var me = this;
        
        me.html = 'test';
        
        me.callParent(arguments);
    }
});

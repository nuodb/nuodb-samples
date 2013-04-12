/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.model.Host', {
    extend: 'Ext.data.Model',
    idProperty: 'name',
    fields: [{
        name: 'name',
        type: 'string'
    }]
});

/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.model.Metric', {
    extend: 'Ext.data.Model',
    fields: ['name', 'category', 'seriesName', 'aggregate', 'title', 'unit', 'view', 'historyStore'],
    idgen: {
        type: 'sequential',
        prefix: 'metric'
    }    
});

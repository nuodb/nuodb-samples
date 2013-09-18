/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.model.Metric', {
    extend: 'Ext.data.Model',
    fields: ['name', 'category', 'seriesName', 'aggregateIdx', 'title', 'unit', 'view', 'groupBy', 'groupBy2', 'historyStore', 'historyStore2'],
    idgen: {
        type: 'sequential',
        prefix: 'metric'
    }    
});

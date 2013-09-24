/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.model.Metric', {
    extend: 'Ext.data.Model',
    fields: ['name', 'category0', 'category1', 'seriesName', 'defaultCategoryIdx', 'title', 'unit', 'view', 'groupBy0', 'groupBy1', 'historyStore0', 'historyStore1'],
    idgen: {
        type: 'sequential',
        prefix: 'metric'
    }    
});

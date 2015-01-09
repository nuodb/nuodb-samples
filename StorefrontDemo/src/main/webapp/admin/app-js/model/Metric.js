/* Copyright (c) 2013-2014 NuoDB, Inc. */

Ext.define('App.model.Metric', {
    extend: 'Ext.data.Model',
    fields: ['name', 'category0', 'category1', 'seriesName', 'defaultCategoryIdx', 'title', 'unit', 'view', 'groupBy0', 'groupBy1', 'historyStore0', 'historyStore1', 'chartType', {
        name: 'maxValue',
        type: 'int',
        defaultValue: 0
    }, {
        name: 'maxStackedValue0',
        type: 'int',
        defaultValue: 0
    }, {
        name: 'maxStackedValue1',
        type: 'int',
        defaultValue: 0
    }],
    idgen: {
        type: 'sequential',
        prefix: 'metric'
    }
});

/* Copyright (c) 2013-2015 NuoDB, Inc. */

Ext.define('App.store.Metrics', {
    extend: 'Ext.data.Store',
    model: 'App.model.Metric',
    proxy: 'memory',

    data: [{
        name: 'totalCountDelta',
        category0: 'transactionStats',
        category1: 'regionTransactionStats',
        defaultCategoryIdx: 0,
        view: 'metrics-throughput',
        title: 'Throughput',
        unit: 'Calls/second',
        groupBy0: 'Service Method',
        groupBy1: 'Region'
    }, {
        name: 'avgDurationCalc',
        category0: 'transactionStats',
        category1: 'regionTransactionStats',
        defaultCategoryIdx: 0,
        view: 'metrics-latency',
        title: 'Avg. Latency',
        unit: 'Milliseconds/call',
        groupBy0: 'Service Method',
        groupBy1: 'Region',
        chartType: 'area'
    }, {
        name: 'activeWorkerCount',
        category0: 'workloadStats',
        category1: 'regionWorkloadStats',
        defaultCategoryIdx: 0,
        view: 'metrics-users',
        title: 'Active Simulated Users',
        unit: 'Users',
        groupBy0: 'Workload',
        groupBy1: 'Region'
    }, {
        name: 'completionCountDelta',
        category0: 'workloadStepStats',
        category1: 'regionWorkloadStepStats',
        defaultCategoryIdx: 0,
        view: 'metrics-users',
        title: 'Simulated Steps per Second',
        unit: 'Steps/second',
        groupBy0: 'Workload Step',
        groupBy1: 'Region'
    }]
});

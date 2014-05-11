/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.store.Metrics', {
    extend: 'Ext.data.Store',
    model: 'App.model.Metric',
    proxy: 'memory',
    
    data: [
           // DB metrics
           //{ category0: 'dbStats', name: 'tps', aggregate: true, view: 'metrics-db', title: 'Transactions per Second', unit: 'Transactions/second' },
           //{ category0: 'dbStats', name: 'cpu', aggregate: true, view: 'metrics-db', title: 'CPU Utilization', unit: 'Utilization %' },
           //{ category0: 'dbStats', name: 'clientConnectionCount', aggregate: true, view: 'metrics-db', title: 'Client Connections', unit: 'Connections' },
           //{ category0: 'dbStats', name: 'commitCount', aggregate: true, view: 'metrics-db', title: 'Commits per Second', unit: 'Commits/second' },
           
           // Service metrics
           { category0: 'transactionStats', name: 'totalCountDelta', defaultCategoryIdx: null, view: 'metrics-service', title: 'Service Calls per Second', unit: 'Calls/second', groupBy0: 'Service Method', category1: 'regionTransactionStats', groupBy1: 'Region' },
           { category0: 'transactionStats', name: 'avgDurationCalc', defaultCategoryIdx: 0, view: 'metrics-service', title: 'Avg. Service Processing Time', unit: 'Milliseconds/call', groupBy0: 'Service Method', category1: 'regionTransactionStats', groupBy1: 'Region', chartType: 'column' },
           //{ category0: 'transactionStats', name: 'totalDurationMsDelta', defaultCategoryIdx: 0, view: 'metrics-service', title: 'Service Processing Time', unit: 'Milliseconds', groupBy0: 'Service Method', category1: 'regionTransactionStats', groupBy1: 'Region' },
           
           //{ category0: 'transactionStats', name: 'successCount', aggregate: true, view: 'metrics-service', title: 'Total Successful Calls', unit: 'Calls' },
           //{ category0: 'transactionStats', name: 'failureCount', aggregate: true, view: 'metrics-service', title: 'Total Failed Calls', unit: 'Calls' },
           
           // Storefront metrics
           { category0: 'storefrontStats', name: 'cartItemCount', defaultCategoryIdx: 0, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items', groupBy0: 'Region' },
           { category0: 'storefrontStats', name: 'activeCustomerCount', defaultCategoryIdx: 0, view: 'metrics-storefront', title: 'Active Customer Sessions (20 min expiry)', unit: 'Sessions', groupBy0: 'Region' },
           { category0: 'storefrontStats', name: 'purchaseItemCountDelta', defaultCategoryIdx: 0, view: 'metrics-storefront', title: 'Items Purchased per Second', unit: 'Items/second', groupBy0: 'Region' },
           { category0: 'storefrontStats', name: 'productReviewCountDelta', defaultCategoryIdx: 0, view: 'metrics-storefront', title: 'Reviews Written per Second', unit: 'Reviews/second', groupBy0: 'Region' },
           //{ category0: 'storefrontStats', name: 'cartItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items' },
           //{ category0: 'storefrontStats', name: 'purchaseItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items Purchased', unit: 'Items' },
           
           // Simulator metrics
           { category0: 'workloadStats', name: 'activeWorkerCount', defaultCategoryIdx: 0, view: 'metrics-simulator', title: 'Active Simulated Users', unit: 'Users', groupBy0: 'Workload', category1: 'regionWorkloadStats', groupBy1: 'Region' },
           { category0: 'workloadStepStats', name: 'completionCountDelta', defaultCategoryIdx: 0, view: 'metrics-simulator', title: 'Simulated Steps per Second', unit: 'Steps/second', groupBy0: 'Workload Step', category1: 'regionWorkloadStepStats', groupBy1: 'Region'  }
           //{ category0: 'workloadStepStats', name: 'completionCount', aggregate: false, view: 'metrics-simulator', title: 'Total Steps Completed', unit: 'Steps' },
           //{ category0: 'workloadStats', name: 'avgWorkTimeMs', aggregate: false, view: 'metrics-simulator', title: 'Average Seconds per Step', unit: 'Seconds/step' }
    ]
});

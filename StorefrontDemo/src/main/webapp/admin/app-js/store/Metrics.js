/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.store.Metrics', {
    extend: 'Ext.data.Store',
    model: 'App.model.Metric',
    proxy: 'memory',
    
    data: [
           // DB metrics
           //{ category: 'dbStats', name: 'tps', aggregate: true, view: 'metrics-db', title: 'Transactions per Second', unit: 'Transactions/second' },
           //{ category: 'dbStats', name: 'cpu', aggregate: true, view: 'metrics-db', title: 'CPU Utilization', unit: 'Utilization %' },
           //{ category: 'dbStats', name: 'clientConnectionCount', aggregate: true, view: 'metrics-db', title: 'Client Connections', unit: 'Connections' },
           //{ category: 'dbStats', name: 'commitCount', aggregate: true, view: 'metrics-db', title: 'Commits per Second', unit: 'Commits/second' },
           
           // Service metrics
           { category: 'transactionStats', name: 'totalCountDelta', aggregateIdx: 0, view: 'metrics-service', title: 'Service Calls per Second', unit: 'Calls/second', groupBy: 'Service Method', groupBy2: 'Region' },
           { category: 'transactionStats', name: 'totalDurationMsDelta', aggregateIdx: 0, view: 'metrics-service', title: 'Service Processing Time', unit: 'Milliseconds', groupBy: 'Service Method', groupBy2: 'Region' },
           //{ category: 'transactionStats', name: 'successCount', aggregate: true, view: 'metrics-service', title: 'Total Successful Calls', unit: 'Calls' },
           //{ category: 'transactionStats', name: 'failureCount', aggregate: true, view: 'metrics-service', title: 'Total Failed Calls', unit: 'Calls' },
           
           // Storefront metrics
           { category: 'storefrontStats', name: 'cartItemCount', aggregateIdx: 1, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items', groupBy: 'Region' },
           { category: 'storefrontStats', name: 'activeCustomerCount', aggregateIdx: 1, view: 'metrics-storefront', title: 'Active Customer Sessions (20 min expiry)', unit: 'Sessions', groupBy: 'Region' },
           { category: 'storefrontStats', name: 'purchaseItemCountDelta', aggregateIdx: 1, view: 'metrics-storefront', title: 'Items Purchased per Second', unit: 'Items/second', groupBy: 'Region' },
           { category: 'storefrontStats', name: 'productReviewCountDelta', aggregateIdx: 1, view: 'metrics-storefront', title: 'Reviews Written per Second', unit: 'Reviews/second', groupBy: 'Region' },
           //{ category: 'storefrontStats', name: 'cartItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items' },
           //{ category: 'storefrontStats', name: 'purchaseItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items Purchased', unit: 'Items' },
           
           // Simulator metrics
           { category: 'workloadStats', name: 'activeWorkerCount', aggregateIdx: 0, view: 'metrics-simulator', title: 'Active Simulated Users', unit: 'Users', groupBy: 'Workload', groupBy2: 'Region' },
           { category: 'workloadStepStats', name: 'completionCountDelta', aggregateIdx: 0, view: 'metrics-simulator', title: 'Simulated Steps per Second', unit: 'Steps/second', groupBy: 'Workload Step', groupBy2: 'Region'  }
           //{ category: 'workloadStepStats', name: 'completionCount', aggregate: false, view: 'metrics-simulator', title: 'Total Steps Completed', unit: 'Steps' },
           //{ category: 'workloadStats', name: 'avgWorkTimeMs', aggregate: false, view: 'metrics-simulator', title: 'Average Seconds per Step', unit: 'Seconds/step' }
    ]
});

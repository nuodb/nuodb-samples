/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.store.Metrics', {
    extend: 'Ext.data.Store',
    model: 'App.model.Metric',
    proxy: 'memory',
    
    data: [
           // DB metrics
           { category: 'dbStats', name: 'tps', aggregate: true, view: 'metrics-db', title: 'Transactions per Second', unit: 'Transactions/second' },
           { category: 'dbStats', name: 'cpu', aggregate: true, view: 'metrics-db', title: 'CPU Utilization', unit: 'Utilization %' },
           { category: 'dbStats', name: 'clientConnectionCount', aggregate: true, view: 'metrics-db', title: 'Client Connections', unit: 'Connections' },
           { category: 'dbStats', name: 'commitCount', aggregate: true, view: 'metrics-db', title: 'Commits per Second', unit: 'Commits/second' },
           
           // Service metrics
           { category: 'transactionStats', name: 'totalCountDelta', aggregate: true, view: 'metrics-service', title: 'Service Calls per Second', unit: 'Calls/second' },
           { category: 'transactionStats', name: 'totalDurationMsDelta', aggregate: true, view: 'metrics-service', title: 'Service Processing Time', unit: 'Milliseconds' },
           //{ category: 'transactionStats', name: 'successCount', aggregate: true, view: 'metrics-service', title: 'Total Successful Calls', unit: 'Calls' },
           //{ category: 'transactionStats', name: 'failureCount', aggregate: true, view: 'metrics-service', title: 'Total Failed Calls', unit: 'Calls' },
           
           // Storefront metrics
           { category: 'storefrontStats', name: 'cartItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items' },
           { category: 'storefrontStats', name: 'activeCustomerCount', aggregate: true, view: 'metrics-storefront', title: 'Active Customer Sessions (20 min expiry)', unit: 'Sessions' },
           { category: 'storefrontStats', name: 'purchaseItemCountDelta', aggregate: true, view: 'metrics-storefront', title: 'Items Purchased per Second', unit: 'Items/second' },
           { category: 'storefrontStats', name: 'productReviewCountDelta', aggregate: true, view: 'metrics-storefront', title: 'Reviews Written per Second', unit: 'Reviews/second' },
           //{ category: 'storefrontStats', name: 'cartItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items in Shopping Carts', unit: 'Items' },
           //{ category: 'storefrontStats', name: 'purchaseItemCount', aggregate: true, view: 'metrics-storefront', title: 'Total Items Purchased', unit: 'Items' },
           
           // Simulator metrics
           { category: 'workloadStats', name: 'activeWorkerCount', aggregate: false, view: 'metrics-simulator', title: 'Active Simulated Users', unit: 'Users' },
           { category: 'workloadStepStats', name: 'completionCountDelta', aggregate: false, view: 'metrics-simulator', title: 'Simulated Steps per Second', unit: 'Steps/second' }
           //{ category: 'workloadStepStats', name: 'completionCount', aggregate: false, view: 'metrics-simulator', title: 'Total Steps Completed', unit: 'Steps' },
           //{ category: 'workloadStats', name: 'avgWorkTimeMs', aggregate: false, view: 'metrics-simulator', title: 'Average Seconds per Step', unit: 'Seconds/step' }
    ]
});

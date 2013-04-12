/* Copyright (c) 2013 NuoDB, Inc. */

Ext.define('App.model.Workload', {
    extend: 'Ext.data.Model',
    fields: ['name', 'avgThinkTimeMs', 'thinkTimeVariance', 'autoRepeat', 'steps', 'activeWorkerLimit', 'activeWorkerCount', 'failedWorkerCount',
            'killedWorkerCount', 'completedWorkerCount', 'workInvocationCount', 'workCompletionCount', 'totalWorkTimeMs',
            'totalWorkCompletionTimeMs', 'totalWorkerCount', 'avgWorkTimeMs', 'avgWorkCompletionTimeMs']
});

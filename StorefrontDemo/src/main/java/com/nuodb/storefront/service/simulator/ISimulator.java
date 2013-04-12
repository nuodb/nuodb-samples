/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.simulator;

import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.service.IStorefrontService;

/**
 * Provides basic access to the simulator, which schedules workers to do bursts of work.
 */
public interface ISimulator {
    /**
     * Adds a worker to the simulator.
     * 
     * @param worker
     *            The worker to run
     * @param startDelayMs
     *            The minimum amount of time to delay before calling the worker's {@link #doWork} method.
     * @return True if the worker was added, false if the workload associated with this worker has reached a limit and no additional workers are
     *         currently permitted.
     */
    public boolean addWorker(IWorker worker, long startDelayMs);

    /**
     * Gets the Storefront service associated with this simulator. This is for convenience so workers need not manage their own service instances.
     */
    public IStorefrontService getService();
    
    public void incrementStepCompletionCount(WorkloadStep step);    
}

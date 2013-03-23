package com.nuodb.storefront.service.simulator;

import com.nuodb.storefront.model.WorkloadType;

/**
 * Describes a worker that can be run by a simulator.
 * 
 * A worker typically performs work in a series of steps or busts. Each step is performed within the {@link #doWork()} method. The simulator will call
 * this method repeatedly, with a worker-specified delay between each step, until the work has been completed or is forced to stop.
 * 
 * A worker need not be thread-safe.  While successive calls to {@link #doWork()} may occur on different threads, no two calls will overlap.
 */
public interface IWorker {
    /**
     * Magic constant used with the {@link #doWork()} method to indicate this worker has no additional work to perform.
     */
    public static final long DONE = -1;

    /**
     * Indicates the type of workload this worker is executing. The type may be used by the simulator to manage workers as a group, such as reporting
     * statistics on workloads, as well as pausing, and canceling workloads.
     */
    public WorkloadType getWorkloadType();

    /**
     * Performs the next action.
     * 
     * @return The number of milliseconds that must pass until the next step can be taken, or {@link #DONE} to indicate the actor has no more steps.
     *         Note that this is the <b>minimum</b> time that must transpire before the method is called again, not a guarantee.
     */
    public long doWork();
}

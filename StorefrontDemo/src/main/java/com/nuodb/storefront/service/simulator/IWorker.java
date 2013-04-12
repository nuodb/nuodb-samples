/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.simulator;

import com.nuodb.storefront.model.Workload;

/**
 * Describes a worker that can be run by a simulator.
 * 
 * A worker typically performs work in a series of steps or busts. Each step is performed within the {@link #doWork()} method. The simulator will call
 * this method repeatedly, with a worker-specified delay between each step, until the work has been completed or is forced to stop.
 * 
 * A worker need not be thread-safe. While successive calls to {@link #doWork()} may occur on different threads, no two calls will overlap.
 */
public interface IWorker {
    /**
     * Magic constant used with the {@link #doWork()} method to indicate this worker has no additional work to perform. If the work may auto-repeat,
     * the simulator may re-engage the worker to start the work again by calling {@link #doWork()}.
     */
    public static final long COMPLETE = -1;

    /**
     * Magic constant used with the {@link #doWork()} method to indicate this worker has no additional work to perform, and no work can be repeated.
     * Subsequent calls to {@link #doWork()} should not be made.
     */
    public static final long COMPLETE_NO_REPEAT = -2;

    /**
     * Indicates the type of workload this worker is executing. The type may be used by the simulator to manage workers as a group, such as reporting
     * statistics on workloads, as well as pausing, and canceling workloads. For a given worker, this value should remain fixed and not vary across
     * calls.
     */
    public Workload getWorkload();

    /**
     * Performs the next action.
     * 
     * @return The number of milliseconds that must pass until the next step can be taken, or {@link #COMPLETE} to indicate the actor has no more
     *         steps. Note that this is the <b>minimum</b> time that must transpire before the method is called again, not a guarantee. Note that any
     *         negative number is interpreted as {@link #COMPLETE}.
     */
    public long doWork();
}

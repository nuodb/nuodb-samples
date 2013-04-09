package com.nuodb.storefront.service;

import java.util.Map;

import com.nuodb.storefront.model.Workload;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadStepStats;

public interface ISimulatorService {
    /** Looks up a workload by name */
    public Workload getWorkload(String name);

    /**
     * Adds new workers for a give workload, staggering the arrival of each with an optional entry delay. Note that if there is a limit set on the
     * number of active workers for the workload, the workers may not be added. To adjust the limit, call
     * {@link #adjustWorkers(Workload, int, Integer)} first. By default, a workload has no limit.
     * 
     * @param workload
     *            The workload to which workers should be added.
     * @param numWorkers
     *            The number of workers to add.
     * @param entryDelayMs
     *            The delay period between the arrival of each worker. There is no delay before the arrival of the first worker.
     */
    public void addWorkers(Workload workload, int numWorkers, long entryDelayMs);

    /**
     * Adjusts the workers associated with a workload.
     * 
     * @param workload
     *            The workload.
     * @param minActiveWorkers
     *            The minimum number of active workers that should exist before this method returns. Must be non-negative. If the current number of
     *            active workers is below this number, additional workers are added (with no entry delay) to bring the active worker count up to this
     *            number.
     * @param activeWorkerLimit
     *            The maximum number of active workers that can exist concurrently. If additional workers of this workload type are added via
     *            {@link #addWorkers(Workload, int, int)} or some other means , they are immediately killed.
     */
    public WorkloadStats adjustWorkers(Workload workload, int minActiveWorkers, Integer activeWorkerLimit);

    /**
     * Removes all workers across all workloads, and sets the active worker limit to 0 across all workloads.
     */
    public void removeAll();

    public Map<String, WorkloadStats> getWorkloadStats();

    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats();

    /**
     * Synchronously stops all simulator threads, running work, and queued work. Once this method is called, you cannot add additional workloads.
     */
    public void shutdown();
}

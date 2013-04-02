package com.nuodb.storefront.service.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.nuodb.storefront.model.Workload;
import com.nuodb.storefront.model.WorkloadFlow;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadStepStats;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

public class SimulatorService implements ISimulator, ISimulatorService {
    private final ScheduledThreadPoolExecutor threadPool;
    private final IStorefrontService svc;
    private final Map<Workload, WorkloadStats> workloadStatsMap = new HashMap<Workload, WorkloadStats>();
    private final Object workloadStatsLock = new Object();
    private final Map<WorkloadStep, AtomicInteger> stepCompletionCounts = new TreeMap<WorkloadStep, AtomicInteger>();

    public SimulatorService(IStorefrontService svc) {
        this.threadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 10);
        this.svc = svc;

        try {
            for (WorkloadStep step : WorkloadStep.values()) {
                if (step.getClass().getField(step.name()).getAnnotation(WorkloadFlow.class) == null) {
                    stepCompletionCounts.put(step, new AtomicInteger(0));
                }
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addWorkload(Workload workload, int numWorkers, int entryDelayMs) {
        addWorker(new SimulatedUserFactory(this, workload, numWorkers, entryDelayMs), 0);

    }

    @Override
    public void downsizeWorkload(Workload workload, int newWorkerLimit) {
        if (newWorkerLimit < 0) {
            throw new IllegalArgumentException("newWorkerLimit");
        }

        synchronized (workloadStatsLock) {
            WorkloadStats stats = getOrCreateWorkloadStats(workload);
            stats.setLimit(newWorkerLimit);
        }
    }

    @Override
    public void removeAll() {
        threadPool.getQueue().clear();

        synchronized (workloadStatsLock) {
            for (WorkloadStats stats : workloadStatsMap.values()) {
                stats.setLimit(0);
            }
        }
    }

    @Override
    public Map<Workload, WorkloadStats> getWorkloadStats() {
        Map<Workload, WorkloadStats> mapCopy = new TreeMap<Workload, WorkloadStats>();
        synchronized (workloadStatsLock) {
            // Do a deep copy
            for (Map.Entry<Workload, WorkloadStats> entry : workloadStatsMap.entrySet()) {
                mapCopy.put(entry.getKey(), new WorkloadStats(entry.getValue()));
            }
        }
        return mapCopy;
    }

    @Override
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats() {
        Map<WorkloadStep, WorkloadStepStats> map = new TreeMap<WorkloadStep, WorkloadStepStats>();
        for (Map.Entry<WorkloadStep, AtomicInteger> stepEntry : stepCompletionCounts.entrySet()) {
            WorkloadStepStats stepStats = new WorkloadStepStats();
            stepStats.setCompletionCount(stepEntry.getValue().get());
            map.put(stepEntry.getKey(), stepStats);
        }
        return map;
    }

    @Override
    public void shutdown() {
        threadPool.shutdownNow();
    }

    @Override
    public boolean addWorker(final IWorker worker, int startDelayMs) {
        synchronized (workloadStatsLock) {
            WorkloadStats info = getOrCreateWorkloadStats(worker.getWorkload());
            if (!info.canAddWorker()) {
                info.setKilledWorkerCount(info.getKilledWorkerCount() + 1);
                return false;
            }
            info.setActiveWorkerCount(info.getActiveWorkerCount() + 1);
            addWorker(new RunnableWorker(worker), startDelayMs);
            return true;
        }
    }

    @Override
    public IStorefrontService getService() {
        return svc;
    }

    @Override
    public void incrementStepCompletionCount(WorkloadStep step) {
        stepCompletionCounts.get(step).incrementAndGet();
    }

    protected void addWorker(RunnableWorker worker, long startDelayMs) {
        threadPool.schedule(worker, startDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * You must have a lock on workloadStatsLock to call this method.
     */
    protected WorkloadStats getOrCreateWorkloadStats(Workload workloadType) {
        WorkloadStats stats = workloadStatsMap.get(workloadType);
        if (stats == null) {
            stats = new WorkloadStats();
            workloadStatsMap.put(workloadType, stats);
        }
        return stats;
    }

    protected class RunnableWorker implements Runnable {
        private final IWorker worker;
        private long completionWorkTimeMs;

        public RunnableWorker(IWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            Workload workload = worker.getWorkload();

            // Verify this worker can still run
            synchronized (workloadStatsLock) {
                WorkloadStats stats = getOrCreateWorkloadStats(workload);
                if (stats.exceedsWorkerLimit()) {
                    // Don't run this worker. We're over the limit
                    stats.setActiveWorkerCount(stats.getActiveWorkerCount() - 1);
                    stats.setKilledWorkerCount(stats.getKilledWorkerCount() + 1);
                    return;
                }
            }

            // Run the worker
            long startTimeMs = System.currentTimeMillis();
            long delay;
            boolean workerFailed = false;
            try {
                delay = worker.doWork();
            } catch (Exception e) {
                delay = IWorker.COMPLETE_NO_REPEAT;
                workerFailed = true;
            }
            long endTimeMs = System.currentTimeMillis();
            completionWorkTimeMs += (endTimeMs - startTimeMs);

            // Update stats
            synchronized (workloadStatsLock) {
                WorkloadStats stats = getOrCreateWorkloadStats(workload);
                stats.setWorkInvocationCount(stats.getWorkInvocationCount() + 1);
                stats.setTotalWorkTimeMs(stats.getTotalWorkTimeMs() + endTimeMs - startTimeMs);
                if (delay < 0) {
                    if (!workerFailed) {
                        stats.setWorkCompletionCount(stats.getWorkCompletionCount() + 1);
                        stats.setTotalWorkCompletionTimeMs(completionWorkTimeMs);
                        completionWorkTimeMs = 0;
                    }
                    
                    // Determine whether this worker should run again
                    if (delay != IWorker.COMPLETE_NO_REPEAT && workload.isAutoRepeating()) {
                        delay = workload.calcNextThinkTimeMs();
                    }
                    if (delay < 0) {
                        stats.setActiveWorkerCount(stats.getActiveWorkerCount() - 1);
                        if (!workerFailed) {
                            stats.setCompletedWorkerCount(stats.getCompletedWorkerCount() + 1);
                        }
                    }
                }
                if (workerFailed) {
                    stats.setFailedWorkerCount(stats.getFailedWorkerCount() + 1);
                }
            }

            // Queue up next run
            if (delay >= 0) {
                addWorker(this, delay);
            }
        }
    }
}

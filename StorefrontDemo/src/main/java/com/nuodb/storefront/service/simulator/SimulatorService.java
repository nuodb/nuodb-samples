package com.nuodb.storefront.service.simulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.nuodb.storefront.model.WorkloadFlow;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadType;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

public class SimulatorService implements ISimulator, ISimulatorService {
    private final ScheduledThreadPoolExecutor threadPool;
    private final IStorefrontService svc;
    private final Map<WorkloadType, WorkloadStatsEx> workloadStatsMap = new HashMap<WorkloadType, WorkloadStatsEx>();
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
    public void addWorkload(WorkloadType workload, int numWorkers, int entryDelayMs) {
        addWorker(new SimulatedUserFactory(this, workload, numWorkers, entryDelayMs), 0);

    }

    @Override
    public void downsizeWorkload(WorkloadType workload, int newWorkerLimit) {
        if (newWorkerLimit < 0) {
            throw new IllegalArgumentException("newWorkerLimit");
        }

        synchronized (workloadStatsLock) {
            WorkloadStatsEx stats = getOrCreateWorkloadStats(workload);
            stats.setLimit(newWorkerLimit);
        }
    }

    @Override
    public void removeAll() {
        threadPool.getQueue().clear();

        synchronized (workloadStatsLock) {
            for (WorkloadStatsEx stats : workloadStatsMap.values()) {
                stats.setLimit(0);
            }
        }
    }

    @Override
    public Collection<WorkloadStats> getWorkloadStats() {
        Collection<WorkloadStats> statsList = new TreeSet<WorkloadStats>();
        synchronized (workloadStatsLock) {
            for (WorkloadStatsEx stats : workloadStatsMap.values()) {
                statsList.add(new WorkloadStats(stats));
            }
        }
        return statsList;
    }

    @Override
    public Map<WorkloadStep, Integer> getWorkloadStepCompletionCounts() {
        Map<WorkloadStep, Integer> map = new TreeMap<WorkloadStep, Integer>();
        for (Map.Entry<WorkloadStep, AtomicInteger> stepStats : stepCompletionCounts.entrySet()) {
            map.put(stepStats.getKey(), stepStats.getValue().get());
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
            WorkloadStatsEx info = getOrCreateWorkloadStats(worker.getWorkloadType());
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
    protected WorkloadStatsEx getOrCreateWorkloadStats(WorkloadType workloadType) {
        WorkloadStatsEx stats = workloadStatsMap.get(workloadType);
        if (stats == null) {
            stats = new WorkloadStatsEx();
            stats.setWorkloadType(workloadType);
            workloadStatsMap.put(workloadType, stats);
        }
        return stats;
    }

    protected class WorkloadStatsEx extends WorkloadStats {
        public static final int NO_LIMIT = -1;
        private int limit = NO_LIMIT;

        public WorkloadStatsEx() {
        }

        public boolean canAddWorker() {
            return limit == NO_LIMIT || getActiveWorkerCount() < limit;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public boolean exceedsWorkerLimit() {
            return limit != NO_LIMIT && getActiveWorkerCount() > limit;
        }
    }

    protected class RunnableWorker implements Runnable {
        private final IWorker worker;
        private long completionWorkTimeMs;

        public RunnableWorker(IWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            WorkloadType workload = worker.getWorkloadType();

            // Verify this worker can still run
            synchronized (workloadStatsLock) {
                WorkloadStatsEx stats = getOrCreateWorkloadStats(workload);
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
                WorkloadStatsEx stats = getOrCreateWorkloadStats(workload);
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

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
    private final Map<WorkloadStep, AtomicInteger> stepCounts = new TreeMap<WorkloadStep, AtomicInteger>();

    public SimulatorService(IStorefrontService svc) {
        this.threadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 10);
        this.svc = svc;

        try {
            for (WorkloadStep step : WorkloadStep.values()) {
                if (step.getClass().getField(step.name()).getAnnotation(WorkloadFlow.class) == null) {
                    stepCounts.put(step, new AtomicInteger(0));
                }
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addWorkload(WorkloadType workload, int numUsers, int entryDelayMs) {
        addWorker(new SimulatedUserFactory(this, workload, numUsers, entryDelayMs), 0);

    }

    @Override
    public void downsizeWorkload(WorkloadType workload, int newLimit) {
        if (newLimit < 0) {
            throw new IllegalArgumentException("newLimit");
        }

        synchronized (workloadStatsLock) {
            WorkloadStatsEx stats = getOrCreateWorkloadStats(workload);
            stats.setLimit(newLimit);
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
    public Map<WorkloadStep, Integer> getExecutionCounts() {
        Map<WorkloadStep, Integer> map = new TreeMap<WorkloadStep, Integer>();
        for (Map.Entry<WorkloadStep, AtomicInteger> stepStats : stepCounts.entrySet()) {
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
                return false;
            }
            info.setActiveUserCount(info.getActiveUserCount() + 1);
            addWorker(new RunnableWorker(worker), startDelayMs);
            return true;
        }
    }

    @Override
    public IStorefrontService getService() {
        return svc;
    }

    @Override
    public void incrementStepCount(WorkloadStep step) {
        stepCounts.get(step).incrementAndGet();
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
            return limit == NO_LIMIT || getActiveUserCount() < limit;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public boolean exceedsWorkerLimit() {
            return limit != NO_LIMIT && getActiveUserCount() > limit;
        }
    }

    protected class RunnableWorker implements Runnable {
        private final IWorker worker;

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
                    stats.setActiveUserCount(stats.getActiveUserCount() - 1);
                    return;
                }
            }

            // Run the worker
            long startTimeMs = System.currentTimeMillis();
            long delay;
            try {
                delay = worker.doWork();
            } catch (Exception e) {
                delay = IWorker.DONE;
            }
            long endTimeMs = System.currentTimeMillis();

            // Update stats
            synchronized (workloadStatsLock) {
                WorkloadStatsEx stats = getOrCreateWorkloadStats(workload);
                stats.setTotalActionCount(stats.getTotalActionCount() + 1);
                stats.setTotalActionTimeMs(stats.getTotalActionTimeMs() + endTimeMs - startTimeMs);
                if (delay < 0) {
                    stats.setActiveUserCount(stats.getActiveUserCount() - 1);
                }
            }

            // Queue up next run
            if (delay >= 0) {
                addWorker(this, delay);
            }
        }
    }
}

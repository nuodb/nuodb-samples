package com.nuodb.storefront.model;

import org.hibernate.util.EqualsHelper;

import com.nuodb.storefront.service.simulator.IWorker;

/**
 * Provides statistics about a {@link WorkloadType} executing within a simulator.
 * 
 */
public class WorkloadStats implements Comparable<WorkloadStats> {
    private WorkloadType workloadType;
    private int activeWorkerCount;
    private int failedWorkerCount;
    private int killedWorkerCount;
    private int completedWorkerCount;
    private int workInvocationCount;
    private int workCompletionCount;
    private long totalWorkTimeMs;
    private long totalWorkCompletionTimeMs;

    public WorkloadStats() {

    }

    public WorkloadStats(WorkloadStats stats) {
        this.workloadType = stats.workloadType;
        this.activeWorkerCount = stats.activeWorkerCount;
        this.failedWorkerCount = stats.failedWorkerCount;
        this.killedWorkerCount = stats.killedWorkerCount;
        this.completedWorkerCount = stats.completedWorkerCount;
        this.workInvocationCount = stats.workInvocationCount;
        this.workCompletionCount = stats.workCompletionCount;
        this.totalWorkTimeMs = stats.totalWorkTimeMs;
        this.totalWorkCompletionTimeMs = stats.totalWorkCompletionTimeMs;
    }

    /**
     * Indicates the workload associated with these statistics.
     */
    public WorkloadType getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(WorkloadType workloadType) {
        this.workloadType = workloadType;
    }

    /**
     * Gets the number of workers the simulator is currently scheduling.
     */
    public int getActiveWorkerCount() {
        return activeWorkerCount;
    }

    public void setActiveWorkerCount(int activeWorkerCount) {
        this.activeWorkerCount = activeWorkerCount;
    }

    /**
     * Gets the number of workers that threw exceptions and, consequently, are no longer being scheduled.
     */
    public int getFailedWorkerCount() {
        return failedWorkerCount;
    }

    public void setFailedWorkerCount(int failedWorkerCount) {
        this.failedWorkerCount = failedWorkerCount;
    }

    /**
     * Gets the number of workers that were killed before their work was complete due to workload downsizing or simulator shutdown.
     */
    public int getKilledWorkerCount() {
        return killedWorkerCount;
    }

    public void setKilledWorkerCount(int killedWorkerCount) {
        this.killedWorkerCount = killedWorkerCount;
    }

    public int getCompletedWorkerCount() {
        return completedWorkerCount;
    }

    public void setCompletedWorkerCount(int completedWorkerCount) {
        this.completedWorkerCount = completedWorkerCount;
    }

    /**
     * Gets the total number of workers across all states: active, completed, failed, and killed.
     */
    public int getTotalWorkerCount() {
        return activeWorkerCount + completedWorkerCount + failedWorkerCount + killedWorkerCount;
    }

    /**
     * Gets the number of times the {@link IWorker#doWork()} method was called across all workers.
     */
    public int getWorkInvocationCount() {
        return workInvocationCount;
    }

    public void setWorkInvocationCount(int workInvocationCount) {
        this.workInvocationCount = workInvocationCount;
    }

    /**
     * Gets the total number of times workers completed their work. If the workload type is set to auto-repeat, each worker may complete the work
     * multiple times. Note that a worker may require multiple calls to {@link IWorker#doWork()} before the worker considers the work complete.
     */
    public int getWorkCompletionCount() {
        return workCompletionCount;
    }

    public void setWorkCompletionCount(int workCompletionCount) {
        this.workCompletionCount = workCompletionCount;
    }

    /**
     * Gets the total number of milliseconds spent within {@link IWorker#doWork()} calls across all workers.
     */
    public long getTotalWorkTimeMs() {
        return totalWorkTimeMs;
    }

    public void setTotalWorkTimeMs(long totalWorkTimeMs) {
        this.totalWorkTimeMs = totalWorkTimeMs;
    }

    public long getTotalWorkCompletionTimeMs() {
        return totalWorkCompletionTimeMs;
    }

    public void setTotalWorkCompletionTimeMs(long totalWorkCompletionTimeMs) {
        this.totalWorkCompletionTimeMs = totalWorkCompletionTimeMs;
    }

    public float getAvgWorkTimeMs() {
        return totalWorkTimeMs / (float) workInvocationCount;
    }

    public float getAvgWorkCompletionTimeMs() {
        return totalWorkCompletionTimeMs / (float) workCompletionCount;
    }

    @Override
    public int compareTo(WorkloadStats o) {
        if (workloadType == null) {
            return (o.workloadType == null) ? 0 : -1;
        } else if (o.workloadType == null) {
            return 1;
        }
        return workloadType.compareTo(o.workloadType);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof WorkloadStats) && EqualsHelper.equals(((WorkloadStats) obj).workloadType, workloadType);
    }

    @Override
    public int hashCode() {
        return (workloadType == null) ? 0 : workloadType.hashCode();
    }
}

/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.nuodb.storefront.service.simulator.IWorker;
import com.nuodb.storefront.util.LoggerToStringStyle;

/**
 * Provides statistics about a {@link Workload} executing within a simulator.
 */
public class WorkloadStats {
    private Workload workload;
    private Integer activeWorkerLimit;
    private int activeWorkerCount;
    private int failedWorkerCount;
    private int killedWorkerCount;
    private int completedWorkerCount;
    private int workInvocationCount;
    private int workCompletionCount;
    private long totalWorkTimeMs;
    private long totalWorkCompletionTimeMs;
    
    public WorkloadStats(Workload workload) {
        this.workload = workload;
    }

    public WorkloadStats(WorkloadStats stats) {
        this.workload = stats.workload;
        this.activeWorkerLimit = stats.activeWorkerLimit;
        this.activeWorkerCount = stats.activeWorkerCount;
        this.failedWorkerCount = stats.failedWorkerCount;
        this.killedWorkerCount = stats.killedWorkerCount;
        this.completedWorkerCount = stats.completedWorkerCount;
        this.workInvocationCount = stats.workInvocationCount;
        this.workCompletionCount = stats.workCompletionCount;
        this.totalWorkTimeMs = stats.totalWorkTimeMs;
        this.totalWorkCompletionTimeMs = stats.totalWorkCompletionTimeMs;
    }

    public Workload getWorkload() {
        return workload;
    }

    public void setWorkload(Workload workload) {
        this.workload = workload;
    }

    public boolean canAddWorker() {
        return activeWorkerLimit == null || getActiveWorkerCount() < activeWorkerLimit;
    }

    public Integer getActiveWorkerLimit() {
        return activeWorkerLimit;
    }

    public void setActiveWorkerLimit(Integer limit) {
        this.activeWorkerLimit = limit;
    }

    public boolean exceedsWorkerLimit() {
        return (activeWorkerLimit != null && activeWorkerCount > activeWorkerLimit)
                || (workload.getMaxWorkers() > 0 && activeWorkerCount > workload.getMaxWorkers());
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

    public Float getAvgWorkTimeMs() {
        return (workInvocationCount == 0) ? null : totalWorkTimeMs / (float) workInvocationCount;
    }

    public Float getAvgWorkCompletionTimeMs() {
        return (workCompletionCount == 0) ? null : totalWorkCompletionTimeMs / (float) workCompletionCount;
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, LoggerToStringStyle.INSTANCE).toString();
    }
}

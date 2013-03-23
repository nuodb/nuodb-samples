package com.nuodb.storefront.model;

import org.hibernate.util.EqualsHelper;

public class WorkloadStats implements Comparable<WorkloadStats> {
    private WorkloadType workloadType;
    private int activeUserCount;
    private int totalActionCount;
    private int totalUserCount;
    private long totalActionTimeMs;

    public WorkloadStats() {

    }

    public WorkloadStats(WorkloadStats stats) {
        this.workloadType = stats.workloadType;
        this.activeUserCount = stats.activeUserCount;
        this.totalActionCount = stats.totalActionCount;
        this.totalUserCount = stats.totalUserCount;
        this.totalActionTimeMs = stats.totalActionTimeMs;
    }

    public WorkloadType getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(WorkloadType workloadType) {
        this.workloadType = workloadType;
    }

    public int getActiveUserCount() {
        return activeUserCount;
    }

    public void setActiveUserCount(int activeUserCount) {
        this.activeUserCount = activeUserCount;
    }

    public int getTotalActionCount() {
        return totalActionCount;
    }

    public void setTotalActionCount(int totalActionCount) {
        this.totalActionCount = totalActionCount;
    }

    public int getTotalUserCount() {
        return totalUserCount;
    }

    public void setTotalUserCount(int totalUserCount) {
        this.totalUserCount = totalUserCount;
    }

    public long getTotalActionTimeMs() {
        return totalActionTimeMs;
    }

    public void setTotalActionTimeMs(long totalActionTimeMs) {
        this.totalActionTimeMs = totalActionTimeMs;
    }

    public float getAvgActionTimeMs() {
        return totalActionTimeMs / (float) totalActionCount;
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
        return (obj instanceof WorkloadStats) && EqualsHelper.equals(((WorkloadStats)obj).workloadType, workloadType);
    }
    
    @Override
    public int hashCode() {
        return (workloadType == null) ? 0 : workloadType.hashCode();
    }
}

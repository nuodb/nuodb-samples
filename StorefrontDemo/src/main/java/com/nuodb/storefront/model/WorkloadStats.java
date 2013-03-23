package com.nuodb.storefront.model;

public class WorkloadStats {
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
        return totalActionTimeMs / (float)totalActionCount;
    }
}

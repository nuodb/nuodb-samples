package com.nuodb.storefront.model;

public class TransactionStats {
    private int successCount;
    private int failureCount;
    private long minDurationMs;
    private long maxDurationMs;
    private long totalDurationMs;

    public TransactionStats() {
    }
    
    public TransactionStats(TransactionStats stats) {
        this.successCount = stats.successCount;
        this.failureCount = stats.failureCount;
        this.minDurationMs = stats.minDurationMs;
        this.maxDurationMs = stats.maxDurationMs;
        this.totalDurationMs = stats.totalDurationMs;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public long getMinDurationMs() {
        return minDurationMs;
    }

    public void setMinDurationMs(long minDurationMs) {
        this.minDurationMs = minDurationMs;
    }

    public long getMaxDurationMs() {
        return maxDurationMs;
    }

    public void setMaxDurationMs(long maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public int getTotalCount() {
        return successCount + failureCount;
    }

    public Float getAvgDurationMs() {
        int total = getTotalCount();
        return (total == 0) ? null : (float) totalDurationMs / getTotalCount();
    }

    public void incrementCount(String transactionName, long durationMs, boolean success) {
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
        totalDurationMs += durationMs;
        maxDurationMs = Math.max(maxDurationMs, durationMs);
        minDurationMs = (getTotalCount() == 1) ? durationMs : Math.min(minDurationMs, durationMs);
    }
}

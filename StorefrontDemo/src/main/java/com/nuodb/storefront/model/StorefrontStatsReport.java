package com.nuodb.storefront.model;

import java.util.Map;

public class StorefrontStatsReport {
    private StorefrontStats storefrontStats;
    private Map<String, TransactionStats> transactionStats;
    private Map<String, WorkloadStats> workloadStats;
    private Map<WorkloadStep, WorkloadStepStats> workloadStepStats;

    public StorefrontStatsReport() {
    }

    public StorefrontStats getStorefrontStats() {
        return storefrontStats;
    }

    public void setStorefrontStats(StorefrontStats storefrontStats) {
        this.storefrontStats = storefrontStats;
    }

    public Map<String, TransactionStats> getTransactionStats() {
        return transactionStats;
    }

    public void setTransactionStats(Map<String, TransactionStats> transactionStats) {
        this.transactionStats = transactionStats;
    }

    public Map<String, WorkloadStats> getWorkloadStats() {
        return workloadStats;
    }

    public void setWorkloadStats(Map<String, WorkloadStats> workloadStats) {
        this.workloadStats = workloadStats;
    }

    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats() {
        return workloadStepStats;
    }

    public void setWorkloadStepStats(Map<WorkloadStep, WorkloadStepStats> workloadStepStats) {
        this.workloadStepStats = workloadStepStats;
    }
}

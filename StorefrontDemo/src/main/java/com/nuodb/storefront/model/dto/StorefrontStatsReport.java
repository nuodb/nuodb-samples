/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import java.util.Calendar;
import java.util.Map;

import com.nuodb.storefront.model.entity.AppInstance;

public class StorefrontStatsReport {
    private Calendar timestamp;
    private AppInstance appInstance;
    private Map<String, TransactionStats> transactionStats;
    private Map<String, WorkloadStats> workloadStats;
    private Map<WorkloadStep, WorkloadStepStats> workloadStepStats;
    private DbFootprint dbStats;

    public StorefrontStatsReport() {
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public AppInstance getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(AppInstance appInstance) {
        this.appInstance = appInstance;
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

    public DbFootprint getDbStats() {
        return dbStats;
    }

    public void setDbStats(DbFootprint dbStats) {
        this.dbStats = dbStats;
    }
}

/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

public class WorkloadStepStats {
    private int completionCount;

    public WorkloadStepStats() {
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }
}

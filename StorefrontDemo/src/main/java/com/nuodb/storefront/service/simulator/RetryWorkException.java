/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.simulator;

public class RetryWorkException extends RuntimeException {
    private static final long serialVersionUID = 6916476003704733769L;
    
    public final long retryDelayMs;

    public RetryWorkException(long retryDelayMs)
    {
        this.retryDelayMs = retryDelayMs;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }
}

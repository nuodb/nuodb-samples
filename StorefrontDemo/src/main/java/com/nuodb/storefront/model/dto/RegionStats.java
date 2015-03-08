/* Copyright (c) 2013-2015 NuoDB, Inc. */
package com.nuodb.storefront.model.dto;

public class RegionStats {
    public String region;
    public int usedHostCount;
    public int hostCount;
    public int transactionManagerCount;
    public int storageManagerCount;
    
    public RegionStats() {        
    }
    
    public RegionStats(RegionStats stats) {
        this.region = stats.region;
        this.usedHostCount = stats.usedHostCount;
        this.hostCount = stats.hostCount;
        this.transactionManagerCount = stats.transactionManagerCount;
        this.storageManagerCount = stats.storageManagerCount;
    }
}

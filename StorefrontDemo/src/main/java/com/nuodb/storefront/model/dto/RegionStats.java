/* Copyright (c) 2013-2015 NuoDB, Inc. */
package com.nuodb.storefront.model.dto;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class RegionStats {
    public String region;
    public int usedHostCount;
    public int hostCount;
    public int transactionManagerCount;
    public int storageManagerCount;
    public Set<URI> usedHostUrls;

    public RegionStats() {
    }

    public RegionStats(RegionStats stats) {
        this.region = stats.region;
        this.usedHostCount = stats.usedHostCount;
        this.hostCount = stats.hostCount;
        this.transactionManagerCount = stats.transactionManagerCount;
        this.storageManagerCount = stats.storageManagerCount;
        this.usedHostUrls = (stats.usedHostUrls == null) ? null : new HashSet<URI>(stats.usedHostUrls);
    }
}

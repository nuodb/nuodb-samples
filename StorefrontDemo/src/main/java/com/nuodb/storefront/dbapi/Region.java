/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import com.nuodb.storefront.model.dto.RegionStats;

public class Region extends RegionStats {
    public Database[] databases;
    public Host[] hosts;
}
/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

public class Region {
    public String region;
    public Database[] databases;
    public Host[] hosts;
    
    public int usedHostCount;
    public int hostCount;
    public int transactionManagerCount;
    public int storageManagerCount;
}
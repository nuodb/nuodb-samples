/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.List;

import com.nuodb.storefront.model.dto.DbStats;

public interface IDbApi {
    public Database validateDbSetup();
    
    public Database fixDbSetup();
    
    public Database createDatabase();
    
    /**
     * Fetches information about all the database nodes running in support of the underlying database schema. This method returns an empty list unless
     * NuoDB is running.
     */
    public List<Process> getDbProcesses();

    public void shutdownProcess(String uid);
    
    public List<Region> getRegions();
    
    public DbStats getDbStats();
    
    public DbStats setDbFootprint(int numRegions, int numHosts);

    public String getBaseUrl();

    public String getAuthUser();
}

/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.List;

public interface IDbApi {
    public Database createDatabase(String dbName, String username, String password, String template);
    
    /**
     * Fetches information about all the database nodes running in support of the underlying database schema. This method returns an empty list unless
     * NuoDB is running.
     */
    public List<Process> getProcesses(String dbName);

    public void shutdownProcess(String uid);
    
    public List<Region> getRegions();
}

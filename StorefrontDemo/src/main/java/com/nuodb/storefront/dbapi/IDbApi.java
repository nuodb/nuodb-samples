/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.List;

import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.exception.DataValidationException;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbFootprint;
import com.nuodb.storefront.model.dto.RegionStats;

/**
 * Interface to interact with NuoDB's RESTful API. For Storefront database-specific API calls, uses the database name associated with the connection
 * string on the Storefront database.
 */
public interface IDbApi {
    /**
     * Gets the username and URL used by this implementation when connecting to the NuoDB RESTful API.
     * For security, the password is not reported (but rather set to null).
     */
    public ConnInfo getApiConnInfo();
    
    /**
     * Makes a read-only API request to test the connection.  Throws an exception if a valid response was not received. 
     */
    public void testConnection();

    /**
     * Fetches information about the Storefront DB.
     */
    public Database getDb() throws ApiProxyException;

    /**
     * Fetches information about all the database processes running in support of the Storefront DB. This method returns an empty list if the
     * Storefront DB not running.
     */
    public List<Process> getDbProcesses() throws ApiProxyException;

    /**
     * Shuts down a NuoDB process (TE or SM) with the given UID (which is a globally unique identifier, not PID). You can use this to shut down any
     * NuoDB process, not just ones running the Storefront DB.
     */
    public void shutdownProcess(String uid) throws ApiProxyException;

    /**
     * Fetches some basic stats on the Storefront DB's footprint and the NuoDB domain overall.
     */
    public DbFootprint getDbFootprint() throws ApiProxyException;

    /**
     * Updates the Storefront DB's footprint. If the Storefront DB is not running, this method still changes tags on hosts for the proper footprint,
     * but does not create the Storefront DB nor thrown an exception.
     */
    public DbFootprint setDbFootprint(int numRegions, int numHosts) throws ApiProxyException, DataValidationException;

    /**
     * If the database does not yet exist, creates it, unless createIfDne is false in which case a {@link DatabaseNotFoundException} is thrown.
     * 
     * If it does exist, properly sets the database template, database variables, and host tags as appropriate.
     * 
     * @return Database metadata
     */
    public Database fixDbSetup(boolean createIfDne) throws DatabaseNotFoundException, ApiProxyException;

    /**
     * Gets stats for all regions, including ones where the Storefront DB is not currently running.
     */
    public List<RegionStats> getRegionStats() throws ApiProxyException;
}

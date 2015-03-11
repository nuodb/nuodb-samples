/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.DbFootprint;
import com.nuodb.storefront.model.dto.RegionStats;
import com.nuodb.storefront.model.dto.StorefrontStats;
import com.nuodb.storefront.model.dto.StorefrontStatsReport;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.dto.WorkloadStep;
import com.nuodb.storefront.model.dto.WorkloadStepStats;

@Path("/stats")
public class StatsApi extends BaseApi {
    public StatsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStatsReport getAllStatsReport(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec) {
        StorefrontStatsReport rpt = getSimulator().getStorefrontStatsReport(sessionTimeoutSec);
        rpt.setDbStats(getDbApi().getDbFootprint());
        clearWorkloadProperty(rpt.getWorkloadStats());
        return rpt;
    }

    @GET
    @Path("/storefront")
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStats getStorefrontStats(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec, @QueryParam("maxAgeSec") Integer maxAgeSec) {
        int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? StorefrontApp.DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
        return getService().getStorefrontStats(maxCustomerIdleTimeSec, maxAgeSec);
    }

    @GET
    @Path("/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, TransactionStats> getTransactionStats() {
        return getService().getTransactionStats();
    }

    @GET
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, WorkloadStats> getWorkloadStats() {
        return clearWorkloadProperty(getSimulator().getWorkloadStats());
    }

    @GET
    @Path("/workload-steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats() {
        return getSimulator().getWorkloadStepStats();
    }

    @GET
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public DbFootprint getDbStats() {
        return getDbApi().getDbFootprint();
    }

    @PUT
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public DbFootprint setDbStats(@QueryParam("numRegions") Integer numRegions, @QueryParam("numHosts") Integer numHosts) {
        return getDbApi().setDbFootprint(numRegions.intValue(), numHosts.intValue());
    }
    
    @GET
    @Path("/regions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegionStats> getRegionStats() {
        return getDbApi().getRegionStats();
    }
    
    @GET
    @Path("/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DbConnInfo> getTenantStats() {
        List<DbConnInfo> dbs = new ArrayList<DbConnInfo>();
        dbs.add(StorefrontFactory.getDbConnInfo());
        return dbs;
    }

    protected Map<String, WorkloadStats> clearWorkloadProperty(Map<String, WorkloadStats> statsMap)
    {
        // Clear unnecessary workload property to reduce payload size by ~25%
        for (WorkloadStats stats : statsMap.values()) {
            stats.setWorkload(null);
        }
        return statsMap;
    }
}

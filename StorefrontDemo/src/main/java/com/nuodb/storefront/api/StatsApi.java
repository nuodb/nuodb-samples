/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.model.dto.DbStats;
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
    public StorefrontStatsReport getAllStatsReport(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec,
            @QueryParam("includeStorefront") Boolean includeStorefront) {

        StorefrontStatsReport rpt = getSimulator().getStorefrontStatsReport(sessionTimeoutSec);

        // Storefront stats are expensive to fetch (DB query required), so only fetch them if specifically requested
        if (includeStorefront != null && includeStorefront.booleanValue()) {
            if (includeStorefront) {
                int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? StorefrontApp.DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
                rpt.setStorefrontStats(getService().getStorefrontStatsByRegion(maxCustomerIdleTimeSec));
            }
        }

        rpt.setDbStats(getDbApi().getDbStats());

        clearWorkloadProperty(rpt.getWorkloadStats());

        return rpt;
    }

    @GET
    @Path("/storefront")
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStats getStorefrontStats(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec) {
        int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? StorefrontApp.DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
        return getService().getStorefrontStats(maxCustomerIdleTimeSec);
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
    public DbStats getDbStats() {
        return getDbApi().getDbStats();
    }

    @PUT
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public DbStats getDbStats(@QueryParam("numRegions") Integer numRegions, @QueryParam("numHosts") Integer numHosts) {
        return getDbApi().setDbFootprint(numRegions.intValue(), numHosts.intValue());
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

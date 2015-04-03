/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontApp;
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
    public StorefrontStatsReport getAllStatsReport(@Context HttpServletRequest req, @QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec) {
        StorefrontStatsReport rpt = getSimulator(req).getStorefrontStatsReport(sessionTimeoutSec);
        DbFootprint footprint = getDbApi(req).getDbFootprint();

        rpt.setDbStats(footprint);
        clearWorkloadProperty(rpt.getWorkloadStats());

        if (footprint.usedRegionCount > 1) {
            getTenant(req).getStorefrontPeerService().asyncWakeStorefrontsInOtherRegions();
        }

        return rpt;
    }

    @GET
    @Path("/storefront")
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStats getStorefrontStats(@Context HttpServletRequest req, @QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec, @QueryParam("maxAgeSec") Integer maxAgeSec) {
        int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? StorefrontApp.DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
        return getService(req).getStorefrontStats(maxCustomerIdleTimeSec, maxAgeSec);
    }

    @GET
    @Path("/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, TransactionStats> getTransactionStats(@Context HttpServletRequest req) {
        return getService(req).getTransactionStats();
    }

    @GET
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, WorkloadStats> getWorkloadStats(@Context HttpServletRequest req) {
        return clearWorkloadProperty(getSimulator(req).getWorkloadStats());
    }

    @GET
    @Path("/workload-steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats(@Context HttpServletRequest req) {
        return getSimulator(req).getWorkloadStepStats();
    }

    @GET
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public DbFootprint getDbStats(@Context HttpServletRequest req) {
        return getDbApi(req).getDbFootprint();
    }

    @PUT
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public DbFootprint setDbStats(@Context HttpServletRequest req, @QueryParam("numRegions") Integer numRegions, @QueryParam("numHosts") Integer numHosts) {
        DbFootprint footprint = getDbApi(req).setDbFootprint(numRegions.intValue(), numHosts.intValue());
        if (footprint.usedRegionCount > 1) {
            getTenant(req).getStorefrontPeerService().asyncWakeStorefrontsInOtherRegions();
        }
        return footprint;
    }

    @GET
    @Path("/regions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegionStats> getRegionStats(@Context HttpServletRequest req) {
        return getDbApi(req).getRegionStats();
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

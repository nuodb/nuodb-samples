/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Calendar;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.dto.StorefrontStats;
import com.nuodb.storefront.model.dto.StorefrontStatsReport;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.dto.WorkloadStep;
import com.nuodb.storefront.model.dto.WorkloadStepStats;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

@Path("/stats")
public class StatsApi extends BaseApi {
    private static final int DEFAULT_SESSION_TIMEOUT_SEC = 60 * 20;

    public StatsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStatsReport getAllStatsReport(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec,
            @QueryParam("includeStorefront") Boolean includeStorefront) {
        return getStorefrontStatsReport(sessionTimeoutSec, includeStorefront != null && includeStorefront.booleanValue());
    }

    @GET
    @Path("/storefront")
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStats getStorefrontStats(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec) {
        int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
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
        return getSimulator().getWorkloadStats();
    }

    @GET
    @Path("/workload-steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats() {
        return getSimulator().getWorkloadStepStats();
    }

    public static StorefrontStatsReport getStorefrontStatsReport(Integer sessionTimeoutSec, boolean includeStorefront)
    {
        StorefrontStatsReport report = new StorefrontStatsReport();
        IStorefrontService svc = StorefrontFactory.createStorefrontService();
        ISimulatorService sim = StorefrontFactory.getSimulatorService();

        report.setTimestamp(Calendar.getInstance());
        report.setAppInstance(StorefrontApp.APP_INSTANCE);
        report.setTransactionStats(svc.getTransactionStats());
        report.setWorkloadStats(sim.getWorkloadStats());
        report.setWorkloadStepStats(sim.getWorkloadStepStats());

        // Storefront stats are expensive to fetch (DB query required), so only fetch them if specifically requested
        if (includeStorefront) {
            int maxCustomerIdleTimeSec = (sessionTimeoutSec == null) ? DEFAULT_SESSION_TIMEOUT_SEC : sessionTimeoutSec;
            report.setStorefrontStats(svc.getStorefrontStatsByRegion(maxCustomerIdleTimeSec));
        }

        return report;
    }
}

package com.nuodb.storefront.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.model.StorefrontStats;
import com.nuodb.storefront.model.StorefrontStatsReport;
import com.nuodb.storefront.model.TransactionStats;
import com.nuodb.storefront.model.Workload;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadStepStats;

@Path("/stats")
public class StatsApi extends BaseApi {
    private static final int DEFAULT_SESSION_TIMEOUT_SEC = 60 * 20;

    public StatsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public StorefrontStatsReport getStats(@QueryParam("sessionTimeoutSec") Integer sessionTimeoutSec) {
        StorefrontStatsReport report = new StorefrontStatsReport();
        report.setStorefrontStats(getStorefrontStats(sessionTimeoutSec));
        report.setTransactionStats(getTransactionStats());
        report.setWorkloadStats(getWorkloadStats());
        report.setWorkloadStepStats(getWorkloadStepStats());
        return report;
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
        Map<String, WorkloadStats> stats = new HashMap<String, WorkloadStats>();
        for (Entry<Workload, WorkloadStats> stat : getSimulator().getWorkloadStats().entrySet()) {
            stats.put(stat.getKey().getName(), stat.getValue());
        }
        return stats;
    }
    

    @GET
    @Path("/workload-steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats() {
        return getSimulator().getWorkloadStepStats();
    }
}

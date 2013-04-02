package com.nuodb.storefront.api;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.Workload;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadStepStats;
import com.nuodb.storefront.service.ISimulatorService;

@Path("/simulator")
public class SimulatorApi {
    public SimulatorApi() {
    }

    @GET
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Workload, WorkloadStats> getWorkloadStats() {
        return getService().getWorkloadStats();
    }

    @DELETE
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAll() {
        getService().removeAll();
        return Response.ok().build();
    }

    @PUT
    @Path("/workloads/{workload}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWorkload(
            @PathParam("workload") String workload, @FormParam("numWorkers") int numWorkers, @FormParam("entryDelayMs") int entryDelayMs) {
        getService().addWorkload(lookupWorkloadByName(workload), numWorkers, entryDelayMs);
        return Response.ok().build();
    }

    @DELETE
    @Path("/workloads/{workload}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downsizeWorkload(@PathParam("workload") String workload, @FormParam("newWorkerLimit") int newWorkerLimit) {
        getService().downsizeWorkload(lookupWorkloadByName(workload), newWorkerLimit);
        return Response.ok().build();
    }

    @GET
    @Path("/steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepCompletionCounts() {
        return getService().getWorkloadStepStats();
    }

    protected ISimulatorService getService() {
        return StorefrontFactory.getSimulatorService();
    }
    
    protected Workload lookupWorkloadByName(String name) {
        try {
            return (Workload)Workload.class.getField(name).get(null);
        } catch (Exception e) {
            return null;
        }
    }
}

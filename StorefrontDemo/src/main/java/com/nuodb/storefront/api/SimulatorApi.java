/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.nuodb.storefront.model.dto.Message;
import com.nuodb.storefront.model.dto.Workload;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.dto.WorkloadStep;
import com.nuodb.storefront.model.type.MessageSeverity;
import com.nuodb.storefront.service.ISimulatorService;

@Path("/simulator")
public class SimulatorApi extends BaseApi {
    public SimulatorApi() {
    }

    @GET
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<WorkloadStats> getWorkloads() {
        return getSimulator().getWorkloadStats().values();
    }

    @DELETE
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAll() {
        getSimulator().removeAll();
        return Response.ok().build();
    }

    @PUT
    @Path("/workloads")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setWorkloads(MultivaluedMap<String, String> formParams) {
        Map<String, Object> respData = new HashMap<String, Object>();
        List<Message> messages = new ArrayList<Message>();
        ISimulatorService simulator = getSimulator();
        int updatedWorkloadCount = 0;
        int alertCount = 0;
        for (Map.Entry<String, List<String>> param : formParams.entrySet()) {
            if (param.getKey().startsWith("workload-")) {
                String workloadName = param.getKey().substring(9);
                int quantity = Integer.parseInt(param.getValue().get(0));
                Workload workload = simulator.getWorkload(workloadName);
                if (workload != null) {
                    if (workload.getMaxWorkers() > 0 && quantity > workload.getMaxWorkers()) {
                        messages.add(new Message(MessageSeverity.WARNING, workload.getName() + " is limited to " + workload.getMaxWorkers()
                                + " users; number of users set accordingly."));
                        quantity = workload.getMaxWorkers();
                        alertCount++;
                    }
                    try {
                        simulator.adjustWorkers(workload, quantity, quantity);
                    } catch (Exception e) {
                        messages.add(new Message(e));
                    }
                    updatedWorkloadCount++;
                }
            }
        }
        if (updatedWorkloadCount > 0 && alertCount == 0) {
            messages.add(new Message(MessageSeverity.INFO, "Workloads updated successfully."));
        }

        respData.put("messages", messages);
        respData.put("workloadStats", getSimulator().getWorkloadStats());
        return respData;
    }

    @POST
    @Path("/workloads/{workload}/workers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWorkers(@PathParam("workload") String workload, @FormParam("numWorkers") int numWorkers,
            @FormParam("entryDelayMs") int entryDelayMs) {
        getSimulator().addWorkers(lookupWorkloadByName(workload), numWorkers, entryDelayMs);
        return Response.ok().build();
    }

    @PUT
    @Path("/workloads/{workload}/workers")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkloadStats adjustWorkers(@PathParam("workload") String workload, @FormParam("minWorkers") int minWorkers,
            @FormParam("limit") Integer limit) {
        return getSimulator().adjustWorkers(lookupWorkloadByName(workload), minWorkers, limit);
    }

    @GET
    @Path("/steps")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<WorkloadStep> getWorkloadSteps() {
        return getSimulator().getWorkloadStepStats().keySet();
    }

    protected Workload lookupWorkloadByName(String name) {
        try {
            Workload workload = getSimulator().getWorkload(name);
            if (workload != null) {
                return workload;
            }
        } catch (Exception e) {
        }
        throw new IllegalArgumentException("Unknown workload '" + name + "'");
    }
}

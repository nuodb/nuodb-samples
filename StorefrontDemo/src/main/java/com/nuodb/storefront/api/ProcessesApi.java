/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.nuodb.storefront.model.db.Process;
import com.nuodb.storefront.model.dto.ProcessDetail;
import com.nuodb.storefront.model.entity.AppInstance;

@Path("/processes")
public class ProcessesApi extends BaseApi {
    public ProcessesApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ProcessDetail> getProcesses(@Context HttpServletRequest req) {
        int currentNodeId = getTenant(req).getAppInstance().getNodeId();

        // Fetch processes
        Map<Integer, ProcessDetail> processMap = new HashMap<Integer, ProcessDetail>();
        for (Process process : getDbApi(req).getDbProcesses()) {
            ProcessDetail detail;
            processMap.put(process.nodeId, detail = new ProcessDetail(process));
            
            if (process.nodeId == currentNodeId) {
                detail.setCurrentConnection(true);
            }
        }

        // Marry with AppInstances
        for (AppInstance appInstance : getService(req).getAppInstances(true)) {
            ProcessDetail detail = processMap.get(appInstance.getNodeId());
            if (detail != null) {
                detail.getAppInstances().add(appInstance.getUrl());
            }
        }

        return processMap.values();
    }

    @DELETE
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest req, @PathParam("uid") String uid) {
        getDbApi(req).shutdownProcess(uid);
        return Response.ok().build();
    }
}
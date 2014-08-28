/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.nuodb.storefront.dbapi.Process;

@Path("/processes")
public class ProcessesApi extends BaseApi {
    public ProcessesApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Process> getProcesses() {
        return getDbApi().getDbProcesses();
    }

    @DELETE
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("uid") String uid) {
        getDbApi().shutdownProcess(uid);
        return Response.ok().build();
    }
}
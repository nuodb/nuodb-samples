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

import com.nuodb.storefront.model.dto.DbNode;

@Path("/db-nodes")
public class DbNodeApi extends BaseApi {
    public DbNodeApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DbNode> getDbNodes() {
        return getDbApiService().getDbNodes();
    }

    @DELETE
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("uid") String uid) {
        getDbApiService().shutdownDbNode(uid);
        return Response.ok().build();
    }
}
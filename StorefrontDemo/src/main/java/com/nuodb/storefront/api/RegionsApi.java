/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.dbapi.Region;

@Path("/regions")
public class RegionsApi extends BaseApi {
    public RegionsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Region> getRegions() {
        return getDbApi().getRegions();
    }
}
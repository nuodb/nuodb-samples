/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;

@Path("/categories")
public class CategoriesApi extends BaseApi {
    public CategoriesApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult<Category> getAll() {
        return getService().getCategories();
    }
}

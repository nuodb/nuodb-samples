/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.exception.DataValidationException;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.type.Currency;

@Path("/app-instances")
public class AppInstanceApi extends BaseApi {
    public AppInstanceApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AppInstance> getActiveAppInstances() {
        return getService().getAppInstances(true);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public AppInstance updateAppInstance(@FormParam("currency") Currency currency) {
        if (currency == null) {
            throw new DataValidationException("Currency not valid.");
        }
        
        AppInstance instance = StorefrontApp.APP_INSTANCE;
        instance.setCurrency(currency);
        return instance;
    }
}

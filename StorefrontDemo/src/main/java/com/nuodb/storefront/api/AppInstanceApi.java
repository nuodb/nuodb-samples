/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.dto.DbConnInfo;
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

    @GET
    @Path("/init-params")
    public Map<String, String> getParams(@Context HttpServletRequest req) {
        Map<String, String> map = new HashMap<String, String>();

        ServletContext ctx = req.getServletContext();
        Enumeration<String> names = ctx.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            map.put("init." + name, ctx.getInitParameter(name));
        }

        names = ctx.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            map.put("attr." + name, ctx.getAttribute(name) + "");
        }

        return map;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public AppInstance updateAppInstance(@FormParam("currency") Currency currency, @FormParam("stopUsersWhenIdle") Boolean stopUsersWhenIdle) {
        AppInstance instance = StorefrontApp.APP_INSTANCE;
        if (currency != null) {
            instance.setCurrency(currency);
        }
        if (stopUsersWhenIdle != null) {
            instance.setStopUsersWhenIdle(stopUsersWhenIdle);
        }
        return instance;
    }

    @PUT
    @Path("/sync")
    @Produces(MediaType.APPLICATION_JSON)
    public DbConnInfo setDbStats(DbConnInfo newConfig) {
        DbConnInfo dbConfig = StorefrontFactory.getDbConnInfo();
        if (dbConfig.equals(newConfig)) {
            // Already have this connection info
            return dbConfig;
        }

        if (!StringUtils.isEmpty(newConfig.getDbName())) {
            dbConfig.setDbName(newConfig.getDbName());
        }
        if (StringUtils.isEmpty(newConfig.getDbProcessTag())) {
            dbConfig.setDbProcessTag(newConfig.getDbProcessTag());
        }
        if (StringUtils.isEmpty(newConfig.getHost())) {
            dbConfig.setHost(newConfig.getHost());
        }
        if (StringUtils.isEmpty(newConfig.getPassword())) {
            dbConfig.setPassword(newConfig.getPassword());
        }
        if (StringUtils.isEmpty(newConfig.getUrl())) {
            dbConfig.setPassword(newConfig.getUrl());
        }
        if (StringUtils.isEmpty(newConfig.getUsername())) {
            dbConfig.setPassword(newConfig.getUsername());
        }

        StorefrontFactory.setDbConnInfo(dbConfig);
        getDbApi().fixDbSetup(true);
        return dbConfig;
    }

}

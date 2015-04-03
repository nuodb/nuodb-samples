/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.service.IDbApi;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.servlet.BaseServlet;

public abstract class BaseApi {
    protected BaseApi() {
    }
    
    protected IStorefrontTenant getTenant(HttpServletRequest req) {
        return BaseServlet.getTenant(req);
    }

    protected IStorefrontService getService(HttpServletRequest req) {
        return BaseServlet.getStorefrontService(req);
    }

    protected IDbApi getDbApi(HttpServletRequest req) {
        return BaseServlet.getDbApi(req);
    }

    protected ISimulatorService getSimulator(HttpServletRequest req) {
        getTenant(req).getAppInstance().setLastApiActivity(Calendar.getInstance());
        return BaseServlet.getSimulator(req);
    }

    protected Customer getOrCreateCustomer(HttpServletRequest req, HttpServletResponse resp) {
        return BaseServlet.getOrCreateCustomer(req, resp);
    }
}

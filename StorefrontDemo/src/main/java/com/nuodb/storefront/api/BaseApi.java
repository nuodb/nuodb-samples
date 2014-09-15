/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.dbapi.IDbApi;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;

public abstract class BaseApi {
    protected BaseApi() {
    }

    protected IStorefrontService getService() {
        return BaseServlet.getStorefrontService();
    }

    protected IDbApi getDbApi() {
        return BaseServlet.getDbApi();
    }

    protected ISimulatorService getSimulator() {
        StorefrontApp.APP_INSTANCE.setLastApiActivity(Calendar.getInstance());
        return BaseServlet.getSimulator();
    }

    protected Customer getOrCreateCustomer(HttpServletRequest req, HttpServletResponse resp) {
        return BaseServlet.getOrCreateCustomer(req, resp);
    }
}

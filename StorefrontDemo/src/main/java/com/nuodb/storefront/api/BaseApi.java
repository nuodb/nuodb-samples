/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.service.IDbApiService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;

public abstract class BaseApi {
    protected BaseApi() {
    }

    protected IStorefrontService getService() {
        return BaseServlet.getStorefrontService();
    }

    protected IDbApiService getDbApiService() {
        return BaseServlet.getDbApiService();
    }

    
    protected ISimulatorService getSimulator() {
        return BaseServlet.getSimulator();
    }

    protected Customer getOrCreateCustomer(HttpServletRequest req, HttpServletResponse resp) {
        return BaseServlet.getOrCreateCustomer(req, resp);
    }
}

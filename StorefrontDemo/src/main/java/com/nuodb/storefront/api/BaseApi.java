package com.nuodb.storefront.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;

public abstract class BaseApi {
    protected BaseApi() {
    }

    protected IStorefrontService getService() {
        return BaseServlet.getService();
    }

    protected ISimulatorService getSimulator() {
        return BaseServlet.getSimulator();
    }
    
    protected Customer getOrCreateCustomer(HttpServletRequest req, HttpServletResponse resp) {
        return BaseServlet.getOrCreateCustomer(req, resp);
    }
}

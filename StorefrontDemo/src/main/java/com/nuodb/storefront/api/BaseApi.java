package com.nuodb.storefront.api;

import javax.servlet.http.HttpServletRequest;

import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;

public abstract class BaseApi {
    protected BaseApi() {
    }

    protected IStorefrontService getService() {
        return BaseServlet.getService();
    }

    protected Customer getOrCreateCustomer(HttpServletRequest req) {
        return BaseServlet.getOrCreateCustomer(req);
    }
}

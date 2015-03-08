/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.entity.Customer;

public class TourMultiTenantServlet extends BaseServlet {
    private static final long serialVersionUID = -128151977569715735L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("adminConsoleUrl", StorefrontFactory.getAdminConsoleUrl());
            
            showPage(req, resp, "Multi-Tenant", "tour-multi-tenant", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

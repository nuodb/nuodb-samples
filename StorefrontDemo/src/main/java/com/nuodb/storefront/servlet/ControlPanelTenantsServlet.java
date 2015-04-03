/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.api.TenantsApi;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

public class ControlPanelTenantsServlet extends BaseServlet {
    private static final long serialVersionUID = 8647899002345555513L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("tenants", new TenantsApi().getTenants(req));

            showPage(req, resp, "Control Panel", "control-panel-tenants", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tenantName = req.getParameter("tenant-name");
        if (!StringUtils.isEmpty(tenantName)) {
            try {
                StorefrontTenantManager.createTenant(tenantName);
            } catch (Exception e) {
                addMessage(req, MessageSeverity.ERROR, e.getMessage());
            }
        }
        doGet(req, resp);
    }
}

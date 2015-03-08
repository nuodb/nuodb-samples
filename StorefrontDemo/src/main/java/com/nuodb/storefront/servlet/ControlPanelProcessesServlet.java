/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.api.ProcessesApi;
import com.nuodb.storefront.model.entity.Customer;

public class ControlPanelProcessesServlet extends BaseServlet {
    private static final long serialVersionUID = 6321822277810614623L;

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("processes", new ProcessesApi().getProcesses());
            pageData.put("adminConsoleUrl", StorefrontFactory.getAdminConsoleUrl());

            showPage(req, resp, "Control Panel", "control-panel-processes", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    /**
     * POST: Handle data load requests, then display control panel as normal
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
}

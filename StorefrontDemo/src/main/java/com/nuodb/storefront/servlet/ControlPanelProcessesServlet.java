/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.dbapi.Process;
import com.nuodb.storefront.model.entity.Customer;

public class ControlPanelProcessesServlet extends BaseServlet {
    private static final long serialVersionUID = 6321822277810614623L;

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Fetch nodes data
            List<Process> processes = getDbApi().getProcesses(StorefrontFactory.getDbConnInfo().getDbName());

            // Fetch data the page needs
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("processes", processes);
            pageData.put("isConsoleLocal", StorefrontWebApp.isConsoleLocal());

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

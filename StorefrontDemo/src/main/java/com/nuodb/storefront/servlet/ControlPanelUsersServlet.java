/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.model.dto.StorefrontStatsReport;
import com.nuodb.storefront.model.entity.Customer;

public class ControlPanelUsersServlet extends BaseServlet {
    private static final long serialVersionUID = 8435653806520224541L;

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StorefrontStatsReport stats = getSimulator(req).getStorefrontStatsReport(null);
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("stats", stats);
            pageData.put("maxIdleSec", StorefrontApp.STOP_USERS_AFTER_IDLE_UI_SEC);
            pageData.put("stopUsersWhenIdle", getTenant(req).getAppInstance().getStopUsersWhenIdle());

            showPage(req, resp, "Control Panel", "control-panel-users", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

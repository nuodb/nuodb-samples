/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;
import com.nuodb.storefront.service.IStorefrontTenant;

public class ControlPanelLogServlet extends BaseServlet {
    private static final long serialVersionUID = 7893467234193233L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String logData = getTenant(req).getLogWriter().getBuffer().toString();
            String logWithHeader = generateLogHeader(req) + ((logData == null) ? "No log entries available." : logData);

            if (req.getParameter("download") != null) {
                resp.addHeader("Content-Disposition", "attachment; filename=storefront.log");
                resp.addHeader("Content-Type", "text/html");
                Writer writer = resp.getWriter();
                if (logData != null) {
                    writer.write(logWithHeader);
                }
                writer.flush();
                writer.close();
            } else {
                Map<String, Object> pageData = new HashMap<String, Object>();
                req.setAttribute("log", StringEscapeUtils.escapeHtml4(logWithHeader));
                if (logData == null) {
                    addMessage(req, MessageSeverity.ERROR, "In-memory logging is not enabled.  Configure InMemoryAppender in log4j.xml.");
                }
                showPage(req, resp, "Control Panel", "control-panel-log", pageData, new Customer());
            }
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("clear") != null) {
            getTenant(req).getLogWriter().getBuffer().setLength(0);
        }

        doGet(req, resp);
    }

    protected String generateLogHeader(HttpServletRequest req) {
        StringBuilder buff = new StringBuilder();
        IStorefrontTenant thisTenant = getTenant(req);

        // Storefront instances
        int appCount = 0;
        buff.append("STOREFRONT INSTANCES:\n\n");
        try {
            for (AppInstance instance : getStorefrontService(req).getAppInstances(true)) {
                buff.append(++appCount);
                buff.append(". ");
                buff.append(instance);
                buff.append("\n");
            }
        } catch (Exception e) {
            buff.append(thisTenant.getAppInstance());
            buff.append("\n");
        }

        // Connections
        buff.append(StringUtils.repeat('-', 150));
        buff.append("\n\n");
        buff.append("CONNECTIONS:\n\n");
        buff.append("1. Database: ");
        buff.append(thisTenant.getDbConnInfo());
        buff.append("\n");
        buff.append("2. API: ");
        buff.append(thisTenant.getApiConnInfo());
        buff.append("\n");

        // Simulator status
        int workerCount = 0;
        buff.append(StringUtils.repeat('-', 150));
        buff.append("\n\n");
        buff.append("LOCAL SIMULATED WORKLOADS:\n\n");
        try {
            for (Map.Entry<String, WorkloadStats> entry : getSimulator(req).getWorkloadStats().entrySet()) {
                buff.append(++workerCount);
                buff.append(". ");
                buff.append(entry.getKey());
                buff.append(": ");
                buff.append(entry.getValue());
                buff.append("\n");
            }
        } catch (Exception e) {
            buff.append(e.getMessage() + "\n\n");
        }

        // Other tenants
        List<IStorefrontTenant> allTenants = StorefrontTenantManager.getAllTenants();
        if (allTenants.size() > 1) {
            int tenantCount = 0;
            buff.append(StringUtils.repeat('-', 150));
            buff.append("\n\n");
            buff.append("OTHER LOCAL TENANTS:\n\n");
            for (IStorefrontTenant tenant : allTenants) {
                if (tenant != thisTenant) {
                    buff.append(++tenantCount);
                    buff.append(". ");
                    buff.append(tenant.getAppInstance().getTenantName() + ": ");
                    buff.append(tenant.getAppInstance());
                    buff.append("\n");
                }
            }
        }

        // End of header
        buff.append(StringUtils.repeat('-', 150));
        buff.append("\n\n");
        buff.append("LOG ENTRIES:\n\n");
        return buff.toString();
    }
}

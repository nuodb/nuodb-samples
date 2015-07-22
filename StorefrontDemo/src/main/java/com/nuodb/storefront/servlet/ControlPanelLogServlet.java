/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

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

        // Storefront instances
        buff.append('\n');
        buff.append("STOREFRONT INSTANCES:\n\n");
        int appCount = 0;
        try {
            for (AppInstance instance : getStorefrontService(req).getAppInstances(true)) {
                buff.append(++appCount);
                buff.append(". ");
                buff.append(instance);
                buff.append("\n");
            }
        } catch (Exception e) {
            buff.append(getStorefrontService(req).getAppInstance());
            buff.append("\n\n");
        }

        // Connections
        buff.append(StringUtils.repeat('-', 150));
        buff.append("\n\n");
        buff.append("CONNECTIONS:\n\n");
        buff.append("1. Database: ");
        buff.append(getTenant(req).getDbConnInfo());
        buff.append("\n");
        buff.append("2. API: ");
        buff.append(getTenant(req).getApiConnInfo());
        buff.append("\n");
        
        // Simulator status
        buff.append(StringUtils.repeat('-', 150));
        int workerCount = 0;
        buff.append("\n\n");
        buff.append("LOCAL SIMULATED WORKLOADS:\n\n");
        for (Map.Entry<String, WorkloadStats> entry : getSimulator(req).getWorkloadStats().entrySet()) {
            buff.append(++workerCount);
            buff.append(". ");
            buff.append(entry.getKey());
            buff.append(": ");
            buff.append(entry.getValue());
            buff.append("\n");
        }

        buff.append(StringUtils.repeat('-', 150));
        buff.append("\n\n");        
        buff.append("LOG ENTRIES:\n\n");
        return buff.toString();
    }
}

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

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;
import com.nuodb.storefront.util.InMemoryAppender;

public class ControlPanelLogServlet extends BaseServlet {
    private static final long serialVersionUID = 7893467234193233L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            InMemoryAppender appender = InMemoryAppender.getInstance();
            String logData = (appender != null) ? appender.getLog() : null;

            if (req.getParameter("download") != null) {
                resp.addHeader("Content-Disposition", "attachment; filename=storefront.log");
                resp.addHeader("Content-Type", "text/html");
                Writer writer = resp.getWriter();
                if (logData != null) {
                    writer.write(logData);
                }
                writer.flush();
                writer.close();
            } else {
                Map<String, Object> pageData = new HashMap<String, Object>();
                req.setAttribute("log", (logData == null || logData.isEmpty()) ? "Log is empty." : StringEscapeUtils.escapeHtml4(logData));
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
            InMemoryAppender appender = InMemoryAppender.getInstance();
            if (appender != null) {
                appender.clear();
            }
        }

        doGet(req, resp);
    }
}

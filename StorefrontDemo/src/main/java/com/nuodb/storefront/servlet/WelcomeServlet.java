/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.entity.Customer;

public class WelcomeServlet extends BaseServlet {
    private static final long serialVersionUID = 4369262156023258885L;
    private static volatile String s_ddl;

    /**
     * GET: Shows the welcome screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Fetch data the page needs
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("ddl", getDdl());

            showPage(req, resp, "Welcome", "welcome", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    private static synchronized String getDdl() {
        if (s_ddl == null) {
            StringBuilder buff = new StringBuilder();
            SchemaExport export = StorefrontFactory.createSchemaExport();
            appendDdlScript(export, "dropSQL", buff);
            appendDdlScript(export, "createSQL", buff);
            s_ddl = buff.toString();
        }
        return s_ddl;
    }

    private static void appendDdlScript(SchemaExport export, String fieldName, StringBuilder buffer) {
        try {
            Field createSqlField = export.getClass().getDeclaredField(fieldName);
            createSqlField.setAccessible(true);
            for (String stmt : (String[]) createSqlField.get(export)) {
                buffer.append(stmt);
                buffer.append(";\r\n");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

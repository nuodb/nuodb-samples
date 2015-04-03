/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.service.IStorefrontTenant;

public class ControlPanelDatabaseServlet extends BaseServlet {
    private static final long serialVersionUID = 5983572345193336181L;
    private static volatile String s_ddl;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            IStorefrontTenant tenant = getTenant(req);
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("db", tenant.getDbApi().getDb());
            pageData.put("dbConnInfo", tenant.getDbConnInfo());
            pageData.put("apiConnInfo", tenant.getDbApi().getApiConnInfo());
            pageData.put("adminConsoleUrl", tenant.getAdminConsoleUrl());
            pageData.put("sqlExplorerUrl", tenant.getSqlExplorerUrl());
            pageData.put("ddl", getDdl(tenant));

            showPage(req, resp, "Control Panel", "control-panel-database", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    private static synchronized String getDdl(IStorefrontTenant tenant) {
        if (s_ddl == null) {
            StringBuilder buff = new StringBuilder();
            SchemaExport export = tenant.createSchemaExport();
            buff.append("-- Drop statements --\r\n");
            appendDdlScript(export, "dropSQL", buff, ";\r\n");
            buff.append("\r\n");
            buff.append("-- Create statements --\r\n");
            appendDdlScript(export, "createSQL", buff, ";\r\n\r\n");
            s_ddl = buff.toString();
        }
        return s_ddl;
    }

    private static void appendDdlScript(SchemaExport export, String fieldName, StringBuilder buffer, String delimiter) {
        try {
            Field createSqlField = export.getClass().getDeclaredField(fieldName);
            createSqlField.setAccessible(true);
            for (String stmt : (String[]) createSqlField.get(export)) {
                buffer.append(stmt);
                buffer.append(delimiter);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

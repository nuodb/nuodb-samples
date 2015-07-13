/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.util.DDLUtil;

public class ControlPanelDatabaseServlet extends BaseServlet {
    private static final long serialVersionUID = 5983572345193336181L;

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
            pageData.put("ddl", DDLUtil.generateAndFormat(tenant.createSchemaExport()));

            showPage(req, resp, "Control Panel", "control-panel-database", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

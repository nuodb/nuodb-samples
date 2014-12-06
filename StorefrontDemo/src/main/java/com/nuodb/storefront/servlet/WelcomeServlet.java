/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.exception.ApiUnavailableException;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

public class WelcomeServlet extends ControlPanelProductsServlet {
    private static final long serialVersionUID = 4369262156023258885L;
    private static final Logger s_logger = Logger.getLogger(WelcomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doHealthCheck(req);
        showPage(req, resp, "Welcome", "welcome", null, new Customer());
    }

    @Override
    protected void doPostAction(HttpServletRequest req, HttpServletResponse resp, String btnAction) throws IOException {
        if (btnAction.contains("create")) {
            getDbApi().fixDbSetup(true);
        }
        super.doPostAction(req, resp, btnAction);
    }

    protected void doHealthCheck(HttpServletRequest req) throws ServletException {
        try {
            getDbApi().fixDbSetup(false);

            synchronized (s_schemaUpdateLock) {
                try {
                    checkForProducts(req);
                } catch (SQLGrammarException e) {
                    // Database exists, but schema might not yet exist. Try creating it automatically.
                    try {
                        StorefrontFactory.createSchema();
                        checkForProducts(req);
                    } catch (Exception e2) {
                        // Schema repair didn't work
                        throw e;
                    }
                }
            }
        } catch (DatabaseNotFoundException e) {
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(req, MessageSeverity.INFO, "The " + dbInfo.getDbName() + " database does not yet exist.", "Create database");
        } catch (GenericJDBCException e) {
            s_logger.warn("Servlet handled JDBC error", e);

            // Database may not exist. Inform the user
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(req, MessageSeverity.WARNING,
                    "The " + dbInfo.getDbName() + " database may not yet exist.  The Storefront is trying to connect to \""
                            + dbInfo.getUrl() + "\" with the username \"" + dbInfo.getUsername() + "\".", "Create database");

        } catch (ApiUnavailableException e) {
            s_logger.error("Can't connect to API", e);
            addMessage(req, MessageSeverity.ERROR,
                    "Cannot connect to NuoDB RESTful API.  The Storefront is trying to connect to \""
                            + getDbApi().getBaseUrl() + "\" with the username \"" + getDbApi().getAuthUser() + "\".", "Retry");
        } catch (ApiProxyException e) {
            s_logger.error("Health check failed", e);
            addMessage(req, MessageSeverity.ERROR, "NuoDB RESTful API at " + getDbApi().getBaseUrl() + " returned an error:  " + e.getMessage());
        }
    }
}

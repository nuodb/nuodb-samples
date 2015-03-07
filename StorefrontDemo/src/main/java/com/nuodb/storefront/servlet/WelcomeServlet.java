/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.exception.ApiConnectionException;
import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.exception.ApiUnauthorizedException;
import com.nuodb.storefront.exception.ApiUnavailableException;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

public class WelcomeServlet extends ControlPanelProductsServlet {
    private static final long serialVersionUID = 4369262156023258885L;
    private static final Logger s_logger = Logger.getLogger(WelcomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> pageData = new HashMap<String, Object>();
        Pair<String, Object> prop = doHealthCheck(req);
        if (prop != null) {
            pageData.put(prop.getKey(), prop.getValue());
        }
        showPage(req, resp, "Welcome", "welcome", pageData, new Customer());
    }

    @Override
    protected void doPostAction(HttpServletRequest req, HttpServletResponse resp, String btnAction) throws IOException {
        if (btnAction.equals("api")) {
            ConnInfo apiConnInfo = new ConnInfo();
            apiConnInfo.setUrl(req.getParameter("api-url"));
            apiConnInfo.setUsername(req.getParameter("api-username"));
            apiConnInfo.setPassword(req.getParameter("api-password"));
            StorefrontFactory.setApiConnInfo(apiConnInfo);

            // Wait until the API is connected to the domain
            for (int secondsWaited = 0; secondsWaited < StorefrontApp.MAX_API_UNAVAILABLE_RETRY_TIME_SEC; secondsWaited++) {
                try {
                    getDbApi().testConnection();
                    break;
                } catch (ApiUnavailableException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        } else if (btnAction.equals("db")) {
            DbConnInfo connInfo = new DbConnInfo();
            connInfo.setUrl(req.getParameter("url"));
            connInfo.setUsername(req.getParameter("username"));
            connInfo.setPassword(req.getParameter("password"));
            StorefrontFactory.setDbConnInfo(connInfo);

            getDbApi().fixDbSetup(true);

            // Wait until API acknowledges the DB exists
            for (int secondsWaited = 0; secondsWaited < StorefrontApp.MAX_DB_INIT_WAIT_TIME_SEC; secondsWaited++) {
                try {
                    getDbApi().fixDbSetup(false);
                    break;
                } catch (DatabaseNotFoundException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        }

        super.doPostAction(req, resp, btnAction);
    }

    protected Pair<String, Object> doHealthCheck(HttpServletRequest req) throws ServletException {
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
                        s_logger.warn("Schema repair didn't work", e2);
                        throw e;
                    }
                }
            }
        } catch (DatabaseNotFoundException e) {
            return new ImmutablePair<String, Object>("db", StorefrontFactory.getDbConnInfo());
        } catch (GenericJDBCException e) {
            s_logger.warn("Servlet handled JDBC error", e);

            // Database may not exist. Inform the user
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(req, MessageSeverity.WARNING, "Could not connect to " + dbInfo.getDbName() + ":  " + e.getMessage());
            return new ImmutablePair<String, Object>("db", dbInfo);
        } catch (ApiUnavailableException e) {
            addMessage(req, MessageSeverity.ERROR, "The NuoDB API is temporarily unavailable.", "Retry");
            return null;
        } catch (ApiConnectionException e) {
            s_logger.error("Can't connect to API", e);
            ConnInfo apiConnInfo = getDbApi().getApiConnInfo();
            addMessage(req, MessageSeverity.ERROR,
                    "Cannot connect to NuoDB API.  The Storefront is trying to connect to \"" + apiConnInfo.getUrl() + "\" with the username \""
                            + apiConnInfo.getUsername() + "\".", "Retry");
            return new ImmutablePair<String, Object>("api", apiConnInfo);
        } catch (ApiUnauthorizedException e) {
            ConnInfo apiConnInfo = getDbApi().getApiConnInfo();
            apiConnInfo.setPassword(null);
            addMessage(req, MessageSeverity.INFO, "Unable to connect to NuoDB API at \"" + apiConnInfo.getUrl() + "\" with the provided credentials.");
            return new ImmutablePair<String, Object>("api", apiConnInfo);
        } catch (ApiProxyException e) {
            s_logger.error("Health check failed", e);
            addMessage(req, MessageSeverity.ERROR,
                    "NuoDB RESTful API at " + getDbApi().getApiConnInfo().getUrl() + " returned an error:  " + e.getMessage(), "Retry");
        } catch (Exception e) {
            s_logger.error("Health check failed", e);
            Throwable ei = e.getCause();
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            String msg = (ei != null ) ? ei.getMessage() : null;
            if (msg != null && msg.indexOf("Database is inactive") >= 0) {
                addMessage(req, MessageSeverity.WARNING, dbInfo.getDbName() + " is inactive.  Unquiesce the database via the Automation Console.",
                        "Retry");
            } else {
                addMessage(req, MessageSeverity.ERROR, "Unable to connect to " + dbInfo.getDbName() + ":  " + e.getMessage(), "Retry");
            }
        }

        return null;
    }
}

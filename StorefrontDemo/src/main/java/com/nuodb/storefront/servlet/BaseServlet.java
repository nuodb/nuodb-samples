/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.dbapi.IDbApi;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.Message;
import com.nuodb.storefront.model.dto.PageConfig;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

public abstract class BaseServlet extends HttpServlet {
    public static final String ATTR_PAGE_CONFIG = "pageConfig";
    public static final String SESSION_PRODUCT_FILTER = "productFilter";

    private static final Logger s_logger = Logger.getLogger(BaseServlet.class.getName());
    private static final String ATTR_CUSTOMER = "customer";
    private static final String COOKIE_CUSTOMER_ID = "customerId";
    private static final String SESSION_MESSAGES = "messages";
    private static final String SESSION_CUSTOMER_ID = "customerId";
    private static final int COOKIE_MAX_AGE_SEC = 60 * 60 * 24 * 31; // 1 month
    private static final long serialVersionUID = 1452096145544476070L;
    private static final Object s_svcLock = new Object();
    private static volatile IStorefrontService s_svc;
    protected static Object s_schemaUpdateLock = new Object();

    protected BaseServlet() {
    }

    public static IStorefrontService getStorefrontService() {
        if (s_svc == null) {
            synchronized (s_svcLock) {
                if (s_svc == null) {
                    s_svc = StorefrontFactory.createStorefrontService();
                }
            }
        }
        return s_svc;
    }

    public static IDbApi getDbApi() {
        return StorefrontFactory.getDbApi();
    }

    public static ISimulatorService getSimulator() {
        return StorefrontFactory.getSimulatorService();
    }

    public static Customer getOrCreateCustomer(HttpServletRequest req, HttpServletResponse resp) {
        // For simplicity in this demo, we're implicitly trusting parameters and cookies rather than authenticating users.

        Customer customer = (Customer) req.getAttribute(ATTR_CUSTOMER);
        if (customer == null) {
            Integer customerId = (Integer) req.getSession().getAttribute(SESSION_CUSTOMER_ID);
            if (customerId == null && req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if (COOKIE_CUSTOMER_ID.equals(cookie.getName())) {
                        try {
                            customerId = Integer.parseInt(cookie.getValue());
                            break;
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            if (customerId == null) {
                customerId = 0;
            }

            customer = getStorefrontService().getOrCreateCustomer(customerId, null);
            req.getSession().setAttribute(SESSION_CUSTOMER_ID, customer.getId());
            req.setAttribute(ATTR_CUSTOMER, customer);

            // Add customer ID cookie
            Cookie customerCookie = new Cookie(COOKIE_CUSTOMER_ID, String.valueOf(customer.getId()));
            customerCookie.setMaxAge(COOKIE_MAX_AGE_SEC);
            resp.addCookie(customerCookie);
        }
        return customer;
    }

    public static ProductFilter getOrCreateProductFilter(HttpServletRequest req) {
        HttpSession session = req.getSession();
        ProductFilter filter = (ProductFilter) session.getAttribute(SESSION_PRODUCT_FILTER);
        if (filter == null) {
            filter = new ProductFilter();
            session.setAttribute(SESSION_PRODUCT_FILTER, filter);
        }
        return filter;
    }

    public static Message addErrorMessage(HttpServletRequest req, Exception e) {
        Message msg = new Message(e);
        getMessages(req).add(msg);
        return msg;
    }

    public static Message addMessage(HttpServletRequest req, MessageSeverity severity, String message, String... buttons) {
        Message msg = new Message(severity, message, buttons);
        getMessages(req).add(msg);
        return msg;
    }

    public static List<Message> getMessages(HttpServletRequest req) {
        HttpSession session = req.getSession();

        @SuppressWarnings("unchecked")
        List<Message> messages = (List<Message>) session.getAttribute(SESSION_MESSAGES);
        if (messages == null) {
            messages = new ArrayList<Message>();
            session.setAttribute(SESSION_MESSAGES, messages);
        }

        return messages;
    }

    protected static void showPage(HttpServletRequest req, HttpServletResponse resp, String pageTitle, String pageName, Object pageData)
            throws ServletException, IOException {
        showPage(req, resp, pageTitle, pageName, pageData, null);
    }

    protected static void showPage(HttpServletRequest req, HttpServletResponse resp, String pageTitle, String pageName, Object pageData,
            Customer customer) throws ServletException, IOException {

        StorefrontWebApp.updateWebAppUrl(req);

        // Build full page title
        String storeName = StorefrontApp.APP_INSTANCE.getName() + " - NuoDB Storefront Demo";
        if (pageTitle == null || pageTitle.isEmpty()) {
            pageTitle = storeName;
        } else {
            pageTitle = pageTitle + " - " + storeName;
        }

        // Share data with JSP page
        if (customer == null) {
            customer = getOrCreateCustomer(req, resp);
        }

        // Fetch app instance list for region dropdown menu
        List<AppInstance> appInstances;
        try {
            appInstances = getStorefrontService().getAppInstances(true);
        } catch (Exception e) {
            appInstances = new ArrayList<AppInstance>();
            appInstances.add(StorefrontApp.APP_INSTANCE);
        }

        PageConfig initData = new PageConfig(pageTitle, pageName, pageData, customer, getMessages(req), appInstances);
        req.setAttribute(ATTR_PAGE_CONFIG, initData);
        req.getSession().removeAttribute(SESSION_MESSAGES);

        // Render JSP page
        s_logger.info("Servicing \"" + req.getMethod() + " " + req.getRequestURI() + "\" with \"" + pageName + ".jsp\" for customer "
                + ((customer == null) ? null : customer.getId()) + " from " + req.getRemoteAddr());
        req.getRequestDispatcher("/WEB-INF/pages/" + pageName + ".jsp").forward(req, resp);
    }

    protected static void showCriticalErrorPage(HttpServletRequest req, HttpServletResponse resp, Exception ex) throws ServletException, IOException {
        getMessages(req).clear();
        addErrorMessage(req, ex);

        if (ex instanceof GenericJDBCException) {
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(
                    req,
                    MessageSeverity.INFO,
                    "Tip:  Check to see whether NuoDB is running and the database exists.  The storefront is trying to connect to \""
                            + dbInfo.getUrl() + "\" with the username \"" + dbInfo.getUsername() + "\".");
        } else if (ex instanceof SQLGrammarException) {
            // Tables could be missing or bad. This could happen if a user re-creates the Storefront DB while it's running. Try repairing.
            try {
                synchronized (s_schemaUpdateLock) {
                    StorefrontFactory.createSchema();
                }
                addMessage(req, MessageSeverity.WARNING, "The Storefront schema has been updated.  One or more tables were missing or out of date.",
                        "Refresh page");
            } catch (Exception e) {
                // Repair didn't work
            }
        }
        Customer customer = (Customer) req.getAttribute(ATTR_CUSTOMER);
        showPage(req, resp, "Storefront Problem", "error", null, (customer == null) ? new Customer() : customer);

        s_logger.warn("Servlet handled critical error", ex);
    }
}

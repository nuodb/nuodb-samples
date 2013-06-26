/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.exception.GenericJDBCException;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.DbConnInfo;
import com.nuodb.storefront.model.Message;
import com.nuodb.storefront.model.MessageSeverity;
import com.nuodb.storefront.model.PageConfig;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
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
    private static volatile String s_storefrontName = "Default Storefront";

    protected BaseServlet() {
    }

    public static IStorefrontService getService() {
        if (s_svc == null) {
            synchronized (s_svcLock) {
                if (s_svc == null) {
                    s_svc = StorefrontFactory.createStorefrontService();
                    s_storefrontName = getService().getStorefrontStats(0).getStorefrontName();
                }
            }
        }
        return s_svc;
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

            customer = getService().getOrCreateCustomer(customerId);
            req.getSession().setAttribute(SESSION_CUSTOMER_ID, customer.getId());
            req.setAttribute(ATTR_CUSTOMER, customer);

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

    public static void addErrorMessage(HttpServletRequest req, Exception e) {
        getMessages(req).add(new Message(e));
    }

    public static void addMessage(HttpServletRequest req, MessageSeverity severity, String message, String... buttons) {
        getMessages(req).add(new Message(severity, message, buttons));
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
            Customer customer)
            throws ServletException, IOException {

        // Build full page title
        if (pageTitle == null || pageTitle.isEmpty()) {
            pageTitle = s_storefrontName;
        } else {
            pageTitle = pageTitle + " - " + s_storefrontName;
        }

        // Share data with JSP page
        if (customer == null) {
            customer = getOrCreateCustomer(req, resp);
        }
        PageConfig initData = new PageConfig(s_storefrontName, pageTitle, pageName, pageData, customer, getMessages(req));
        req.setAttribute(ATTR_PAGE_CONFIG, initData);
        req.getSession().removeAttribute(SESSION_MESSAGES);

        // Render JSP page
        req.getRequestDispatcher("/WEB-INF/pages/" + pageName + ".jsp").forward(req, resp);
    }

    protected static void showCriticalErrorPage(HttpServletRequest req, HttpServletResponse resp, Exception ex) throws ServletException, IOException {
        addErrorMessage(req, ex);
        if (ex instanceof GenericJDBCException) {
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(req, MessageSeverity.INFO, "Tip:  Check that the NuoDB database is running.  The storefront is trying to connect to \""
                    + dbInfo.getUrl() + "\" with the username \"" + dbInfo.getUsername() + "\".");
        }
        Customer customer = (Customer) req.getAttribute(ATTR_CUSTOMER);
        showPage(req, resp, "Storefront Problem", "error", null, (customer == null) ? new Customer() : customer);

        s_logger.log(Level.WARNING, "Servlet handled critical error", ex);
    }

    /**
     * Adds a message prompting the user to seed the database with data if it currently contains 0 products and 0 categories.
     */
    protected static void addDataLoadMessage(HttpServletRequest req, SearchResult<Category> categoryList, SearchResult<Product> productList,
            Map<String, Object> productInfo) {
        if (categoryList.getResult().isEmpty() && productList.getResult().isEmpty()) {
            addMessage(
                    req,
                    MessageSeverity.INFO,
                    "There are no products in the database.  Click a button below to seed the database with some sample products and reviews.  Note that the loading process may take around 10 seconds.",
                    "Load 900 Real Products (with pictures served by Amazon.com)", "Generate 5,000 Fake Products (without pictures)");
            if (productInfo != null) {
                productInfo.put("hasData", false);
            }
        } else {
            if (productInfo != null) {
                productInfo.put("hasData", true);
                productInfo.put("productCount", productList.getTotalCount());
                productInfo.put("categoryCount", categoryList.getTotalCount());
            }
        }
    }

    protected static boolean handleDataLoadRequest(HttpServletRequest req) throws IOException {
        String btnAction = req.getParameter("btn-msg");
        if (btnAction == null) {
            return false;
        }

        btnAction = btnAction.toLowerCase();
        if (btnAction.contains("load")) {
            StorefrontApp.loadData();
            addMessage(req, MessageSeverity.INFO, "Product data loaded successfully.");
            s_logger.log(Level.INFO, "Product data loaded");
        } else if (btnAction.contains("generate")) {
            StorefrontApp.generateData();
            addMessage(req, MessageSeverity.INFO, "Product data generated successfully.");
            s_logger.log(Level.INFO, "Product data generated");
        } else if (btnAction.contains("remove")) {
            // Stop simulated workloads first; this prevents update conflicts
            getSimulator().removeAll();

            // Now remove all data
            try {
                StorefrontApp.removeData();
                s_logger.log(Level.INFO, "Product data removed");
            } catch (Exception e) {
                s_logger.log(Level.SEVERE, "Unable to remove product data", e);
            }
        }
        return true;
    }
}

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

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Message;
import com.nuodb.storefront.model.MessageSeverity;
import com.nuodb.storefront.model.PageConfig;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.service.IStorefrontService;

public abstract class BaseServlet extends HttpServlet {
    public static final String ATTR_PAGE_CONFIG = "pageConfig";
    public static final String SESSION_PRODUCT_FILTER = "productFilter";

    private static final String ATTR_CUSTOMER = "customer";
    private static final String COOKIE_CUSTOMER_ID = "customerId";
    private static final String SESSION_MESSAGES = "messages";
    private static final String SESSION_CUSTOMER_ID = "customerId";
    private static final int COOKIE_MAX_AGE_SEC = 60 * 60 * 24 * 31; // 1 month
    private static final long serialVersionUID = 1452096145544476070L;
    private static final IStorefrontService s_svc = StorefrontFactory.createStorefrontService();
    private static final String s_storefrontName = getService().getStorefrontStats(0).getStorefrontName();

    protected BaseServlet() {
    }

    public static IStorefrontService getService() {
        return s_svc;
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
        String errorMsg = e.getClass().getName() + ((e.getMessage() == null) ? "" : (":  " + e.getMessage()));
        addMessage(req, MessageSeverity.ERROR, errorMsg);
    }

    public static void addMessage(HttpServletRequest req, MessageSeverity severity, String message) {
        getMessages(req).add(new Message(severity, message));
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
        
        // Build full page title
        if (pageTitle == null || pageTitle.isEmpty()) {
            pageTitle = s_storefrontName;
        } else {
            pageTitle = pageTitle + " - " + s_storefrontName;
        }

        // Share data with JSP page
        Customer customer = getOrCreateCustomer(req, resp);
        PageConfig initData = new PageConfig(s_storefrontName, pageTitle, pageName, pageData, customer, getMessages(req));
        req.setAttribute(ATTR_PAGE_CONFIG, initData);
        req.getSession().removeAttribute(SESSION_MESSAGES);

        // Render JSP page
        req.getRequestDispatcher("/WEB-INF/pages/" + pageName + ".jsp").forward(req, resp);
    }
}

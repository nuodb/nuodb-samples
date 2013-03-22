package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
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
    public static final String ATTR_CUSTOMER = "customer";
    public static final String ATTR_PAGE_CONFIG = "pageConfig";
    public static final String SESSION_MESSAGES = "messages";
    public static final String SESSION_CUSTOMER_ID = "customerId";
    public static final String SESSION_PRODUCT_FILTER = "productFilter";

    private static final long serialVersionUID = 1452096145544476070L;
    private static final IStorefrontService svc = StorefrontFactory.createStorefrontService();

    protected BaseServlet() {
    }

    public static IStorefrontService getService() {
        return svc;
    }

    public static Customer getOrCreateCustomer(HttpServletRequest req) {
        Customer customer = (Customer) req.getAttribute(ATTR_CUSTOMER);
        if (customer == null) {
            Integer customerId = (Integer) req.getSession().getAttribute(SESSION_CUSTOMER_ID);
            if (customerId == null) {
                customerId = 0;
            }

            customer = getService().getOrCreateCustomer(customerId);
            req.getSession().setAttribute(SESSION_CUSTOMER_ID, customer.getId());
            req.setAttribute(ATTR_CUSTOMER, customer);
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

    protected static void showPage(HttpServletRequest req, HttpServletResponse resp, String pageName, Object pageData) throws ServletException,
            IOException {
        // Share data with JSP page
        Customer customer = getOrCreateCustomer(req);
        PageConfig initData = new PageConfig(pageName, pageData, customer, getMessages(req));
        req.setAttribute(ATTR_PAGE_CONFIG, initData);
        req.getSession().removeAttribute(SESSION_MESSAGES);

        // Render JSP page
        req.getRequestDispatcher("/WEB-INF/pages/" + pageName + ".jsp").forward(req, resp);
    }
}

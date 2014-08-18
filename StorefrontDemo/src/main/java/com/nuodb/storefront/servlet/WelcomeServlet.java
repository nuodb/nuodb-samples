/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.model.type.MessageSeverity;
import com.nuodb.storefront.service.IStorefrontService;

public class WelcomeServlet extends BaseServlet {
    private static final long serialVersionUID = 4369262156023258885L;
    private static final Logger s_logger = Logger.getLogger(WelcomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doHealthCheck(req);
        showPage(req, resp, "Welcome", "welcome", null, new Customer());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String btnAction = req.getParameter("btn-msg");
            if (btnAction != null) {
                btnAction = btnAction.toLowerCase();

                if (btnAction.contains("create")) {
                    DbConnInfo connInfo = StorefrontFactory.getDbConnInfo();
                    getDbApi().createDatabase(connInfo.getDbName(), connInfo.getUsername(), connInfo.getPassword(), connInfo.getTemplate());
                } else if (btnAction.contains("load")) {
                    StorefrontApp.loadData();
                    addMessage(req, MessageSeverity.INFO, "Product data loaded successfully.");
                    s_logger.info("Product data loaded");
                } else if (btnAction.contains("generate")) {
                    StorefrontApp.generateData();
                    addMessage(req, MessageSeverity.INFO, "Product data generated successfully.");
                    s_logger.info("Product data generated");
                } else if (btnAction.contains("remove")) {
                    // Now remove all data
                    try {
                        StorefrontApp.removeData();
                        s_logger.info("Product data removed");
                    } catch (Exception e) {
                        s_logger.error("Unable to remove product data", e);
                    }
                }
            }
        } catch (Exception ex) {
            addErrorMessage(req, ex);
        }

        doGet(req, resp);
    }

    protected void doHealthCheck(HttpServletRequest req) {
        try {
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
        } catch (GenericJDBCException e) {
            s_logger.warn("Servlet handled JDBC error", e);

            // Database may not exist. Inform the user
            DbConnInfo dbInfo = StorefrontFactory.getDbConnInfo();
            addMessage(req, MessageSeverity.INFO,
                    "The Storefront database may not yet exist.  The Storefront is trying to connect to \""
                            + dbInfo.getUrl() + "\" with the username \"" + dbInfo.getUsername() + "\".", "Create database");

        }
    }

    protected void checkForProducts(HttpServletRequest req) {
        // Fetch product data (and add a warning if the Storefront has no products yet)
        IStorefrontService svc = getStorefrontService();
        Map<String, Object> productInfo = new HashMap<String, Object>();
        SearchResult<Category> categoryList = svc.getCategories();
        SearchResult<Product> productList = svc.getProducts(new ProductFilter());
        if (categoryList.getResult().isEmpty() && productList.getResult().isEmpty()) {
            addMessage(
                    req,
                    MessageSeverity.INFO,
                    "There are no products in the database.  Click a button below to seed the database with some sample products and reviews.  Note that the loading process may take around 10 seconds.",
                    "Load 900 Real Products (with pictures served by Amazon.com)", "Generate 5,000 Fake Products (without pictures)");
            if (productInfo != null) {
                productInfo.put("hasData", false);
            }
        }
    }
}

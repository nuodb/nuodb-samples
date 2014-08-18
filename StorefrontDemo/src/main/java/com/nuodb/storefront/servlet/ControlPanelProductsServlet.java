/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

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

public class ControlPanelProductsServlet extends BaseServlet {
    private static final long serialVersionUID = -1224032390706203080L;
    private static final Logger s_logger = Logger.getLogger(ControlPanelProductsServlet.class.getName());

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Fetch product data (and add a warning if the Storefront has no products yet)
            IStorefrontService svc = getStorefrontService();
            SearchResult<Category> categoryList = svc.getCategories();
            SearchResult<Product> productList = svc.getProducts(new ProductFilter());
            checkForProducts(req); // FIXME: Consolidate
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("hasData", productList.getTotalCount() > 0);
            pageData.put("productCount", productList.getTotalCount());
            pageData.put("categoryCount", categoryList.getTotalCount());

            showPage(req, resp, "Control Panel", "control-panel-products", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String btnAction = req.getParameter("btn-msg");
            if (btnAction != null) {
                btnAction = btnAction.toLowerCase();
                doPostAction(req, resp, btnAction);
            }
        } catch (Exception ex) {
            addErrorMessage(req, ex);
        }

        doGet(req, resp);
    }
    
    protected void doPostAction(HttpServletRequest req, HttpServletResponse resp, String btnAction) throws IOException {
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

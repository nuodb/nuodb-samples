/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.api.StatsApi;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;

public class ControlPanelServlet extends BaseServlet {
    private static final long serialVersionUID = 89888972347145111L;

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Fetch data the page needs
            Map<String, Object> productInfo = new HashMap<String, Object>();
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("stats", StatsApi.getStorefrontStatsReport(null, true));
            pageData.put("productInfo", productInfo);

            // Also add a warning if the Storefront has no products yet
            SearchResult<Category> categoryList = getService().getCategories();
            SearchResult<Product> productList = getService().getProducts(new ProductFilter());
            addDataLoadMessage(req, categoryList, productList, productInfo);

            showPage(req, resp, "Control Panel", "control-panel", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    /**
     * POST:  Handle data load requests, then display control panel as normal
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleDataLoadRequest(req);
            doGet(req, resp);
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

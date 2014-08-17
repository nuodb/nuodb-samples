/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet.controlpanel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.DbNode;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.StorefrontStatsReport;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;
import com.nuodb.storefront.servlet.StorefrontWebApp;

public class UsersServlet extends BaseServlet {
    private static final long serialVersionUID = 8435653806520224541L;

    /**
     * GET: Shows the control panel screen, including the list of simulated workloads.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Fetch store stats
            StorefrontStatsReport stats = getSimulator().getStorefrontStatsReport(null, true);

            // Fetch product data (and add a warning if the Storefront has no products yet)
            IStorefrontService svc = getStorefrontService();
            Map<String, Object> productInfo = new HashMap<String, Object>();
            SearchResult<Category> categoryList = svc.getCategories();
            SearchResult<Product> productList = svc.getProducts(new ProductFilter());
            addDataLoadMessage(req, categoryList, productList, productInfo);

            // Fetch nodes data
            List<DbNode> dbNodes = getDbApiService().getDbNodes();

            // Fetch data the page needs
            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("stats", stats);
            pageData.put("productInfo", productInfo);
            pageData.put("dbNodes", dbNodes);
            pageData.put("isConsoleLocal", StorefrontWebApp.isConsoleLocal());

            showPage(req, resp, "Control Panel", "control-panel/users", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }

    /**
     * POST: Handle data load requests, then display control panel as normal
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
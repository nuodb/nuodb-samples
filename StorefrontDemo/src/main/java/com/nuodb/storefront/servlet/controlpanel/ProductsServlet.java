/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet.controlpanel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.servlet.BaseServlet;

public class ProductsServlet extends BaseServlet {
    private static final long serialVersionUID = -1224032390706203080L;

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

            Map<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("hasData", productList.getTotalCount() > 0);
            pageData.put("productCount", productList.getTotalCount());
            pageData.put("categoryCount", categoryList.getTotalCount());

            showPage(req, resp, "Control Panel", "control-panel/products", pageData, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

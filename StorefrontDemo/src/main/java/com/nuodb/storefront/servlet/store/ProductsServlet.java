/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.servlet.BaseServlet;

public class ProductsServlet extends BaseServlet {
    private static final long serialVersionUID = 4369262156023258885L;

    /**
     * GET: Shows the list of products. If a search param is specified, it's used as matchText in the product filter.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Prepare filters
            ProductFilter filter = getOrCreateProductFilter(req);
            filter.setMatchText(req.getParameter("search"));
            filter.setPage(1);
            String[] categories = req.getParameterValues("categories");
            if (categories != null) {
                filter.setCategories(new ArrayList<String>());
                for (String category : categories) {
                    if (category != null && !category.isEmpty()) {
                        filter.getCategories().add(category);
                    }
                }
            }

            // Fetch initial products
            Map<String, Object> pageData = new HashMap<String, Object>();
            SearchResult<Category> categoryList = getStorefrontService().getCategories();
            SearchResult<Product> productList = getStorefrontService().getProducts(filter);
            pageData.put("products", productList);
            pageData.put("categories", categoryList);
            pageData.put("filter", filter);

            showPage(req, resp, null, "products", pageData);
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

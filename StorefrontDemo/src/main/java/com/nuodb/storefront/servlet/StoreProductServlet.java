/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.exception.ProductNotFoundException;
import com.nuodb.storefront.model.entity.Product;

public class StoreProductServlet extends BaseServlet {
    private static final long serialVersionUID = 7440733613054861406L;

    /**
     * GET: Show product information based on the "productId" parameter. Redirect to the products page if information could not be fetched.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int productId = Integer.valueOf(req.getParameter("productId"));
            Product product = getStorefrontService().getProductDetails(productId);
            showPage(req, resp, product.getName(), "store-product", product);
        } catch (ProductNotFoundException ex) {
            addErrorMessage(req, ex);
            resp.sendRedirect("store-products");
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

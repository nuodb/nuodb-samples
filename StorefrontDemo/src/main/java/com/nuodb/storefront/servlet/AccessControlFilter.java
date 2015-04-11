/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.exception.TenantNotFoundException;

/**
 * Filter to permit CORS requests (AJAX requests from other domains) for data aggregation across regions/instances
 */
public class AccessControlFilter implements Filter {
    public AccessControlFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            StorefrontTenantManager.getTenant((HttpServletRequest) request).startUp();
        } catch (TenantNotFoundException e) {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }

        HttpServletResponse httpResp = (HttpServletResponse) response;
        httpResp.addHeader("Access-Control-Allow-Origin", "*");
        httpResp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        httpResp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

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
import com.nuodb.storefront.model.dto.PageConfig;
import com.nuodb.storefront.util.RequestUtil;

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
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            StorefrontTenantManager.getTenant(req).startUp();
        } catch (TenantNotFoundException e) {
            HttpServletResponse resp = (HttpServletResponse) response;
            if (RequestUtil.isApiRequest(req)) {
                // API request for tenant failed -- just send error code
                resp.sendError(HttpServletResponse.SC_GONE, e.getMessage());
            } else {
                // UI request for tenant failed -- send detailed error page
                request.setAttribute(BaseServlet.ATTR_PAGE_CONFIG, new PageConfig());
                request.setAttribute(BaseServlet.ATTR_TENANT, e.getTenantName());
                request.setAttribute(BaseServlet.ATTR_BASE_HREF, RequestUtil.getBaseHref(req));
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.getRequestDispatcher("/WEB-INF/pages/error-tenant.jsp").forward(request, response);
            }
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

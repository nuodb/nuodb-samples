package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class AccessControlFilter implements Filter {
    public AccessControlFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) response;
        httpResp.addHeader("Access-Control-Allow-Origin", "*");
        httpResp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        httpResp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

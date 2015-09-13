/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.util.NetworkUtil;

public class StorefrontWebApp implements ServletContextListener {
    private static final String CONTEXT_INIT_PARAM_PUBLIC_URL = "storefront.publicUrl";
    private static final String CONTEXT_INIT_PARAM_LAZY_LOAD = "storefront.lazyLoad";

    private static boolean s_initialized = false;
    private static String s_webAppUrlTemplate;
    private static String s_hostname;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (s_initialized) {
            // Context might be reinitialized due to code edits -- don't reinitialize hearbeat service, though
            return;
        }

        ServletContext context = sce.getServletContext();

        // Get external URL of this web app
        initWebAppUrl(context);

        // Initialize heartbeat service
        if (!isInitParameterTrue(CONTEXT_INIT_PARAM_LAZY_LOAD, context, false)) {
            StorefrontTenantManager.getDefaultTenant().startUp();
        }

        s_initialized = true;
    }

    protected static boolean isInitParameterTrue(String name, ServletContext context, boolean defaultValue) {
        String val = context.getInitParameter(name);
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        }
        return (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop sending heartbeats
        for (IStorefrontTenant tenant : StorefrontTenantManager.getAllTenants()) {
            tenant.shutDown();
        }
    }

    public static void updateWebAppUrl(HttpServletRequest req) {
        updateWebAppUrl(req.isSecure(), req.getHeader("HOST").split(":")[0], req.getServerPort(), req.getServletContext().getContextPath());
    }

    public static void initWebAppUrl(ServletContext context) {
        // Get URL from command line argument
        s_webAppUrlTemplate = StorefrontApp.DEFAULT_URL;
        if (StringUtils.isEmpty(s_webAppUrlTemplate)) {
            s_webAppUrlTemplate = context.getInitParameter(CONTEXT_INIT_PARAM_PUBLIC_URL);
            if (StringUtils.isEmpty(s_webAppUrlTemplate)) {
                s_webAppUrlTemplate = StorefrontApp.DEFAULT_URL;
            }
        }

        // Guess port
        int port = StorefrontApp.DEFAULT_PORT;

        // Get context path
        String contextPath = context.getContextPath();

        updateWebAppUrl(port == 443, NetworkUtil.getLocalIpAddress(), port, contextPath);
    }

    public static void updateWebAppUrl(boolean isSecure, String hostname, int port, String contextPath) {
        if (s_hostname != null && ("localhost".equals(hostname) || "127.0.0.1".equals(hostname) || "::1".equals(hostname))) {
            // Not helpful to update to a local address
            hostname = s_hostname;
        } else {
            s_hostname = hostname;
        }

        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "";
        } else if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }

        String url = s_webAppUrlTemplate
                .replace("{protocol}", isSecure ? "https" : "http")
                .replace("{host}", hostname)
                .replace("{port}", String.valueOf(port))
                .replace("{context}", contextPath);

        if (url.endsWith("/")) {
            // Don't want a trailing slash
            url = url.substring(0, url.length() - 1);
        }
        
        for (IStorefrontTenant tenant : StorefrontTenantManager.getAllTenants()) {
            tenant.getAppInstance().setUrl(url);            
        }        
    }
}

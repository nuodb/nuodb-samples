/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import com.nuodb.impl.util.StringUtils;
import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.service.IHeartbeatService;

public class StorefrontWebApp implements ServletContextListener {
    private static ScheduledExecutorService executor;
    private static int s_port;
    private static IHeartbeatService heartbeatSvc;
    private static final String ENV_PROP_REGION = "storefront.region";
    private static final String ENV_PROP_URL = "storefront.url";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (executor != null) {
            // Context might be reinitialized due to code edits -- don't reinitialize hearbeat service, though
            return;
        }

        // Get external URL of this web app
        String url = getWebAppUrl(sce.getServletContext(), StorefrontApp.DEFAULT_PORT);
        
        // Handle region override (if provided) 
        String region = System.getProperty(ENV_PROP_REGION);
        if (!StringUtils.isEmpty(region)) {
            StorefrontApp.APP_INSTANCE.setRegion(region);
            StorefrontApp.APP_INSTANCE.setRegionOverride(true);
        }

        // Initiate heartbeat service
        executor = Executors.newSingleThreadScheduledExecutor();
        heartbeatSvc = StorefrontFactory.createHeartbeatService(url);
        executor.scheduleAtFixedRate(heartbeatSvc, StorefrontApp.HEARTBEAT_INTERVAL_SEC, StorefrontApp.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

        // Send a heartbeat immediately to ensure AppInstance is initialized before any API or web request is processed
        heartbeatSvc.run();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop sending heartbeats
        executor.shutdown();
    }

    public static void updateWebAppUrl(HttpServletRequest req) {
        if (s_port == req.getServerPort()) {
            // URL is up to date
            return;
        }

        // Update URL
        StorefrontApp.APP_INSTANCE.setUrl(getWebAppUrl(req.getServletContext(), req.getServerPort()));

        if (heartbeatSvc != null) {
            // Save changes
            heartbeatSvc.run();
        }
    }

    public static String getWebAppUrl(ServletContext context, int port) {
        // Remember port
        s_port = port;

        // Get URL from command line argument
        String url = System.getProperty(ENV_PROP_URL);
        if (StringUtils.isEmpty(url)) {
            url = context.getInitParameter("public-url");
            if (StringUtils.isEmpty(url)) {
                url = StorefrontApp.DEFAULT_URL;
            }
        }

        // Get IP address
        String ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "localhost";
        }

        // Get context path
        String contextPath = context.getContextPath();
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "";
        }
        else if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }

        return url.replace("{host}", ipAddress).replace("{port}", String.valueOf(port)).replace("{context}", contextPath);
    }
}

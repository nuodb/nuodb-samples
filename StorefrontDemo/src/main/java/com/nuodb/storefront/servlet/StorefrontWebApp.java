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
    private static final String ENV_PROP_REGION = "storefront.region";
    private static final String ENV_PROP_URL = "storefront.url";
    private static final String ENV_MAVEN_TOMCAT_PORT = "maven.tomcat.port";
    private static final String CONTEXT_INIT_PARAM_PUBLIC_URL = "storefront.publicUrl";
    private static final String CONTEXT_INIT_PARAM_LAZY_LOAD = "storefront.lazyLoad";

    private static ScheduledExecutorService executor;
    private static int s_port;
    private static IHeartbeatService heartbeatSvc;
    private static final Object heartbeatSvcLock = new Object();
    private static boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (initialized) {
            // Context might be reinitialized due to code edits -- don't reinitialize hearbeat service, though
            return;
        }

        // Get external URL of this web app
        ServletContext context = sce.getServletContext();
        String url = buildWebAppUrl(context, guessWebAppPort());
        StorefrontApp.APP_INSTANCE.setUrl(url);
        
        // Handle region override (if provided)
        String region = System.getProperty(ENV_PROP_REGION);
        if (!StringUtils.isEmpty(region)) {
            StorefrontApp.APP_INSTANCE.setRegion(region);
            StorefrontApp.APP_INSTANCE.setRegionOverride(true);
        }

        // Initiate heartbeat service
        executor = Executors.newSingleThreadScheduledExecutor();
        String lazyLoad = context.getInitParameter(CONTEXT_INIT_PARAM_LAZY_LOAD);
        if (StringUtils.isEmpty(lazyLoad) || (!lazyLoad.equalsIgnoreCase("true") && !lazyLoad.equals("1"))) {
            initHeartbeatService();
        }

        initialized = true;
    }
    
    public static void initHeartbeatService() {
        synchronized (heartbeatSvcLock) {
            if (heartbeatSvc == null) {
                heartbeatSvc = StorefrontFactory.createHeartbeatService();
                executor.scheduleAtFixedRate(heartbeatSvc, 0, StorefrontApp.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop sending heartbeats
        executor.shutdown();
    }
    
    public static void updateWebAppPort(HttpServletRequest req) {
        if (s_port == req.getServerPort()) {
            // URL is up to date
            return;
        }

        // Update URL
        StorefrontApp.APP_INSTANCE.setUrl(buildWebAppUrl(req.getServletContext(), req.getServerPort()));
    }

    public static String buildWebAppUrl(ServletContext context, int port) {
        // Remember port
        s_port = port;

        // Get URL from command line argument
        String url = System.getProperty(ENV_PROP_URL);
        if (StringUtils.isEmpty(url)) {
            url = context.getInitParameter(CONTEXT_INIT_PARAM_PUBLIC_URL);
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

    private static int guessWebAppPort() {
        String portStr = System.getProperty(ENV_MAVEN_TOMCAT_PORT);
        if (!StringUtils.isEmpty(portStr)) {
            try {

                return Integer.valueOf(portStr).intValue();
            } catch (Exception e) {
            }
        }
        return StorefrontApp.DEFAULT_PORT;
    }
}

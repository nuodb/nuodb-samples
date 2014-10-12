/* Copyright (c) 2013-2014 NuoDB, Inc. */

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

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.util.PerformanceUtil;

public class StorefrontWebApp implements ServletContextListener {
    private static final String ENV_PROP_REGION = "storefront.region";
    private static final String ENV_PROP_URL = "storefront.url";
    private static final String ENV_MAVEN_TOMCAT_PORT = "maven.tomcat.port";
    private static final String CONTEXT_INIT_PARAM_PUBLIC_URL = "storefront.publicUrl";
    private static final String CONTEXT_INIT_PARAM_LAZY_LOAD = "storefront.lazyLoad";

    private static ScheduledExecutorService s_executor;
    private static IHeartbeatService s_heartbeatSvc;
    private static final Object s_heartbeatSvcLock = new Object();
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
        s_executor = Executors.newSingleThreadScheduledExecutor();

        // Get external URL of this web app
        initWebAppUrl(context);

        // Handle region override (if provided)
        String region = System.getProperty(ENV_PROP_REGION);
        if (!StringUtils.isEmpty(region)) {
            StorefrontApp.APP_INSTANCE.setRegion(region);
            StorefrontApp.APP_INSTANCE.setRegionOverride(true);
        }

        // Initialize heartbeat service
        if (!isInitParameterTrue(CONTEXT_INIT_PARAM_LAZY_LOAD, context, false)) {
            initHeartbeatService();
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

    public static void initHeartbeatService() {
        synchronized (s_heartbeatSvcLock) {
            if (s_heartbeatSvc == null) {
                s_heartbeatSvc = StorefrontFactory.createHeartbeatService();
                s_executor.scheduleAtFixedRate(s_heartbeatSvc, 0, StorefrontApp.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
                
                Runnable sampler = PerformanceUtil.createSampler();
                if (sampler != null) {
                    s_executor.scheduleAtFixedRate(sampler, 0, StorefrontApp.CPU_SAMPLING_INTERVAL_SEC, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop sending heartbeats
        s_executor.shutdown();
    }

    public static void updateWebAppUrl(HttpServletRequest req) {
        updateWebAppUrl(req.isSecure(), req.getHeader("HOST").split(":")[0], req.getServerPort(), req.getServletContext().getContextPath());
    }

    public static void initWebAppUrl(ServletContext context) {
        // Get URL from command line argument
        s_webAppUrlTemplate = System.getProperty(ENV_PROP_URL);
        if (StringUtils.isEmpty(s_webAppUrlTemplate)) {
            s_webAppUrlTemplate = context.getInitParameter(CONTEXT_INIT_PARAM_PUBLIC_URL);
            if (StringUtils.isEmpty(s_webAppUrlTemplate)) {
                s_webAppUrlTemplate = StorefrontApp.DEFAULT_URL;
            }
        }
        
        // Guess port
        String portStr = System.getProperty(ENV_MAVEN_TOMCAT_PORT);
        int port = StorefrontApp.DEFAULT_PORT;
        if (!StringUtils.isEmpty(portStr)) {
            try {
                port = Integer.valueOf(portStr).intValue();
            } catch (Exception e) {
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

        updateWebAppUrl(port == 443, ipAddress, port, contextPath);
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

        StorefrontApp.APP_INSTANCE.setUrl(s_webAppUrlTemplate
                .replace("{protocol}", isSecure ? "https" : "http")
                .replace("{host}", hostname)
                .replace("{port}", String.valueOf(port))
                .replace("{context}", contextPath));
    }
}

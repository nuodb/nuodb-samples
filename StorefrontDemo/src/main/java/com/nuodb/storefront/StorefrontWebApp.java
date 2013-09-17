package com.nuodb.storefront;

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
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.storefront.HeartbeatService;

public class StorefrontWebApp implements ServletContextListener {
    private static ScheduledExecutorService executor;
    private static final int DEFAULT_PORT = 8080;
    private static int s_port;
    private static IHeartbeatService heartbeatSvc;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (executor != null) {
            // Context might be reinitialized due to code edits -- don't reinitialize hearbeat service, though
            return;
        }

        // Get external URL of this web app
        String url = getWebAppUrl(sce.getServletContext(), DEFAULT_PORT);

        // Initiate heartbeat service
        executor = Executors.newSingleThreadScheduledExecutor();
        heartbeatSvc = StorefrontFactory.createHeartbeatService(url);
        executor.scheduleAtFixedRate(heartbeatSvc, HeartbeatService.HEARTBEAT_INTERVAL_SEC, HeartbeatService.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

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

        // FIXME:  For debugging only
        StorefrontApp.APP_INSTANCE.setRegion(((s_port % 2) == 0) ? "US/East" : "US/West");
        
        if (heartbeatSvc != null) {
            // Save changes
            heartbeatSvc.run();
        }
    }

    public static String getWebAppUrl(ServletContext context, int port) {
        s_port = port;
        
        // Get IP address
        String ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "localhost";
        }
        String url = context.getInitParameter("public-url");
        if (StringUtils.isEmpty(url)) {
            url = "http://{ipAddress}:{port}/{context}";
        }

        // Get context path
        String contextPath = context.getContextPath();
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "";
        }
        else if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }

        return url.replace("{ipAddress}", ipAddress).replace("{port}", String.valueOf(port)).replace("{contextPath}", contextPath);
    }
}

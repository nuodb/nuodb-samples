package com.nuodb.storefront;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.nuodb.storefront.service.IHeartbeatService;

public class StorefrontWebApp implements ServletContextListener {
    private static final int HEARTBEAT_INTERVAL_SEC = 60;
    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String url = sce.getServletContext().getInitParameter("public-url"); // FIXME: come up with reasonable default
        executor = Executors.newSingleThreadScheduledExecutor();
        IHeartbeatService heartbeatSvc = StorefrontFactory.createHeartbeatService(url);
        executor.scheduleAtFixedRate(heartbeatSvc, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

        // Run immediately (synchronously) to send the first heartbeat.
        // This way, the AppInstance is initialized before any API or web request is processed.
        heartbeatSvc.run();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop sending heartbeats
        executor.shutdown();
    }
}

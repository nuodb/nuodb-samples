package com.nuodb.storefront;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StorefrontWebApp implements ServletContextListener {
    private static final int HEARTBEAT_INTERVAL_SEC = 60;
    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String url = sce.getServletContext().getInitParameter("public-url"); // FIXME: come up with reasonable default
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(StorefrontFactory.createHeartbeatService(url), 0, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
    }
}

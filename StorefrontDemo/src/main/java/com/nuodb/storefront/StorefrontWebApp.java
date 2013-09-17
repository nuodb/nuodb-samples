package com.nuodb.storefront;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.storefront.HeartbeatService;

public class StorefrontWebApp implements ServletContextListener {
    private static ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (executor != null) {
            // Context might be reinitialized due to code edits -- don't reinitialize hearbeat service, though
            return;
        }
        
        String url = sce.getServletContext().getInitParameter("public-url"); // FIXME: come up with reasonable default
        executor = Executors.newSingleThreadScheduledExecutor();
        IHeartbeatService heartbeatSvc = StorefrontFactory.createHeartbeatService(url);
        executor.scheduleAtFixedRate(heartbeatSvc, HeartbeatService.HEARTBEAT_INTERVAL_SEC, HeartbeatService.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

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

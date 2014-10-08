/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.dto.DbRegionInfo;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.simulator.SimulatorService;
import com.nuodb.storefront.util.PerformanceUtil;

public class HeartbeatService implements IHeartbeatService {
    private static final Logger s_log = Logger.getLogger(SimulatorService.class.getName());
    private int secondsUntilNextPurge = 0;
    private int consecutiveFailureCount = 0;

    static {
        StorefrontDao.registerTransactionNames(new String[] { "sendHeartbeat" });
    }

    public HeartbeatService() {
    }

    @Override
    public void run() {
        try {
            final IStorefrontDao dao = StorefrontFactory.createStorefrontDao();
            dao.runTransaction(TransactionType.READ_WRITE, "sendHeartbeat", new Runnable() {
                @Override
                public void run() {
                    Calendar now = Calendar.getInstance();
                    AppInstance appInstance = StorefrontApp.APP_INSTANCE;
                    secondsUntilNextPurge -= StorefrontApp.HEARTBEAT_INTERVAL_SEC;

                    if (appInstance.getFirstHeartbeat() == null) {
                        appInstance.setFirstHeartbeat(now);
                        appInstance.setLastApiActivity(now);
                    }

                    // Send the heartbeat with the latest "last heartbeat time"
                    appInstance.setCpuUtilization(PerformanceUtil.getAvgCpuUtilization());
                    appInstance.setLastHeartbeat(now);
                    if (!appInstance.getRegionOverride()) {
                        DbRegionInfo region = dao.getCurrentDbNodeRegion();
                        appInstance.setRegion(region.regionName);
                        appInstance.setNodeId(region.nodeId);
                    }
                    dao.save(StorefrontApp.APP_INSTANCE); // this will create or update as appropriate

                    // If enough time has elapsed, also delete rows of instances that are no longer sending heartbeats
                    if (secondsUntilNextPurge <= 0) {
                        Calendar maxLastHeartbeat = Calendar.getInstance();
                        maxLastHeartbeat.add(Calendar.SECOND, -StorefrontApp.MIN_INSTANCE_PURGE_AGE_SEC);
                        dao.deleteDeadAppInstances(maxLastHeartbeat);
                        secondsUntilNextPurge = StorefrontApp.PURGE_FREQUENCY_SEC;
                    }

                    // If interactive user has left the app, shut down any active workloads
                    Calendar idleThreshold = Calendar.getInstance();
                    idleThreshold.add(Calendar.SECOND, -StorefrontApp.STOP_USERS_AFTER_IDLE_UI_SEC);
                    if (appInstance.getLastApiActivity().before(idleThreshold)) {
                        // Don't do any heavy lifting if there are no simulated workloads in progress
                        int activeWorkerCount = StorefrontFactory.getSimulatorService().getActiveWorkerLimit();
                        if (activeWorkerCount > 0) {
                            // Check for idleness across *all* instances
                            if (dao.getActiveAppInstanceCount(idleThreshold) == 0) {
                                s_log.info("Stopping all " + activeWorkerCount + " simulated users due to idle app instances.");
                                StorefrontFactory.getSimulatorService().stopAll();
                            }
                        }
                    }

                    consecutiveFailureCount = 0;
                }
            });
        } catch (Exception e) {
            if (++consecutiveFailureCount == 1) {
                s_log.error("Unable to send heartbeat", e);
            }
        }
    }
}

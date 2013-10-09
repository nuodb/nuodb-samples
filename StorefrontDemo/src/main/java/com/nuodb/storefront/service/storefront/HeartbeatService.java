/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.simulator.SimulatorService;
import com.nuodb.storefront.util.PerformanceUtil;

public class HeartbeatService implements IHeartbeatService {
    private static final Logger s_log = Logger.getLogger(SimulatorService.class.getName());
    private final String appUrl;
    private int secondsUntilNextPurge = 0;
    private int consecutiveFailureCount = 0;

    static {
        StorefrontDao.registerTransactionNames(new String[] { "sendHeartbeat" });
    }

    public HeartbeatService(String appUrl) {
        this.appUrl = appUrl;
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
                        appInstance.setUrl(appUrl);
                    }

                    // Send the heartbeat with the latest "last heartbeat time"
                    appInstance.setCpuUtilization(PerformanceUtil.getCpuUtilization());
                    appInstance.setLastHeartbeat(now);
                    dao.save(StorefrontApp.APP_INSTANCE); // this will create or update as appropriate

                    // If enough time has elapsed, also delete rows of instances that are no longer sending heartbeats
                    if (secondsUntilNextPurge <= 0) {
                        Calendar maxLastHeartbeat = Calendar.getInstance();
                        maxLastHeartbeat.add(Calendar.SECOND, -StorefrontApp.MIN_INSTANCE_PURGE_AGE_SEC);
                        dao.deleteDeadAppInstances(maxLastHeartbeat);
                        secondsUntilNextPurge = StorefrontApp.PURGE_FREQUENCY_SEC;
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

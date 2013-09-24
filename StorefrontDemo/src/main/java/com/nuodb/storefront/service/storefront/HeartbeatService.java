package com.nuodb.storefront.service.storefront;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static final int HEARTBEAT_INTERVAL_SEC = 60;
    public static final int PURGE_FREQUENCY_SEC = 60 * 30; // 30 min
    public static final int MAX_HEARTBEAT_AGE_SEC = 90;
    public static final int MIN_INSTANCE_PURGE_AGE_SEC = 60 * 60; // 1 hour

    private static final Logger s_log = Logger.getLogger(SimulatorService.class.getName());
    private final String appUrl;
    private int secondsUntilNextPurge = 0;

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
                    secondsUntilNextPurge -= HEARTBEAT_INTERVAL_SEC;

                    if (appInstance.getFirstHeartbeat() == null) {
                        appInstance.setFirstHeartbeat(now);
                        appInstance.setUrl(appUrl);
                    }

                    // Send the heartbeat by updating the "last heartbeat time"
                    appInstance.setCpuUtilization(PerformanceUtil.getCpuUtilization());
                    appInstance.setLastHeartbeat(now);
                    dao.save(StorefrontApp.APP_INSTANCE); // this will create or update as appropriate

                    // If enough time has elapsed, also delete rows of instances that are no longer sending heartbeats
                    if (secondsUntilNextPurge <= 0) {
                        secondsUntilNextPurge = PURGE_FREQUENCY_SEC;
                        Calendar maxLastHeartbeat = Calendar.getInstance();
                        maxLastHeartbeat.add(Calendar.SECOND, -MIN_INSTANCE_PURGE_AGE_SEC);
                        dao.deleteDeadAppInstances(maxLastHeartbeat);
                    }
                }
            });
        } catch (Exception e) {
            s_log.log(Level.SEVERE, "Unable to send heartbeat", e);
        }
    }
}

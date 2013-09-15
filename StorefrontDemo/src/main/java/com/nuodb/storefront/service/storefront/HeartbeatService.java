package com.nuodb.storefront.service.storefront;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.AppInstance;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.simulator.SimulatorService;

public class HeartbeatService implements IHeartbeatService {
    private static final Logger s_log = Logger.getLogger(SimulatorService.class.getName());
    private final String appUrl;

    static {
        StorefrontDao.registerTransactionNames(new String[] { "sendHeartbeat" });
    }

    public HeartbeatService(String appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public void run() {
        s_log.info("Sending hearbeat");

        try {
            final IStorefrontDao dao = StorefrontFactory.createStorefrontDao();
            dao.runTransaction(TransactionType.READ_WRITE, "sendHeartbeat", new Runnable() {
                @Override
                public void run() {

                    Calendar now = Calendar.getInstance();
                    AppInstance appInstance = StorefrontApp.APP_INSTANCE;

                    if (appInstance.getFirstHeartbeat() == null) {
                        appInstance.setFirstHeartbeat(now);
                        appInstance.setUrl(appUrl);
                    }
                    appInstance.setCpuUtilization(0); // TODO: Detect utilization using SIGAR library
                    appInstance.setLastHeartbeat(now);

                    dao.save(StorefrontApp.APP_INSTANCE); // this will create or update as appropriate
                }
            });
        } catch (Exception e) {
            s_log.log(Level.SEVERE, "Unable to send heartbeat", e);
        }
    }
}

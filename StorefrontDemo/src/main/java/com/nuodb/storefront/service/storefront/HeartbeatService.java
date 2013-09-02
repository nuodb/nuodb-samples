package com.nuodb.storefront.service.storefront;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.service.simulator.SimulatorService;

public class HeartbeatService implements Runnable {
    private static final Logger s_log = Logger.getLogger(SimulatorService.class.getName());
    private final IStorefrontDao dao;
    private final String appUrl;

    static {
        StorefrontDao.registerTransactionNames(new String[] { "sendHeartbeat" });
    }

    public HeartbeatService(IStorefrontDao dao, String appUrl) {
        this.dao = dao;
        this.appUrl = appUrl;
    }

    @Override
    public void run() {
        s_log.info("Sending hearbeat");

        try {
            dao.runTransaction(TransactionType.READ_WRITE, "sendHeartbeat", new Runnable() {
                @Override
                public void run() {
                    dao.sendHeartbeat(appUrl);
                }
            });
        } catch (Exception e) {
            s_log.log(Level.SEVERE, "Unable to send heartbeat", e);
        }
    }
}

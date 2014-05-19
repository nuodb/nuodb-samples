/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import org.apache.log4j.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.model.type.Currency;
import com.nuodb.storefront.util.StringUtils;

/**
 * Performs one-time initialization of this Storefront's {@link StorefrontApp#APP_INSTANCE} singleton when a Hibernate session factory is created.
 */
public class AppInstanceInitService implements Runnable {
    private static final Logger s_log = Logger.getLogger(AppInstanceInitService.class.getName());
    private final IStorefrontDao dao;

    public AppInstanceInitService(IStorefrontDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        // Init region name
        if (!StorefrontApp.APP_INSTANCE.getRegionOverride()) {
            String region;
            try {
                region = dao.getCurrentDbNodeRegion();
            } catch (Exception e) {
                region = null;
            }
            
            if (StringUtils.isEmpty(region)) {
                s_log.warn("Your database version does not support regions.  Upgrade to NouDB 2.0 or greater.");
            } else if (!region.isEmpty()) {
                StorefrontApp.APP_INSTANCE.setRegion(region);
            }
        }

        // Init currency
        Currency currency = dao.getRegionCurrency(StorefrontApp.APP_INSTANCE.getRegion());
        if (currency != null) {
            StorefrontApp.APP_INSTANCE.setCurrency(currency);
        }
    }
}

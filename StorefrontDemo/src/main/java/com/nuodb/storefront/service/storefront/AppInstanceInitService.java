/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import org.apache.log4j.Logger;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.type.Currency;
import com.nuodb.storefront.util.StringUtils;

/**
 * Performs initialization of an {@class AppInstance} when a Hibernate session factory is created.
 */
public class AppInstanceInitService {
    private static final Logger s_log = Logger.getLogger(AppInstanceInitService.class.getName());
    private final IStorefrontDao dao;

    public AppInstanceInitService(IStorefrontDao dao) {
        this.dao = dao;
    }

    public void init(final AppInstance app) {
        dao.runTransaction(TransactionType.READ_ONLY, null, new Runnable() {
            @Override
            public void run() {
                // Init currency
                Currency currency = dao.getRegionCurrency(app.getRegion());
                if (currency != null) {
                    app.setCurrency(currency);
                }

                // Init region name
                if (!app.getRegionOverride()) {
                    String region;
                    try {
                        region = dao.getCurrentDbNodeRegion();
                    } catch (Exception e) {
                        region = null;
                    }

                    if (StringUtils.isEmpty(region)) {
                        s_log.warn("Your database version does not support regions.  Upgrade to NouDB 2.0 or greater.");
                    } else if (!region.isEmpty()) {
                        app.setRegion(region);
                    }
                }
            };
        });
    }
}

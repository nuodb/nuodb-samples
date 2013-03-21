package com.nuodb.storefront.api;

import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.service.IStorefrontService;

public abstract class BaseApi {
    private final IStorefrontService svc = StorefrontFactory.createStorefrontService();

    public IStorefrontService getService() {
        return svc;
    }

    public Customer getOrCreateCustomer(String customerIdStr) {
        int customerId;
        try {
            customerId = Integer.valueOf(customerIdStr);
        } catch (NumberFormatException e) {
            customerId = 0;
        }
        return getService().getOrCreateCustomer(customerId);
    }
}

/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class TenantNotFoundException extends StorefrontException {
    private static final long serialVersionUID = -47812367347893329L;

    public TenantNotFoundException(String tenantName) {
        super(Status.NOT_FOUND, "Tenant \"" + tenantName + "\" does not exist");
    }
}

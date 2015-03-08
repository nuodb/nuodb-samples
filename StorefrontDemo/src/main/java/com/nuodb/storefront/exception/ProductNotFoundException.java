/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class ProductNotFoundException extends StorefrontException {
    private static final long serialVersionUID = -4993028658518393329L;

    public ProductNotFoundException() {
        super(Status.NOT_FOUND);
    }
}

/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class CartEmptyException extends StorefrontException {
    private static final long serialVersionUID = 419056219488990089L;

    public CartEmptyException() {
        super(Status.BAD_REQUEST);
    }
}

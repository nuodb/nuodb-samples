/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class CustomerNotFoundException extends StorefrontException {
    private static final long serialVersionUID = -5604325601104375171L;

    public CustomerNotFoundException() {
        super(Status.NOT_FOUND);
    }
}

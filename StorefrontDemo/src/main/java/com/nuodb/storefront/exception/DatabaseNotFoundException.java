/* Copyright (c) 2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class DatabaseNotFoundException extends StorefrontException {
    private static final long serialVersionUID = -1633094091099365145L;

    public DatabaseNotFoundException() {
        super(Status.INTERNAL_SERVER_ERROR, "The Storefront database does not exist.");
    }
}

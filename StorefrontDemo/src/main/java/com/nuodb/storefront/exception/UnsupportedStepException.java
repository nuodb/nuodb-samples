/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

/**
 * The specified step isn't supported by the simulation worker class. An implementation may not be written yet.
 */
public class UnsupportedStepException extends StorefrontException {
    private static final long serialVersionUID = 3147731827773036813L;

    public UnsupportedStepException() {
        super(Status.SERVICE_UNAVAILABLE);
    }
}

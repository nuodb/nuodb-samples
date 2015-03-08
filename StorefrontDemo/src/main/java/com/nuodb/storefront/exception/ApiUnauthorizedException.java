/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Indicates credentials to the API are invalid.
 */
public class ApiUnauthorizedException extends ApiProxyException {
    private static final long serialVersionUID = 8713489247894511123L;

    public ApiUnauthorizedException(Throwable e) {
        super(Status.UNAUTHORIZED, null, e);
    }
}

/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Indicates a problem with an API request or the API server itself.
 */
public class ApiProxyException extends StorefrontException {
    private static final long serialVersionUID = 347845891781234711L;

    public ApiProxyException(Status errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

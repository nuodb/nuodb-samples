/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public abstract class StorefrontException extends RuntimeException {
    private static final long serialVersionUID = 2920676817885918406L;

    private final Status errorCode;

    public StorefrontException(Status errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public StorefrontException(Status errorCode, String message) {
        this(errorCode, message, null);
    }

    public StorefrontException(Status errorCode) {
        this(errorCode, null, null);
    }

    public Status getErrorCode() {
        return errorCode;
    }
}

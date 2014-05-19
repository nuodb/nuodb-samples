/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.service.nuoapi;

import javax.ws.rs.core.Response.Status;

public class NuoApiException extends RuntimeException {
    private static final long serialVersionUID = 347845891781234711L;

    private final Status errorCode;

    public NuoApiException(Throwable innerException) {
        super(innerException);
        errorCode = null;
    }

    public NuoApiException(Status errorCode) {
        this.errorCode = errorCode;
    }

    public Status getErrorCode() {
        return errorCode;
    }
}

/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Indicates the API server is experiencing a problem and temporarily unable to service the request.
 * This typically happens when the API needs some time to connect to the underlying NuoDB domain.
 * The request may succeed if be reattempted later.
 */
public class ApiUnavailableException extends ApiException {
    private static final long serialVersionUID = 178569023789676845L;

    public ApiUnavailableException(Throwable e) {
        super(Status.SERVICE_UNAVAILABLE, null, e);
    }
}

/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.client.ClientHandlerException;

/**
 * Indicates a connection could not be established with the API
 */
public class ApiConnectionException extends ApiProxyException {
    private static final long serialVersionUID = 3478458917234871234L;

    public ApiConnectionException(ClientHandlerException ex) {
        super(Status.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }
}

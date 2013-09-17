package com.nuodb.storefront.exception;

import javax.ws.rs.core.Response.Status;

public class DataValidationException extends StorefrontException {
    private static final long serialVersionUID = 2340925681791208724L;

    public DataValidationException(String message) {
        super(Status.BAD_REQUEST, message);
    }
}

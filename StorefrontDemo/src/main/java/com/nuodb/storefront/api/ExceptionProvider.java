/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.nuodb.storefront.exception.StorefrontException;
import com.nuodb.storefront.model.dto.Message;

@Provider
public class ExceptionProvider implements ExceptionMapper<RuntimeException> {
    //private static final Logger s_logger = Logger.getLogger(ExceptionProvider.class.getName());

    public ExceptionProvider() {
    }

    @Override
    public Response toResponse(RuntimeException exception) {
        Status errorCode;

        if (exception instanceof StorefrontException) {
            errorCode = ((StorefrontException) exception).getErrorCode();
        } else if (exception instanceof IllegalArgumentException) {
            errorCode = Status.BAD_REQUEST;
        } else {
            errorCode = Status.INTERNAL_SERVER_ERROR;
        }

        //s_logger.warn("API exception provider handling RuntimeException with HTTP status " + errorCode.getStatusCode(), exception);

        return Response.status(errorCode).entity(new Message(exception)).build();
    }
}

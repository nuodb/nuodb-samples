/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.nuodb.storefront.exception.CartEmptyException;
import com.nuodb.storefront.exception.CustomerNotFoundException;
import com.nuodb.storefront.exception.ProductNotFoundException;
import com.nuodb.storefront.model.Message;

@Provider
public class ExceptionProvider implements ExceptionMapper<RuntimeException> {
    private static final Logger s_logger = Logger.getLogger(ExceptionProvider.class.getName());

    public ExceptionProvider() {
    }

    @Override
    public Response toResponse(RuntimeException exception) {
        Status status;
        if (exception instanceof CustomerNotFoundException || exception instanceof ProductNotFoundException) {
            status = Status.NOT_FOUND;
        } else if (exception instanceof CartEmptyException || exception instanceof IllegalArgumentException) {
            status = Status.BAD_REQUEST;
        } else {
            status = Status.INTERNAL_SERVER_ERROR;
        }

        s_logger.log(Level.WARNING, "API exception provider handling RuntimeException with HTTP status " + status.getStatusCode(), exception);

        return Response.status(status).entity(new Message(exception)).build();
    }
}

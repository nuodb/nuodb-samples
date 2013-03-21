package com.nuodb.storefront.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.nuodb.storefront.exception.CartEmptyException;
import com.nuodb.storefront.exception.CustomerNotFoundException;
import com.nuodb.storefront.exception.ProductNotFoundException;

@Provider
public class ExceptionProvider implements ExceptionMapper<RuntimeException> {
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
        return Response.status(status).entity(exception.getClass().toString()).build();
    }
}

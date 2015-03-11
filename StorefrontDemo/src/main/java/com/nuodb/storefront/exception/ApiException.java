/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Indicates a problem with an API request or the API server itself.
 */
public class ApiException extends StorefrontException {
    private static final long serialVersionUID = 347845891781234711L;

    public ApiException(Status errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static ApiException toApiException(Exception e) {
        if (e instanceof ApiException) {
            return (ApiException)e;
        }

        if (e instanceof ClientHandlerException) {
            return new ApiConnectionException((ClientHandlerException)e);
        }

        if (e instanceof UniformInterfaceException) {
            ClientResponse response = ((UniformInterfaceException)e).getResponse();
            String msg = readResponseMessage(response);
            Status status = Status.fromStatusCode(response.getStatus());

            switch (status) {
                case UNAUTHORIZED:
                    return new ApiUnauthorizedException(e);

                case BAD_REQUEST:
                    if (msg.startsWith("Domain is not connected")) {
                        return new ApiUnavailableException(e);
                    }
                    // Otherwise fall through

                default:
                    return new ApiException(status, msg, e);
            }
        }

        return new ApiException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    public static String readResponseMessage(ClientResponse resp)
    {
        try {
            if (resp.getType() == MediaType.TEXT_PLAIN_TYPE) {
                InputStream in = resp.getEntityInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                in.close();
                String msg = out.toString();
                if (!msg.isEmpty()) {
                    return msg;
                }
            }

            int statusCode = resp.getStatus();
            Status status = Status.fromStatusCode(statusCode);
            String reason = (status != null) ? status.getReasonPhrase() : ("Code " + statusCode);
            return reason;
        } catch (IOException e) {
            return null;
        }
    }
}

/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

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
            return (ApiException) e;
        }

        if (e instanceof ClientHandlerException) {
            return new ApiConnectionException((ClientHandlerException) e);
        }

        if (e instanceof UniformInterfaceException) {
            ClientResponse response = ((UniformInterfaceException) e).getResponse();
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
            boolean isText = MediaType.TEXT_PLAIN.equals(resp.getType());
            boolean isJson = MediaType.APPLICATION_JSON_TYPE.equals(resp.getType());
            if (isText || isJson) {
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
                    if (isJson) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, String> errorObj = new ObjectMapper().readValue(msg, HashMap.class);
                            String errorObjMsg = errorObj.get("message");
                            if (!StringUtils.isEmpty(errorObjMsg)) {
                                String details = errorObj.get("details");
                                if (details != null) {
                                    errorObjMsg += ": " + details;                                     
                                }
                                return errorObjMsg;
                            }
                        } catch (Exception e) {
                            // Just return full message
                        }
                    }

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

/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.client.ClientResponse;

public class ApiProxyException extends StorefrontException {
    private static final long serialVersionUID = 347845891781234711L;

    public ApiProxyException(ClientResponse resp, Throwable cause) {
        super(Status.fromStatusCode(resp.getStatus()), readResponseMessage(resp), cause);
    }

    public ApiProxyException(Status errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    private static String readResponseMessage(ClientResponse resp)
    {
        try {
            InputStream in = resp.getEntityInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
            in.close();
            return out.toString() + " (response code " + resp.getStatus() + ")";
        } catch (IOException e) {
            return null;
        }
    }
}

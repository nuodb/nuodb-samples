/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet.tour;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.servlet.BaseServlet;

public class ContinousAvailabilityServlet extends BaseServlet {
    private static final long serialVersionUID = 8555464129927540059L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            showPage(req, resp, "Continous Availability", "tour/continuous-availability", null, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

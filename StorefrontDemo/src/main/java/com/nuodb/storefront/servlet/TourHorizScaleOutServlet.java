/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.dto.MessageLink;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

public class TourHorizScaleOutServlet extends BaseServlet {
    private static final long serialVersionUID = 8555464129927540059L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            if (getDbApi().getDbStats().hostCount <= 1) {
                addMessage(req, MessageSeverity.WARNING,
                        "You cannot see this feature in action because you have no additional hosts available. "
                                + "An easy way to run NuoDB with multiple hosts is to use the NuoDB AWS Script with your Amazon Web Services (AWS) account. "
                                + "NuoDB automatically provisions everything you need in the cloud.")
                                .setLink(new MessageLink("Get the script", "https://github.com/nuodb/cloudformation"));
            }

            showPage(req, resp, "Horizontal Scale-Out", "tour-horiz-scale-out", null, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

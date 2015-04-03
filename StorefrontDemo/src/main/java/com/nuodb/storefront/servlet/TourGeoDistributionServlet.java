/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.dto.MessageLink;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.type.MessageSeverity;

public class TourGeoDistributionServlet extends BaseServlet {
    private static final long serialVersionUID = -3681278405197531929L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (getDbApi(req).getDbFootprint().regionCount <= 1) {
                addMessage(req, MessageSeverity.WARNING,
                        "You cannot see Geo-Distribution in action because you have no additional regions available. "
                                + "An easy way to run NuoDB with multiple regions is to use the NuoDB AWS Quickstart kit with with your Amazon Web Services (AWS) account. "
                                + "NuoDB automatically provisions everything you need in the cloud.")
                                .setLink(new MessageLink("Get the kit", "https://github.com/nuodb/nuodbTools")); 
            }
                        
            showPage(req, resp, "Geo-Distribution", "tour-geo-distribution", null, new Customer());
        } catch (Exception ex) {
            showCriticalErrorPage(req, resp, ex);
        }
    }
}

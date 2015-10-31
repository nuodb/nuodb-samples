/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.dbapi;

import org.apache.log4j.Logger;

/**
 * Serves as a bridge between Jersey's LoggingFilter (which uses java.util logging) and the log4j logging used by Storefront.
 */
public class RequestLogger extends java.util.logging.Logger {
    private final Logger logger;

    public RequestLogger(Logger logger) {
        super(logger.getName(), null);
        this.logger = logger;
    }

    @Override
    public void info(String msg) {
        logger.debug(msg);
    }
}

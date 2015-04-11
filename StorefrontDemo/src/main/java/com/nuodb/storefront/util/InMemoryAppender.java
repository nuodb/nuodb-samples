/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.io.StringWriter;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontTenantManager;

public class InMemoryAppender extends WriterAppender {
    protected long maxFileSize = 10 * 1024 * 1024;

    public InMemoryAppender() {
        setWriter(new StringWriter());
    }

    public long getMaximumFileSize() {
        return maxFileSize;
    }

    public void setMaximumFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setMaxFileSize(String value) {
        maxFileSize = OptionConverter.toFileSize(value, maxFileSize);
    }

    @Override
    protected synchronized void subAppend(LoggingEvent event) {
        String[] loggerNameParts = event.getLoggerName().split(StorefrontApp.LOGGER_NAME_TENANT_SEP, 2);
        String tenantName = loggerNameParts.length > 1 ? loggerNameParts[1] : null;
        StringWriter writer = StorefrontTenantManager.getTenantOrDefault(tenantName).getLogWriter();
        setWriter(writer);
        StringBuffer buff = writer.getBuffer();

        if (buff.length() >= maxFileSize) {
            int nextLineIdx = buff.indexOf("\n", (int) (buff.length() - maxFileSize));
            if (nextLineIdx > 0) {
                buff.delete(0, nextLineIdx);
            } else {
                buff.setLength(0);
            }
        }
        super.subAppend(event);
    }
}

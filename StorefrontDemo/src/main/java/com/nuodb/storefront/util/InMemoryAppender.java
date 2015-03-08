/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.io.StringWriter;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

public class InMemoryAppender extends WriterAppender {
    private StringWriter writer = new StringWriter();
    protected long maxFileSize = 10 * 1024 * 1024;
    private static InMemoryAppender instance;

    public InMemoryAppender() {
        instance = this;
        setWriter(writer);
    }

    public static InMemoryAppender getInstance() {
        return instance;
    }

    public void clear() {
        writer.getBuffer().setLength(0);
    }

    public String getLog() {
        return writer.getBuffer().toString();
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
    protected void subAppend(LoggingEvent event) {
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

/* Copyright (c) 2013-2015 NuoDB, Inc. */
package com.nuodb.storefront.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LoggerToStringStyle extends ToStringStyle {
    public static final LoggerToStringStyle INSTANCE = new LoggerToStringStyle();
    private static final long serialVersionUID = 7591552852813015916L;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LoggerToStringStyle() {
        super();
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
        this.setContentStart(":");
        this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "      ");
        this.setFieldSeparatorAtStart(true);
        this.setContentEnd(SystemUtils.LINE_SEPARATOR);
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if ("password".equals(fieldName)) {
            super.appendDetail(buffer, fieldName, "******");
        } else if (value instanceof Calendar) {
            super.appendDetail(buffer, fieldName, dateFormat.format(((Calendar) value).getTime()));            
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }
}

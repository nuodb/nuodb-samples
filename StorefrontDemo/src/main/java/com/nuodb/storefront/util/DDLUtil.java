/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class DDLUtil {

    public static String generateAndFormat(SchemaExport export) {
        return format(generate(export));
    }

    public static String generate(SchemaExport export) {
        StringBuilder buff = new StringBuilder();
        buff.append("-- Drop statements --\n\n");
        appendDdlScript(export, "dropSQL", buff, ";\n");
        buff.append("\n\n");
        buff.append("-- Create statements --\n\n");
        appendDdlScript(export, "createSQL", buff, ";\n");
        return buff.toString();
    }

    public static String format(String ddl) {
        // Split "create table" statements onto multiple lines
        Pattern createFormatter = Pattern.compile("(create table [A-Z_]+) \\((.+?)\\);");
        Matcher m = createFormatter.matcher(ddl);
        StringBuffer mBuff = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(mBuff, m.group(1) + "\n(\n    " + StringUtils.join(m.group(2).split(",\\s?(?![^()]*\\))"), ",\n    ") + "\n);\n");
        }
        m.appendTail(mBuff);
        ddl = mBuff.toString();

        // Indent the latter parts of "alter table"
        ddl = ddl.replaceAll("(alter table [A-Za-z_]+) ([^;]+);", "$1\n    $2;");
        
        // Insert extra line before first "alter table");
        ddl = ddl.replaceFirst("alter table", "\nalter table");
        
        return ddl;
    }

    private static void appendDdlScript(SchemaExport export, String fieldName, StringBuilder buffer, String delimiter) {
        try {
            Field createSqlField = export.getClass().getDeclaredField(fieldName);
            createSqlField.setAccessible(true);
            for (String stmt : (String[]) createSqlField.get(export)) {
                buffer.append(stmt);
                buffer.append(delimiter);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

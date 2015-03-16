/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import org.hibernate.internal.util.compare.EqualsHelper;

public class DbConnInfo extends ConnInfo {
    private String host;
    private String dbName;
    private String dbProcessTag;

    public DbConnInfo() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbProcessTag() {
        return dbProcessTag;
    }

    public void setDbProcessTag(String dbProcessTag) {
        this.dbProcessTag = dbProcessTag;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DbConnInfo)) {
            return false;
        }
        DbConnInfo o = (DbConnInfo)obj;
        return super.equals(obj) &&
                EqualsHelper.equals(host, o.host) &&
                EqualsHelper.equals(dbName, o.dbName) &&
                EqualsHelper.equals(dbProcessTag, o.dbProcessTag);
    }
}

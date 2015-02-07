/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

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
}

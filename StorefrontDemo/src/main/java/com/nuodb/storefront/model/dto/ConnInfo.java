/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

public class ConnInfo {
    private String url;
    private String username;
    private String password;

    public ConnInfo() {
    }

    public ConnInfo(ConnInfo info) {
        this.url = info.url;
        this.username = info.username;
        this.password = info.password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

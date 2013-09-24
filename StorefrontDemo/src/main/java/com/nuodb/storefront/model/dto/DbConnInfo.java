package com.nuodb.storefront.model.dto;

public class DbConnInfo {
    private String url;
    private String username;

    public DbConnInfo() {
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
}

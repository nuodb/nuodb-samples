package com.nuodb.storefront.model.dto;

import com.nuodb.storefront.service.IStorefrontTenant;

public class TenantInfo {
    private String name;
    private DbConnInfo dbConnInfo;
    private ConnInfo apiConnInfo;
    private boolean isDefault;

    public TenantInfo() {
    }

    public TenantInfo(IStorefrontTenant tenant, boolean isDefault) {
        this.name = tenant.getAppInstance().getTenantName();
        this.dbConnInfo = tenant.getDbConnInfo();
        this.apiConnInfo = tenant.getApiConnInfo();
        this.isDefault = isDefault;
        
        this.apiConnInfo.setPassword(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DbConnInfo getDbConnInfo() {
        return dbConnInfo;
    }

    public void setDbConnInfo(DbConnInfo dbConnInfo) {
        this.dbConnInfo = dbConnInfo;
    }

    public ConnInfo getApiConnInfo() {
        return apiConnInfo;
    }

    public void setApiConnInfo(ConnInfo apiConnInfo) {
        this.apiConnInfo = apiConnInfo;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}

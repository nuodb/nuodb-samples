/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.exception.DataValidationException;
import com.nuodb.storefront.exception.TenantNotFoundException;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.service.storefront.StorefrontTenant;

public class StorefrontTenantManager {
    private static final AppInstance s_defaultAppInstance = new AppInstance(StorefrontApp.DEFAULT_REGION_NAME, StorefrontApp.DEFAULT_TENANT_NAME,
            true);
    private static final IStorefrontTenant s_defaultTenant = new StorefrontTenant(s_defaultAppInstance);
    private static final Map<String, IStorefrontTenant> s_tenantMap = new TreeMap<String, IStorefrontTenant>(String.CASE_INSENSITIVE_ORDER);

    static {
        s_tenantMap.put(StorefrontApp.DEFAULT_TENANT_NAME, s_defaultTenant);
    }

    public static IStorefrontTenant getDefaultTenant() {
        return s_defaultTenant;
    }

    public static IStorefrontTenant getTenant(HttpServletRequest request) {
        return getTenant(request.getParameter(StorefrontApp.TENANT_PARAM_NAME));
    }
    
    public static IStorefrontTenant getTenantOrDefault(String tenantName) {
        try {
            return getTenant(tenantName);
        } catch (TenantNotFoundException e) {
            return s_defaultTenant;
        }
    }

    public static IStorefrontTenant getTenant(String tenantName) {
        if (StringUtils.isEmpty(tenantName)) {
            return s_defaultTenant;
        }

        IStorefrontTenant tenant = s_tenantMap.get(tenantName);
        if (tenant == null) {
            throw new TenantNotFoundException(tenantName);
        }

        return tenant;
    }

    public static List<IStorefrontTenant> getAllTenants() {
        synchronized (s_tenantMap) {
            return new ArrayList<IStorefrontTenant>(s_tenantMap.values());
        }
    }

    public static IStorefrontTenant createTenant(String tenantName) {
        synchronized (s_tenantMap) {
            if (s_tenantMap.containsKey(tenantName)) {
                throw new DataValidationException("Tenant \"" + tenantName + "\" already exists");
            }
            
            if (!Pattern.matches("^[0-9A-Za-z\\-]+$", tenantName)) {
                throw new DataValidationException("Tenant name can contain only letters, numbers, and dashes.");
            }
            
            // Configure app instance
            AppInstance tenantApp = new AppInstance(s_defaultAppInstance.getName(), tenantName, true);
            tenantApp.setUrl(s_defaultAppInstance.getUrl());
            
            // Configure DB connection info
            DbConnInfo dbConnInfo = s_defaultTenant.getDbConnInfo();
            if (tenantName.equals(dbConnInfo.getDbName())) {
                //throw new DataValidationException("Existing tenant is using database name \"" + tenantName + "\".");
            }
            Matcher dbNameMatcher = Pattern.compile("jdbc:com.nuodb://([^/]+)/(.+)$").matcher(dbConnInfo.getUrl());
            if (!dbNameMatcher.matches()) {
                throw new DataValidationException("Unable to build database URL");
            }
            dbConnInfo.setUrl("jdbc:com.nuodb://" + dbNameMatcher.group(1) + "/" + tenantName);
            
            // Build and start tenant
            StorefrontTenant tenant = new StorefrontTenant(tenantApp);
            tenant.setDbConnInfo(dbConnInfo);
            
            s_tenantMap.put(tenantName, tenant);
            tenant.startUp();
            return tenant;
        }
    }

    public static void destroyTenant(String tenantName) {
        IStorefrontTenant tenant;

        synchronized (s_tenantMap) {
            tenant = s_tenantMap.get(tenantName);
            if (tenant == null) {
                throw new DataValidationException("Tenant \"" + tenantName + "\" does not exist");
            }
            if (tenant == s_defaultTenant) {
                throw new DataValidationException("Cannot remove default tenant");
            }
            s_tenantMap.remove(tenantName);
        }

        tenant.shutDown();
    }
}

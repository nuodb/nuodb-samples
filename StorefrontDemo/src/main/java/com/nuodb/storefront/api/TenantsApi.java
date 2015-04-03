/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.model.dto.TenantInfo;
import com.nuodb.storefront.service.IStorefrontTenant;

@Path("/tenants")
public class TenantsApi extends BaseApi {
    public TenantsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TenantInfo> getTenants(@Context HttpServletRequest req) {
        List<TenantInfo> dbs = new ArrayList<TenantInfo>();
        
        IStorefrontTenant defaultTenant = StorefrontTenantManager.getDefaultTenant();
        
        for (IStorefrontTenant tenant : StorefrontTenantManager.getAllTenants()) {
            dbs.add(new TenantInfo(tenant, tenant == defaultTenant));
        }
        return dbs;
    }
    
    @DELETE
    @Path("/{tennantName}")
    public void deleteTenant(@Context HttpServletRequest req, @PathParam("tenantName") String tenantName) {
        StorefrontTenantManager.destroyTenant(tenantName);
        
    }
}

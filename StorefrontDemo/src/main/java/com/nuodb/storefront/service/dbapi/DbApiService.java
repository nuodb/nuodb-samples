/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.service.dbapi;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response.Status;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.dbapi.IDbApi;
import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.model.dto.DbNode;
import com.nuodb.storefront.service.IDbApiService;

public class DbApiService implements IDbApiService {
    private final IStorefrontDao dao;
    private final IDbApi api;
    private final String dbName;

    public DbApiService(IStorefrontDao dao, IDbApi proxy, String dbName) {
        this.dao = dao;
        this.api = proxy;
        this.dbName = dbName;
    }

    @Override
    public List<DbNode> getDbNodes() {
        // Try using the API first
        if (api != null) {
            try {
                return api.getDbNodes(dbName);
            } catch (Exception e) {
            }
        }

        // Fall back to using the database
        return dao.runTransaction(TransactionType.READ_ONLY, "getDbNodes", new Callable<List<DbNode>>() {
            @Override
            public List<DbNode> call() {
                return dao.getDbNodes();
            }
        });
    }

    @Override
    public void shutdownDbNode(String uid) {
        if (api == null) {
            throw new ApiProxyException(Status.SERVICE_UNAVAILABLE, "This database does not support node shutdowns");
        }

        api.shutdownDbNode(uid);
    }
}

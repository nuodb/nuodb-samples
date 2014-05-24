/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.List;

import com.nuodb.storefront.model.dto.DbNode;

public interface IDbApi {
    /**
     * Fetches information about all the database nodes running in support of the underlying database schema. This method returns an empty list unless
     * NuoDB is running.
     */
    public List<DbNode> getDbNodes(String dbName);

    public void shutdownDbNode(String uid);
}

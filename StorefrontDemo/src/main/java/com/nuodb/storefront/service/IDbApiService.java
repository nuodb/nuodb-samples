/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.service;

import java.util.List;

import com.nuodb.storefront.model.dto.DbNode;

public interface IDbApiService {
    public List<DbNode> getDbNodes();

    public void shutdownDbNode(String uid);
}

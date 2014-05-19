/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.service.nuoapi;

import java.util.List;

import com.nuodb.storefront.model.dto.DbNode;

public interface INuoApiService {
    public List<DbNode> getDbNodes();

    public void shutdownDbNode(String uid);
}

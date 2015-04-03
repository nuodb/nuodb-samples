/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.dbapi;

import com.nuodb.storefront.model.db.Host;
import com.nuodb.storefront.model.db.Region;

class HomeHostInfo {
    public HomeHostInfo() {
    }
    
    public HomeHostInfo(Host host, Region region) {
        this.host = host;
        this.region = region;
    }
    
    public Host host;
    public Region region;
}

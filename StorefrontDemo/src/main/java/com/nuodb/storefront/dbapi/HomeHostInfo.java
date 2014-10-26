package com.nuodb.storefront.dbapi;

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

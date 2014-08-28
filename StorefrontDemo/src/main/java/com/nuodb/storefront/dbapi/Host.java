package com.nuodb.storefront.dbapi;

import java.util.Map;

public class Host {
    public String id;
    public String ipaddress;
    public int port;
    public String address;
    public String hostname;
    public Process[] processes;
    public Map<String, String> tags;
    public boolean isBroker;
}

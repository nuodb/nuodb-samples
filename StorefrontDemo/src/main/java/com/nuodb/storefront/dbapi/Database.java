/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.Map;

public class Database {
    public String name;
    public boolean active;
    public boolean ismet;
    public Process[] processes;
    public String status;    
    public String username;
    public String password;
    public Object template;  // string on POST/PUT, map on GET
    public Map<String, String> variables;
    public Map<String, Map<String, String>> tagConstraints;
}
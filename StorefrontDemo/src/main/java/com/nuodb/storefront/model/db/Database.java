/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.db;

import java.util.HashMap;
import java.util.Map;

public class Database {
    // Definition properties
    public String name;
    public String username;
    public String password;
    public Object template; // string on POST/PUT, map on GET
    public Map<String, String> options;
    public Map<String, String> variables;
    public Map<String, Map<String, String>> tagConstraints;

    // Status properties
    public boolean active;
    public boolean ismet;
    public Process[] processes;
    public String status;
    public Map<String, Map<String, String>> archives;

    public Map<String, Object> toDefinition() {
        Map<String, Object> def = new HashMap<String, Object>();
        def.put("name", name);
        def.put("username", username);
        def.put("password", password);
        def.put("template", template);
        def.put("options", options);
        def.put("variables", variables);
        def.put("tagConstraints", tagConstraints);
        return def;
    }
}
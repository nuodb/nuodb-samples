/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.db;

public class Process {
    public Process() {
    }

    public Process(Process process) {
        this.address = process.address;
        this.agentid = process.agentid;
        this.dbname = process.dbname;
        this.hostname = process.hostname;
        this.nodeId = process.nodeId;
        this.pid = process.pid;
        this.port = process.port;
        this.status = process.status;
        this.type = process.type;
        this.uid = process.uid;
        this.region = process.region;
    }

    public String address;
    public String agentid;
    public String dbname;
    public String hostname;
    public int nodeId;
    public int pid;
    public int port;
    public String status;
    public String type;
    public String uid;
    public String region;
}
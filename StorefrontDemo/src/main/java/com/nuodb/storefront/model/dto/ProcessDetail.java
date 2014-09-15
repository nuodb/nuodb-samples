/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.nuodb.storefront.dbapi.Process;

public class ProcessDetail extends Process {
    private List<String> appInstances = new ArrayList<String>();
    private boolean isCurrentConnection;

    public ProcessDetail() {
    }

    public ProcessDetail(Process process) {
        super(process);
    }

    public List<String> getAppInstances() {
        return appInstances;
    }

    public void setAppInstances(List<String> appInstances) {
        this.appInstances = appInstances;
    }

    public boolean isCurrentConnection() {
        return isCurrentConnection;
    }

    public void setCurrentConnection(boolean isCurrentConnection) {
        this.isCurrentConnection = isCurrentConnection;
    }
}

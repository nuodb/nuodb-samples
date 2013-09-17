/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class AppInstance implements IModel {
    @Id
    @Column(length = 36)
    private String uuid = UUID.randomUUID().toString();

    @NotNull
    private String name = "Default Storefront";

    @NotNull
    private String url;

    @NotNull
    private String region;

    @NotNull
    private Calendar dateStarted = Calendar.getInstance();

    @NotNull
    private Calendar firstHeartbeat;

    @NotNull
    private Calendar lastHeartbeat;

    private int cpuUtilization;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.US_DOLLAR;

    public AppInstance() {
    }
    
    public long getUptimeMs() {
        return System.currentTimeMillis() - dateStarted.getTimeInMillis();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Calendar getFirstHeartbeat() {
        return firstHeartbeat;
    }

    public void setFirstHeartbeat(Calendar firstHeartbeat) {
        this.firstHeartbeat = firstHeartbeat;
    }

    public Calendar getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Calendar lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public int getCpuUtilization() {
        return cpuUtilization;
    }

    public void setCpuUtilization(int cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Calendar getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Calendar dateStarted) {
        this.dateStarted = dateStarted;
    }
}

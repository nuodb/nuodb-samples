/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.model.type.Currency;

@Entity
@Table(indexes = { @Index(name = "idx_app_instance_last_heartbeat", columnList = "lastHeartbeat") })
public class AppInstance extends UuidEntity {
    @NotNull
    private String url;

    @NotNull
    private String region;

    @NotNull
    private long dateStarted = new Date().getTime();

    @NotNull
    private Calendar firstHeartbeat;

    @NotNull
    private Calendar lastHeartbeat;

    private int cpuUtilization;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.US_DOLLAR;

    @NotNull
    private int nodeId;

    @NotNull
    private Calendar lastApiActivity;

    @NotNull
    private boolean stopUsersWhenIdle = true;

    @Transient
    private boolean local;

    @Transient
    private String tenantName;
    
    public AppInstance() {
    }

    public AppInstance(String region, String tenant, boolean local) {
        this.region = region;
        this.tenantName = tenant;
        this.local = local;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - dateStarted;
    }

    public String getName() {
        return "Storefront (" + region + ")";
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
        if (StringUtils.isEmpty(region)) {
            throw new IllegalArgumentException();
        }
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

    public long getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(long dateStarted) {
        this.dateStarted = dateStarted;
    }

    public boolean getLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public Calendar getLastApiActivity() {
        return lastApiActivity;
    }

    public void setLastApiActivity(Calendar lastApiActivity) {
        this.lastApiActivity = lastApiActivity;
    }

    public boolean getStopUsersWhenIdle() {
        return stopUsersWhenIdle;
    }

    public void setStopUsersWhenIdle(boolean stopUsersWhenIdle) {
        this.stopUsersWhenIdle = stopUsersWhenIdle;
    }

    public String getTenantName() {
        return tenantName;
    }
}

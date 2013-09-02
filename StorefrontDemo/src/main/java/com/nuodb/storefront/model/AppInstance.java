package com.nuodb.storefront.model;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class AppInstance implements IModel {
    @Id
    @Column(length=36)
    private String uuid;

    @NotNull
    private String name;

    @NotNull
    private String url;

    @NotNull
    private String region;

    @NotNull
    private Calendar firstHeartbeat;

    @NotNull
    private Calendar lastHeartbeat;

    private int cpuUtilization;

    @NotNull
    private Currency currency;

    public AppInstance() {
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

}

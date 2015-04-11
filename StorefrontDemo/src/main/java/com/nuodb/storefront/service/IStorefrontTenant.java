/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service;

import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.entity.AppInstance;
import com.sun.jersey.api.client.Client;

public interface IStorefrontTenant {
    public AppInstance getAppInstance();
    
    public void startUp();

    public void shutDown();

    // Connection management
    
    public DbConnInfo getDbConnInfo();

    public void setDbConnInfo(DbConnInfo dbConnInfo);

    public ConnInfo getApiConnInfo();

    public void setApiConnInfo(ConnInfo info);

    // URLs
    
    public String getAdminConsoleUrl();

    public String getSqlExplorerUrl();

    // Schema management
    
    public SchemaExport createSchemaExport();

    public void createSchema();

    // Factory methods
    
    public IStorefrontService createStorefrontService();

    public IDataGeneratorService createDataGeneratorService();

    public IStorefrontDao createStorefrontDao();
    
    public Client createApiClient();

    // Tenant singletons
    
    public ISimulatorService getSimulatorService();

    public IStorefrontPeerService getStorefrontPeerService();

    public IDbApi getDbApi();
    
    public Logger getLogger(Class<?> clazz);
    
    public StringWriter getLogWriter();
    
    public Map<String, TransactionStats> getTransactionStats();
}
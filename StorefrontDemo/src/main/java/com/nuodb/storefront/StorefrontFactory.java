/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.datagen.DataGeneratorService;
import com.nuodb.storefront.service.simulator.SimulatorService;
import com.nuodb.storefront.service.storefront.StorefrontService;

/**
 * Factory for creating Storefront services and schema managers. To keep code in
 * this demo straightforward, this factory is used in lieu of dependency
 * injection, e.g. via the Spring framework.
 */
public class StorefrontFactory {
    private static final SessionFactory sessionFactory;
    private static final Configuration configuration;

    static {
        configuration = new Configuration();
        configuration.configure();
        
        String dbName = System.getProperty("storefront.db.name");
        String dbUser = System.getProperty("storefront.db.user");
        String dbPassword = System.getProperty("storefront.db.password");
        
        if (dbName != null) {
            Matcher dbNameMatcher = Pattern.compile("([^@]*)@([^@:]*(?::\\d+|$))").matcher(dbName);
            if (!dbNameMatcher.matches()) {
                throw new IllegalArgumentException("Database name must be of the format name@host[:port]");
            }
            String name = dbNameMatcher.group(1);
            String host = dbNameMatcher.group(2);            
            
            String url = "jdbc:com.nuodb://" + host + "/" + name;
            
            configuration.setProperty("hibernate.connection.url", url);
        }
        if (dbUser != null) {
            configuration.setProperty("hibernate.connection.username", dbUser);
        }
        if (dbPassword != null) {
            configuration.setProperty("hibernate.connection.password", dbPassword);
        }
                
        sessionFactory = configuration.buildSessionFactory();
    }
    
    private static class SimulatorSingletonHolder {
        // IODH-based lazy loading singleton
        public static ISimulatorService simulator = new SimulatorService(createStorefrontService());
    }

    private StorefrontFactory() {
    }

    public static SchemaExport createSchemaExport() {
        return new SchemaExport(configuration);
    }

    public static IStorefrontService createStorefrontService() {
        return new StorefrontService(createStorefrontDao());
    }

    public static IDataGeneratorService createDataGeneratorService() {
        return new DataGeneratorService(createStorefrontDao());
    }
    
    public static ISimulatorService getSimulatorService() {
        return SimulatorSingletonHolder.simulator;
    }

    private static IStorefrontDao createStorefrontDao() {
        StorefrontDao dao = new StorefrontDao();
        dao.setSessionFactory(sessionFactory);
        return dao;
    }
}

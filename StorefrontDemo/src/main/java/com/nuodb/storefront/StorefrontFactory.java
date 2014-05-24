/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.UpperCaseNamingStrategy;
import com.nuodb.storefront.dbapi.DbApi;
import com.nuodb.storefront.dbapi.IDbApi;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.IDbApiService;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.datagen.DataGeneratorService;
import com.nuodb.storefront.service.dbapi.DbApiService;
import com.nuodb.storefront.service.simulator.SimulatorService;
import com.nuodb.storefront.service.storefront.AppInstanceInitService;
import com.nuodb.storefront.service.storefront.HeartbeatService;
import com.nuodb.storefront.service.storefront.StorefrontService;

/**
 * Factory for creating Storefront services and schema managers. To keep code in this demo straightforward, this factory is used in lieu of dependency
 * injection, e.g. via the Spring framework.
 */
public class StorefrontFactory {
    private static final Configuration s_configuration;
    private static volatile SessionFactory s_sessionFactory;
    private static volatile ISimulatorService s_simulator;

    static {
        s_configuration = new Configuration();
        s_configuration.setNamingStrategy(new UpperCaseNamingStrategy());
        s_configuration.configure();

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

            s_configuration.setProperty(Environment.URL, url);
        }
        if (dbUser != null) {
            s_configuration.setProperty(Environment.USER, dbUser);
        }
        if (dbPassword != null) {
            s_configuration.setProperty(Environment.PASS, dbPassword);
        }
    }

    private StorefrontFactory() {
    }

    public static DbConnInfo getDbConnInfo() {
        DbConnInfo info = new DbConnInfo();
        info.setUrl(s_configuration.getProperty(Environment.URL));
        info.setUsername(s_configuration.getProperty(Environment.USER));
        return info;
    }

    public static SchemaExport createSchemaExport() {
        return new SchemaExport(s_configuration);
    }

    public static void createSchema() {
        new SchemaExport(s_configuration).create(false, true);
    }

    public static IStorefrontService createStorefrontService() {
        return new StorefrontService(createStorefrontDao());
    }

    public static IDataGeneratorService createDataGeneratorService() {
        StatelessSession session = getOrCreateSessionFactory().openStatelessSession();
        try {
            session.connection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new DataGeneratorService(session);
    }

    public static ISimulatorService getSimulatorService() {
        if (s_simulator == null) {
            synchronized (s_configuration) {
                s_simulator = new SimulatorService(createStorefrontService());
            }
        }
        return s_simulator;
    }

    public static IDbApiService createDbApiService() {
        IDbApi api;
        String dbName;
        
        String url = s_configuration.getProperty(Environment.URL);
        Matcher dbNameMatcher = Pattern.compile("jdbc:com.nuodb://([^/:]+)(:[^/]*)?/(.+)$").matcher(url);
        if (!dbNameMatcher.matches()) {
            // Not a NuoDB-database.  The DB API is not supported.
            api = null;
            dbName = null;
        } else {
            dbName = dbNameMatcher.group(3);
            String host = System.getProperty("storefront.dbapi.host", dbNameMatcher.group(1));
            String user = System.getProperty("storefront.dbapi.user", "domain");
            String password = System.getProperty("storefront.dbapi.password", "bird");
            String port = System.getProperty("storefront.dbapi.port", "8888");
            api = new DbApi("http://" + host + ":" + port, user, password);
        }
        
        return new DbApiService(createStorefrontDao(), api, dbName);
    }

    public static IStorefrontDao createStorefrontDao() {
        StorefrontDao dao = new StorefrontDao();
        dao.setSessionFactory(getOrCreateSessionFactory());
        return dao;
    }

    public static IHeartbeatService createHeartbeatService() {
        return new HeartbeatService();
    }

    private static SessionFactory getOrCreateSessionFactory() {
        if (s_sessionFactory == null) {
            synchronized (s_configuration) {
                if (s_sessionFactory == null) {
                    s_sessionFactory = s_configuration.buildSessionFactory();
                    try {
                        new AppInstanceInitService(createStorefrontDao()).init(StorefrontApp.APP_INSTANCE);
                    } catch (Exception e) {
                        s_sessionFactory = null;
                        throw (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
                    }
                }
            }
        }
        return s_sessionFactory;
    }
}

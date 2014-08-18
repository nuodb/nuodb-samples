/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Properties;
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
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.datagen.DataGeneratorService;
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

        try {
            String propertyFile = System.getProperty("properties", null);
            if (propertyFile != null) {
                Properties overrides = new Properties();
                overrides.load(new FileInputStream(propertyFile));
                System.getProperties().putAll(overrides);
            }
        } catch (Exception x) {
            ;
        }

        String dbName = System.getProperty("storefront.db.name");
        String dbUser = System.getProperty("storefront.db.user");
        String dbPassword = System.getProperty("storefront.db.password");
        String dbOptions = System.getProperty("storefront.db.options");
        if (dbName != null) {
            dbName = dbName.replace("{domain.broker}", System.getProperty("domain.broker"));

            Matcher dbNameMatcher = Pattern.compile("([^@]*)@([^@:]*(?::\\d+|$))").matcher(dbName);
            if (!dbNameMatcher.matches()) {
                throw new IllegalArgumentException("Database name must be of the format name@host[:port]");
            }
            String name = dbNameMatcher.group(1);
            String host = dbNameMatcher.group(2);

            String url = "jdbc:com.nuodb://" + host + "/" + name;
            if (dbOptions != null) {
                url = url + "?" + dbOptions;
            }
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
        String url = s_configuration.getProperty(Environment.URL);
        Matcher dbNameMatcher = Pattern.compile("jdbc:com.nuodb://([^/:]+)(:[^/]*)?/(.+)$").matcher(url);

        DbConnInfo info = new DbConnInfo();
        info.setUrl(url);
        if (dbNameMatcher.matches()) {
            info.setHost(dbNameMatcher.group(1));
            info.setDbName(dbNameMatcher.group(3));
        } else {
            info.setHost(StorefrontApp.DEFAULT_DB_HOST);
            info.setDbName(StorefrontApp.DEFAULT_DB_NAME);
        }
        info.setUsername(s_configuration.getProperty(Environment.USER));
        info.setPassword(s_configuration.getProperty(Environment.PASS));
        info.setTemplate(System.getProperty("storefront.db.template", StorefrontApp.DEFAULT_DB_TEMPLATE));
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

    public static IDbApi createDbApi() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String user = System.getProperty("storefront.dbapi.user", "domain");
        String password = System.getProperty("storefront.dbapi.password", "bird");
        String port = System.getProperty("storefront.dbapi.port", "8888");
        return new DbApi("http://" + host + ":" + port, user, password);
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

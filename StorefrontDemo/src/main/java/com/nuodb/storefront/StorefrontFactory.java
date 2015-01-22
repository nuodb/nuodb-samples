/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.UpperCaseNamingStrategy;
import com.nuodb.storefront.dbapi.DbApiProxy;
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
import com.nuodb.storefront.servlet.WelcomeServlet;

/**
 * Factory for creating Storefront services and schema managers. To keep code in this demo straightforward, this factory is used in lieu of dependency
 * injection, e.g. via the Spring framework.
 */
public class StorefrontFactory {
    private static final Configuration s_configuration;
    private static volatile SessionFactory s_sessionFactory;
    private static volatile ISimulatorService s_simulator;
    private static volatile boolean s_initializedApp = false;
    private static volatile IDbApi s_dbApi;
    private static final Logger s_logger = Logger.getLogger(WelcomeServlet.class.getName());

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
        } catch (Exception e) {
            s_logger.warn("Failed to read properties file", e);
        }

        String dbName = System.getProperty("storefront.db.name");
        String dbUser = System.getProperty("storefront.db.user");
        String dbPassword = System.getProperty("storefront.db.password");
        String dbOptions = System.getProperty("storefront.db.options");
        if (dbName != null) {
            dbName = dbName.replace("{domain.broker}", System.getProperty("domain.broker", "localhost"));

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
        info.setDbProcessTag(System.getProperty("storefront.db.processTag", StorefrontApp.DEFAULT_DB_PROCESS_TAG));
        return info;
    }

    public static void setDbConnInfo(DbConnInfo dbConnInfo) {
        s_configuration.setProperty(Environment.USER, dbConnInfo.getUsername());
        s_configuration.setProperty(Environment.PASS, dbConnInfo.getPassword());
        s_configuration.setProperty(Environment.URL, dbConnInfo.getUrl());

        synchronized (s_configuration) {
            s_dbApi = null;
            if (s_sessionFactory != null) {
                s_sessionFactory.close();
                s_sessionFactory = null;
            }
        }
    }

    public static String getAdminConsoleUrl() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String port = System.getProperty("storefront.dbapi.port", "8888");
        return "http://" + host + ":" + port + "/console";
    }

    public static String getSqlExplorerUrl() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String port = System.getProperty("storefront.sqlexplorer.port", "9001");
        return "http://" + host + ":" + port + "/explorer.jsp";
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
        SessionFactory factory = getOrCreateSessionFactory();
        try {
            Connection connection = factory.getSessionFactoryOptions().getServiceRegistry().getService(ConnectionProvider.class).getConnection();
            StatelessSession session = factory.openStatelessSession(connection);
            connection.setAutoCommit(true);
            return new DataGeneratorService(session, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ISimulatorService getSimulatorService() {
        if (s_simulator == null) {
            synchronized (s_configuration) {
                s_simulator = new SimulatorService(createStorefrontService());
            }
        }
        return s_simulator;
    }

    public static IDbApi getDbApi() {
        if (s_dbApi == null) {
            synchronized (s_configuration) {
                if (s_dbApi == null) {
                    s_dbApi = StorefrontFactory.createDbApi();
                }
            }
        }
        return s_dbApi;
    }

    public static IDbApi createDbApi() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String user = System.getProperty("storefront.dbapi.user", "domain");
        String password = System.getProperty("storefront.dbapi.password", "bird");
        String port = System.getProperty("storefront.dbapi.port", "8888");
        return new DbApiProxy("http://" + host + ":" + port + "/api/1", user, password, connInfo);
    }

    public static IStorefrontDao createStorefrontDao() {
        return createStorefrontDao(getOrCreateSessionFactory());
    }

    public static IHeartbeatService createHeartbeatService() {
        return new HeartbeatService();
    }

    public static IStorefrontDao createStorefrontDao(SessionFactory sessionFactory) {
        StorefrontDao dao = new StorefrontDao();
        dao.setSessionFactory(sessionFactory);
        return dao;
    }

    private static SessionFactory getOrCreateSessionFactory() {
        if (!s_initializedApp) {
            synchronized (s_configuration) {
                if (s_sessionFactory == null) {
                    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(s_configuration.getProperties()).build();
                    s_sessionFactory = s_configuration.buildSessionFactory(serviceRegistry);
                }
                try {
                    new AppInstanceInitService(createStorefrontDao(s_sessionFactory)).init(StorefrontApp.APP_INSTANCE);
                    s_initializedApp = true;
                } catch (Exception e) {
                    throw (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
                }
            }
        }
        return s_sessionFactory;
    }
}

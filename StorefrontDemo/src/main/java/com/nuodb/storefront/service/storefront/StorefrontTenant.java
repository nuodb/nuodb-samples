/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.UpperCaseNamingStrategy;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.IDbApi;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontPeerService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.service.datagen.DataGeneratorService;
import com.nuodb.storefront.service.dbapi.DbApiProxy;
import com.nuodb.storefront.service.simulator.SimulatorService;
import com.nuodb.storefront.util.PerformanceUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Service container and factory for a single instance of Storefront.
 */
public class StorefrontTenant implements IStorefrontTenant {
    private static final Logger s_logger = Logger.getLogger(StorefrontTenant.class.getName());
    private static final ClientConfig s_apiCfg = new DefaultClientConfig();

    private Object lock = new Object();
    private boolean initializedApp = false;
    private final AppInstance appInstance;
    private final Configuration hibernateCfg;
    private SessionFactory sessionFactory;
    private ISimulatorService simulatorSvc;
    private IHeartbeatService heartbeatSvc;
    private IDbApi dbApi;
    private ConnInfo apiConnInfo;
    private ScheduledExecutorService executor;

    // Initialize API client config
    static {
        Map<String, Object> props = s_apiCfg.getProperties();
        props.put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, StorefrontApp.API_CONNECT_TIMEOUT_SEC * 1000);
        props.put(ClientConfig.PROPERTY_READ_TIMEOUT, StorefrontApp.API_READ_TIMEOUT_SEC * 1000);

        s_apiCfg.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    // Initialize Hibernate
    public StorefrontTenant(AppInstance appInstance) {
        this.appInstance = appInstance;
        
        hibernateCfg = new Configuration();
        hibernateCfg.setNamingStrategy(new UpperCaseNamingStrategy());
        hibernateCfg.configure();

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
            hibernateCfg.setProperty(Environment.URL, url);
        }
        if (dbUser != null) {
            hibernateCfg.setProperty(Environment.USER, dbUser);
        }
        if (dbPassword != null) {
            hibernateCfg.setProperty(Environment.PASS, dbPassword);
        }
    }

    @Override
    public AppInstance getAppInstance() {
        return appInstance;
    }

    @Override
    public void startUp() {
        synchronized (lock) {
            if (executor == null) {
                executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(getHeartbeatService(), 0, StorefrontApp.HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

                Runnable sampler = PerformanceUtil.createSampler();
                if (sampler != null) {
                    executor.scheduleAtFixedRate(sampler, 0, StorefrontApp.CPU_SAMPLING_INTERVAL_SEC, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void shutDown() {
        synchronized (lock) {
            if (executor == null) {
                executor.shutdown();
            }
            if (simulatorSvc != null) {
                simulatorSvc.removeAll();
            }
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    @Override
    public DbConnInfo getDbConnInfo() {
        String url = hibernateCfg.getProperty(Environment.URL);
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
        info.setUsername(hibernateCfg.getProperty(Environment.USER));
        info.setPassword(hibernateCfg.getProperty(Environment.PASS));
        info.setDbProcessTag(System.getProperty("storefront.db.processTag", StorefrontApp.DEFAULT_DB_PROCESS_TAG_PREFIX + info.getDbName()));
        return info;
    }

    @Override
    public void setDbConnInfo(DbConnInfo dbConnInfo) {
        hibernateCfg.setProperty(Environment.USER, dbConnInfo.getUsername());
        hibernateCfg.setProperty(Environment.PASS, dbConnInfo.getPassword());
        hibernateCfg.setProperty(Environment.URL, dbConnInfo.getUrl());

        synchronized (lock) {
            dbApi = null;
            if (sessionFactory != null) {
                sessionFactory.close();
                sessionFactory = null;
            }
        }
    }

    @Override
    public ConnInfo getApiConnInfo() {
        synchronized (lock) {
            if (apiConnInfo == null) {
                DbConnInfo connInfo = getDbConnInfo();
                String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
                String port = System.getProperty("storefront.dbapi.port", "8888");
                ConnInfo info = new ConnInfo();
                info.setUsername(System.getProperty("storefront.dbapi.user", "domain"));
                info.setPassword(System.getProperty("storefront.dbapi.password", "bird"));
                info.setUrl("http://" + host + ":" + port + "/api/1");
                apiConnInfo = info;
            }
        }
        return new ConnInfo(apiConnInfo);
    }

    @Override
    public void setApiConnInfo(ConnInfo info) {
        synchronized (lock) {
            apiConnInfo = new ConnInfo(info);
            dbApi = null;
        }
    }

    @Override
    public String getAdminConsoleUrl() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String port = System.getProperty("storefront.dbapi.port", "8888");
        return "http://" + host + ":" + port + "/console";
    }

    @Override
    public String getSqlExplorerUrl() {
        DbConnInfo connInfo = getDbConnInfo();
        String host = System.getProperty("storefront.dbapi.host", connInfo.getHost());
        String port = System.getProperty("storefront.sqlexplorer.port", "9001");
        return "http://" + host + ":" + port + "/explorer.jsp";
    }

    @Override
    public SchemaExport createSchemaExport() {
        SchemaExport export = new SchemaExport(hibernateCfg);
        export.setDelimiter(";");
        return export;
    }

    @Override
    public void createSchema() {
        new SchemaExport(hibernateCfg).create(false, true);
    }

    @Override
    public IStorefrontService createStorefrontService() {
        return new StorefrontService(appInstance, createStorefrontDao());
    }

    @Override
    public IDataGeneratorService createDataGeneratorService() {
        SessionFactory factory = getOrCreateSessionFactory();
        try {
            Connection connection = factory.getSessionFactoryOptions().getServiceRegistry().getService(ConnectionProvider.class).getConnection();
            StatelessSession session = factory.openStatelessSession(connection);
            connection.setAutoCommit(true);
            return new DataGeneratorService(session, connection, appInstance.getRegion());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ISimulatorService getSimulatorService() {
        if (simulatorSvc == null) {
            synchronized (lock) {
                if (simulatorSvc == null) {
                    simulatorSvc = new SimulatorService(createStorefrontService());
                }
            }
        }
        return simulatorSvc;
    }

    @Override
    public IDbApi getDbApi() {
        if (dbApi == null) {
            synchronized (lock) {
                if (dbApi == null) {
                    dbApi = createDbApi();
                }
            }
        }
        return dbApi;
    }

    @Override
    public IStorefrontDao createStorefrontDao() {
        return createStorefrontDao(getOrCreateSessionFactory());
    }

    @Override
    public IStorefrontPeerService getStorefrontPeerService() {
        return (IStorefrontPeerService) getHeartbeatService();
    }

    @Override
    public Client createApiClient() {
        return Client.create(s_apiCfg);
    }

    protected IDbApi createDbApi() {
        return new DbApiProxy(this);
    }
    
    protected IHeartbeatService getHeartbeatService() {
        if (heartbeatSvc == null) {
            synchronized (lock) {
                heartbeatSvc = new HeartbeatService(this);
            }
        }
        return heartbeatSvc;
    }

    protected SessionFactory getOrCreateSessionFactory() {
        if (!initializedApp) {
            synchronized (lock) {
                if (sessionFactory == null) {
                    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(hibernateCfg.getProperties()).build();
                    sessionFactory = hibernateCfg.buildSessionFactory(serviceRegistry);
                }
                try {
                    new AppInstanceInitService(createStorefrontDao(sessionFactory)).init(appInstance);
                    initializedApp = true;
                } catch (Exception e) {
                    throw (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
                }
            }
        }
        return sessionFactory;
    }
    
    protected IStorefrontDao createStorefrontDao(SessionFactory sf) {
        StorefrontDao dao = new StorefrontDao();
        dao.setSessionFactory(sf);
        return dao;
    }
}

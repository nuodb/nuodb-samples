/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.impl.util.StringUtils;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.UpperCaseNamingStrategy;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.type.Currency;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.datagen.DataGeneratorService;
import com.nuodb.storefront.service.simulator.SimulatorService;
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
    private static final Logger s_log = Logger.getLogger(StorefrontFactory.class.getName());

    static {
        s_configuration = new Configuration();
        s_configuration.setNamingStrategy(UpperCaseNamingStrategy.INSTANCE);
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

    public static IStorefrontDao createStorefrontDao() {
        StorefrontDao dao = new StorefrontDao();
        dao.setSessionFactory(getOrCreateSessionFactory());
        return dao;
    }

    private static SessionFactory getOrCreateSessionFactory() {
        if (s_sessionFactory == null) {
            synchronized (s_configuration) {
                if (s_sessionFactory == null) {
                    s_sessionFactory = s_configuration.buildSessionFactory();
                    try {
                        Session session = s_sessionFactory.openSession();

                        testSession(session);
                    } catch (Exception e) {
                        s_sessionFactory = null;
                        throw (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
                    }
                }
            }
        }
        return s_sessionFactory;
    }

    public static IHeartbeatService createHeartbeatService(String url) {
        return new HeartbeatService(url);
    }

    private static void testSession(Session session) {
        // Run a test transaction to ensure we have a valid connection
        Transaction t = session.beginTransaction();

        // Ask the DB for the region name if it hasn't been supplied manually
        if (StringUtils.isEmpty(StorefrontApp.APP_INSTANCE.getRegion())) {
            String region = null;
            try {
                Object result = session.createSQLQuery("SELECT GEOREGION FROM SYSTEM.NODES WHERE ID=GETNODEID()").uniqueResult();
                region = result.toString();
            } catch (SQLGrammarException e) {
                s_log.warning("Your database version does not support regions.  Upgrade to NouDB 2.0 or greater.");
            }
            if (!StringUtils.isEmpty(region)) {
                StorefrontApp.APP_INSTANCE.setRegion(region);
            }
        }

        // Use most recent currency associated with this region
        @SuppressWarnings("unchecked")
        List<String> currencies = (List<String>) session
                .createSQLQuery("SELECT DISTINCT Currency FROM APP_INSTANCE WHERE REGION=:REGION ORDER BY LAST_HEARTBEAT DESC")
                .setParameter("REGION", StorefrontApp.APP_INSTANCE.getRegion()).list();
        if (!currencies.isEmpty()) {
            try {
                Currency currency = Currency.valueOf(currencies.get(0));
                StorefrontApp.APP_INSTANCE.setCurrency(currency);
            } catch (IllegalArgumentException e) {
                // not fatal, just use default
            }
        }

        t.rollback();
        session.close();
    }
}

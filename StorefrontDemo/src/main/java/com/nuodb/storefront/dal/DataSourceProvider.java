/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;

/**
 * Furnishes connections directly from NuoDB's DataSource implementation without relying on JNDI.
 * 
 * All properties specified in hibernate.cfg.xml with a "hibernate.connection" prefix get forwarded to the DataSource implementation to give you have
 * full control over the pool's configuration.
 */
public class DataSourceProvider implements ConnectionProvider, Configurable {
    private static final long serialVersionUID = 7906975777950509725L;
    private static final String PROP_LOGIN_TIMEOUT = "loginTimeout";
    private DataSource dataSource;
    private int isolationLevel = 5;

    @Override
    public synchronized void configure(@SuppressWarnings("rawtypes") Map props) throws HibernateException {
        int loginTimeout = 0;

        try {
            // Consolidate data source properties
            Properties dsProps = new Properties();
            for (Object key : props.keySet()) {
                String keyStr = key.toString();

                if (keyStr.startsWith(Environment.CONNECTION_PREFIX)) {
                    // Strip out "hibernate.connection" prefix
                    String dsKey = keyStr.substring(Environment.CONNECTION_PREFIX.length() + 1);
                    String dsVal = props.get(key).toString();
                    if (dsKey.equals(PROP_LOGIN_TIMEOUT)) {
                        loginTimeout = Integer.parseInt(dsVal);
                    } else {
                        dsProps.put(dsKey, dsVal);
                        if (dsKey.equals("isolation")) {
                            isolationLevel = Integer.parseInt(dsVal);
                        }
                    }
                }
            }

            dataSource = new com.nuodb.jdbc.DataSource(dsProps);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to configure NuoDB data source", e);
        }

        try {
            dataSource.setLoginTimeout(loginTimeout);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to set NuoDB data source login timeout", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setTransactionIsolation(isolationLevel);
        return conn;
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(@SuppressWarnings("rawtypes") Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnknownUnwrapTypeException(unwrapType);
    }
}

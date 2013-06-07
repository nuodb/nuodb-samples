/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;

/**
 * Furnishes connections directly from NuoDB's DataSource implementation without relying on JNDI.
 * 
 * All properties specified in hibernate.cfg.xml with a "hibernate.connection" prefix get forwarded to the DataSource implementation to give you have
 * full control over the pool's configuration.
 */
public class DataSourceProvider implements ConnectionProvider {
    private static volatile DataSource dataSource;

    @Override
    public synchronized void configure(Properties props) throws HibernateException {
        if (dataSource != null) {
            // We need to configure the data source only once.
            return;
        }

        try {
            // Consolidate data source properties
            Properties dsProps = new Properties();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith(Environment.CONNECTION_PREFIX)) {
                    // Strip out "hibernate.connection" prefix
                    String dsKey = key.substring(Environment.CONNECTION_PREFIX.length() + 1);
                    String dsVal = props.getProperty(key);
                    dsProps.put(dsKey, dsVal);
                }
            }

            dataSource = new com.nuodb.jdbc.DataSource(dsProps);
        } catch (RuntimeException e) {
            throw new RuntimeException("Could not configure NuoDB data source", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }
}

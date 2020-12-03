package com.nuodb.docs.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;

import com.nuodb.jdbc.DataSource;

/**
 * Examples of using JDBC with NuoDB's driver. Also using Spring's JDBC
 * Template.
 * 
 * @author Paul Chapman
 */
public class JavaClient {

    /**
     * Create a data source using setters to configure NuoDB's DataSource class.
     * 
     * @return The new data source.
     * 
     * @throws SQLException
     */
    public javax.sql.DataSource createDataSource1() throws SQLException {
        String url = "jdbc:com.nuodb://localhost/test?clientInfo=javaClient";

        com.nuodb.jdbc.DataSource dataSource = new com.nuodb.jdbc.DataSource();
        dataSource.setUrl(url); // "jdbc:com.nuodb://localhost/test");
        dataSource.setUser("dba");
        dataSource.setPassword("goalie");
        dataSource.setDefaultSchema("Hockey");

        return dataSource;
    }

    /**
     * Create a data source using a properties instance to configure NuoDB's
     * DataSource class.
     * 
     * @return The new data source.
     * 
     * @throws SQLException
     */
    public javax.sql.DataSource createDataSource2() throws SQLException {
        // Using a Java properties instance but setting properties in code
        Properties properties = new Properties();
        properties.put(DataSource.PROP_URL, "jdbc:com.nuodb://localhost/test?clientInfo=javaClient");
        properties.put(DataSource.PROP_USER, "dba");
        properties.put(DataSource.PROP_PASSWORD, "goalie");
        properties.put(DataSource.PROP_SCHEMA, "Hockey");
        
        javax.sql.DataSource dataSource = new com.nuodb.jdbc.DataSource(properties);
        return dataSource;
    }

    /**
     * Create a data source using a properties file to configure NuoDB's DataSource
     * class.
     * 
     * @return The new data source.
     * 
     * @throws SQLException
     */
    public javax.sql.DataSource createDataSource3() throws IOException, SQLException {
        // Using a Java properties file
        Properties properties = new Properties();
        properties.load(new FileInputStream("nuodb.properties"));
        javax.sql.DataSource dataSource = new com.nuodb.jdbc.DataSource(properties);

        return dataSource;
    }

    /**
     * Run a query to fetch the first player in the Players table with key
     * "aaltoan01".
     * 
     * @param dataSource
     * @throws SQLException
     */
    public void runQuery(javax.sql.DataSource dataSource) throws SQLException {
        try (java.sql.Connection dbConnection = dataSource.getConnection()) {

            dbConnection.setAutoCommit(false);

            // Find player with ID = 10
            String id = "aaltoan01";

            try (PreparedStatement stmt = //
                    dbConnection.prepareStatement("SELECT firstname, lastname FROM Players WHERE playerid=?")) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

               if (rs.next())
                    System.out.println("Found: " + rs.getString(1) + ' ' + rs.getString(2));

            } // End block automatically runs stmt.close() which in turn closes rs

        } // End block automatically runs dbConnection.close();
    }

    /**
     * Using Spring's JdbcTemplate class to fetch the first player in the Players
     * table with key "aaltoan01".
     * <p>
     * Hides all the JDBC and maps the row in the result-set to a Java map, keyed by
     * column name. Note that the keys are case-insensitive.
     * 
     * @param dataSource The dataSource for the database.
     */
    public void runQuerySpring(javax.sql.DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT firstname, lastname FROM Players WHERE playerid=?";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, "aaltoan01");

        // The map is an instance of org.springframework.util.LinkedCaseInsensitiveMap
        // The keys are case insensitive.
        System.out.println("Found: " + result.get("FIRSTNAME") + ' ' + result.get("lastname"));
    }

    /**
     * Run each different configuration, plus the Spring code.
     */
    public void run() {
        try {
            // Setters
            javax.sql.DataSource dataSource;
            dataSource = createDataSource1();
            runQuery(dataSource);

            // Properties instance created in code
            dataSource = createDataSource2();
            runQuery(dataSource);

            // Properties from nuodb.properties
            dataSource = createDataSource3();
            runQuery(dataSource);

            // Using Spring's JdbcTemplate
            runQuerySpring(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new JavaClient().run();
    }
}

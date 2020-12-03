package com.nuodb.docs.java;

import java.math.BigDecimal;

/* (C) Copyright NuoDB, Inc. 2011-2020  All Rights Reserved. */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

import com.nuodb.jdbc.DataSource;

class Account {
    String name;
    BigDecimal balance;
}

/** An example program for connecting to a NuoDB database server. */
public class HelloDB {

    /** The base URL for connecting to a local NuoDB database. */
    public static final String DATABASE_URL = "jdbc:com.nuodb://localhost/";

    public static final String[] ACCOUNT_OWNERS = { //
            "Ayesha", "Andrei", "Belkis", "Ching", "Dan", //
            "Kareem", "Leslie", "Matt", "Max", "Morgan", //
            "Nasir", "Omar", "Silas", "Stefan", "Taj", //
            "Tyler", "Uma", "Val", "Wang Li", "Zara" //
    };

    private DataSource dataSource;

    /** Main program for this example. */
    public static void main(String[] args) throws Exception {

        HelloDB helloDB = new HelloDB("dba", "goalie", "test");
        helloDB.createAccountTable();
        helloDB.populateDemo();
        helloDB.listAccounts(helloDB);
        helloDB.close();
    }

    /**
     * Creates an instance of HelloDB and connects to a local server, as the given
     * user, to work with the given named database
     *
     * @param user     the user name for the connection
     * @param password the password for the given user
     * @param dbName   the name of the database at the server to use
     */
    public HelloDB(String user, String password, String dbName) throws SQLException {
        Properties properties = new Properties();
        properties.put(DataSource.PROP_URL, DATABASE_URL + dbName);
        properties.put(DataSource.PROP_USER, user);
        properties.put(DataSource.PROP_PASSWORD, password);
        properties.put(DataSource.PROP_SCHEMA, "hello");

        dataSource = new DataSource(properties);
    }

    /** Closes the connection to the server. */
    public void close() throws Exception {
        dataSource.close();
    }

    /** Creates a simple three-column table: id-name-balance. */
    public void createAccountTable() throws SQLException {

        try (Connection dbConnection = dataSource.getConnection()) {
            dbConnection.setAutoCommit(false);

            try (Statement stmt = dbConnection.createStatement()) {
                stmt.execute("DROP TABLE accounts IF EXISTS");
                stmt.execute("CREATE TABLE accounts (id int primary key, name string)");
                stmt.execute("ALTER TABLE accounts ADD COLUMN balance DECIMAL(9,2)");
                dbConnection.commit();
            } catch (Exception exception) {
                System.out.println("Skipping table creation: " + exception.getMessage());
                dbConnection.rollback();
            }
        }
    }

    /**
     * Populate Accounts table with some sample data.
     * 
     * @throws SQLException
     */
    public void populateDemo() throws SQLException {

        Random r = new Random(42);
        int id = 1;

        try (Connection dbConnection = dataSource.getConnection()) {
            dbConnection.setAutoCommit(false);

            try (PreparedStatement stmt = dbConnection.prepareStatement( //
                    "insert into accounts (id, name, balance) values (?, ?, ?)")) {
                for (String name : ACCOUNT_OWNERS) {
                    stmt.setInt(1, id++);
                    stmt.setString(2, name);

                    int a = r.nextInt(1000000);
                    int b = r.nextInt(100);
                    stmt.setBigDecimal(3, new BigDecimal(a + "." + b));
                    stmt.addBatch();
                }

                stmt.executeBatch();
                dbConnection.commit();
            } catch (Exception exception) {
                System.out.println("Skipping populateDemo..." + exception.getMessage());
                dbConnection.rollback();
            }
        }
    }

    public void listAccounts(HelloDB helloDB) throws SQLException {
        for (int i = 1; i < ACCOUNT_OWNERS.length; i++) {
            Account account = helloDB.getAccount(i);

            if (account != null) {
                System.out.println(account.name + ":\n Account ID = " + i + "\n Balance = $" + account.balance + "\n");
            } else {
                System.out.println("Account ID " + i + ": NOT FOUND");
            }
        }
    }

    /**
     * Gets the name for the given id, or null if no name exists.
     *
     * @param id an identifier
     * @return the name associated with the identifier, or null
     */
    protected Account getAccount(int id) throws SQLException {
        try (Connection dbConnection = dataSource.getConnection()) {
            try (PreparedStatement pst = dbConnection.prepareStatement( //
                    "select name, balance from accounts where id=?")) {
                pst.setInt(1, id);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account();
                        account.name = rs.getString(1);
                        account.balance = rs.getBigDecimal(2);
                        return account;
                    }

                    return null;
                }
            }
        }
    }

}

package com.nuodb.samples;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A database trigger to calculate the MD5 unique hash for an account.
 * <p/>
 * Example:
 * <p/>
 * > CREATE TABLE account (id INT, address STRING NOT NULL, unique_hash STRING);
 * > DROP TRIGGER [IF EXISTS] set_record_unique_hash_on_create;
 * > DROP TRIGGER [IF EXISTS] set_record_unique_hash_on_update;
 * > DROP JAVACLASS account_unique_hash [IF EXISTS];
 * > CREATE JAVACLASS [IF NOT EXISTS] account_unique_hash FROM 'target/trigger-sample-1.0-SNAPSHOT.jar';
 * > CREATE TRIGGER set_record_unique_hash_on_create FOR account ACTIVE BEFORE INSERT LANGUAGE JAVA EXTERNAL 'account_unique_hash:com.nuodb.samples.SetRecordUniqueHash.setUniqueHash';
 * > CREATE TRIGGER set_record_unique_hash_on_update FOR account ACTIVE BEFORE UPDATE LANGUAGE JAVA EXTERNAL 'account_unique_hash:com.nuodb.samples.SetRecordUniqueHash.setUniqueHash';
 * > INSERT INTO account (id, address, unique_hash) VALUES (2, 'support@nuodb.com', null);
 * > SELECT * FROM account;
 * <p/>
 * > ID        ADDRESS               UNIQUE_HASH
 * > --- ----------------- --------------------------------
 * > 2   support@nuodb.com a5ce815389220a6e498162f539f88667
 */
public class SetRecordUniqueHash {
    /**
     * Generates a unique account hash.
     *
     * @param connection the related connection
     * @param oldResult  the previous record
     * @param newResult  the replacement record
     * @throws java.sql.SQLException we allow SQL exceptions to pass to the caller, but all other exceptions we log
     */
    public static void setUniqueHash(Connection connection, ResultSet oldResult, ResultSet newResult) throws SQLException {
        // note that you could use the connection to perform an in-proc query
        // using jdbc to other tables, infer information, and use that result
        // to derive a value you store elsewhere, but here we just keep things
        // simple for demonstration purposes.
        String message = newResult.getString("address"); // hard-coded column name
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            newResult.updateString("unique_hash", sb.toString());
        } catch (UnsupportedEncodingException ex) {
            // will never occur, if it does, it's a jvm error, at any rate, do the right thing...
            Logger.getLogger(SetRecordUniqueHash.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            // will never occur, if it does, it's a jvm error, at any rate, do the right thing...
            Logger.getLogger(SetRecordUniqueHash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

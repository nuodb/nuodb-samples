package com.nuodb.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A sample stored procedure that lists system tables.
 * <p/>
 * A couple of notes here which is not abundantly clear in the documentation.
 * <p/>
 * First, although Java has capabilities to handle results of differing shape,
 * the stored procedure implementation here is strictly statically typed, and
 * the shape of the returned result set fixed and rigid. Because of this, when
 * you declare the stored procedure, you must declare each and every input,
 * each and every return column, and the types of each. The implication is
 * clear, you cannot use a stored procedure to return results of varying
 * shape (return extra data should a particular condition manifest) and
 * therefore if you want to achieve code-reuse you have only one option:
 * return a fixed shape with nulls in the unneeded columns; otherwise you
 * must create one stored procedure for each combination of returns. The
 * documentation states:
 * <p/>
 * "simply assign the result set from a query to the results[0] field"
 * <p/>
 * The statement does not make any representations that shape or type can
 * vary -- they cannot. Errors will occur if shapes are mismatched with
 * declarations.
 * <p/>
 * So presented below is a recipe for a simple procedure and how to use it to
 * return the list of tables. The declarations are also provided in the doc
 * here as well as in a related install script you may want to use or examine.
 * <p/>
 * Example:
 * <p/>
 * > DROP PROCEDURE [IF EXISTS] get_system_tables_proc;
 * > DROP JAVACLASS sample_java_stored_procedures [IF EXISTS];
 * > CREATE JAVACLASS [IF NOT EXISTS] sample_java_stored_procedures FROM 'target/sample-java-stored-procedures-1.0-SNAPSHOT.jar';
 * > CREATE PROCEDURE get_system_tables_proc() RETURNS output (schema STRING, tablename STRING) LANGUAGE JAVA EXTERNAL 'sample_java_stored_procedures:com.nuodb.samples.SystemTables.getSystemTables';
 * > CALL get_system_tables_proc()
 */
public class SystemTables {

    private static final boolean USE_OPTION_1 = true;

    private static final String SQL = "SELECT * FROM system.tables";

    /**
     * Generates result that lists the system tables.
     *
     * @param connection the related connection
     * @param results    the generated results
     * @throws SQLException we allow SQL exceptions to pass to the caller, but all other exceptions we log
     */
    public static void getSystemTables(Connection connection, final ResultSet[] results) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(SQL);
            resultSet.
            while (resultSet.next()) {
                results[0].moveToInsertRow();
                results[0].updateString(1, resultSet.getString("schema"));
                results[0].updateString(2, resultSet.getString("tablename"));
                results[0].insertRow();
            }
        } finally {
            statement.close();
        }
    }
}

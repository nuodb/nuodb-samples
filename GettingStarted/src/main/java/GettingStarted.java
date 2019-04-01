
/* GettingStarted.java */

import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Created by nik on 27/01/2017.
 */
public class GettingStarted {

	private static final String DEFAULT_QUERY = "SELECT * from User.Teams WHERE year < ?";
	private static final String DEFAULT_SCHEMA = "User";
	private static final String DEFAULT_TIME = "1";

	private static Logger log = Logger.getLogger(GettingStarted.class.getName());

	public static void main(String[] args) {
		Properties props = parseCommandLine(args);

		if (props.getProperty("url") == null || props.getProperty("user") == null || props.get("password") == null) {
			System.out.println("Missing mandatory argument.");
			System.out.println();
			System.out.println("Usage: The following options are recognized:");
			System.out.println("    -url DATABASE_URL     (mandatory)");
			System.out.println("    -user USER            (mandatory)");
			System.out.println("    -password PASSWORD    (mandatory)");
			System.out.println("    -schema SCHEMA-NAME   (optional)");
			System.out.println("    -time TIME_IN_SECONDS (optional)");
			System.out.println("    -threads THREAD_COUNT (optional)");
			System.out.println("    -query SQL            (optional)");
			System.exit(0);
		}

		// Show properties being used.
		System.out.println("Using properties: ");
		props.entrySet().forEach(e -> System.out
				.println("    " + e.getKey() + " = " + (e.getKey().equals("password") ? "***" : e.getValue())));
		System.out.println();

		// Create the data source and thread-pool
		DataSource dataSource = new com.nuodb.jdbc.DataSource(props);
		ExecutorService executor = Executors.newCachedThreadPool();

		int threadCount = Integer.parseInt(props.getProperty("threads", "10"));
		for (int index = 0; index < threadCount; index++) {
			executor.submit(new Task(index, dataSource, props));
		}

		executor.shutdown();
	}

	/**
	 * This task runs the specified query as many times as it can in the time
	 * allowed. The default query is {@link GettingStarted#DEFAULT_QUERY}, the
	 * default time is {@link GettingStarted#DEFAULT_TIME}.
	 */
	private static class Task implements Runnable {

		private final int id;
		private final String query;
		private final DataSource dataSource;
		private final long timeout;

		private static AtomicLong counter = new AtomicLong(0);

		public Task(int id, DataSource dataSource, Properties props) {
			this.id = id;
			this.dataSource = dataSource;

			String query = props.getProperty("query", DEFAULT_QUERY);
			String schema = props.getProperty("schema", "");

			if (schema.length() > 0 && !schema.equals(DEFAULT_SCHEMA))
				query = query.replace(DEFAULT_SCHEMA, schema);

			this.query = query;

			long duration = 1000 * Long.parseLong(props.getProperty("time", DEFAULT_TIME));
			timeout = System.currentTimeMillis() + duration;
		}

		@Override
		public void run() {

			// count the parameters in the query
			int paramCount = 0;
			for (int cx = 0, max = query.length(); cx < max; cx++) {
				if (query.charAt(cx) == '?')
					paramCount++;
			}

			int retry = 0;

			// loop until timeout expires
			while (System.currentTimeMillis() < timeout) {

				int teId = -1;
				try (Connection conn = dataSource.getConnection();
						PreparedStatement sql = conn.prepareStatement(query)) {

					teId = com.nuodb.jdbc.Connection.class.cast(conn).getConnectedNodeId();

					for (int cycle = 0; cycle < 100; cycle++) {

						for (int paramNo = 1; paramNo <= paramCount; paramNo++) {
							sql.setInt(paramNo, new Random().nextInt(111) + 1900);
						}

						ResultSet rs = sql.executeQuery();

						// Iterate over the result set
						int count = 0;
						while (rs.next()) {
							count++;
						}
					}

					long current = counter.addAndGet(100);
					log.info(String.format("Ran 100 queries in task %d on TE %d; total=%d", id, teId, current));

				} catch (SQLTransientConnectionException transientFailure) {
					log.info(String.format("Transient error encountered - retrying: %s", transientFailure.toString()));
				} catch (SQLNonTransientConnectionException nonTransientFailuer) {
					log.info(String.format("Error making initial connection - retrying: %s",
							nonTransientFailuer.toString()));

					retry++;
					if (retry > 3) {
						log.warning("Too many retries - exiting");
						break;
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException interrupted) {
					}
				} catch (SQLException queryFailure) {
					log.info(String.format("Error executing query %s\n\t%s", query, queryFailure.toString()));
					break;
				} catch (Exception nonSqlError) {
					log.info(String.format("Processing error\n\t%s", nonSqlError.toString()));
					break;
				}
			}
		}
	}

	/**
	 * Parses command line arguments of form [value]</code> where <code>value</code>
	 * is optional. Once parsed:
	 * <ul>
	 * <li><code>-arg</code> sets property <code>arg=true</code>
	 * <li><code>-arg value</code> sets property <code>arg=value</code>
	 * </ul>
	 *
	 * @param args Command line arguments from main.
	 * @return Corresponding properties.
	 */
	public static Properties parseCommandLine(String[] args) {

		// create an empty Properties object
		Properties props = new Properties();

		// iterate the command arguments
		String name = null;
		for (String arg : args) {

			// arg begins with '-', so it's an option name
			if (arg.charAt(0) == '-') {
				if (name != null) {
					log.info(String.format("setting %s=true", name));
					props.setProperty(name, "true");
				}

				name = arg.substring(1);
			}

			// arg comes immediately after an option name
			else if (name != null) {
				log.info(String.format("setting %s=%s", name, arg));
				props.setProperty(name, arg);
				name = null;
			}

			// no preceding name - error
			else {
				throw new IllegalArgumentException(String.format("Option value with no name: ", arg));
			}
		}

		return props;
	}
}

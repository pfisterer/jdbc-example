package jdbcexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jdbcexample.logging.LoggingConfiguration;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.LoggerFactory;

public class Main {
	static org.slf4j.Logger log;

	static {
		LoggingConfiguration.configureLog4JFromClasspath();
	}

	public static void main(String[] args) throws IOException {
		log = LoggerFactory.getLogger(Main.class);
		final CommandLineOptions options = parseCmdLineOptions(args);
		LoggingConfiguration.setLog4RootLogLevel(options);
		log.debug("Startup");

		try {
			log.debug("Connecting to {} as {}", options.databaseJdbcUrl, options.databaseUsername);

			Connection connection = DriverManager.getConnection(options.databaseJdbcUrl, options.databaseUsername,
					options.databasePassword);

			displayConnectionInformation(connection);
			runDatabaseConsole(connection);

			connection.close();

		} catch (SQLException ex) {
			log.error("SQLException: " + ex, ex.getMessage());
			log.error("SQLState: " + ex, ex.getSQLState());
			log.error("VendorError: " + ex, ex.getErrorCode());
		}

		log.debug("Shutdown");
	}

	private static void displayConnectionInformation(Connection connection) throws SQLException {
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		log.info("Metadata: {} = {}", "URL", databaseMetaData.getURL());
		log.info("Metadata: {} = {}", "Username", databaseMetaData.getUserName());
		log.info("Metadata: {} = {}", "Product Name", databaseMetaData.getDatabaseProductName());
		log.info("Metadata: {} = {}", "Product Version", databaseMetaData.getDatabaseProductVersion());
		log.info("Metadata: {} = {}", "Driver Name", databaseMetaData.getDriverName());
		log.info("Metadata: {} = {}", "Driver Version", databaseMetaData.getDriverVersion());
		log.info("Metadata: {} = {}", "Quote String", databaseMetaData.getIdentifierQuoteString());
		log.info("Metadata: {} = {}", "Default Transaction Isolation", databaseMetaData.getDefaultTransactionIsolation());
		log.info("Metadata: {} = {}", "JDBC Version",
				databaseMetaData.getJDBCMajorVersion() + "." + databaseMetaData.getJDBCMinorVersion());
		log.info("Metadata: {} = {}", "Numeric functions", databaseMetaData.getNumericFunctions());
		log.info("Metadata: {} = {}", "String functions", databaseMetaData.getStringFunctions());
		log.info("Metadata: {} = {}", "Time and date functions", databaseMetaData.getTimeDateFunctions());
	}

	private static void runDatabaseConsole(Connection connection) throws IOException, SQLException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		log.info("Enter SQL statement (or exit to quit) and press ENTER");

		while (!"exit".equals(line = br.readLine())) {

			// Run Select statements and display output
			if (line.toUpperCase().startsWith("SELECT")) {

				Statement sqlStmt = connection.createStatement();

				// execute the statement and check whether there is a result
				ResultSet resultSet = sqlStmt.executeQuery(line);

				log.info("Result: \n{}", resultSetToString(resultSet, true, ", "));
				resultSet.close();

				// Run Update, Delete, and Insert statements and display output
			} else if (line.toUpperCase().startsWith("UPDATE") || line.toUpperCase().startsWith("DELETE")
					|| line.toUpperCase().startsWith("INSERT")) {
				Statement sqlStmt = connection.createStatement();

				int affectedRows = sqlStmt.executeUpdate(line);

				sqlStmt.close();

				log.info("{} row(s) affected", affectedRows);
			}
		}

	}

	private static String resultSetToString(ResultSet resultSet, boolean includeColumnNames, String divider) throws SQLException {

		int columnCount = resultSet.getMetaData().getColumnCount();
		StringBuilder s = new StringBuilder();

		while (resultSet.next()) {
			for (int i = 1; i <= columnCount; ++i) {
				if (i > 1)
					s.append(divider);

				if (includeColumnNames)
					s.append(resultSet.getMetaData().getColumnLabel(i) + " = ");

				s.append(resultSet.getString(i));
			}
			s.append("\n");
		}

		return s.toString();
	}

	private static CommandLineOptions parseCmdLineOptions(final String[] args) {
		CommandLineOptions options = new CommandLineOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
			if (options.help)
				printHelpAndExit(parser);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printHelpAndExit(parser);
		}

		return options;
	}

	private static void printHelpAndExit(CmdLineParser parser) {
		System.err.print("Usage: java " + Main.class.getCanonicalName());
		parser.printSingleLineUsage(System.err);
		System.err.println();
		parser.printUsage(System.err);
		System.exit(1);
	}

}

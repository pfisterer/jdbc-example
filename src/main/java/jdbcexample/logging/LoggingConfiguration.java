package jdbcexample.logging;

import java.io.InputStream;
import java.util.Properties;

import jdbcexample.CommandLineOptions;
import jdbcexample.Main;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

public class LoggingConfiguration {

	public static void configureLog4JFromClasspath() {
		InputStream log4JPropertiesStream = Main.class.getClassLoader().getResourceAsStream("log4j.properties");

		if (log4JPropertiesStream != null) {
			try {
				Properties properties = new Properties();
				properties.load(log4JPropertiesStream);
				PropertyConfigurator.configure(properties);
			} catch (Exception e) {
				System.err
						.println("Tried to load log4j configuration from classpath, resulting in the following exception: {}"
								+ e);
				System.err.println("Using default logging configuration.");
			}
		}
	}

	public static void setLog4RootLogLevel(CommandLineOptions options) {
		if (options.logLevel != null) {
			org.apache.log4j.Logger.getRootLogger().setLevel(options.logLevel);
		} else if (options.verbose) {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
		}
	}

}

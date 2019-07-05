/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */
package logTool;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

//Logger define
public class allLogger {

	/** fileHandler for logging to file */
	static private FileHandler fileHandler;

	public static void setup(final String logFile) throws IOException {
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		logger.setUseParentHandlers(false); // don't log the console output to file
		logger.setLevel(Level.INFO);
		fileHandler = new FileHandler(logFile, true);
		fileHandler.setFormatter(new logFormat());
		logger.addHandler(fileHandler);
	}
}

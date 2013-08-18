/*
 * Created on 22. March 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class that extends Formatter and defines the format of the console output of the
 * logging messages.
 * 
 * @author Sita Lange
 *
 */
public class ConsoleFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		String statusline = "";
		statusline += record.getMessage();
		statusline += "\n";
		return statusline;
	}

}

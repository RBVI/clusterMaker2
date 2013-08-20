/* 
 * Created on 5. April 2008
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**'
 * A filter for logging, which filters out double messages that come after each other.
 * 
 * @author Sita Lange
 *
 */
public class UniqueFilter implements Filter {
	String msg = null;

	/**
	 * Returns true if it the first instance of the record message.
	 */
	public boolean isLoggable(LogRecord record) {
		String newMsg = record.getMessage();
		if (msg != null && msg.equals(newMsg)) {
			return false; // duplicate
		} else {
			msg = newMsg;
			return true; // new message
		}
	}
}

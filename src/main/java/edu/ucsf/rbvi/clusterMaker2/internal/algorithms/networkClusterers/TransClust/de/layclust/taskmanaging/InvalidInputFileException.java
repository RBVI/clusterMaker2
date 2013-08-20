/*
 * Created on 16. November
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

/**
 * This Exception is for invalid input files.
 * 
 * @author sita
 *
 */
public class InvalidInputFileException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidInputFileException(String message) {
		super(message);		
	}

}

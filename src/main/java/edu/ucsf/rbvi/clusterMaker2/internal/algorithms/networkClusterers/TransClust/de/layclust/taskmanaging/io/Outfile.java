/* 
 * Created on 21. January 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * An super class for all output files that contains the basic instantiation and
 * writing methods for file writing.
 * @author Sita Lange
 * 2008
 *
 */
public class Outfile {
	
	protected static final String NL=TaskConfig.NL;
	protected final static String TAB = "\t";
	
	protected BufferedWriter bw = null;
	protected String fileName = null;

	/**
	 * Creates a file instance: a BufferedWriter instance and sets the file name.
	 * @param filename The name of the file to be created.
	 */
	public void instantiateFile(String filename) {
		this.fileName = filename;
		if(new File(this.fileName).exists())new File(this.fileName).delete();
		try {
			this.bw = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			System.err.println("Unable to write this file: "+this.fileName+".");
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Creates the header for the particular type of output file. Gets all of the
//	 * necessary information from the TaskConfig.
//	 */
//	public abstract void createHeader();
	
	/**
	 * Flushes and closes the BufferedWriter for this File object.
	 */
	public void closeFile() {
		try {
			this.bw.flush();
				this.bw.close();
		} catch (IOException e) {
			System.err.println("Unable to write this file: " + this.fileName+".");
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends the given string to the end of the text.
	 * @param s The string to be appended.
	 */
	public void print(String s) {
		try {
				this.bw.write(s);
		} catch (IOException e) {
			System.err.println("Unable to write this file: " + this.fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints the given String at the end of the file and ends with a new line.
	 * @param s The String to be printed.
	 */
	public void println(String s) {
		try {
				this.bw.write(s + NL);
		} catch (IOException e) {
			System.err.println("Unable to write this file: " + this.fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends a new line to the end of the file.
	 *
	 */
	public void printnewln(){
		try {
			this.bw.write(NL);
		} catch (IOException e) {
			System.err.println("Unable to write this file: " + this.fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Flushes the printed text into the file.
	 *
	 */
	public void flushbw(){
		try {
			bw.flush();
		} catch (IOException e) {
			System.err.println("Unable to write this file: " + this.fileName);
			e.printStackTrace();
		}
	}
}

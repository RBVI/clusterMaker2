/* 
 * Created on 28. March 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * This class extends the superclass Outfile, which implements the basic reading and writing
 * methods for files. This class contains methods that are especially for generating config 
 * files with respect to the format used in this program.
 * 
 * @author Sita Lange
 *
 */
public class ConfigFile extends Outfile {

	/**
	 * Prints the header of the config file.
	 *
	 */
	public void printHeader(){
		this.printHashRow();
		this.printHash();
		this.print(TaskConfig.NAME);
		this.print(TAB+TAB+TAB+TAB+TAB);
		this.printHash();
		this.printnewln();
		this.printHash();
		this.print(TaskConfig.NAME_EXTENDED);
		this.printHash();
		this.printnewln();
		this.printHashRow();
		this.printHash();
		this.print("Copyright (c) 2009 by: ");
		this.printnewln();
		for(String author : TaskConfig.AUTHORS) {
			this.printHln(author);
		}
		this.printHash();
		this.printnewln();
		this.printHln("Bielefeld University");
		this.printHln("Center for Biotechnology (CeBiTec)");

	}
	
	public void printSubHeader(String subheader){
		this.printHashRow();
		this.printHash();
		this.print(subheader);
		this.print(TAB+TAB+TAB);
		this.printHash();
		this.printnewln();
		this.printHashRow();
	}
	
	public void printParameter(String key, String value){
		this.print(key);
		this.print("=");
		this.print(value);
		this.printnewln();
	}
	
	private void printHashRow(){
		this.println("###############################################################");
	}
	
	/**
	 * Prints a hash plus a space.
	 *
	 */
	private void printHash(){
		this.print("# ");
	}
	
//	private void printNLHash(){
//		this.print(NL);
//		this.printHash();
//	}
	
	private void printHln(String string){
		this.printHash();
		this.println(string);
	}
	

}

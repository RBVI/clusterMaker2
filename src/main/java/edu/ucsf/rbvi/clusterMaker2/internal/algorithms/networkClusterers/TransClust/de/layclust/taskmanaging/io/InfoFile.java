package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class InfoFile extends Outfile {
	
	private static StringBuffer projectDetails = null;
	private static StringBuffer projectResults = null;
	
	public void createAndCloseInfoFile(){
		printInfoHeader();
		printProjectDetails();
		printProjectResultSummary();
		closeFile();	
	}
	
	public void printInfoHeader(){

		SimpleDateFormat fmt = new SimpleDateFormat();
		fmt.applyPattern( "MM'/'dd'/'yyyy '-' HH:mm" );
		Calendar cal = new GregorianCalendar();
		
		String dateTime = fmt.format(cal.getTime());
		
		println("-------------------------------------------------------------------------------");
		println(TaskConfig.NAME + " v" + TaskConfig.VERSION + NL);
		println("Copyright (c) 2009 by");
		
//		println("Contact:");
		for (int i = 0; i < TaskConfig.AUTHORS.length; i++) {
			println(TaskConfig.AUTHORS[i]);
		}	
		
		println("Developed by ");
		for (int i = 0; i < TaskConfig.DEVELOPERS.length; i++) {
			println(TaskConfig.DEVELOPERS[i]);
		}
		
		
		
		println("-------------------------------------------------------------------------------");
		println(NL);
		println("InfoFile from: " + dateTime);
		println(NL);
		
	}
	
	public static void apppendLnToProjectDetails(String string){
		if(TaskConfig.info){
			if(projectDetails == null){
				projectDetails = new StringBuffer();
			}
			projectDetails.append(string);
			projectDetails.append(NL);
		}
	}
	
	public static void appendToProjectDetails(String string){
		if(TaskConfig.info){
			if(projectDetails == null){
				projectDetails = new StringBuffer();
			}
			projectDetails.append(string);
		}
	}
	
	public static void appendNewLnToProjectDetails(){
		if(TaskConfig.info){
			if(projectDetails == null){
				projectDetails = new StringBuffer();
			}
			projectDetails.append(NL);
		}
	}
	
	public static void appendHeaderToProjectDetails(String header){
		if(TaskConfig.info){
			if(projectDetails == null){
				projectDetails = new StringBuffer();
			}
			projectDetails.append(NL);
			projectDetails.append("##  ");
			projectDetails.append(header);
			projectDetails.append("  ##");
			projectDetails.append(NL);
		}
	}
	
	public static void appendLnProjectResults(String string){
		if(TaskConfig.info){
			if(projectResults == null){
				projectResults = new StringBuffer();
			}
			projectResults.append(string);
			projectResults.append(NL);
		}
	}
	
	private void printProjectDetails(){
		if(TaskConfig.info){
			printnewln();
	
			println("######  PROJECT DETAILS  ######");
	
			printnewln();
			print(projectDetails.toString());
			printnewln();
		}

	}
	
	private void printProjectResultSummary(){
		if(TaskConfig.info){

			printnewln();
			println("###### RESULTS SUMMARY ######");
			printnewln();
			print(projectResults.toString());
			printnewln();
		}
	}
	
	/**
	 * Assigns a new StringBuffer to the results and information buffers for a new project/run.
	 *
	 */
	public static void clearData(){
		InfoFile.projectDetails = new StringBuffer();
		InfoFile.projectResults = new StringBuffer();
	}
}

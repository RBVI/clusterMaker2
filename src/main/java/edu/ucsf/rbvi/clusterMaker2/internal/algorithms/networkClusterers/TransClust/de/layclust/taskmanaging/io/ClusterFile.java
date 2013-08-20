/*
 * Created on 22. January 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a subclass of the Outfile class. It is for writing the clustering
 * results into a file.
 * 
 * @author Sita Lange
 * 
 */
public class ClusterFile extends Outfile {

	private int currentClusterNo = 0;

	/**
	 * This method takes the file from the pre-processing step with all
	 * transitive connectedd components already written in the clusters format
	 * and transfers these to the result clusters file. Clusters format: // TODO
	 * 
	 * @param tccFile
	 *            The file including the transitive connected components.
	 */
	public void printPreProcessingClusters(String tccFile) {

		if (tccFile != null && !tccFile.equals("")) {

			/*
			 * Check whether the tcc file is written in the old FORCE format.
			 * This included various amount of metadata lines at the beginning
			 * and then the cluster size, tab, followed by all objects in this
			 * cluster also tab delimited. This format should be written
			 */
			if (tccFile.endsWith("1.tcc")) {
				printOldTccFormat(tccFile);
			}

			else {
				BufferedReader tccBuffer;
				try {
					/* read file */
					tccBuffer = new BufferedReader(new FileReader(tccFile));

					String line = "";
					String clusterNo = "";
					/* only read lines in this format */
					Pattern objectLine = Pattern.compile(".+\\t(\\d+)");
					Matcher objectMatcher;
					while ((line = tccBuffer.readLine()) != null) {
						objectMatcher = objectLine.matcher(line);
						if (objectMatcher.find()) {
							/*
							 * if the format is correct, then write this line in
							 * the clusters file as it is
							 */
							println(line);
							clusterNo = objectMatcher.group(1);
						}
					}

					/*
					 * get last cluster number and add one and set this as
					 * current cluster no
					 */
					currentClusterNo = Integer.parseInt(clusterNo) + 1;

				} catch (IOException e) {
					System.err.println("Unable to read this file:  " + tccFile);
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
		flushbw();
	}

	/**
	 * Reads and prints out the old .tcc format, which ends in 1.tcc.
	 * 
	 * @param tccFile
	 *            The input transitive closed components file (.tcc)
	 */
	private void printOldTccFormat(String tccFile) {
		BufferedReader tccBuffer;
		try {
			/* read file */
			tccBuffer = new BufferedReader(new FileReader(tccFile));

			String line = "";
			int clusterNo = 0;
			/* only read lines in this format */
			Pattern objectLine = Pattern.compile("^\\d+\\t(.+)\\s*");
			Matcher objectMatcher;
			String objectString = "";
			while ((line = tccBuffer.readLine()) != null) {
				objectMatcher = objectLine.matcher(line);
				if (objectMatcher.find()) {
					/* get object IDs for the cluster of this line */
					objectString = objectMatcher.group(1);
					String[] objects = objectString.split(TAB);
					for (int i = 0; i < objects.length; i++) {
						this.println(objects[i] + TAB + clusterNo);
					}
					/* increase cluster no for next line */
					++clusterNo;
				}
			}

			/* set currentClusterNo ready for next cluster entry */
			this.currentClusterNo = clusterNo;

		} catch (IOException e) {
			System.err.println("Unable to read this file:  " + tccFile);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Appends the given cluster to the clusters file with the current cluster
	 * number and increases the cluster number by one when finished.
	 * 
	 * @param cluster
	 *            String array of the object names in one cluster.
	 */
	public synchronized void printCluster(ArrayList<String> cluster) {
		StringBuffer clusterbuffer = new StringBuffer();
		for (String objectString : cluster) {
			/*
			 * objectString could contain more than one object name - separate
			 * these!
			 */
			String[] separatedObjectString = objectString.split(TAB);
			for (int i = 0; i < separatedObjectString.length; i++) {
				clusterbuffer.append(separatedObjectString[i]);
				clusterbuffer.append(TAB);
				clusterbuffer.append(this.currentClusterNo);
				clusterbuffer.append(NL);
			}
		}
		/* print cluster and increase the current cluster no. by one */
		this.print(clusterbuffer.toString());
		++this.currentClusterNo;
	}

	/**
	 * @return the currentClusterNo
	 */
	public int getCurrentClusterNo() {
		return currentClusterNo;
	}

	/**
	 * @param currentClusterNo
	 *            the currentClusterNo to set
	 */
	public void setCurrentClusterNo(int currentClusterNo) {
		this.currentClusterNo = currentClusterNo;
	}

}

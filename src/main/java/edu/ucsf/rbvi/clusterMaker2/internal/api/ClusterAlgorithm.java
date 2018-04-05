package edu.ucsf.rbvi.clusterMaker2.internal.api;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import java.lang.Math;
import java.util.Arrays;

public interface ClusterAlgorithm extends Task {

	// Property change
	public static String CLUSTER_COMPUTED = "CLUSTER_COMPUTED";

	public String getShortName();
	public String getName();

	/**
 	 * Hooks for the results.  This is so results can
 	 * be returned to commands.
 	 *
 	 * @return cluster results.
 	 */
	public ClusterResults getResults();

}



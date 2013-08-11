package edu.ucsf.rbvi.clusterMaker2;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import java.lang.Math;
import java.util.Arrays;

public interface ClusterAlgorithm {
	
	// Property change
		public static String CLUSTER_COMPUTED = "CLUSTER_COMPUTED";

		/**
	 	 * Get the short name of this algorithm
	 	 *
	 	 * @return short-hand name for algorithm
	 	 */
		public String getShortName();

		/**
	 	 * Get the name of this algorithm
	 	 *
	 	 * @return name for algorithm
	 	 */
		public String getName();

		/**
	 	 * Get the context object for this algorithm.  This object should contain
	 	 * all of the tunables that the algorithm requires.  
	 	 *
	 	 * @return context object
	 	 */
		public Object getContext();

		/**
	 	 * This method is used to re-initialize the properties for an algorithm.  This
	 	 * might be used, for example, by an external command, or when a new network
	 	 * is loaded.
	 	 */
		public void initializeProperties();

		/**
		 * This method is used to signal a running cluster algorithm to stop
		 *
		 */
		public void halt();

		/**
		 * This is the main interface to trigger a cluster to compute
		 *
		 * @param monitor a TaskMonitor
		 */
		public void doCluster(CyNetwork network, TaskMonitor monitor);

		/**
	 	 * Hooks for the visualizer
	 	 *
	 	 * @return the visualizer or null if one doesn't exist
	 	 */
		public ClusterViz getVisualizer();

		/**
	 	 * Hooks for the results.  This is so results can
	 	 * be returned to commands.
	 	 *
	 	 * @return cluster results.
	 	 */
		public ClusterResults getResults();

		/**
	 	 * Returns 'true' if this algorithm has already been run on this network
	 	 *
	 	 * @return true if the algorithm attributes exist
	 	 */
		public boolean isAvailable();
}



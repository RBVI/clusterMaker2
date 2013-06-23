package org.cytoscape.myapp.internal.algorithms;



//import cytoscape.task.ui.JTaskConfig;
//import clusterMaker.ui.ClusterViz;

import org.cytoscape.work.TaskMonitor;

import java.beans.PropertyChangeSupport;
import java.lang.Math;
import java.util.Arrays;

import javax.swing.JPanel;

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
	 	 * Get the settings panel for this algorithm
	 	 *
	 	 * @return settings panel
	 	 */
		public JPanel getSettingsPanel();

		/**
		 * This method is used to ask the algorithm to revert its settings
		 * to some previous state.  It is called from the settings dialog
		 * when the user presses the "Cancel" button.
		 *
		 * NOTE: ClusterAlgorithmBase implements this on behalf of all its subclasses
		 * by using Java Preferences.
		 */
		public void revertSettings();

	  /**
		 * This method is used to ask the algorithm to get its settings
		 * from the settings dialog.  It is called from the settings dialog
		 * when the user presses the "Done" or the "Execute" buttons.
		 *
		 * NOTE: ClusterAlgorithmBase implements this on behalf of all its subclasses
		 * by using Java Preferences.
		 */
		public void updateSettings();
		public void updateSettings(boolean force);

	  /**
		 * This method is used to ask the algorithm to get all of its tunables
		 * and return them to the caller.
		 *
		 * @return the cluster properties for this algorithm
		 *
		 */
		public ClusterProperties getSettings();

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
		public void doCluster(TaskMonitor monitor);

		/**
		 * This call returns a JTaskConfig option
		 *
		 * @return the JTaskconfig
		 */
		public JTaskConfig getDefaultTaskConfig();

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

		/**
	 	 * This is a hook to notify interested parties that we have finished
	 	 * computing a cluster.  The major use is for clusters with visualizers
	 	 * to inform UI components that the visualizer can now be launched.
	 	 */
		public PropertyChangeSupport getPropertyChangeSupport();

}



package edu.ucsf.rbvi.clusterMaker2;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

/**
 * This interface provides hooks for a general return value from the
 * various cluster algorithms.
 */
public interface ClusterResults {

	/**
 	 * Return the text representation of the results suitable for display on
 	 * a command line or simple log.
 	 * 
 	 * @return text representation of results
 	 */
	public String toString();

	/**
 	 * This method will be used by tasks to show the results in the task dialog.
 	 * This allows for separation of warnings, errors, and informational messages.
 	 *
 	 * @param tm the TaskMonitor to be used
 	 */
	// public void showResults(TaskMonitor tm);

	/**
 	 * The actual results of the cluster operation.  This is designed to
 	 * be easily used by tasks as their requisite "getResults" method for
 	 * ObservableTask
 	 *
 	 * @return the list of clusters with each cluster consisting of a list of nodes.
 	 * If this is a fuzzy cluster, nodes may appear in multiple lists.
 	 */
	public Object getResults(Class requestedType);

	/**
 	 * The calculated "score" of the clustering.  This might be a silhouette, a
 	 * cluster coefficient, or a p-value of some form.
 	 *
 	 * @return the score
 	 */
	public double getScore();
}

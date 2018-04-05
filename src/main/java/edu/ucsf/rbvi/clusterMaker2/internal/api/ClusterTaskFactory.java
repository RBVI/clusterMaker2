package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;

public interface ClusterTaskFactory extends TaskFactory {
	public enum ClusterType { NETWORK, ATTRIBUTE, FILTER, DIMRED, UI };

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
 	 * Hooks for the visualizer
 	 *
 	 * @return the visualizer or null if one doesn't exist
 	 */
	public ClusterViz getVisualizer();

	/**
	 * Returns true if this algorithm has been run on this network
	 * and the results are available
	 *
	 * @param network the network to look at
	 * @return true if the clustering information is available
	 */
	public boolean isAvailable(CyNetwork network);

	/**
 	 * Returns the list of types this algorithm supports.  This allows us
	 * to have a single cluster algorithm that supports multiple types
	 * of clusterers (e.g. AutoSOME supports both network and attribute
	 * partitioning.
 	 *
 	 * @return the list of cluster types
 	 */
	public List<ClusterType> getTypeList();

	/**
	 * Returns the long description for this cluster algorithm.
	 *
	 * @return the long description
	 */
	public String getLongDescription();

	/**
	 * Returns a string that represents an example of what the JSON return
	 * for this task factory is.
	 *
	 * @return example JSON string
	 */
	public String getExampleJSON();

	/**
	 * Returns "true" if this task factory supports returns.
	 *
	 * @return "true" if this task factory support JSON, "false" otherwise.
	 */
	default public String getSupportsJSON() { return "true"; }
}



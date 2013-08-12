package edu.ucsf.rbvi.clusterMaker2;

import org.cytoscape.model.CyNetwork;

/**
 * This interface provides some general methods for cluster context
 * objects
 */
public interface ClusterAlgorithmContext {

	public CyNetwork getNetwork();
	public void setNetwork(CyNetwork network);

}

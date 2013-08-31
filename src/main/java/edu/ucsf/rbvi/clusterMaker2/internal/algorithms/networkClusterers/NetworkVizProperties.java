package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers;

import org.cytoscape.work.Tunable;

public class NetworkVizProperties {
	@Tunable(description="Create new clustered network", groups={"Visualization Options"}, 
	         gravity=150.0)
	public boolean showUI = false;

	@Tunable(description="Restore inter-cluster edges after layout", 
	         groups={"Visualization Options"}, gravity=151.0)
	public boolean restoreEdges = false;

	public NetworkVizProperties() {
	}

	public NetworkVizProperties(NetworkVizProperties clone) {
		showUI = clone.showUI;
		restoreEdges = clone.restoreEdges;
	}

}

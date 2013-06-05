package edu.ucsf.rbvi.clusterMaker2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.ClusterManager;

public class ClusterMakerSettingsTask extends AbstractTask {
	CyNetwork network;
	ClusterManager clusterManager;

	// @ContainsTunables
	// public ClusterSettings clusterSettings = null;

	public ClusterMakerSettingsTask(CyNetwork network, ClusterManager clusterManager) {
		this.network = network;
		this.clusterManager = clusterManager;
		// At some point, we'll probably need this or something like it....
		// clusterSettings = new ClusterMakerSettings(network, clusterManager);
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
	}

	@ProvidesTitle
	public String getTitle() {
		return "ClusterMaker Settings";
	}

}

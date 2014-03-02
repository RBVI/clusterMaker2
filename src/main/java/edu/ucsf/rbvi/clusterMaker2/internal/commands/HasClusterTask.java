package edu.ucsf.rbvi.clusterMaker2.internal.commands;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

//clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;

public class HasClusterTask extends AbstractTask implements ObservableTask {
	ClusterManager clusterManager;
	Boolean hasCluster = false;

	@Tunable(description="Network to look for cluster in", context="nogui")
	public CyNetwork network;

	@Tunable(description="Algorithm", context="nogui")
	public String algorithm;

	public HasClusterTask(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public void run(TaskMonitor monitor) {
		if (network == null)
			network = clusterManager.getNetwork();

		hasCluster = false;
		if (algorithm != null) {
			ClusterTaskFactory algTF = clusterManager.getAlgorithm(algorithm);
			if (algTF != null) {
				if(algTF.isAvailable(network)) {
					hasCluster = true;
				} else {
					monitor.showMessage(TaskMonitor.Level.ERROR, "This network doesn't have a '"+algorithm+"' cluster");
				}
			} else {
				monitor.showMessage(TaskMonitor.Level.ERROR, "Can't find algorithm: '"+algorithm+"'");
			}
		} else {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Algorithm must be specified");
		}
	}

	public Object getResults(Class type) {
		if (type.equals(Boolean.class)) {
			return Boolean.valueOf(hasCluster);
		}
		return Boolean.toString(hasCluster);
	}
}

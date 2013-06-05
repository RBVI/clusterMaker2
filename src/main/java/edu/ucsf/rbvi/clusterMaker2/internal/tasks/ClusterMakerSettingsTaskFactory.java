package edu.ucsf.rbvi.clusterMaker2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.ClusterManager;


public class ClusterMakerSettingsTaskFactory extends AbstractTaskFactory 
                                             implements NetworkTaskFactory {

	ClusterManager clusterManager;
	
	public ClusterMakerSettingsTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetwork network) {
		return true;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new ClusterMakerSettingsTask(network, clusterManager));
	}

}

package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

//clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ClusterManagerImpl;


public class LinkSelectionTaskFactory implements NetworkTaskFactory   {
	ClusterManager clusterManager;
	
	public LinkSelectionTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new LinkSelectionTask(network, clusterManager));
	}

	public boolean isReady(CyNetwork network) {
		if (((ClusterManagerImpl)clusterManager).isLinked(network))
			return false;
		return true;
	}
}
	
	




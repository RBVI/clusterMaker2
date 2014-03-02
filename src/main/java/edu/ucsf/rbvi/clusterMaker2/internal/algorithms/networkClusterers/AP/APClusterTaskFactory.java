package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class APClusterTaskFactory extends AbstractClusterTaskFactory {
	ClusterManager clusterManager;
	APContext context = null;
	
	public APClusterTaskFactory(ClusterManager clusterManager) {
		this.context = new APContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return APCluster.SHORTNAME;};
	public String getName() {return APCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return true;
	}

	public List<ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new APCluster(context, clusterManager));
	}
	
}
	
	




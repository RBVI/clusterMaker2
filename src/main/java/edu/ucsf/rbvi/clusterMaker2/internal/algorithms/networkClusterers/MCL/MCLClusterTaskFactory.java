package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class MCLClusterTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	MCLContext context = null;
	
	public MCLClusterTaskFactory(ClusterManager clusterManager) {
		context = new MCLContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return MCLCluster.SHORTNAME;};
	public String getName() {return MCLCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return true;
	}

	public boolean isAvailable() {
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCLCluster(context, clusterManager));
	}
	
}
	
	




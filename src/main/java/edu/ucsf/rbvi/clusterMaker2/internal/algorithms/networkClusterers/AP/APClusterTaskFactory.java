package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class APClusterTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	APContext context = null;
	
	public APClusterTaskFactory(ClusterManager clusterManager) {
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

	public boolean isAvailable() {
		return false;
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		if (context == null)
			context = new APContext();
		else
			context = new APContext(context);

		return new TaskIterator(new APCluster(context, clusterManager));
	}
	
}
	
	




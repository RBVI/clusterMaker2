package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.ConnectedComponents;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class ConnectedComponentsTaskFactory extends AbstractClusterTaskFactory {
	ClusterManager clusterManager;
	ConnectedComponentsContext context = null;
	
	public ConnectedComponentsTaskFactory(ClusterManager clusterManager) {
		context = new ConnectedComponentsContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return ConnectedComponentsCluster.SHORTNAME;};
	public String getName() {return ConnectedComponentsCluster.NAME;};

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
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new ConnectedComponentsCluster(context, clusterManager));
	}
	
}
	
	




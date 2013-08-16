package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class FCMCLusterTaskFactory implements ClusterTaskFactory {
	
	ClusterManager clusterManager;
	FCMContext context = null;
	
	public void FCMClusterTaskFactory(ClusterManager clusterManager) {
		context = new FCMContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return FCMCluster.SHORTNAME;};
	public String getName() {return FCMCluster.NAME;};

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

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.NETWORK); 
	}
	
	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		context = new FCMContext(context);
		return new TaskIterator(new FCMCluster(context, clusterManager));
	}
	

}

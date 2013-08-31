package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class SCPSClusterTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	SCPSContext context = null;
	
	public SCPSClusterTaskFactory(ClusterManager clusterManager) {
		context = new SCPSContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return SCPSCluster.SHORTNAME;};
	public String getName() {return SCPSCluster.NAME;};

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
		return new TaskIterator(new SCPSCluster(context, clusterManager));
	}
	
}
	
	




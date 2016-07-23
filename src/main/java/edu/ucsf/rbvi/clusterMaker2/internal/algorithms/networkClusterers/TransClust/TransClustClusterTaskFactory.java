package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class TransClustClusterTaskFactory extends AbstractClusterTaskFactory {
	TransClusterContext context = null;
	
	public TransClustClusterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new TransClusterContext();
	}
	
	public String getShortName() {return TransClustCluster.SHORTNAME;};
	public String getName() {return TransClustCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new TransClustCluster(context, clusterManager));
	}
	
}
	
	




package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class APClusterTaskFactory extends AbstractClusterTaskFactory {
	APContext context = null;
	
	public APClusterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		this.context = new APContext();
	}
	
	public String getShortName() {return APCluster.SHORTNAME;};
	public String getName() {return APCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new APCluster(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Affinity propagation (AP) is a clustering algorithm based on the concept of "+
		       "'message passing' between data points. Unlike clustering algorithms such "+
		       "as k-means or k-medoids, affinity propagation does not require the number "+
		       "of clusters to be determined or estimated before running the algorithm. "+
		       "Similar to k-medoids, affinity propagation finds 'exemplars', members of "+
		       "the input set that are representative of clusters";
	}
}

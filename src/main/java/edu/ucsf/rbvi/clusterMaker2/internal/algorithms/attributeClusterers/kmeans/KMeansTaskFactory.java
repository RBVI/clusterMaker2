package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class KMeansTaskFactory extends AbstractClusterTaskFactory {
	KMeansContext context = null;
	
	public KMeansTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new KMeansContext();
	}
	
	public String getShortName() {return KMeansCluster.SHORTNAME;};
	public String getName() {return KMeansCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.ATTRIBUTE); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new KMeansCluster(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "K-Means clustering is a partitioning algorithm that divides "+
		       "the data into k non-overlapping clusters, where k is an input "+
		       "parameter. One of the challenges in k-Means clustering is that "+
		       "the number of clusters must be chosen in advance. A simple rule "+
		       "of thumb for choosing the number of clusters is to take the square "+
		       "root of half of the number of nodes. Beginning with clusterMaker version "+
		       "1.6, this value is provided as the default value for the number of "+
		       "clusters. Beginning with clusterMaker version 1.10, k may be estimated "+
		       "by iterating over number of estimates for k and choosing the value that "+
		       "maximizes the silhouette for the cluster result. Since this is an iterative "+
		       "approach, for larger clusters, it can take a very long time, even though "+
		       "the process is multi-threaded.";
	}
}

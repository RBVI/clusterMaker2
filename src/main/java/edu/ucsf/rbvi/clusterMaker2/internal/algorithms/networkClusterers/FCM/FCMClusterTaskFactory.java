package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class FCMClusterTaskFactory extends AbstractClusterTaskFactory {
	FCMContext context = null;
	
	public FCMClusterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new FCMContext();
	}
	
	public String getShortName() {return FCMCluster.SHORTNAME;};
	public String getName() {return FCMCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new FCMCluster(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "The fuzzy c-means algorithm is very similar to the k-means algorithm:"+
		       "<br/><br/>"+
		       "* Choose a number of clusters."+
		       "* Assign coefficients randomly to each data point for being in the clusters."+
		       "* Repeat until the algorithm has converged (that is, the coefficients' "+
		       "change between two iterations is no more than some epsilon, the given "+
		       "sensitivity threshold) :"+
		       " * Compute the centroid for each cluster (shown below)."+
		       " * For each data point, compute its coefficients of being in the clusters.";
	}

	@Override
	public String getExampleJSON() {
		return AbstractClusterResults.getFuzzyExampleJSON();
	}
}

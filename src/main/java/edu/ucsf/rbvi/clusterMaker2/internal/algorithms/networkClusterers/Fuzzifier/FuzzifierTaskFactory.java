package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class FuzzifierTaskFactory extends AbstractClusterTaskFactory {
	FuzzifierContext context = null;
	
	public FuzzifierTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new FuzzifierContext();
	}
	
	public String getShortName() {return Fuzzifier.SHORTNAME;};
	public String getName() {return Fuzzifier.NAME;};

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
		return new TaskIterator(new Fuzzifier(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "This algorithm will take the output of a network clusterer and 'fuzzify' it "+
		       "by finding the centroid of each cluster as determined by the previous algorithm "+
		       "and then calculating the distance between every node and that centroid.  "+
		       "Each node is then assigned a proportional membership to each cluster.  A cutoff "+
		       "value determines the minimum proportion required to be considered as part of "+
		       "a cluster.";
	}

	@Override
	public String getExampleJSON() {
		return AbstractClusterResults.getFuzzyExampleJSON();
	}

}

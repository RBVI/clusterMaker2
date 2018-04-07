package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class MCLClusterTaskFactory extends AbstractClusterTaskFactory {
	MCLContext context = null;
	
	public MCLClusterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new MCLContext();
	}
	
	public String getShortName() {return MCLCluster.SHORTNAME;};
	public String getName() {return MCLCluster.NAME;};

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
		return new TaskIterator(new MCLCluster(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "The MCL algorithm is short for the Markov Cluster Algorithm, a fast and "+
		       "scalable unsupervised cluster algorithm for graphs (also known as networks) "+
		       "based on simulation of (stochastic) flow in graphs. "+
		       "The MCL algorithm simulates random walks within a graph by "+
		       "alternation of two operators called expansion and inflation . Expansion "+
		       "coincides with taking the power of a stochastic matrix using the normal matrix "+
		       "product (i.e. matrix squaring). Inflation corresponds with taking the Hadamard "+
		       "power of a matrix (taking powers entrywise), followed by a scaling step, such "+
		       "that the resulting matrix is stochastic again, i.e. the matrix elements (on each "+
		       "column) correspond to probability values.";
	}
}

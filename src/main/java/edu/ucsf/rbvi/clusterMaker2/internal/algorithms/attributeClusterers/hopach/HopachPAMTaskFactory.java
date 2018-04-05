package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class HopachPAMTaskFactory extends AbstractClusterTaskFactory {
	HopachPAMContext context = null;
	
	public HopachPAMTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new HopachPAMContext();
	}
	
	public String getShortName() {return HopachPAMClusterer.SHORTNAME;};
	public String getName() {return HopachPAMClusterer.NAME;};

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
		return new TaskIterator(new HopachPAMClusterer(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "The HOPACH clustering algorithm builds a hierarchical tree "+
		       "of clusters by recursively partitioning a data set, while "+
		       "ordering and possibly collapsing clusters at each level. The "+
		       "algorithm uses the Mean/Median Split Silhouette (MSS) criteria "+
		       "to identify the level of the tree with maximally homogeneous "+
		       "clusters. It also runs the tree down to produce a final ordered "+
		       "list of the elements. The non-parametric bootstrap allows one to "+
		       "estimate the probability that each element belongs to each "+
		       "cluster (fuzzy clustering).";
	}
}

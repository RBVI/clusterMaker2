package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class BiMineTaskFactory extends AbstractClusterTaskFactory {
	BiMineContext context = null;
	
	public BiMineTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new BiMineContext();
	}
	
	public String getShortName() {return BiMine.SHORTNAME;};
	public String getName() {return BiMine.NAME;};

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
		return new TaskIterator(new BiMine(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "BiMine is an enumeration algorithm for biclustering of attribute data. "+
		       "The algorithm is based on three original features. First, BiMine relies "+
		       "on an evaluation function called Average Spearman's rho (ASR). Second, BiMine "+
		       "uses a tree structure, called Bicluster Enumeration Tree (BET), to represent the "+
		       "different biclusters discovered during the enumeration process. Third, to avoid "+
		       "the combinatorial explosion of the search tree, BiMine introduces a parametric "+
		       "rule that allows the enumeration process to cut tree branches that cannot "+
		       "lead to good biclusters.";
	}

}

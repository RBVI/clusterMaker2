package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.CuttingEdge;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class CuttingEdgeFilterTaskFactory extends AbstractClusterTaskFactory {
	CuttingEdgeContext context = null;
	
	public CuttingEdgeFilterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new CuttingEdgeContext();
	}
	
	public String getShortName() {return CuttingEdgeFilter.SHORTNAME;};
	public String getName() {return CuttingEdgeFilter.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.FILTER); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new CuttingEdgeFilter(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Cutting Edge is a relatively coarse filter that discards clusters "+
		       "that don't meet the criteria. The criteria is defined as a density "+
		       "value where the cluster density is equal to the number of intra-cluster "+
		       "edges divided by the total number of edges (both intra-cluster and "+
		       "inter-cluster) that connect to nodes belonging to this cluster. If the "+
		       "density is less than the value, the cluster is dropped.";
	}
}

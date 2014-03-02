package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class FilterTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	
	public FilterTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return "filter"; }
	public String getName() {return "--- Network Filter Algorithms ---";}

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return false;
	}

	public boolean isAvailable(CyNetwork network) {
		return false;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.FILTER); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return null;
	}
	
}
	
	




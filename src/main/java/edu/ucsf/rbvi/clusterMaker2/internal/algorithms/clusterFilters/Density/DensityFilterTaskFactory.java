package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.Density;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class DensityFilterTaskFactory extends AbstractClusterTaskFactory {
	DensityContext context = null;
	
	public DensityFilterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new DensityContext();
	}
	
	public String getShortName() {return DensityFilter.SHORTNAME;};
	public String getName() {return DensityFilter.NAME;};

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
		return new TaskIterator(new DensityFilter(context, clusterManager));
	}
	
}
	
	




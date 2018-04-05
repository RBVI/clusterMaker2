package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BicFinder;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class BicFinderTaskFactory extends AbstractClusterTaskFactory {
	BicFinderContext context = null;
	
	public BicFinderTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new BicFinderContext();
	}
	
	public String getShortName() {return BicFinder.SHORTNAME;};
	public String getName() {return BicFinder.NAME;};

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
		return new TaskIterator(new BicFinder(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "BicFinder is an algorithm the discovers biclusters in attribute data.  "+
		       "BicFinder relies on an evaluation function called Average Correspondence "+
		       "Similarity Index (ACSI) to assess the coherence of a given bicluster and "+
		       "utilizes a directed acyclic graph to construct its biclusters.";
	}

}

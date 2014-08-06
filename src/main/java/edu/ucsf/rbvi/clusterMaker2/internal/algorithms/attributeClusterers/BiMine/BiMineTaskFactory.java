package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class BiMineTaskFactory extends AbstractClusterTaskFactory {

	ClusterManager clusterManager;
	BiMineContext context = null;
	
	public BiMineTaskFactory(ClusterManager clusterManager) {
		context = new BiMineContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return BiMine.SHORTNAME;};
	public String getName() {return BiMine.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return true;
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

}

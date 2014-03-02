package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class AttributeClusterTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	
	public AttributeClusterTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return "attribute"; }
	public String getName() {return "--- Attribute Cluster Algorithms ---";}

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
		return Collections.singletonList(ClusterType.ATTRIBUTE); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return null;
	}
	
}
	
	




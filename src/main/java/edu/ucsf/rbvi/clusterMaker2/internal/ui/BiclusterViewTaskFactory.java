package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;

public class BiclusterViewTaskFactory implements ClusterVizFactory   {
	ClusterManager clusterManager;
	
	public BiclusterViewTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public String getName() {
		return BiclusterView.NAME;
	}

	public String getShortName() {
		return BiclusterView.SHORTNAME;
	}

	public ClusterViz getVisualizer() {
		// return new BiclusterViewTask(true);
		return null;
	}

	public boolean isReady() {
		CyNetwork myNetwork = clusterManager.getNetwork();
		return BiclusterView.isReady(myNetwork);
	}

	public boolean isAvailable(CyNetwork network) {
		if (network == null) return false;
		return BiclusterView.isReady(network);
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new BiclusterView(clusterManager));
	}

	@Override
	public String getSupportsJSON() { return "false"; }

	@Override
	public String getExampleJSON() { return ""; }

	@Override
	public String getLongDescription() { return "Display the results for bi-cluster calculations"; }

}

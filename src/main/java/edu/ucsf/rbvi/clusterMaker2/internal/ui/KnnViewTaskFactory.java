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

public class KnnViewTaskFactory implements ClusterVizFactory   {
	ClusterManager clusterManager;
	
	public KnnViewTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public String getName() {
		return KnnView.NAME;
	}

	public String getShortName() {
		return KnnView.SHORTNAME;
	}

	public ClusterViz getVisualizer() {
		// return new KnnViewTask(true);
		return null;
	}

	public boolean isReady() {
		CyNetwork myNetwork = clusterManager.getNetwork();
		return KnnView.isReady(myNetwork);
	}

	public boolean isAvailable(CyNetwork network) {
		if (network == null) return false;
		return KnnView.isReady(network);
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new KnnView(clusterManager));
	}
	
}
	
	




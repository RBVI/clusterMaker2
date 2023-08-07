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

public class NewNetworkViewFactory implements ClusterVizFactory   {
	ClusterManager clusterManager;
	NewNetworkViewContext context = null;
	boolean checkAvailable;
	
	public NewNetworkViewFactory(ClusterManager clusterManager, boolean checkAvailable) {
		// context = new NewNetworkViewContext();
		this.clusterManager = clusterManager;
		this.checkAvailable = checkAvailable;
	}
	
	public String getName() {
		if (checkAvailable) {
			return NewNetworkView.CLUSTERNAME;
		} else {
			return NewNetworkView.ATTRIBUTENAME; 
		}
	}

	public String getShortName() {
		if (checkAvailable) {
			return NewNetworkView.CLUSTERSHORTNAME;
		} else {
			return NewNetworkView.ATTRSHORTNAME; 
		}
	}

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		if (!checkAvailable)
			return true;
		return NewNetworkView.isReady(clusterManager.getNetwork(), clusterManager);
	}

	public boolean isAvailable(CyNetwork network) {
		if (!checkAvailable)
			return true;

		if (network == null)
			return false;

		return NewNetworkView.isReady(network, clusterManager);
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new NewNetworkView(clusterManager, checkAvailable, true));
	}

	@Override
	public String getSupportsJSON() { return "true"; }

	@Override
	public String getExampleJSON() { return "{\"view\": 101}"; }

	@Override
	public String getLongDescription() { 
		return "Create a new network from the results of a network partition cluster algorithm. "+
		       "Edges between clusters are suppressed before the layout is applied and optionally redrawn afterwards.";
	}
	
}
	
	




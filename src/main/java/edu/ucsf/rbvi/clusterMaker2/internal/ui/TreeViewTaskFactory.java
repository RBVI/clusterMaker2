package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class TreeViewTaskFactory implements ClusterVizFactory   {
	ClusterManager clusterManager;
	
	public TreeViewTaskFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public String getName() {
		return TreeView.NAME;
	}

	public String getShortName() {
		return TreeView.SHORTNAME;
	}

	public ClusterViz getVisualizer() {
		// return new TreeViewTask(true);
		return null;
	}

	public boolean isReady() {
		CyNetwork myNetwork = clusterManager.getNetwork();
		return TreeView.isReady(myNetwork);
	}

	public boolean isAvailable(CyNetwork network) {
		if (network == null) return false;
		return TreeView.isReady(network);
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new TreeView(clusterManager));
	}

	@Override
	public String getLongDescription() { 
		return "Display the tree view for hierarchical clusters using the JTreeView widget."; 
	}
}

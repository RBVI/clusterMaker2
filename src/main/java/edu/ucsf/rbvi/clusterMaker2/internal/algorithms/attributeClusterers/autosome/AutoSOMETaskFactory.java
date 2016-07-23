package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class AutoSOMETaskFactory extends AbstractClusterTaskFactory {
	AutoSOMEContext context = null;
	boolean heatmap = true;
	
	public AutoSOMETaskFactory(ClusterManager clusterManager, boolean heatmap) {
		super(clusterManager);
		context = new AutoSOMEContext();
		this.heatmap = heatmap;
	}
	
	public String getShortName() {
		if (heatmap)
			return AutoSOMECluster.SHORTNAME;
		else
			return AutoSOMECluster.NET_SHORTNAME;
	}

	public String getName() {
		if (heatmap)
			return AutoSOMECluster.NAME;
		else
			return AutoSOMECluster.NET_NAME;
	}

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		if (heatmap)
			return Collections.singletonList(ClusterType.ATTRIBUTE); 
		else
			return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new AutoSOMECluster(context, clusterManager, heatmap));
	}
	
}
	
	




package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class tSNETaskFactory extends AbstractClusterTaskFactory{

	
	tSNEContext context = null;

	public tSNETaskFactory(ClusterManager clusterManager){
		super(clusterManager);
		context = new tSNEContext();
	}

	public String getShortName() {return tSNE.SHORTNAME;};
	public String getName() {return tSNE.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMERD); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new tSNE(context, clusterManager));
	}
    
}

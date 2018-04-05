package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class PAMTaskFactory extends AbstractClusterTaskFactory {
	PAMContext context = null;
	
	public PAMTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new PAMContext();
	}
	
	public String getShortName() {return PAMClusterer.SHORTNAME;};
	public String getName() {return PAMClusterer.NAME;};

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
		return new TaskIterator(new PAMClusterer(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Partitioning around medoids (PAM) is a k-medoid clustering algorithm that "+
		       "uses a greedy search to find a good, but not necessarily optimum solution "+
		       "k-cluster problem.";
	}
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class SCPSClusterTaskFactory extends AbstractClusterTaskFactory {
	SCPSContext context = null;
	
	public SCPSClusterTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new SCPSContext();
	}
	
	public String getShortName() {return SCPSCluster.SHORTNAME;};
	public String getName() {return SCPSCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SCPSCluster(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "SCPS (Spectral Clustering of Protein Sequences) is an efficient "+
		       "and user-friendly implementation of a spectral method for inferring "+
		       "protein families. The method uses only pairwise sequence similarities, "+
		       "and is therefore practical when only sequence information is available."; 
	}

}

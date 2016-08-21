package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCA;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCAContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class PCoATaskFactory extends AbstractClusterTaskFactory{

	PCoAContext context = null;

	public PCoATaskFactory(ClusterManager clusterManager){
		super(clusterManager);
		context = new PCoAContext();
	}

	public String getShortName() {return PCoA.SHORTNAME;};
	public String getName() {return PCoA.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PCoA(context, clusterManager));
	}


}

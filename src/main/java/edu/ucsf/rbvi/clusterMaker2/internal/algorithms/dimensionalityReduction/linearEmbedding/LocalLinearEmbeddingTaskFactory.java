package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.linearEmbedding;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class LocalLinearEmbeddingTaskFactory extends AbstractClusterTaskFactory {
	LocalLinearEmbeddingContext context = null;
	final CyServiceRegistrar registrar;
	
	public LocalLinearEmbeddingTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new LocalLinearEmbeddingContext();
		this.registrar = registrar;
	}
	
	public String getName() {return LocalLinearEmbedding.NAME;}
	
	public String getShortName() {return LocalLinearEmbedding.SHORTNAME;}
	
	@Override
	public String getLongDescription() {
		return "Locally linear embedding (LLE) seeks a lower-dimensional projection of the data which preserves distances within local neighborhoods. It can be thought of as a series of local Principal Component Analyses which are globally compared to find the best non-linear embedding.";
	}
	
	@Override
	public ClusterViz getVisualizer() {
		return null;
	}
	
	@Override
	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new LocalLinearEmbedding(context, clusterManager, registrar));
	}
}

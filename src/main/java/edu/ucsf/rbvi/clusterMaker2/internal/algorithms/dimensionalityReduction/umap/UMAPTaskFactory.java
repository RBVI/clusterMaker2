package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.umap;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class UMAPTaskFactory extends AbstractClusterTaskFactory {
	UMAPContext context = null;
	final CyServiceRegistrar registrar;
	
	public UMAPTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new UMAPContext();
		this.registrar = registrar;
	}
	
	public String getName() {return UMAP.NAME;}
	
	public String getShortName() {return UMAP.SHORTNAME;}
	
	@Override
	public String getLongDescription() {
		return "Uniform Manifold Approximation and Projection (UMAP) is a dimension reduction "+
           "technique that can be used for visualisation similarly to t-SNE, but also for "+
           "general non-linear dimension reduction. The algorithm is founded on three assumptions "+
           "about the data:"+
           "<ol><li>The data is uniformly distributed on a Riemannian manifold;</li>"+
           "<li>The Riemannian metric is locally constant (or can be approximated as such);</li>"+
           "<li>The manifold is locally connected.</li></ol>"+
           "From these assumptions it is possible to model the manifold with a fuzzy topological structure. The embedding is found by searching for a low dimensional projection of the data that has the closest possible equivalent fuzzy topological structure."+
           "<br/><br/>"+
           "The details for the underlying mathematics can be found in the paper on ArXiv:"+
           "<br/><br/>"+
           "McInnes, L, Healy, J, <i>UMAP: Uniform Manifold Approximation and Projection for Dimension Reduction</i>, ArXiv e-prints 1802.03426, 2018";
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
		return new TaskIterator(new UMAP(context, clusterManager, registrar));
	}
}

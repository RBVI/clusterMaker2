package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.umap;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
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
		return "";
	}
	
	@Override
	public ClusterViz getVisualizer() {
		return null;
	}
	
	@Override
	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.NETWORK);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new UMAP(context, clusterManager, registrar));
	}
}

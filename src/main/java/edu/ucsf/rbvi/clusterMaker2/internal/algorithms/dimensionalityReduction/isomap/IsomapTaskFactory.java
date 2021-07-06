package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.isomap;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class IsomapTaskFactory extends AbstractClusterTaskFactory {
	IsomapContext context = null;
	final CyServiceRegistrar registrar;
	
	public IsomapTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new IsomapContext();
		this.registrar = registrar;
	}
	
	public String getName() {return Isomap.NAME;}
	
	public String getShortName() {return Isomap.SHORTNAME;}
	
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
		return Collections.singletonList(ClusterType.DIMRED);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new Isomap(context, clusterManager, registrar));
	}
}

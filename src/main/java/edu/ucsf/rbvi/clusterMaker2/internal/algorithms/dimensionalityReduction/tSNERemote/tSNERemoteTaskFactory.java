package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.tSNERemote;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class tSNERemoteTaskFactory extends AbstractClusterTaskFactory {
	tSNERemoteContext context = null;
	final CyServiceRegistrar registrar;
	
	public tSNERemoteTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new tSNERemoteContext();
		this.registrar = registrar;
	}
	
	public String getName() {return tSNERemote.NAME;}
	
	public String getShortName() {return tSNERemote.SHORTNAME;}
	
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
		return new TaskIterator(new tSNERemote(context, clusterManager, registrar));
	}
}
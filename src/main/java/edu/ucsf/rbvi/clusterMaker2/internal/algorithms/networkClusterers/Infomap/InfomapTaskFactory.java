package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Infomap;

import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

public class InfomapTaskFactory extends AbstractClusterTaskFactory{
	InfomapContext context = null;
	final CyServiceRegistrar registrar;
	
	public InfomapTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new InfomapContext();
		this.registrar = registrar;
	}
	
	public String getName() {return Infomap.NAME;}
	
	public String getShortName() {return Infomap.SHORTNAME;}
	
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
		return new TaskIterator(new Infomap(context, clusterManager, registrar));
	}


}

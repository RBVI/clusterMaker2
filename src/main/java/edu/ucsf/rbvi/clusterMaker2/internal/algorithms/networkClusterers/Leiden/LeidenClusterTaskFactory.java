package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Leiden;

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

public class LeidenClusterTaskFactory extends AbstractClusterTaskFactory implements NetworkTaskFactory{
	LeidenContext context = null;
	final CyServiceRegistrar registrar;
	
	public LeidenClusterTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new LeidenContext();
		this.registrar = registrar;
	}
	
	public String getName() {return LeidenCluster.NAME;}
	
	public String getShortName() {return LeidenCluster.SHORTNAME;}
	
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
		return new TaskIterator(new LeidenCluster(context, clusterManager, registrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new LeidenCluster(context, clusterManager, registrar));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		if (network != null) return true;
		
		return false;
	}
}

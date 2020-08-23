package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FastGreedy;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Infomap.Infomap;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Infomap.InfomapContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

public class FastGreedyTaskFactory extends AbstractClusterTaskFactory {
	FastGreedyContext context = null;
	final CyServiceRegistrar registrar;
	
	public FastGreedyTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new FastGreedyContext();
		this.registrar = registrar;
	}
	
	public String getName() {return FastGreedy.NAME;}
	
	public String getShortName() {return FastGreedy.SHORTNAME;}
	
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
		return new TaskIterator(new FastGreedy(context, clusterManager, registrar));
	}
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FastGreedy;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FastGreedy.FastGreedy;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FastGreedy.FastGreedyContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

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

	@Override
	public String getLongDescription() {
    return "This function implements the fast greedy modularity optimization algorithm for "+
           "finding community structure, see A Clauset, MEJ Newman, C Moore: Finding community "+
           "structure in very large networks, http://www.arxiv.org/abs/cond-mat/0408187 for the details.";
  }
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Multilevel;

	import java.util.Collections;
	import java.util.List;

	import org.cytoscape.service.util.CyServiceRegistrar;
	import org.cytoscape.work.TaskIterator;

	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Multilevel.MultilevelCluster;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Multilevel.MultilevelContext;
	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

	public class MultilevelClusterTaskFactory extends AbstractClusterTaskFactory {
		MultilevelContext context = null;
		final CyServiceRegistrar registrar;
		
		public MultilevelClusterTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
			super(clusterManager);
			context = new MultilevelContext();
			this.registrar = registrar;
		}
		
		public String getName() {return MultilevelCluster.NAME;}
		
		public String getShortName() {return MultilevelCluster.SHORTNAME;}
		
		@Override
		public String getLongDescription() {
			return "This function implements the multi-level "+
			       "modularity optimization algorithm for finding "+
			       "community structure, see Blondel, V. D., "+
			       "Guillaume, J.-L., Lambiotte, R., & Lefebvre, "+
			       "E. (2008). Fast unfolding of communities "+
			       "in large networks. Journal of Statistical "+
			       "Mechanics: Theory and Experiment, 10008(10), 6. "+
			       "https://doi.org/10.1088/1742-5468/2008/10/P10008 "+
			       "for the details (preprint: "+
			       "http://arxiv.org/abs/0803.0476). The algorithm "+
			       "is sometimes known as the 'Louvain' algorithm. "+
             "<br/><br/>"+
             "The algorithm is based on the modularity measure and a hierarchical "+
             "approach. Initially, each vertex is assigned to a community on its own. In "+
             "every step, vertices are re-assigned to communities in a local, greedy "+
             "way: in a random order, each vertex is moved to the community with which "+
             "it achieves the highest contribution to modularity. When no vertices "+
             "can be reassigned, each community is considered a vertex on its own, "+
             "and the process starts again with the merged communities. The process "+
             "stops when there is only a single vertex left or when the modularity "+
             "cannot be increased any more in a step. "+
             "<br/><br/>"+
             "The resolution parameter gamma allows finding communities at different "+
             "resolutions. Higher values of the resolution parameter typically result in "+
             "more, smaller communities. Lower values typically result in fewer, larger "+
             "communities. The original definition of modularity is retrieved when "+
             "setting gamma=1. Note that the returned modularity value is calculated "+
             "using the indicated resolution parameter. See igraph_modularity() for "+
             "more details. This function was contributed by Tom Gregorovic. ";
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
			return new TaskIterator(new MultilevelCluster(context, clusterManager, registrar));
		}
	}


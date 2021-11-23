package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LabelPropagation;

	import java.util.Collections;
	import java.util.List;

	import org.cytoscape.service.util.CyServiceRegistrar;
	import org.cytoscape.work.TaskIterator;

	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LabelPropagation.LabelPropagation;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LabelPropagation.LabelPropagationContext;
	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

	public class LabelPropagationTaskFactory extends AbstractClusterTaskFactory {
		LabelPropagationContext context = null;
		final CyServiceRegistrar registrar;
		
		public LabelPropagationTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
			super(clusterManager);
			context = new LabelPropagationContext();
			this.registrar = registrar;
		}
		
		public String getName() {return LabelPropagation.NAME;}
		
		public String getShortName() {return LabelPropagation.SHORTNAME;}
		
		@Override
		public String getLongDescription() {
			return "This function implements the community detection method described in: Raghavan, U.N. and Albert, R. and "+
             "Kumara, S.: Near linear time algorithm to detect community structures in large-scale networks. Phys Rev E "+
             "76, 036106. (2007). This version extends the original method by the ability to take edge weights into consideration "+
             "and also by allowing some labels to be fixed."+
             ""+
             "Weights are taken into account as follows: when the new label of node i is determined, the algorithm iterates over "+
             "all edges incident on node i and calculate the total weight of edges leading to other nodes with label 0, 1, 2, ..., "+
             "k - 1 (where k is the number of possible labels). The new label of node i will then be the label whose edges "+
             "(among the ones incident on node i) have the highest total weight";
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
			return new TaskIterator(new LabelPropagation(context, clusterManager, registrar));
		}
	}

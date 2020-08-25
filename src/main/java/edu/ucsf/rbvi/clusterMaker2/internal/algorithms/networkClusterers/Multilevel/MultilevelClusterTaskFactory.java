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
			return new TaskIterator(new MultilevelCluster(context, clusterManager, registrar));
		}
	}


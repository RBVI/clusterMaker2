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
			return new TaskIterator(new LabelPropagation(context, clusterManager, registrar));
		}
	}

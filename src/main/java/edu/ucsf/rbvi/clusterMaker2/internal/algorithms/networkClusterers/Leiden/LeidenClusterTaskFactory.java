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

public class LeidenClusterTaskFactory extends AbstractClusterTaskFactory{
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
		return "This function implements the Leiden "+
        	 "algorithm for finding community structure, "+
		       "see Traag, V. A., Waltman, L., & van Eck, "+
		       "N. J. (2019). From Louvain to Leiden: guaranteeing "+
		       "well-connected communities. Scientific reports, 9(1), "+
		       "5233. http://dx.doi.org/10.1038/s41598-019-41695-z "+
           ""+
           "It is similar to the multilevel algorithm, often called the "+
           "Louvain algorithm, but it is faster and yields higher quality "+
           "solutions. It can optimize both modularity and the Constant Potts "+
           "Model, which does not suffer from the resolution-limit (see preprint "+
           "http://arxiv.org/abs/1104.3083). "+
           ""+
           "The Leiden algorithm consists of three phases: (1) local moving of nodes, "+
           "(2) refinement of the partition and (3) aggregation of the network based "+
           "on the refined partition, using the non-refined partition to create "+
           "an initial partition for the aggregate network. In the local move "+
           "procedure in the Leiden algorithm, only nodes whose neighborhood has "+
           "changed are visited. The refinement is done by restarting from a singleton "+
           "partition within each cluster and gradually merging the subclusters. When "+
           "aggregating, a single cluster may then be represented by several nodes "+
           "(which are the subclusters identified in the refinement). "+
           " "+
           "The Leiden algorithm provides several guarantees. The Leiden algorithm "+
           "is typically iterated: the output of one iteration is used as the input "+
           "for the next iteration. At each iteration all clusters are guaranteed to "+
           "be connected and well-separated. After an iteration in which nothing has "+
           "changed, all nodes and some parts are guaranteed to be locally optimally "+
           "assigned. Finally, asymptotically, all subsets of all clusters are "+
           "guaranteed to be locally optimally assigned. For more details, please "+
           "see Traag, Waltman & van Eck (2019). ";
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


}

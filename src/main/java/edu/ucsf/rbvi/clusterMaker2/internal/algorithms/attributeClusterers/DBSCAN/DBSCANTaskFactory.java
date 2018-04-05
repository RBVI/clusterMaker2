package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN.DBSCANContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

public class DBSCANTaskFactory extends AbstractClusterTaskFactory {
	DBSCANContext context = null;
	
	public DBSCANTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new DBSCANContext();
	}
	
	public String getShortName() {return DBSCAN.SHORTNAME;};
	public String getName() {return DBSCAN.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.ATTRIBUTE); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new DBSCAN(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Density-based spatial clustering of applications with noise (DBSCAN) is a data "+
		       "clustering algorithm proposed by Martin Ester, Hans-Peter Kriegel, Jörg Sander "+
		       "and Xiaowei Xu in 1996. It is a density-based clustering algorithm: given a "+
		       "set of points in some space, it groups together points that are closely packed "+
		       "together (points with many nearby neighbors), marking as outliers points that "+
		       "lie alone in low-density regions (whose nearest neighbors are too far away).";
	}
	
}


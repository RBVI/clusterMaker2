package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.DBScan;

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

public class DBScanClusterTaskFactory extends AbstractClusterTaskFactory{
	DBScanContext context = null;
	final CyServiceRegistrar registrar;
	
	public DBScanClusterTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new DBScanContext();
		this.registrar = registrar;
	}
	
	public String getName() {return DBScanCluster.NAME;}
	
	public String getShortName() {return DBScanCluster.SHORTNAME;}
	
	@Override
	public String getLongDescription() {
		return "This function implements the DBScan (Density-Based Spaciel Clustering of Applications with Noise)"+
        	 "algorithm for finding community structure.  It finds core samples of high density and expands clusters"+
           "from them.  Good for data which contains clusters of similar density."+
		       "see Ester, M., H.P.Kriegel, J. Sander, and X. Xu, "+
		       "A Density-Based Algorithm for Discovering Clusters in Large Spatial Databases with Noise."+
		       "In Proceedings of the 2nd International Conference on Knowledge Discovery and Data Mining, "+
           "Portland, OR, AAAI Press, pp. 226–231. 1996"+
           "<br/><br/>"+
           "<p>The DBSCAN algorithm views clusters as areas of high"+
           "density separated by areas of low density. Due to this rather"+
           "generic view, clusters found by DBSCAN can be any shape,"+
           "as opposed to k-means which assumes that clusters are convex"+
           "shaped. The central component to the DBSCAN is the concept"+
           "of core samples, which are samples that are in areas of"+
           "high density. A cluster is therefore a set of core samples,"+
           "each close to each other (measured by some distance measure)"+
           "and a set of non-core samples that are close to a core sample"+
           "(but are not themselves core samples). There are two parameters"+
           "to the algorithm, min_samples and eps, which define formally"+
           "what we mean when we say dense. Higher min_samples or lower"+
           "eps indicate higher density necessary to form a cluster."+
            "<p>More formally, we define a core sample as being a sample"+
            "in the dataset such that there exist min_samples other samples"+
            "within a distance of eps, which are defined as neighbors"+
            "of the core sample. This tells us that the core sample is"+
            "in a dense area of the vector space. A cluster is a set of"+
            "core samples that can be built by recursively taking a core"+
            "sample, finding all of its neighbors that are core samples,"+
            "finding all of their neighbors that are core samples,"+
            "and so on. A cluster also has a set of non-core samples,"+
            "which are samples that are neighbors of a core sample in the"+
            "cluster but are not themselves core samples. Intuitively,"+
            "these samples are on the fringes of a cluster."+
            "<p>Any core sample is part of a cluster, by definition. Any"+
            "sample that is not a core sample, and is at least eps in"+
            "distance from any core sample, is considered an outlier by"+
            "the algorithm."+
            "While the parameter min_samples primarily controls how"+
            "tolerant the algorithm is towards noise (on noisy and large"+
            "data sets it may be desirable to increase this parameter),"+
            "the parameter eps is crucial to choose appropriately for the"+
            "data set and distance function and usually cannot be left"+
            "at the default value. It controls the local neighborhood"+
            "of the points. When chosen too small, most data will not be"+
            "clustered at all (and labeled as -1 for “noise”). When"+
            "chosen too large, it causes close clusters to be merged"+
            "into one cluster, and eventually the entire data set to be"+
            "returned as a single cluster. Some heuristics for choosing"+
            "this parameter have been discussed in the literature, for"+
            "example based on a knee in the nearest neighbor distances plot.";
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
		return new TaskIterator(new DBScanCluster(context, clusterManager, registrar));
	}


}

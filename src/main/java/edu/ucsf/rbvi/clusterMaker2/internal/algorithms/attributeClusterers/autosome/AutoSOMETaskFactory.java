package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class AutoSOMETaskFactory extends AbstractClusterTaskFactory {
	AutoSOMEContext context = null;
	boolean heatmap = true;
	
	public AutoSOMETaskFactory(ClusterManager clusterManager, boolean heatmap) {
		super(clusterManager);
		context = new AutoSOMEContext();
		this.heatmap = heatmap;
	}
	
	public String getShortName() {
		if (heatmap)
			return AutoSOMECluster.SHORTNAME;
		else
			return AutoSOMECluster.NET_SHORTNAME;
	}

	public String getName() {
		if (heatmap)
			return AutoSOMECluster.NAME;
		else
			return AutoSOMECluster.NET_NAME;
	}

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		if (heatmap)
			return Collections.singletonList(ClusterType.ATTRIBUTE); 
		else
			return Collections.singletonList(ClusterType.NETWORK); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new AutoSOMECluster(context, clusterManager, heatmap));
	}

	@Override
	public String getLongDescription() {
		return "AutoSOME clustering is a cluster algorithm that functions both as an "+
		       "attribute cluster algorithm as well as a network cluster algorithm. "+
		       "The AutoSOME algorithm revolves around the use of a Self-Organizing "+
		       "Map (SOM). Unsupervised training of the SOM produces a low-dimensional "+
		       "reprentation of input space. In AutoSOME, that dimensionally reduced "+
		       "spaced is compresed into a 2D representation of similarities between "+
		       "neighboring nodes across the SOM network. These nodes are further "+
		       "distorted in 2D space based on their density of similarity to each "+
		       "other.  Afterwards, a minimum spanning tree is built from rescaled "+
		       "node coordinates. Monte-Carlo sampling is used to calculate p-values "+
		       "for all edges in the tree. Edges below an inputed P-value Threshold are "+
		       "then deleted, leaving behind the clustering results. AutoSOME clustering "+
		       "may be repeated multiple times to minimize stochastic-based output variation. "+
		       "The clustering results stabilize at maximum quality with an increasing Number "+
		       "of Ensemble Runs, which is one of the input parameters. Statistically, 25-50 "+
		       "ensemble runs is enough to generate stable clustering.";
	}
}




package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCA;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCAContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class PCoATaskFactory extends AbstractClusterTaskFactory{

	PCoAContext context = null;

	public PCoATaskFactory(ClusterManager clusterManager){
		super(clusterManager);
		context = new PCoAContext();
	}

	public String getShortName() {return PCoA.SHORTNAME;};
	public String getName() {return PCoA.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PCoA(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Principal Coordinate Analysis (PCoA), also known as multidimensional "+
		       "scaling (MDS), is a means of visualizing the level of similarity of "+
		       "individual cases of a dataset. It refers to a set of related ordination "+
		       "techniques used in information visualization, in particular to display "+
		       "the information contained in a distance matrix. It is a form of non-linear "+
		       "dimensionality reduction. An MDS algorithm aims to place each object in "+
		       "N-dimensional space such that the between-object distances are preserved "+
		       "as well as possible. Each object is then assigned coordinates in each of the "+
		       "N dimensions.  This implementation only supports N=2";
	}
}

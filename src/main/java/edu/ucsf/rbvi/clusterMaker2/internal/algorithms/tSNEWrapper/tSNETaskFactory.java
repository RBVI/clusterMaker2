package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEWrapper;

import java.util.Collections;
import java.util.List;

import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class tSNETaskFactory extends AbstractClusterTaskFactory{

	
	tSNEContext context = null;

	public tSNETaskFactory(ClusterManager clusterManager){
		super(clusterManager);
		context = new tSNEContext();
	}

	public String getShortName() {return tSNE.SHORTNAME;};
	public String getName() {return tSNE.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new tSNE(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "t-distributed stochastic neighbor embedding (t-SNE) is a machine "+
		       "learning algorithm for dimensionality reduction developed by Geoffrey "+
		       "Hinton and Laurens van der Maaten. It is a nonlinear dimensionality "+
		       "reduction technique that is particularly well-suited for embedding "+
		       "high-dimensional data into a space of two or three dimensions, which "+
		       "can then be visualized in a scatter plot. Specifically, it models each "+
		       "high-dimensional object by a two- or three-dimensional point in such "+
		       "a way that similar objects are modeled by nearby points and dissimilar "+
		       "objects are modeled by distant points.";
	} 
}

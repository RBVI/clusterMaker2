/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP.APCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import java.util.Collections;
import java.util.List;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author root
 */
public class PCATaskFactory extends AbstractClusterTaskFactory{
	PCAContext context = null;

	public PCATaskFactory(ClusterManager clusterManager){
		super(clusterManager);
		context = new PCAContext();
	}

	public String getShortName() {return PCA.SHORTNAME;};
	public String getName() {return PCA.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PCA(context, clusterManager));
	}

	@Override
	public String getLongDescription() {
		return "Principal component analysis (PCA) is a statistical "+
		       "procedure that uses an orthogonal transformation to convert "+
		       "a set of observations of possibly correlated variables into "+
		       "a set of values of linearly uncorrelated variables called principal "+
		       "components. The number of distinct principal components is equal "+
		       "to the smaller of the number of original variables or the number of "+
		       "observations minus one. This transformation is defined in such a way "+
		       "that the first principal component has the largest possible variance "+
		       "(that is, accounts for as much of the variability in the data as "+
		       "possible), and each succeeding component in turn has the highest "+
		       "variance possible under the constraint that it is orthogonal to "+
		       "the preceding components. The resulting vectors are an uncorrelated "+
		       "orthogonal basis set. PCA is sensitive to the relative scaling of "+
		       "the original variables.";
	}
}

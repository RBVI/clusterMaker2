/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

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
		return Collections.singletonList(ClusterTaskFactory.ClusterType.PCA); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PCA(context, clusterManager));
	}
    
}

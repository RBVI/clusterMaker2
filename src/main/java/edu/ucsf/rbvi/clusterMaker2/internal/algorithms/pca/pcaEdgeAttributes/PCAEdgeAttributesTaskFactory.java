/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.pcaEdgeAttributes;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import java.util.Collections;
import java.util.List;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author root
 */
public class PCAEdgeAttributesTaskFactory extends AbstractClusterTaskFactory{
        CyServiceRegistrar bc;
        PCAEdgeAttributesContext context = null;
    
        public PCAEdgeAttributesTaskFactory(CyServiceRegistrar bc){
            context = new PCAEdgeAttributesContext();
            this.bc = bc;
        }
    
        public String getShortName() {return PCAEdgeAttributes.SHORTNAME;};
	public String getName() {return PCAEdgeAttributes.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return true;
	}

	public List<ClusterTaskFactory.ClusterType> getTypeList() { 
		return Collections.singletonList(ClusterTaskFactory.ClusterType.PCA); 
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PCAEdgeAttributes(context, bc));
	}
}

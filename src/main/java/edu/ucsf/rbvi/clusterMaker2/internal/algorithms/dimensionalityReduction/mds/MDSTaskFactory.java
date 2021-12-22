package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.mds;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class MDSTaskFactory extends AbstractClusterTaskFactory {
	MDSContext context = null;
	final CyServiceRegistrar registrar;
	
	public MDSTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new MDSContext();
		this.registrar = registrar;
	}
	
	public String getName() {return MDS.NAME;}
	
	public String getShortName() {return MDS.SHORTNAME;}
	
	@Override
	public String getLongDescription() {
		return "Multidimensional scaling (MDS) seeks a low-dimensional representation of the data in "+
           "which the distances respect well the distances in the original high-dimensional space."+
           "<br/><br/>"+
           "In general, MDS is a technique used for analyzing similarity or dissimilarity data. "+
           "It attempts to model similarity or dissimilarity data as distances in a geometric spaces. "+
           "The data can be ratings of similarity between objects, interaction frequencies of "+
           "molecules, or trade indices between countries.";
	}
	
	@Override
	public ClusterViz getVisualizer() {
		return null;
	}
	
	@Override
	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterTaskFactory.ClusterType.DIMRED); 
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MDS(context, clusterManager, registrar));
	}
}

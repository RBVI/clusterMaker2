package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.linearDiscriminant;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class LinearDiscriminantTaskFactory extends AbstractClusterTaskFactory {
	LinearDiscriminantContext context = null;
	final CyServiceRegistrar registrar;
	
	public LinearDiscriminantTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new LinearDiscriminantContext();
		this.registrar = registrar;
	}
	
	public String getName() {return LinearDiscriminant.NAME;}
	
	public String getShortName() {return LinearDiscriminant.SHORTNAME;}
	
	@Override
	public String getLongDescription() {
		return "";
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
		return new TaskIterator(new LinearDiscriminant(context, clusterManager, registrar));
	}
}

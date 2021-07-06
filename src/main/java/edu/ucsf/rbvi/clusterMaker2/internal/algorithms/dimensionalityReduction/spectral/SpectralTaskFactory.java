package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.spectral;

import java.util.Collections;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class SpectralTaskFactory extends AbstractClusterTaskFactory {
	SpectralContext context = null;
	final CyServiceRegistrar registrar;
	
	public SpectralTaskFactory(ClusterManager clusterManager, CyServiceRegistrar registrar) {
		super(clusterManager);
		context = new SpectralContext();
		this.registrar = registrar;
	}
	
	public String getName() {return Spectral.NAME;}
	
	public String getShortName() {return Spectral.SHORTNAME;}
	
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
		return new TaskIterator(new Spectral(context, clusterManager, registrar));
	}
}

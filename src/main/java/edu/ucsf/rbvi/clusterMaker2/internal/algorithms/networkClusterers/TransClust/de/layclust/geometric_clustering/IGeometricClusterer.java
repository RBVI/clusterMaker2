package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

public interface IGeometricClusterer {
	
	public void initGeometricClusterer(ConnectedComponent cc);
	
	public void run();
	
}

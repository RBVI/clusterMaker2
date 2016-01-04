package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;



public class RunPAM extends AbstractKClusterAlgorithm {
	
	private PAMContext context = null;

	public RunPAM(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
			      TaskMonitor monitor, PAMContext context) {
		super(network, weightAttributes, metric, monitor);
		this.context = context;
	}
	
	@Override
	public int kcluster(int nClusters, int nIterations, Matrix matrix, DistanceMetric metric, int[] clusterId) {
		
		PAM pam = new PAM(network, matrix, metric);
		Clusters c = pam.cluster(nClusters);
		
		// copy results into clusterId
		for (int i = 0; i < c.size(); ++i) {
			clusterId[i] = c.getClusterIndex(i);
		}
		
		return c.getNumberOfClusters();
	}
}

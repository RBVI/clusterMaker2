package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam.HopachablePAM;

/**
 * HopachPAM is a specialized a specialized Hopach algorithm that uses the 
 * PAM partitioner (Hopach-PAM).
 * Independent of Cytoscape.
 * @author djh.shih
 *
 */
public class HopachPAM extends Hopach {
	
	public HopachPAM(BaseMatrix data, DistanceMetric metric) {
		super(new HopachablePAM(null, data, metric));
	}
	
	public HopachPAM(HopachablePAM p) {
		super(p);
	}
	
}

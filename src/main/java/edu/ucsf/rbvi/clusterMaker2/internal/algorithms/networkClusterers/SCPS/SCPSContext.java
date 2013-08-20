package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public class SCPSContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	
	@Tunable(description = "Epsilon Parameter",groups={"SCPS Tuning"},gravity=1.0)
	public double epsilon = 1.02;

	@Tunable(description = "Number of iterations",groups={"SCPS Tuning"},gravity=2.0)
	public int iterations = 50;

	@Tunable(description = "Number of clusters",groups={"SCPS Tuning"},gravity=3.0)
	public int clusters = -1;

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	public SCPSContext() {
		advancedAttributes = new AdvancedProperties("__scpsCluster", false);
	}

	public SCPSContext(SCPSContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__scpsCluster", false);
		if (origin.edgeAttributeHandler != null)
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		
		epsilon = origin.epsilon;
		iterations = origin.iterations;
		clusters = origin.clusters;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;

		if (edgeAttributeHandler == null)
			edgeAttributeHandler = new EdgeAttributeHandler(network);
		else
			edgeAttributeHandler.setNetwork(network);
	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}
	
}

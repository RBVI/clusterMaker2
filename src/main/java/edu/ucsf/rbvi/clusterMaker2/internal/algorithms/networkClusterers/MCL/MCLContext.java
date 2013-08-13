package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public class MCLContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	
	@Tunable(description = "Granularity parameter (inflation value)",groups={"Basic MCL Tuning"},gravity=1.0)
	public double inflation_parameter;

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Weak edge weight pruning threshold", groups={"MCL Advanced Settings"}, params="displayState=collapsed",gravity=2.0)
	public double clusteringThresh = 1e-15;
	
	@Tunable(description = "Number of iterations", groups={"MCL Advanced Settings"}, gravity=3.0)
	public int iterations = 16;
	
	@Tunable(description = "Maximum residual value", groups={"MCL Advanced Settings"}, gravity=4.0)
	public double maxResidual = 0.001;
	
	@Tunable(description = "Maximum number of threads", groups={"MCL Advanced Settings"}, gravity=5.0)
	public int maxThreads = 0;
    
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	public MCLContext() {
		advancedAttributes = new AdvancedProperties("__mclCluster", false);
	}

	public void setNetwork(CyNetwork network) {
		this.network = network;
		edgeAttributeHandler = new EdgeAttributeHandler(network);
	}

	public CyNetwork getNetwork() { return network; }
	
}

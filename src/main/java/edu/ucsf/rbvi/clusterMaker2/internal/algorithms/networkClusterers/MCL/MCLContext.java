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
	public double inflation_parameter = 2.5;

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Weak edge weight pruning threshold", groups={"MCL Advanced Settings"}, params="displayState=collapsed",gravity=20.0)
	public double clusteringThresh = 1e-15;
	
	@Tunable(description = "Number of iterations", groups={"MCL Advanced Settings"}, gravity=21.0)
	public int iterations = 16;
	
	@Tunable(description = "Maximum residual value", groups={"MCL Advanced Settings"}, gravity=22.0)
	public double maxResidual = 0.001;
	
	@Tunable(description = "Maximum number of threads", groups={"MCL Advanced Settings"}, gravity=23.0)
	public int maxThreads = 0;
    
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	public MCLContext() {
		advancedAttributes = new AdvancedProperties("__mclCluster", false);
	}

	public MCLContext(MCLContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__mclCluster", false);
		if (origin.edgeAttributeHandler != null)
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		
		inflation_parameter = origin.inflation_parameter;
		clusteringThresh = origin.clusteringThresh;
		iterations = origin.iterations;
		maxResidual = origin.maxResidual;
		maxThreads = origin.maxThreads;
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

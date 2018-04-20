package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

public class MCLContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	
	@Tunable(description = "Granularity parameter (inflation value)",
	         longDescription = "The inflation parameter controls the inflation of the matrix in each pass of the algorithm.  The larger "+
	                           "this value, the smaller the resulting clusters will be.",
	         exampleStringValue = "2.5",
	         groups={"Basic MCL Tuning"},gravity=1.0)
	public double inflation_parameter = 2.5;

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Weak edge weight pruning threshold", 
	         longDescription = "Any edges with weights smaller than this will be removed.",
	         exampleStringValue = "1e-15",
	         groups={"MCL Advanced Settings"}, params="displayState=collapsed",gravity=20.0)
	public double clusteringThresh = 1e-15;
	
	@Tunable(description = "Number of iterations", 
	         longDescription = "The represents the maximum number of iterations of the algorithm.  If the residueal falls below the "+
	                           "maxResidual then the algorithm will complete before this number of iterations",
	         exampleStringValue = "16",
	         groups={"MCL Advanced Settings"}, gravity=21.0)
	public int iterations = 16;
	
	@Tunable(description = "Maximum residual value", 
	         longDescription = "MCL calculates the residual as the maximum difference between the sum of each column and the sum of the squares "+
	                           "of the values in that column.  If the residual is below this value, the algorithm will complete.",
	         exampleStringValue = "0.001",
	         groups={"MCL Advanced Settings"}, gravity=22.0)
	public double maxResidual = 0.001;
	
	@Tunable(description = "Stop if residual increases", 
	         longDescription = "If this value is set to ```true``` then the algorithm will stop if the residual increases",
	         exampleStringValue = "true",
	         groups={"MCL Advanced Settings"}, gravity=23.0)
	public boolean forceDecliningResidual = true;
	
	@Tunable(description = "Maximum number of threads", 
	         longDescription = "The maximum number of threads to use.  If this is set to 0, the algorithm will use all of the available cores.",
	         groups={"MCL Advanced Settings"}, gravity=24.0)
	public int maxThreads = 0;
    
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

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

	public void setUIHelper(TunableUIHelper helper) {
		edgeAttributeHandler.setUIHelper(helper);
	}
	
}

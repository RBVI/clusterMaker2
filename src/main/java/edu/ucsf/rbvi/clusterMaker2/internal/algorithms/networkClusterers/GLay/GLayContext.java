package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

public class GLayContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	@Tunable(description = "Cluster only selected nodes",groups={"Basic GLay Tuning"},gravity=1.0)
	public boolean selectedOnly = false;

	@Tunable(description = "Assume edges are undirected", groups={"Basic GLay Tuning"},gravity=2.0)
	public boolean undirectedEdges = true;

    
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public GLayContext() {
		advancedAttributes = new AdvancedProperties("__glayCluster", false);
	}

	public GLayContext(GLayContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__glayCluster", false);
		
		selectedOnly = origin.selectedOnly;
		undirectedEdges = origin.undirectedEdges;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;

	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) { }
	
}

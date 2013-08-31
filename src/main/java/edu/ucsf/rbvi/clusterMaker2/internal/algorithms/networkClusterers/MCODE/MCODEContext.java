package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

public class MCODEContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	
	@Tunable(description = "Cluster only selected nodes", groups={"MCODE Tuning"}, gravity=2.0)
	public boolean selectedOnly = false;

	@Tunable(description = "Include loops", groups={"MCODE Tuning", "MCODE Advanced Settings", "Network Scoring"}, gravity=2.0)
	public boolean includeLoops = false;

	@Tunable(description = "Degree Cutoff", groups={"MCODE Tuning", "MCODE Advanced Settings", "Network Scoring"}, gravity=2.0)
	public int degreeCutoff = 2;

	@Tunable(description = "Haircut", groups={"MCODE Tuning", "MCODE Advanced Settings", "Cluster Finding"}, gravity=2.0)
	public boolean haircut = true;

	@Tunable(description = "Fluff", groups={"MCODE Tuning", "MCODE Advanced Settings", "Cluster Finding"}, gravity=2.0)
	public boolean fluff = false;

	@Tunable(description = "Node Score Cutoff", groups={"MCODE Tuning", "MCODE Advanced Settings", "Cluster Finding"}, gravity=2.0)
	public double scoreCutoff = 0.2;

	@Tunable(description = "k-Core", groups={"MCODE Tuning", "MCODE Advanced Settings", "Cluster Finding"}, gravity=2.0)
	public int kCore = 2;

	@Tunable(description = "Max Depth", groups={"MCODE Tuning", "MCODE Advanced Settings", "Cluster Finding"}, gravity=2.0)
	public int maxDepth = 100;

	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public MCODEContext() {
		advancedAttributes = new AdvancedProperties("__mcodeCluster", false);
	}

	public MCODEContext(MCODEContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__mcodeCluster", false);
		
		includeLoops = origin.includeLoops;
		haircut = origin.haircut;
		fluff = origin.fluff;
		scoreCutoff = origin.scoreCutoff;
		selectedOnly = origin.selectedOnly;
		degreeCutoff = origin.degreeCutoff;
		kCore = origin.kCore;
		maxDepth = origin.maxDepth;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;

	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) {  }
	
}

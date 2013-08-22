package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public class TransClusterContext implements ClusterAlgorithmContext {
	CyNetwork network;
	
	//Tunables
	
	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;

	@Tunable(description= "Max. Subcluster Size",groups={"Advanced Tuning Parameters", "Find Exact Solution"}, gravity=11.0)
	public int maxSubclusterSize = 20;

	@Tunable(description= "Max. Time (secs)",groups={"Advanced Tuning Parameters", "Find Exact Solution"}, gravity=12.0)
	public int maxTime = 1;

	@Tunable(description= "Merge very similar nodes to one?",groups={"Advanced Tuning Parameters","Merge Nodes"}, gravity=13.0)
	public boolean mergeSimilar = false;

	@Tunable(description= "Threshold for merge",groups={"Advanced Tuning Parameters","Merge Nodes"}, gravity=14.0)
	public int mergeThreshold = 100;

	@Tunable(description= "Number of Processors:",groups={"Advanced Tuning Parameters","Parallelization"}, gravity=15.0)
	public int processors = -1;
	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	public TransClusterContext() {
		advancedAttributes = new AdvancedProperties("__transclustCluster", false);
	}

	public TransClusterContext(TransClusterContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__transclustCluster", false);
		if (origin.edgeAttributeHandler != null)
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		
		maxSubclusterSize = origin.maxSubclusterSize;
		maxTime = origin.maxTime;
		mergeSimilar = origin.mergeSimilar;
		mergeThreshold = origin.mergeThreshold;
		processors = origin.processors;
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
	
	public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }
}

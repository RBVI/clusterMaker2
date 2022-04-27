package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public class NormalizationContext {
	 private CyNetwork network;
	 public ClusterManager manager;
	
	// Normalization
	@Tunable
	(description = "Normalization", 
	exampleStringValue = "None",
	tooltip = "<html>The algorithm assumes value between 0 and 1</html>",
	groups = "Attribute normalization", gravity = 1.0)
    public ListSingleSelection<String> normalization = new ListSingleSelection<String>("None", "Basic");
	
	// Two-tailed attributes : absolute value, pos values, neg values
	@Tunable
	(description = "Two-tailed values",
	exampleStringValue = "Absolute value",
	tooltip = "<html>The algorithm assumes value between 0 and 1</html>",
	groups = "Attribute normalization", gravity = 2.0)
	public ListSingleSelection<String> twoTailedValues = new ListSingleSelection<String>("Absolute value", "Only positive values", "Only negative values");
	
	public NormalizationContext(ClusterManager manager) {
		this.manager = manager;
        network = this.manager.getNetwork();
	}
	
    public void setNetwork(CyNetwork network) {
        this.network = network;
    }

    public CyNetwork getNetwork() {
        return network;
    }
	
}

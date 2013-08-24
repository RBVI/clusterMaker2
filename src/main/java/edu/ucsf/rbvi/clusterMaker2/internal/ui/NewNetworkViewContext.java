package edu.ucsf.rbvi.clusterMaker2.internal.ui;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class NewNetworkViewContext {
	CyNetwork network;
	
	//Tunables
	
	public ListSingleSelection<String> attribute;

	@Tunable(description = "Cluster Attribute to Use", groups={"New Network Options"}, gravity=1.0)
  public ListSingleSelection<String> getattribute(){
    attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
    return attribute;
  }
  public void setattribute(ListSingleSelection<String> attr) { }

	@Tunable(description = "Display only selected nodes (or edges)", 
	         groups={"New Network Options"}, gravity=2.0)
	public boolean selectedOnly = false;

	@Tunable(description = "Restore inter-cluster edges after layout",
	         groups={"New Network Options"}, gravity=3.0)
	public boolean restoreEdges = false;

	public NewNetworkViewContext() {
	}

	public CyNetwork getNetwork() { return network; }

	public void setNetwork(CyNetwork network) {
		this.network = network;
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
	}

}

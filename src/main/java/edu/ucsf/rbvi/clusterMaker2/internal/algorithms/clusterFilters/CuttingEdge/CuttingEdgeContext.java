package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.CuttingEdge;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.AbstractFilterContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

public class CuttingEdgeContext extends AbstractFilterContext {
	CyNetwork network;
	
	//Tunables
	
	@Tunable(description="Inside edge proportion",groups={"Cutting Edge Basic Parameters"}, gravity=1.0)
	public double edgeProportion = 0.8;

	@Tunable(description="Cluster results column to filter",groups={"Cutting Edge Basic Parameters"}, gravity=2.0)
	public ListSingleSelection clusterAttribute = null;

	@ContainsTunables
	public AdvancedProperties advancedAttributes = null;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public CuttingEdgeContext() {
		advancedAttributes = new AdvancedProperties("__cuttingEdgeFilter", false);
	}

	public CuttingEdgeContext(CuttingEdgeContext origin) {
		super(origin);

		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__cuttingEdgeFilter", false);

		edgeProportion = origin.edgeProportion;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;
		List<String> integerColumns = super.getClusterAttributeList(network);
		if (integerColumns != null) {
			clusterAttribute = new ListSingleSelection(integerColumns);
			String defaultAttribute = super.getDefaultAttribute(network);
			if (integerColumns.contains(defaultAttribute))
				clusterAttribute.setSelectedValue(defaultAttribute);
		}
	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttributeName() { return advancedAttributes.clusterAttribute;}

	public String getClusterAttribute() { return clusterAttribute.getSelectedValue().toString();}

	public void setUIHelper(TunableUIHelper helper) {
	}
	
}

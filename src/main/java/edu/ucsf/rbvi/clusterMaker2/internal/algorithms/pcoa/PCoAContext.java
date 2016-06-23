package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.BestNeighbor.BestNeighborContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class PCoAContext {

	CyNetwork network;

	

	@Tunable(description = "Only use selected nodes for PCoA", groups={"Data Input"}, gravity=7.0)
	public boolean selectedOnly = false;

	@Tunable(description="Ignore nodes with no data", groups={"Data Input"}, gravity=8.0)
	public boolean ignoreMissing = true;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;


	public PCoAContext(){

	}

	public void setNetwork(CyNetwork network){
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		if (edgeAttributeHandler == null)
			edgeAttributeHandler = new EdgeAttributeHandler(network);
		else
			edgeAttributeHandler.setNetwork(network);

	}


	public CyNetwork getNetwork() { return network; }

	public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }
}

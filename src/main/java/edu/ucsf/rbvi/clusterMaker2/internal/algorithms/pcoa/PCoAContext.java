package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.BestNeighbor.BestNeighborContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class PCoAContext {
	enum NegEigenHandling {
		DISCARD("Discard", 0),
		KEEP("Keep", 1),
		CORRECT("Correct", 2);

		String name;
		int value;
		NegEigenHandling(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String toString() { return name; }
		public int getValue() { return value; }
	}

	CyNetwork network;
	
	//Tunables

	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;

	@Tunable (description="Negative eigenvalue handling", groups={"PCoA Parameters"}, gravity=80.0)
	public ListSingleSelection<NegEigenHandling> neg = 
		new ListSingleSelection<NegEigenHandling>(NegEigenHandling.DISCARD, NegEigenHandling.KEEP, NegEigenHandling.CORRECT);

	/*
	@Tunable(description = "Create Result Panel with Principal Coordinate selection option", 
	         groups={"Result Options"}, gravity=600.0)
	public boolean pcoaResultPanel = false;
	*/
	
	@Tunable(description = "Create PCoA scatter plot", 
	         groups={"Result Options"}, gravity=84.0)
	public boolean pcoaPlot = true;

	public PCoAContext(){
		
	}
	
	public PCoAContext(PCoAContext origin) {
		if (origin.edgeAttributeHandler != null){
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
			edgeAttributeHandler.setUndirected(false);
			edgeAttributeHandler.setAdjustLoops(false);
		
		}
			
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;
		
		if (edgeAttributeHandler == null){
			
			edgeAttributeHandler = new EdgeAttributeHandler(network);
			edgeAttributeHandler.setUndirected(false);
			edgeAttributeHandler.setAdjustLoops(false);
			
		}
		else{
			edgeAttributeHandler.setNetwork(network);
		}
			
	}

	public CyNetwork getNetwork() { return network; }

	public void setUIHelper(TunableUIHelper helper) {
		edgeAttributeHandler.setUIHelper(helper);
	}
}

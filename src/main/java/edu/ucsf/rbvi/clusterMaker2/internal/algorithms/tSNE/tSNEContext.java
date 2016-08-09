package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public class tSNEContext {

CyNetwork network;
	
	//Tunables

@ContainsTunables
public EdgeAttributeHandler edgeAttributeHandler;
	
@Tunable(description="Initial Dimensions", groups={"t-SNE Advanced Settings"}, gravity=66)
public int int_dims;

@Tunable(description="Perplexity", groups={"t-SNE Advanced Settings"}, gravity=67)
public double perplixity;

@Tunable(description="Number of Iterations", groups={"t-SNE Advanced Settings"}, gravity=68)
public int num_of_iterations;


public tSNEContext(){
	
}

public tSNEContext(tSNEContext origin) {
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

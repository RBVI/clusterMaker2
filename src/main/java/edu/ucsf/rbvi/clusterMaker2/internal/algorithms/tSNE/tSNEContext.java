package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;



public class tSNEContext {
	enum GetVisulaisation {
		NODES("Selected Nodes", 0),
		EDGES("Edges", 1);
		

		String name;
		int value;
		GetVisulaisation(String name, int value) {
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



@Tunable(description="Distance Metric", gravity=10)
public ListSingleSelection<DistanceMetric> metric = 
	new ListSingleSelection<DistanceMetric>(DistanceMetric.values());

@ContainsTunables
public AttributeList attributeList = null;


public boolean selectedOnly = false;
@Tunable(description="Use only selected nodes/edges for cluster",
		groups={"t-SNE Advanced Settingss"}, gravity=65)
public boolean getselectedOnly() { return selectedOnly; }
public void setselectedOnly(boolean sel) {
	if (network != null && this.selectedOnly != sel) //kcluster.updateKEstimates(network, sel);
	this.selectedOnly = sel;
}

@Tunable(description="Initial Dimensions", groups={"t-SNE Advanced Settings"}, gravity=66)
public int int_dims=1;

@Tunable(description="Perplexity", groups={"t-SNE Advanced Settings"}, gravity=67)
public double perplixity=1;

@Tunable(description="Number of Iterations", groups={"t-SNE Advanced Settings"}, gravity=68)
public int num_of_iterations=1;

@Tunable (description="Visualisation with?", groups={"t-SNE Advanced Settings"},gravity=69)
public ListSingleSelection<GetVisulaisation> modeselection = 
	new ListSingleSelection<GetVisulaisation>(GetVisulaisation.NODES, GetVisulaisation.EDGES);


/*@Tunable(description = "Create t-SNE scatter plot", 
groups={"Result Options"}, gravity=84.0)
public boolean tsnePlot = false;*/


public tSNEContext(){
	metric.setSelectedValue(DistanceMetric.EUCLIDEAN);
}

public tSNEContext(tSNEContext origin) {
	if (origin.edgeAttributeHandler != null){
		edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		edgeAttributeHandler.setUndirected(false);
		edgeAttributeHandler.setAdjustLoops(false);
	
	}
	if (attributeList == null)
		attributeList = new AttributeList(network);
	else
		attributeList.setNetwork(network);
		
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
	
	if (attributeList == null)
		attributeList = new AttributeList(network);
	else
		attributeList.setNetwork(network);
		
}

public DistanceMetric getDistanceMetric() {
	return metric.getSelectedValue();
}
public CyNetwork getNetwork() { return network; }


public void setUIHelper(TunableUIHelper helper) {
	edgeAttributeHandler.setUIHelper(helper);
}
}

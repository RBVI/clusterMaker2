package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Infomap;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

public class InfomapContext implements ClusterAlgorithmContext {
	CyNetwork network;
	TunableUIHelper helper;
	
	//Tunables
	
	@Tunable(description = "Trials",
			 longDescription = "The number of attempts to partition the network.",
			 exampleStringValue = "10",
			 tooltip = "<html>The number of attempts to partition the network.</html>",
			 groups =  {"Infomap Advanced Settings"}, gravity = 1.0)
	public int trials = 10;
	
	private ListSingleSelection<String> attribute ;
	@Tunable(description = "Attribute", groups={"Source for array data"}, params="displayState=uncollapsed", 
	         longDescription = "The column containing the data to be used for the clustering. "+
	                           "If no weight column is used, select ```--NONE---```",
	         exampleStringValue = "weight",
	         gravity = 2.0)
	public ListSingleSelection<String> getattribute(){
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		return attribute;
	}
	public void setattribute(ListSingleSelection<String> attr) { }
	
	@Tunable(description = "Synchronous",
			 longDescription = "If ```false``` the algorithm will run in the background after specified wait time",
			 exampleStringValue = "true",
			 tooltip = "<html>If ```false``` the algorithm will run in the background after specified wait time</html>",
			 groups = {"Leiden Advanced Settings"}, gravity = 6.0)
	public boolean isSynchronous = false;
	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public InfomapContext() {
		advancedAttributes = new AdvancedProperties("__infomapCluster", false); //this is the name of the column Integer that is created when click LOAD
	}

	public InfomapContext(InfomapContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__infomapCluster", false);

		trials = origin.trials;
		attribute = origin.attribute;
		isSynchronous = origin.isSynchronous;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;
	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;
		
	}
	
}

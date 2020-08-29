package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Leiden;


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

public class LeidenContext implements ClusterAlgorithmContext {
	CyNetwork network;
	TunableUIHelper helper;
	
	//Tunables
	
	@Tunable(description = "Objective function",
			 longDescription = "Whether to use the Constant Potts Model (CPM) or modularity. Must be either \"CPM\" or \"modularity\".",
			 exampleStringValue = "CPM",
			 groups = {"Leiden Advanced Settings"}, gravity = 1.0)
	public ListSingleSelection<String> objective_function = new ListSingleSelection<>("CPM", "modularity");
	
	private ListSingleSelection<String> attribute ;
	@Tunable(description = "Attribute", groups={"Source for array data"}, params="displayState=uncollapsed", 
	         longDescription = "The column containing the data to be used for the clustering. "+
	                           "If no weight column is used, select ```--NONE---```",
	         exampleStringValue = "weight",
	         gravity=2.0)
	public ListSingleSelection<String> getattribute(){
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		return attribute;
	}
	public void setattribute(ListSingleSelection<String> attr) { }
	
	@Tunable(description = "Resolution parameter",
			 longDescription = "The resolution parameter to use. "
			 		+ "Higher resolutions lead to more smaller communities, "
			 		+ "while lower resolutions lead to fewer larger communities.",
			 exampleStringValue = "1.0",
			 groups = {"Leiden Advanced Settings"}, gravity = 3.0)
	public double resolution_parameter = 1.0;
	
	@Tunable(description = "Beta value",
			 longDescription = "Parameter affecting the randomness in the Leiden algorithm. This affects only the refinement step of the algorithm.",
			 exampleStringValue = "0.01",
			 groups = {"Leiden Advanced Settings"}, gravity = 4.0)
	public double beta = 0.01;
	
	@Tunable(description = "Number of iterations",
			 longDescription = "The number of iterations to iterate the Leiden algorithm. Each iteration may improve the partition further.",
			 exampleStringValue = "2",
			 groups = {"Leiden Advanced Settings"}, gravity = 5.0)
	public int n_iterations = 2;
	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public LeidenContext() {
		advancedAttributes = new AdvancedProperties("__leidenCluster", false); //this is the name of the column Integer that is created when click LOAD
	}

	public LeidenContext(LeidenContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__leidenCluster", false);
		
		objective_function = origin.objective_function;
		attribute = origin.attribute;
		resolution_parameter = origin.resolution_parameter;
		beta = origin.beta;
		n_iterations = origin.n_iterations;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;
		
	}
	
}

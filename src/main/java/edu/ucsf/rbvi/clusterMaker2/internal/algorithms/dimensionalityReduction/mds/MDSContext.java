package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.mds;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class MDSContext {

	CyNetwork network;
	TunableUIHelper helper;
		
		//Tunables

	public ListMultipleSelection<String> nodeAttributeList = null;
	@Tunable(description="Node attributes for dimensionality reduction", groups = "Array sources", 
	         longDescription="Select the node table columns to be used for calculating dimensionality reduction.  "+
	                         "Note that at least 2 node columns are usually required.",
	         exampleStringValue="gal1RGexp,gal4RGExp,gal80Rexp",
	         tooltip = "<html>You must choose at least 2 node columns for dimensionality reduction.</html>", gravity = 68 )
    public ListMultipleSelection<String> getnodeAttributeList() {
		if (network != null && nodeAttributeList == null)
			nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
        return nodeAttributeList;
    }
    public void setnodeAttributeList(ListMultipleSelection<String> nal) { }

	@Tunable(description = "Metric",
			 longDescription = "If True, perform metric MDS; otherwise, perform nonmetric MDS.",
			 exampleStringValue = "True",
			 tooltip = "<html>If checked, perform metric MDS; otherwise, perform nonmetric MDS.</html>",
			 groups = {"MDS Advanced Settings"}, gravity = 69)
	public boolean metric = true;
	
	@Tunable(description = "Number of initializations",
			 longDescription = "Number of times the SMACOF algorithm will be run with different initializations. "
			 		+ "The final results will be the best output of the runs, determined by the run with the smallest final stress.",
			 exampleStringValue = "4",
			 tooltip = "<html>Number of times the SMACOF algorithm will be run with different initializations.<br/>"
			 		+ "The final results will be the best output of the runs, determined by the run with the smallest final stress.</html>",
			 groups = {"MDS Advanced Settings"}, gravity = 70)
	public int n_init = 4; 
	
	@Tunable(description = "Maximum iterations",
			 longDescription = "Maximum number of iterations of the SMACOF algorithm for a single run.",
			 exampleStringValue = "300",
			 tooltip = "<html>Maximum number of iterations of the SMACOF algorithm for a single run.</html>",
			 groups = {"MDS Advanced Settings"}, gravity = 71)
	public int max_iter = 300;
	
	@Tunable(description = "Eps",
			longDescription = "Relative tolerance with respect to stress at which to declare convergence.",
			exampleStringValue = "1 * 10^(-3)",
			tooltip = "<html>Relative tolerance with respect to stress at which to declare convergence.</html>",
			groups = {"MDS Advanced Settings"}, gravity = 72)
	public double eps = 1 * 10^(-3);
	
	@Tunable(description = "Dissimilarity",
			longDescription = "Dissimilarity measure to use:\r\n" + 
					"‘euclidean’: Pairwise Euclidean distances between points in the dataset.\r\n" + 
					"‘precomputed’: Pre-computed dissimilarities are passed directly to fit and fit_transform.",
	 		exampleStringValue = "euclidean",
	 		tooltip = "<html>Dissimilarity measure to use:<br/>"
	 				+ "‘euclidean’: Pairwise Euclidean distances between points in the dataset.<br/>"
	 				+ "‘precomputed’: Pre-computed dissimilarities are passed directly to fit and fit_transform.</html>",
			groups = {"MDS Advanced Settings"}, gravity = 73)
	public ListSingleSelection<String> dissimilarity = new ListSingleSelection<String>("euclidean", "precomputed");
	
	@Tunable(description = "Show scatter plot with results",
	         longDescription = "If this is set to ```true```, show the scatterplot after the calculation is complete.",
	         exampleStringValue = "true",
	         tooltip = "<html>If this is checked, show the scatterplot after the calculation is complete.</html>",
	         groups = {"MDS Advanced Settings"}, gravity = 74)
	public boolean showScatterPlot = true;
	
	public MDSContext() {
		
	}
	
	
	public MDSContext(MDSContext origin) {

		nodeAttributeList = origin.nodeAttributeList;
		metric = origin.metric;
		n_init = origin.n_init;
		max_iter = origin.max_iter;
		eps = origin.eps;
		dissimilarity = origin.dissimilarity;

		}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		this.nodeAttributeList = null;
	}

	public CyNetwork getNetwork() { return network; }


	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;

	}
		
}

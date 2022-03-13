package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.umap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class UMAPContext {

	CyNetwork network;
	TunableUIHelper helper;
	
	//Tunables
	
	
	public ListMultipleSelection<String> nodeAttributeList = null;
	@Tunable(description="Node attributes for dimensionality reduction", groups="Array sources", 
	         longDescription="Select the node table columns to be used for calculating the cluster.  "+
	                         "Note that at least 2 node columns are usually required.",
	         exampleStringValue="gal1RGexp,gal4RGExp,gal80Rexp",
	         tooltip = "<html>You must choose at least 2 node columns for dimensionality reduction.</html>", gravity = 1.0 )
    public ListMultipleSelection<String> getnodeAttributeList() {
		if (network != null && nodeAttributeList == null)
			nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
        return nodeAttributeList;
    }
    public void setnodeAttributeList(ListMultipleSelection<String> nal) { }
	
	@Tunable(description = "Number of neighbors",
    @Tunable(description = "Number of neighbors",
			longDescription = "This parameter controls how UMAP balances local versus "
			+ "global structure in the data. It does this by constraining the size of "
			+ "the local neighborhood UMAP will look at when attempting to learn the manifold "
			+ "structure of the data. This means that low values of n_neighbors will force UMAP "
			+ "to concentrate on very local structure (potentially to the detriment of the big picture), "
			+ "while large values will push UMAP to look at larger neighborhoods of each point when "
			+ "estimating the manifold structure of the data, losing fine detail structure for the sake "
			+ "of getting the broader of the data.",
			exampleStringValue = "2",
			tooltip = "<html>This parameter controls how UMAP balances local versus global structure<br/>"
			              + "in the data. It does this by constraining the size of the local neighborhood<br/>"
			              + "UMAP will look at when attempting to learn the manifold structure of the data.<br/>"
			              + "This means that low value of Number of neighbors will force UMAP to concentrate on very<br/>"
			              + "local structure (potentially to the detriment of the big picture), while large<br/>"
			              + "values will push UMAP to look at larger neighborhoods of each point when estimating<br/>"
			              + "the manifold structure of the data, losing fine detail structure for the sake <br/>"
			              + "of getting the broader of the data.</html>",
			groups = {"UMAP Advanced Settings"}, gravity = 2.0)
	public int n_neighbors = 2;
	
	@Tunable(description = "Minumum distance",
			longDescription = "The min_dist parameter controls how tightly UMAP is allowed to pack points together. "
			+ "It, quite literally, provides the minimum distance apart that points are allowed to be in the low "
			+ "dimensional representation. This means that low values of min_dist will result in clumpier embeddings. "
			+ "This can be useful if you are interested in clustering, or in finer topological structure. "
			+ "Larger values of min_dist will prevent UMAP from packing points together and will focus on the preservation "
			+ "of the broad topological structure instead.",
			exampleStringValue = "1.0",
			tooltip = "<html>The Minimum distance parameter controls how tightly UMAP is allowed to pack points together.<br/>"
					+ "It, quite literally, provides the minimum distance apart that points are allowed to be in the low<br/>"
					+ "dimensional representation. This means that low values of Minimum distance will result in clumpier embeddings.<br/>"
					+ "This can be useful if you are interested in clustering, or in finer topological structure.<br/>"
					+ "Larger values of Minimum distance will prevent UMAP from packing points together and will focus on the preservation<br/>"
					+ "of the broad topological structure instead.</html>",
			groups = {"UMAP Advanced Settings"}, gravity = 3.0)
	public double min_dist = 1.0;
	
	@Tunable(description = "Metric",
			longDescription = "This controls how distance is computed in the ambient space of the input data. By default UMAP supports a wide variety of metrics.",
			exampleStringValue = "euclidean",
			tooltip = "<html>This controls how distance is computed in the ambient space of the input data.</html>",
			groups = {"UMAP Advanced Settings"}, gravity = 4.0)
	public ListSingleSelection<String> metric = new ListSingleSelection<String>("euclidean", "manhattan", "chebyshev", "minkowski", "canberra", "braycurtis",
			"haversine", "mahalanobis", "wminkowski", "seuclidean", "cosine", "correlation", "hamming", "jaccard", "dice", "russellrao", "kulsinski", "rogerstanimoto",
			"sokalmichener", "sokalsneath", "yule");

	@Tunable(description = "Scale",
			longDescription = "true/false. If true, preprocess the data to scale the matrix",
			exampleStringValue = "True",
			tooltip = "<html>If checked, preprocess the data to scale the matrix</html>",
			groups = {"UMAP Advanced Settings"}, gravity = 5.0)
    public Boolean scale = true;
	
	@Tunable(description = "Show scatter plot with results",
	         longDescription = "If this is set to ```true```, show the scatterplot after the calculation is complete",
	         exampleStringValue = "true",
	         tooltip = "If this is checked, show the scatterplot after the calculation is complete",
	         groups = {"UMAP Advanced Settings"}, gravity = 6.0)
	public boolean showScatterPlot = true;
	
	@Tunable(description = "Synchronous",
			 longDescription = "Is the algorithm going on the background after specified wait time",
			 exampleStringValue = "true",
			 tooltip = "<html>Is the algorithm going on the background after specified wait time</html>",
			 groups = {"Leiden Advanced Settings"}, gravity = 7.0)
	public boolean isSynchronous = true;
	
	//@ContainsTunables
	//public AdvancedProperties advancedAttributes;

	//@ContainsTunables
	//public NetworkVizProperties vizProperties = new NetworkVizProperties();
	
	
	public UMAPContext() {
		// advancedAttributes = new AdvancedProperties("__umap", false); //this is the name of the column Integer that is created when click LOAD
	}

	public UMAPContext(UMAPContext origin) {
		// if (origin.advancedAttributes != null)
		// 	advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		// else
		// 	advancedAttributes = new AdvancedProperties("__umap", false);
		
		nodeAttributeList = origin.nodeAttributeList;
		n_neighbors = origin.n_neighbors;
		min_dist = origin.min_dist;
		metric = origin.metric;
		scale = origin.scale;
		showScatterPlot = origin.showScatterPlot;
		isSynchronous = origin.isSynchronous;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
    this.nodeAttributeList = null;
	}

	public CyNetwork getNetwork() { return network; }

	// public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;

	}
	
}

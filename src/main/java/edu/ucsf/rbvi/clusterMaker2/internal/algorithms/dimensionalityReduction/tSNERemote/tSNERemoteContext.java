package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.tSNERemote;

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

public class tSNERemoteContext {

	CyNetwork network;
	TunableUIHelper helper;
		
		//Tunables

	public ListMultipleSelection<String> nodeAttributeList = null;
	@Tunable(description="Node attributes for dimensionality reduction", groups = "Array sources", 
	         longDescription="Select the node table columns to be used for calculating the cluster.  "+
	                         "Note that at least 2 node columns are usually required.",
	         exampleStringValue="gal1RGexp,gal4RGExp,gal80Rexp",
	         tooltip = "<html>You must choose at least 2 node columns for dimensionality reduction.</html>", gravity = 66 )
  public ListMultipleSelection<String> getnodeAttributeList() {
  if (network != null && nodeAttributeList == null)
    nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
      return nodeAttributeList;
  }
  public void setnodeAttributeList(ListMultipleSelection<String> nal) { }
	
  @Tunable(description="Only use data from selected nodes", groups="Array sources",
           longDescription="Only the data from the array sources of the selected nodes will be used.",
           exampleStringValue = "false",
           gravity = 1.5)
  public boolean selectedOnly = false;

	@Tunable(description = "Perplexity", 
		     longDescription = "The perplexity is related to the number of nearest neighbors "
		     		+ "that is used in other manifold learning algorithms. Larger datasets usually "
		     		+ "require a larger perplexity. Consider selecting a value between 5 and 50. "
		     		+ "Different values can result in significantly different results.",
		     exampleStringValue="30.0",
		     tooltip = "<html>The perplexity is related to the number of nearest neighbors that is used in other manifold learning algorithms.<br/>"
		    		+ "Larger datasets usually require a larger perplexity. Consider selecting a value between 5 and 50.<br/>"
		    		+ "Different values can result in significantly different results.</html>",
		     groups = {"t-SNE Advanced Settings"}, gravity = 67)
	public double perplexity = 30.0;

	@Tunable(description = "Number of Iterations", 
		     longDescription = "The number of iterations of the algorithm to perform",
		     exampleStringValue = "1000",
		     tooltip = "<html>The number of iterations of the algorithm to perform.</html>",
		     groups = {"t-SNE Advanced Settings"}, gravity = 68)
	public int n_iter = 1000;

	@Tunable(description ="Early Exaggeration",
			 longDescription = "Controls how tight natural clusters in the original space are in the embedded "
			         + "space and how much space will be between them. For larger values, the space between natural clusters "
			         + "will be larger in the embedded space. Again, the choice of this parameter is not very critical. "
			         + "If the cost function increases during initial optimization, the early exaggeration factor or the learning rate "
			         + "might be too high.",
			 exampleStringValue = "12.0",
			 tooltip = "<html>Controls how tight natural clusters in the original space are in the embedded<br/>"
			 		+ "space and how much space will be between them. For larger values, the space between natural clusters<br/>"
					+ "will be larger in the embedded space. Again, the choice of this parameter is not very critical.<br/>"
			 		+ "If the cost function increases during initial optimization, the early exaggeration factor or the learning rate might be too high.</html>",
			 groups = {"t-SNE Advanced Settings"}, gravity = 69)
	public double early_exaggeration = 12.0;
		
	@Tunable(description = "Metric",
			 longDescription = "The metric to use when calculating distance between instances in a feature array. "
			 		+ "If metric is a string, it must be one of the options allowed by scipy.spatial.distance.pdist "
			 		+ "for its metric parameter, or a metric listed in pairwise.PAIRWISE_DISTANCE_FUNCTIONS. If metric is "
			 		+ "“precomputed”, X is assumed to be a distance matrix. Alternatively, if metric is a callable function, "
			 		+ "it is called on each pair of instances (rows) and the resulting value recorded. The callable should take "
			 		+ "two arrays from X as input and return a value indicating the distance between them. The default is "
			 		+ "“euclidean” which is interpreted as squared euclidean distance.",
			 exampleStringValue = "euclidean",
			 tooltip = "<html>The metric to use when calculating distance between instances in a feature array.<br/>"
					+ "The default is 'euclidean' which is interpreted as squared euclidean distance.</html>",
			 groups = {"t-SNE Advanced Settings"}, gravity = 70)
	public ListSingleSelection<String> metric = new ListSingleSelection<String>("euclidean", "manhattan", "chebyshev", "minkowski", "canberra", "braycurtis",
			"haversine", "mahalanobis", "wminkowski", "seuclidean", "cosine", "correlation", "hamming", "jaccard", "dice", "russellrao", "kulsinski", "rogerstanimoto",
			"sokalmichener", "sokalsneath", "yule");
	
	@Tunable(description = "Learning Rate",
			 longDescription = "The learning rate for t-SNE is usually in the range [10.0, 1000.0]. "
			 		+ "If the learning rate is too high, the data may look like a ‘ball’ with any point approximately equidistant "
			 		+ "from its nearest neighbours. If the learning rate is too low, most points may look compressed in a dense cloud "
			 		+ "with few outliers. If the cost function gets stuck in a bad local minimum increasing the learning rate may help.",
			 exampleStringValue = "200.0",
			 tooltip = "<html>The learning rate for t-SNE is usually in the range [10.0, 1000.0]."
			 		+ "If the learning rate is too high, the data may look like a ‘ball’ with any point approximately equidistant<br/>"
					+ "from its nearest neighbours. If the learning rate is too low, most points may look compressed in a dense cloud<br/>"
			 		+ "with few outliers. If the cost function gets stuck in a bad local minimum increasing the learning rate may help.</html>",
			 groups = {"t-SNE Advanced Settings"}, gravity = 71)
	public double learning_rate = 200.0;
	
	@Tunable(description = "Init",
			longDescription = "Initialization of embedding. Possible options are ‘random’, "
					+ "‘pca’, and a numpy array of shape (n_samples, n_components). PCA initialization cannot be "
					+ "used with precomputed distances and is usually more globally stable than random initialization.",
			exampleStringValue = "PCA",
			tooltip = "<html>Initialization of embedding. Possible options are ‘random’,<br/>"
					+ "‘pca’, and a numpy array of shape (n_samples, n_components). PCA initialization cannot be<br/>"
					+ "used with precomputed distances and is usually more globally stable than random initialization.</html>",
			groups = {"t-SNE Advanced Settings"}, gravity = 72)
	public ListSingleSelection<String> init = new ListSingleSelection<String>("PCA", "random");
	
	@Tunable(description = "Show scatter plot with results",
	         longDescription = "If this is set to ```true```, show the scatterplot after the calculation is complete",
	         exampleStringValue = "true",
	         tooltip = "<html>If this is checked, show the scatterplot after the calculation is complete.</html>",
	         groups = {"t-SNE Advanced Settings"}, gravity = 73)
	public boolean showScatterPlot = true;
	
	@Tunable(description = "Synchronous",
			 longDescription = "If ```false``` the algorithm will run in the background after specified wait time",
			 exampleStringValue = "true",
			 tooltip = "<html>If ```false``` the algorithm will run in the background after specified wait time</html>",
			 groups = {"Leiden Advanced Settings"}, gravity = 74)
	public boolean isSynchronous = false;
	
	//@ContainsTunables
	//public AdvancedProperties advancedAttributes;

	//@ContainsTunables
	//public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public tSNERemoteContext() {
		//advancedAttributes = new AdvancedProperties("__tsneremote", false); //this is the name of the column Integer that is created when click LOAD
	}

	public tSNERemoteContext(tSNERemoteContext origin) {
		/*if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__tsneremote", false); */

		nodeAttributeList = origin.nodeAttributeList;
		perplexity = origin.perplexity;
		n_iter = origin.n_iter;
		early_exaggeration = origin.early_exaggeration;
		metric = origin.metric;
		learning_rate = origin.learning_rate;
		init = origin.init;
		isSynchronous = origin.isSynchronous;
		}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		this.nodeAttributeList = null;
	}

	public CyNetwork getNetwork() { return network; }

	//public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;

	}
		
}

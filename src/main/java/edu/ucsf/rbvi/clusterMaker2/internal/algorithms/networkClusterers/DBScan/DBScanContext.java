package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.DBScan;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

public class DBScanContext implements ClusterAlgorithmContext {
	CyNetwork network;
	TunableUIHelper helper;
	
	//Tunables
  //
	private ListSingleSelection<String> attribute ;
	@Tunable(description = "Attribute", groups={"Source for array data"}, params="displayState=uncollapsed", 
	         longDescription = "The column containing the data to be used for the clustering. "+
	                           "If no weight column is used, select ```--NONE---```",
	         exampleStringValue = "weight",
	         gravity=1.0)
	public ListSingleSelection<String> getattribute(){
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		return attribute;
	}
	public void setattribute(ListSingleSelection<String> attr) { }
	
	@Tunable(description = "EPS",
			 longDescription = "The maximum distance between two samples for one to be considered as in the neighborhood of the other.",
			 exampleStringValue = "0.5",
			 tooltip = "<html>The maximum distance between two samples for one to be considered as in the neighborhood of the other. This is not a maximum bound on the distances of points within a cluster. This is the most important DBSCAN parameter to choose appropriately for your data set and distance function.</html>",
			 groups = {"DBScan Advanced Settings"}, gravity = 1.0)
	public double eps = 0.5;

	@Tunable(description = "Minimum Samples",
    longDescription = "The number of samples (or total weight) in a neighborhood for a point to be considered as a core point. This includes the point itself.",
    exampleStringValue = "5",
    tooltip = "<html>The number of samples (or total weight) in a neighborhood for a point to be considered as a core point. This includes the point itself.</html>",
    groups = {"DBScan Advanced Settings"}, gravity = 2.0)
  public int min_samples = 5;
	
	@Tunable(description = "Metric",
    longDescription = "The metric to use when calculating distance between instances in a feature array.",
    exampleStringValue = "euclidean",
    tooltip = "<html>The metric to use when calculating distance between instances in a feature array.  It must be one of: <ul> <li>cityblock</li> <li>cosine</li> <li>euclidean</li> <li>l1</li> <li>l2</li> <li>manhattan</li> <li>braycurtis</li> <li>canberra</li> <li>chebyshev</li> <li>correlation</li> <li>dice</li> <li>hamming</li> <li>jaccard</li> <li>kulsinski</li> <li>mahalanobis</li> <li>minkowski</li> <li>rogerstanimoto</li> <li>russellrao</li> <li>seuclidean</li> <li>sokalmicherner</li> <li>sokalsneath</li> <li>sqeuclidean</li> <li>yule</li></ul></html>",
    groups = {"DBScan Advanced Settings"}, gravity = 3.0)
  public ListSingleSelection<String> metric = new ListSingleSelection<String>("cityblock", "cosine", "euclidean", "l1", "l2", "manhattan",
                                                                              "braycurtis", "canberra", "chebyshev", "correlation", "dice", 
                                                                              "hamming", "jaccard", "kulsinski", "mahalanobis", "minkowski", 
                                                                              "rogerstanimoto", "russellrao", "seuclidean", "sokalmichener", 
                                                                              "sokalsneath", "sqeuclidean", "yule");

	@Tunable(description = "Algorithm",
    longDescription = "The algorithm to be used by the NearestNeighbors module to compute pointwise distances and find nearest neighbors.",
    tooltip = "<html>The algorithm to be used by the NearestNeighbors module to compute pointwise distances and find nearest neighbors.</html>",
    exampleStringValue = "auto",
    groups = {"DBScan Advanced Settings"}, gravity = 4.0)
  public ListSingleSelection<String> algorithm = new ListSingleSelection<String>("auto", "ball_tree", "kd_tree", "brute");


	@Tunable(description = "Leaf size",
    longDescription = "Leaf size passed to BallTree or cKDTree. This can affect the speed of the construction and query, as well as the memory required to store the tree. The optimal value depends on the nature of the problem.",
    tooltip = "<html>Leaf size passed to BallTree or cKDTree. This can affect the speed of the construction and query, as well as the memory required to store the tree. The optimal value depends on the nature of the problem.</html>",
    exampleStringValue = "30",
    groups = {"DBScan Advanced Settings"}, gravity = 5.0)
  public int leaf_size = 30;


	@Tunable(description = "P",
    longDescription = "The power of the Minkowski metric to be used to calculate distance between points.",

    tooltip = "<html>The power of the Minkowski metric to be used to calculate distance between points.</html>",
    exampleStringValue = "2.0",
    groups = {"DBScan Advanced Settings"}, gravity = 5.0)
  public double p = 2;

  // We let the back-end handle this
  // public int n_jobs;
	
	@Tunable(description = "Synchronous",
			 longDescription = "If ```false``` the algorithm will run in the background after specified wait time",
			 exampleStringValue = "true",
			 tooltip = "<html>If ```false``` the algorithm will run in the background after specified wait time</html>",
			 groups = {"DBScan Advanced Settings"}, gravity = 6.0)
	public boolean isSynchronous = false;
	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public DBScanContext() {
		advancedAttributes = new AdvancedProperties("__dbscanCluster", false); //this is the name of the column Integer that is created when click LOAD
    metric.setSelectedValue("euclidean");
	}

	public DBScanContext(DBScanContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__dbscanCluster", false);
		
		eps = origin.eps;
		min_samples = origin.min_samples;
		metric = origin.metric;
		algorithm = origin.algorithm;
		leaf_size = origin.leaf_size;
    p = origin.p;
		
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

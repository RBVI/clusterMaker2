package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.spectral;

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

public class SpectralContext {

	CyNetwork network;
	TunableUIHelper helper;
		
		//Tunables
	
	public ListMultipleSelection<String> nodeAttributeList = null;
	@Tunable(description="Node attributes for cluster", groups="Array sources", 
	         longDescription="Select the node table columns to be used for calculating the cluster.  "+
	                         "Note that at least 2 node columns are usually required.",
	         exampleStringValue="gal1RGexp,gal4RGExp,gal80Rexp",
	         tooltip="You must choose at least 2 node columns for an attribute cluster", gravity=50 )
    public ListMultipleSelection<String> getnodeAttributeList() {
		if (network != null && nodeAttributeList == null)
			nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
        return nodeAttributeList;
    }
    public void setnodeAttributeList(ListMultipleSelection<String> nal) { }

    @Tunable(description = "Affinity",
			 longDescription = "‘nearest_neighbors’ : construct the affinity matrix by computing a graph of nearest neighbors.\r\n" + 
			 		"‘rbf’ : construct the affinity matrix by computing a radial basis function (RBF) kernel.\r\n" + 
			 		"‘precomputed’ : interpret X as a precomputed affinity matrix.\r\n" + 
			 		"‘precomputed_nearest_neighbors’ : interpret X as a sparse graph of precomputed nearest neighbors,"
			 		+ " and constructs the affinity matrix by selecting the n_neighbors nearest neighbors.\r\n" + 
			 		"callable : use passed in function as affinity the function takes in data matrix (n_samples, n_features) "
			 		+ "and return affinity matrix (n_samples, n_samples).",
			 exampleStringValue = "nearest_neighbors",
			 tooltip = "<html>‘nearest_neighbors’: construct the affinity matrix by computing a graph of nearest neighbors.<br/>"
			 		+ "‘rbf’ : construct the affinity matrix by computing a radial basis function (RBF) kernel.<br/>"
					+ "‘precomputed’: interpret X as a precomputed affinity matrix.<br/>"
			 		+ "‘precomputed_nearest_neighbors’: interpret X as a sparse graph of precomputed nearest neighbors, <br/>"
					+ "and constructs the affinity matrix by selecting the n_neighbors nearest neighbors.<br/>"
			 		+ "'callable': use passed in function as affinity the function takes in data matrix (n_samples, n_features)<br/>"
					+ "and return affinity matrix (n_samples, n_samples).</html>",
			 groups = {"Spectral Advanced Settings"}, gravity = 67)
    public ListSingleSelection<String> affinity = new ListSingleSelection<String>("nearest_neighbors", "rbf", "precomputed", "precomputed_nearest_neighbors");
    
    @Tunable(description = "Gamma",
    		longDescription = "Kernel coefficient for rbf kernel. If None, gamma will be set to 1/n_features.",
    		exampleStringValue = "None",
    		tooltip = "<html>Kernel coefficient for rbf kernel. If None, gamma will be set to 1/n_features.</html>",
    		groups = {"Spectral Advanced Settings"}, gravity = 68)
    public double gamma;
    
    @Tunable(description = "Eigen solver",
    		longDescription = "The eigenvalue decomposition strategy to use. AMG requires pyamg to be installed. "
    				+ "It can be faster on very large, sparse problems. If None, then 'arpack' is used.",
    		exampleStringValue = "None",
    		tooltip = "<html>The eigenvalue decomposition strategy to use. AMG requires pyamg to be installed.<br/>"
    				+ "It can be faster on very large, sparse problems. If None, then 'arpack' is used.</html>",
    		groups = {"Spectral Advanced Settings"}, gravity = 69)
    public ListSingleSelection<String> eigen_solver = new ListSingleSelection<String>("arpack", "lobpcg", "amg");
    
    @Tunable(description = "Number of neighbors",
    		longDescription = "Number of nearest neighbors for nearest_neighbors graph building. If None, n_neighbors will be set to max(n_samples/10, 1).",
    		exampleStringValue = "None",
    		tooltip = "Number of nearest neighbors for nearest_neighbors graph building. If None, Number of neighbors will be set to max(n_samples/10, 1).</html>",
    		groups = {"Spectral Advanced Settings"}, gravity = 70)
    public int n_neighbors;
    

	public SpectralContext() {

	}

	public SpectralContext(SpectralContext origin) {

		nodeAttributeList = origin.nodeAttributeList;
		affinity = origin.affinity;
		gamma = origin.gamma;
		eigen_solver = origin.eigen_solver;
		n_neighbors = origin.n_neighbors;
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

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.linearDiscriminant;

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

public class LinearDiscriminantContext {

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

    @Tunable(description = "Number of neighbors",
			 longDescription = "number of neighbors to consider for each point.",
			 exampleStringValue = "5",
			 groups = {"LinearDiscriminant Advanced Settings"}, gravity = 66)
    public int n_neighbors = 5;
    
    @Tunable(description = "Regularization constant",
    		longDescription = "regularization constant, multiplies the trace of the local covariance matrix of the distances.",
    		exampleStringValue = "1 * 10^(-3)",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 67)
    public double reg = 1 * 10^(-3);
    
    @Tunable(description = "Tolerance",
    		longDescription = "Tolerance for ‘arpack’ method Not used if eigen_solver==’dense’.",
    		exampleStringValue = "1 * 10^(-6)",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 68)
    public double tol = 1 * 10^(-6);
    
    @Tunable(description = "Eigen solver",
    		longDescription = "auto : algorithm will attempt to choose the best method for input data\r\n" + 
    				"\r\n" + 
    				"arpackuse arnoldi iteration in shift-invert mode.\r\n" + 
    				"For this method, M may be a dense matrix, sparse matrix, or general linear operator. Warning: ARPACK can be unstable for some problems. It is best to try several random seeds in order to check results.\r\n" + 
    				"\r\n" + 
    				"denseuse standard dense matrix operations for the eigenvalue\r\n" + 
    				"decomposition. For this method, M must be an array or matrix type. This method should be avoided fo",
    		exampleStringValue = "auto",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 69)
    public ListSingleSelection<String> eigen_solver = new ListSingleSelection<String>("auto", "arpack", "dense");
    
    @Tunable(description = "Maximum iterations",
    		longDescription = "maximum number of iterations for the arpack solver. Not used if eigen_solver==’dense’.",
    		exampleStringValue = "100",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 70)
    public int max_iter = 100;
    
    @Tunable(description = "Method",
	 longDescription = "standard: use the standard locally linear embedding algorithm.\r\n" + 
	 		"\r\n" + 
	 		"hessian: use the Hessian eigenmap method. This method requires n_neighbors > n_components * (1 + (n_components + 1) / 2.\r\n" + 
	 		"\r\n" + 
	 		"modified: use the modified locally linear embedding algorithm.\r\n" + 
	 		"\r\n" + 
	 		"ltsa: use local tangent space alignment algorithm. ",
	 		exampleStringValue = "standard",
	 		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 71)
    public ListSingleSelection<String> method = new ListSingleSelection<String>("standard", "hessian", "modified", "ltsa");
    
    @Tunable(description = "Hessian tolerance",
    		longDescription = "Tolerance for Hessian eigenmapping method. Only used if method == 'hessian'",
    		exampleStringValue = "1 * 10^(-4)",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 72)
    public double hessian_tol = 1 * 10^(-4);
    
    @Tunable(description = "Modified tolerance",
    		longDescription = "Tolerance for modified LLE method. Only used if method == 'modified'",
    		exampleStringValue = "1 * 10^(-12)",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 73)
    public double modified_tol = 1 * 10^(-12);
    
    @Tunable(description = "Neighbors algorithm",
    		longDescription = "algorithm to use for nearest neighbors search, passed to neighbors.NearestNeighbors instance",
    		exampleStringValue = "auto",
    		groups = {"LinearDiscriminant Advanced Settings"}, gravity = 74)
    public ListSingleSelection<String> neighbors_algorithm = new ListSingleSelection<String>("auto", "brute", "kd_tree", "ball_tree");
    
    
	public LinearDiscriminantContext() {

	}

	public LinearDiscriminantContext(LinearDiscriminantContext origin) {

		nodeAttributeList = origin.nodeAttributeList;
		n_neighbors = origin.n_neighbors;
		reg = origin.reg;
		tol = origin.tol;
		eigen_solver = origin.eigen_solver;
		max_iter = origin.max_iter;
		method = origin.method;
		hessian_tol = origin.hessian_tol;
		modified_tol = origin.modified_tol;
		neighbors_algorithm = origin.neighbors_algorithm;
		
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

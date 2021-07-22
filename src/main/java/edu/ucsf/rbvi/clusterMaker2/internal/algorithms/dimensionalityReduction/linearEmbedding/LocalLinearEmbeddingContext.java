package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.linearEmbedding;

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

public class LocalLinearEmbeddingContext {

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
			 longDescription = "Number of neighbors to consider for each point.",
			 exampleStringValue = "5",
			 tooltip = "<html>Number of neighbors to consider for each point.</html>",
			 groups = {"Local Linear Embedding Advanced Settings"}, gravity = 66)
    public int n_neighbors = 5;
    
    @Tunable(description = "Regularization constant",
    		longDescription = "Regularization constant, multiplies the trace of the local covariance matrix of the distances.",
    		exampleStringValue = "1 * 10^(-3)",
    		tooltip = "<html>Regularization constant, multiplies the trace of the local covariance matrix of the distances.</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 67)
    public double reg = 1 * 10^(-3);
    
    @Tunable(description = "Tolerance",
    		longDescription = "Tolerance for ‘arpack’ method. Not used if eigen_solver==’dense’.",
    		exampleStringValue = "1 * 10^(-6)",
    		tooltip = "<html>Tolerance for ‘arpack’ method. Not used if Eigen solver = ’dense’.</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 68)
    public double tol = 1 * 10^(-6);
    
    @Tunable(description = "Eigen solver",
    		longDescription = "'auto' : algorithm will attempt to choose the best method for input data\r\n" + 
    				"'arpack': use arnoldi iteration in shift-invert mode.\r\n" + 
    				"For this method, M may be a dense matrix, sparse matrix, or general linear operator. " +
    				"Warning: ARPACK can be unstable for some problems. It is best to try several random seeds in order to check results.\r\n" + 
    				"'dense': use standard dense matrix operations for the eigenvalue\r\n" + 
    				"decomposition. For this method, M must be an array or matrix type. This method should be avoided fo",
    		exampleStringValue = "auto",
    		tooltip = "<html>'auto': algorithm will attempt to choose the best method for input data<br/>"
    			+ "'arpack':  use arnoldi iteration in shift-invert mode. For this method, M may be a dense matrix, sparse matrix, or general linear operator.<br/> "
    			+ "Warning: ARPACK can be unstable for some problems. It is best to try several random seeds in order to check results.<br/>"
    			+ "'dense': use standard dense matrix operations for the eigenvalue decomposition. For this method, M must be an array or matrix type.</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 69)
    public ListSingleSelection<String> eigen_solver = new ListSingleSelection<String>("auto", "arpack", "dense");
    
    @Tunable(description = "Maximum iterations",
    		longDescription = "Maximum number of iterations for the arpack solver. Not used if eigen_solver==’dense’.",
    		exampleStringValue = "100",
    		tooltip = "<html>Maximum number of iterations for the arpack solver. Not used if Eigen solver = ’dense’.</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 70)
    public int max_iter = 100;
    
    @Tunable(description = "Method",
	 longDescription = "standard: use the standard locally linear embedding algorithm.\r\n" + 
	 		"hessian: use the Hessian eigenmap method. This method requires n_neighbors > n_components * (1 + (n_components + 1) / 2.\r\n" + 
	 		"modified: use the modified locally linear embedding algorithm.\r\n" + 
	 		"ltsa: use local tangent space alignment algorithm. ",
	 		exampleStringValue = "standard",
	 		tooltip = "<html>'standard': use the standard locally linear embedding algorithm.<br/>"
	 			+ "'hessian': use the Hessian eigenmap method. This method requires n_neighbors > n_components * (1 + (n_components + 1) / 2.<br/>"
	 			+ "'modified': use the modified locally linear embedding algorithm.<br/>"
	 			+ "'ltsa': use local tangent space alignment algorithm.</html>",
	 		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 71)
    public ListSingleSelection<String> method = new ListSingleSelection<String>("standard", "hessian", "modified", "ltsa");
    
    @Tunable(description = "Hessian tolerance",
    		longDescription = "Tolerance for Hessian eigenmapping method. Only used if Method == 'hessian'",
    		exampleStringValue = "1 * 10^(-4)",
    		tooltip = "<html>Tolerance for Hessian eigenmapping method. Only used if Method = 'hessian'</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 72)
    public double hessian_tol = 1 * 10^(-4);
    
    @Tunable(description = "Modified tolerance",
    		longDescription = "Tolerance for modified LLE method. Only used if Method == 'modified'",
    		exampleStringValue = "1 * 10^(-12)",
    		tooltip = "<html>Tolerance for modified LLE method. Only used if Method = 'modified'</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 73)
    public double modified_tol = 1 * 10^(-12);
    
    @Tunable(description = "Neighbors algorithm",
    		longDescription = "Algorithm to use for nearest neighbors search, passed to neighbors. NearestNeighbors instance.",
    		exampleStringValue = "auto",
    		tooltip = "<html>Algorithm to use for nearest neighbors search, passed to neighbors.</html>",
    		groups = {"Local Linear Embedding Advanced Settings"}, gravity = 74)
    public ListSingleSelection<String> neighbors_algorithm = new ListSingleSelection<String>("auto", "brute", "kd_tree", "ball_tree");
    
    
	public LocalLinearEmbeddingContext() {

	}

	public LocalLinearEmbeddingContext(LocalLinearEmbeddingContext origin) {

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

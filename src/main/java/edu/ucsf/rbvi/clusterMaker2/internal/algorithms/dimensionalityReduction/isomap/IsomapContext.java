package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.isomap;

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

public class IsomapContext {

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
			 groups = {"Isomap Advanced Settings"}, gravity = 66)
    public int n_neighbors = 5;
    
    @Tunable(description = "Eigen solver",
			 longDescription = "‘auto’ : Attempt to choose the most efficient solver for the given problem.\r\n" + 
			 		"\r\n" + 
			 		"‘arpack’ : Use Arnoldi decomposition to find the eigenvalues and eigenvectors.\r\n" + 
			 		"\r\n" + 
			 		"‘dense’ : Use a direct solver (i.e. LAPACK) for the eigenvalue decomposition.",
			 exampleStringValue = "auto",
			 groups = {"Isomap Advanced Settings"}, gravity = 67)
    public ListSingleSelection<String> eigen_solver = new ListSingleSelection<String>("auto", "arpack", "dense");
    
    @Tunable(description = "Convergence tolerance",
			 longDescription = "Convergence tolerance passed to arpack or lobpcg. not used if eigen_solver == ‘dense’.",
			 exampleStringValue = "0.0",
			 groups = {"Isomap Advanced Settings"}, gravity = 68)
    public double tol = 0.0;

    @Tunable(description = "Path method",
			 longDescription = "Method to use in finding shortest path.\r\n" + 
			 		"\r\n" + 
			 		"‘auto’ : attempt to choose the best algorithm automatically.\r\n" + 
			 		"\r\n" + 
			 		"‘FW’ : Floyd-Warshall algorithm.\r\n" + 
			 		"\r\n" + 
			 		"‘D’ : Dijkstra’s algorithm.\r\n" + 
			 		"\r\n" + 
			 		"",
			 exampleStringValue = "auto",
			 groups = {"Isomap Advanced Settings"}, gravity = 69)
    public ListSingleSelection<String> path_method = new ListSingleSelection<String>("auto", "FW", "D");
    
    @Tunable(description = "Neighbors algorithm",
			 longDescription = "Algorithm to use for nearest neighbors search, passed to neighbors.NearestNeighbors instance.",
			 exampleStringValue = "auto",
			 groups = {"Isomap Advanced Settings"}, gravity = 70)
    public ListSingleSelection<String> neighbors_algorithm = new ListSingleSelection<String>("auto", "brute", "kd_tree", "ball_tree");
    
    @Tunable(description = "Maximum iterations",
			 longDescription = "Maximum number of iterations for the arpack solver. not used if eigen_solver == ‘dense’.",
			 exampleStringValue = "None",
			 groups = {"Isomap Advanced Settings"}, gravity = 71)
    public int max_iter;
    
	public IsomapContext() {
	}

	public IsomapContext(IsomapContext origin) {

		nodeAttributeList = origin.nodeAttributeList;
		n_neighbors = origin.n_neighbors;
		eigen_solver = origin.eigen_solver;
		tol = origin.tol;
		path_method = origin.path_method;
		neighbors_algorithm = origin.neighbors_algorithm;
		max_iter = origin.max_iter;
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

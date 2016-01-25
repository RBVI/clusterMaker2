package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;


import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class RunAP {

	private double lambda; /*lambda value from 0 to 1 dampens messages passed to avoid numberical oscillation*/
	private double pref; //preference parameter determines cluster density. Larger Parameter equals more Clusters. If < 0, automatically set to avg edge_weight threshold
	private int number_iterations; //number of inflation/expansion cycles

	//private double clusteringThresh; Threshold used to remove weak edges between distinct clusters
	//private double maxResidual; The maximum residual to look for

	private List<CyNode> nodes;
	private List<CyEdge> edges;
	private boolean canceled = false;
	private TaskMonitor monitor;
	protected int clusterCount = 0;
	private CyMatrix distanceMatrix = null;
	private ResponsibilityMatrix r_matrix = null;
	private AvailabilityMatrix a_matrix = null;
	private DoubleMatrix2D s_matrix = null;
	private DoubleMatrix1D pref_vector = null;
	private boolean debug;

	public RunAP( CyMatrix dMat,
	              double lambdaParameter, double preferenceParameter, int num_iterations, 
	              TaskMonitor monitor, boolean debug)
	{
		this.distanceMatrix = dMat;

		this.lambda = lambdaParameter;
		this.pref = preferenceParameter;
		this.debug = debug;

		if(lambda < 0)
			lambda = 0;

		else if(lambda > 1)
			lambda = 1;

		this.number_iterations = num_iterations;
		
		nodes = distanceMatrix.getRowNodes();
		this.s_matrix = distanceMatrix.getColtMatrix();

		// Assign the preference vector to the diagonal
		for (int row = 0; row < s_matrix.rows(); row++) {
			s_matrix.set(row, row, pref);
		}

		// System.out.println("lambda = "+lambda);
		r_matrix = new ResponsibilityMatrix(s_matrix, lambda);
		a_matrix = new AvailabilityMatrix(s_matrix, lambda);

		// logger.info("Iterations = "+num_iterations);
	}

	public void cancel () { canceled = true; }

	public List<NodeCluster> run(CyNetwork network, TaskMonitor monitor)
	{
		double numClusters;

		monitor.setProgress(0.01);

		/*
		if (debug) {
			monitor.showMessage(TaskMonitor.Level.INFO, "Input matrix: ");
			monitor.showMessage(TaskMonitor.Level.INFO, distanceMatrix.printMatrix(s_matrix));
		}
		*/
		
		for (int i=0; i<number_iterations; i++)
		{
			monitor.showMessage(TaskMonitor.Level.INFO,"Exchanging messages: iteration "+i);
			iterate_message_exchange(monitor, i);

			if (canceled) {
				monitor.showMessage(TaskMonitor.Level.INFO,"canceled");
				return null;
			}
			monitor.setProgress((double)i/(double)number_iterations);
		}

		if (debug) {
			for (int i = 0; i < s_matrix.rows(); i++) {
				monitor.showMessage(TaskMonitor.Level.INFO,"Node "+nodes.get(i)+" has exemplar "+get_exemplar(i));
			}
		}

		monitor.showMessage(TaskMonitor.Level.INFO,"Assigning nodes to clusters");

		Map<Integer, NodeCluster> clusterMap = getClusterMap();

		//Update node attributes in network to include clusters. Create cygroups from clustered nodes
		monitor.showMessage(TaskMonitor.Level.INFO,"Created "+clusterMap.size()+" clusters");
	       
		if (clusterCount == 0) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Created 0 clusters!!!!");
			return null;
		}

		int clusterNumber = 1;
		Map<NodeCluster,NodeCluster> cMap = new HashMap<NodeCluster,NodeCluster>();
		for (NodeCluster cluster: NodeCluster.sortMap(clusterMap)) {
			if (cMap.containsKey(cluster))
				continue;

			if (debug) {
				monitor.showMessage(TaskMonitor.Level.INFO, "Cluster "+clusterNumber);
				String s = "";
				for (CyNode node: cluster) {
			 		s += node.toString()+"\t";
				}
				monitor.showMessage(TaskMonitor.Level.INFO, s);
			}

			cMap.put(cluster,cluster);

			cluster.setClusterNumber(clusterNumber);
			clusterNumber++;
		}

		Set<NodeCluster>clusters = cMap.keySet();
		return new ArrayList<NodeCluster>(clusters);
	}	

	//Exchange Messages between Responsibility and Availibility Matrix for Single Iteration of Affinity Propogation
	public void iterate_message_exchange(TaskMonitor monitor, int iteration){

		if (debug) 
			monitor.showMessage(TaskMonitor.Level.INFO, "Iteration "+iteration);

		// Calculate the availability maxima
		a_matrix.updateEvidence();

		// OK, now calculate the responsibility matrix
		r_matrix.update(a_matrix);

		/*
		if (debug) {
			monitor.showMessage(TaskMonitor.Level.INFO, "Responsibility matrix: ");
			monitor.showMessage(TaskMonitor.Level.INFO, distanceMatrix.printMatrix(r_matrix.getMatrix()));
		}
		*/

		// Get the maximum positive responsibilities
		r_matrix.updateEvidence();

		// Now, update the availability matrix
		a_matrix.update(r_matrix);

		/*
		if (debug) {
			monitor.showMessage(TaskMonitor.Level.INFO, "Availability matrix: ");
			monitor.showMessage(TaskMonitor.Level.INFO, distanceMatrix.printMatrix(a_matrix.getMatrix()));
		}
		*/
	}

	
	//return exemplar k for element i => Maximizer of a(i,k) + r(i,k)
	private int get_exemplar(int i) {
	
		double max_value = -1000;
		int exemplar = 0;
		double sum;

		for(int k = 0; k < s_matrix.rows(); k++) {
			sum = a_matrix.get(i,k) + r_matrix.get(i,k);

			if(sum > max_value){
				max_value = sum;
				exemplar = k;
			}
		}
		if (debug)
			monitor.showMessage(TaskMonitor.Level.INFO, "Exemplar for "+i+" is "+exemplar);
	  return exemplar;
	}

	private Map<Integer, NodeCluster> getClusterMap(){
	    
		HashMap<Integer, NodeCluster> clusterMap = new HashMap<Integer, NodeCluster>();

		for(int i = 0; i < s_matrix.rows(); i++){
		
			int exemplar = get_exemplar(i);
			// System.out.println("Examplar for node "+i+" is "+exemplar);
		    
			if (clusterMap.containsKey(exemplar)) {
				if (i == exemplar)
					continue;

				// Already seen exemplar
				NodeCluster exemplarCluster = clusterMap.get(exemplar);

				if (clusterMap.containsKey(i)) {
					// We've already seen i also -- join them
					NodeCluster iCluster = clusterMap.get(i);
					if (iCluster != exemplarCluster) {
						exemplarCluster.addAll(iCluster);
						// System.out.println("Combining "+i+"["+iCluster+"] and "+exemplar+" ["+exemplarCluster+"]");
						clusterCount--;
						clusterMap.remove(i);
					}
				} else {
					exemplarCluster.add(nodes, i);
					// System.out.println("Adding "+i+" to ["+exemplarCluster+"]");
				}

				// Update Clusters
				updateClusters(exemplarCluster, clusterMap);
			} else {
				NodeCluster iCluster;

				// First time we've seen this "exemplar" -- have we already seen "i"?
				if (clusterMap.containsKey(i)) {
					if (i == exemplar)
						continue;
					// Yes, just add exemplar to i's cluster
					iCluster = clusterMap.get(i);
					iCluster.add(nodes, exemplar);
					// System.out.println("Adding "+exemplar+" to ["+iCluster+"]");
				} else {
					// No create new cluster from scratch
					iCluster = new NodeCluster();
					iCluster.add(nodes, i);
					if (exemplar != i)
						iCluster.add(nodes, exemplar);
					// System.out.println("New cluster ["+iCluster+"]");
					clusterCount++;
				}
				updateClusters(iCluster, clusterMap);
			}
		}
		return clusterMap;
	}

	private void updateClusters(NodeCluster cluster, Map<Integer, NodeCluster> clusterMap) {
		for (CyNode node: cluster) {
			clusterMap.put(nodes.indexOf(node), cluster);
		}
	}
}


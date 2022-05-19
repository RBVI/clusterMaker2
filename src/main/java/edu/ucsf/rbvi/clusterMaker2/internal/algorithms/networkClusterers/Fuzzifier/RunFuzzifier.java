package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.lang.Math;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.group.*;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Run Fuzzifier has the actual implementation of the fuzzifier algorithm
 * The algorithm calculates the fuzzy nature of nodes with respect to existing clusters
 * by the ratio of distance of a node from the centroid of a cluster over the sum of distances from centroids of all the clusters
 * @author Abhiraj
 *
 */

public class RunFuzzifier {

	HashMap<String,List<CyNode>> groupMap = null;

	private List<NodeCluster> clusters = null;
	private int number_clusters;
	private List<CyNode> nodeList= null;
	private boolean canceled = false;
	private TaskMonitor monitor;
	public final static String GROUP_ATTRIBUTE = "__FuzzifierGroups";
	protected int clusterCount = 0;
	private boolean createMetaNodes = false;
	private CyMatrix distanceMatrix = null;
	//private Matrix data = null;
	double [][] clusterMemberships = null;
	double membershipThreshold = 0;
	private boolean debug = false;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;

	public RunFuzzifier (List<NodeCluster> clusters, CyMatrix distanceMatrix, int cClusters,
			 double membershipThreshold,int maxThreads, TaskMonitor monitor ){

		this.clusters = clusters;
		this.distanceMatrix = distanceMatrix;
		this.number_clusters = cClusters;
		this.monitor = monitor;
		this.membershipThreshold = membershipThreshold;


		if (maxThreads > 0)
			nThreads = maxThreads;
		else
			nThreads = Runtime.getRuntime().availableProcessors()-1;

		monitor.showMessage(TaskMonitor.Level.INFO,"Membership Threshold = "+membershipThreshold);
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		//monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo(matrix));
		monitor.showMessage(TaskMonitor.Level.INFO,"Number of Clusters = "+number_clusters);

	}

	public void cancel () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }

	/**
	 * The method run has the actual implementation of the fuzzifier code
	 * @param monitor, Task monitor for the process
	 * @return List of FuzzyNodeCLusters
	 */

	public List<FuzzyNodeCluster> run(CyNetwork network, FuzzifierContext context, TaskMonitor monitor){

    /**
     * Algorithm (transcoded from the R usedist package's dist_to_centroids method:
     *   Given the matrix distMatrix:
     *       d2 = distMatrix ** 2
     *       group_items = list of nodes in each cluster
     *       group_sizes = sizes of each cluster
     *       group_d2s = list of subportions of the d2 for each cluster
     *       within_group_sums = list of sums for each group_d2s
     *       foreach node:
     *           foreach group:
     *               idx1 = group_items[group]
     *               n1 = group_sizes[group]
     *               sum1 = winthin_group_sums[group]
     *               sum12 = sum(d2[idx1, node]) # Note: idx1 is a vector
     *               term1 = sum1 / n1**2
     *               term12 = sum12 / n1
     *               result_squared = term12 - term1
     *               dist[node, group] = sqrt(result_sqared)
     */

		long startTime = System.currentTimeMillis();
		int nelements = distanceMatrix.nRows();
		nodeList = distanceMatrix.getRowNodes();
    int max_cluster = Math.max(NodeCluster.getMaxClusterNumber(), number_clusters);

		//Matrix to store the temporary cluster membership values of elements 
		double [][] clusterMemberships = new double[nelements][max_cluster];

		//Initializing all membership values to NaN
		for (int i = 0; i < nelements; i++){
			for (int j = 0; j < max_cluster; j++){
				clusterMemberships[i][j] = Double.NaN;
			}
		}

    monitor.showMessage(TaskMonitor.Level.INFO, "Calculating distances");

    CyMatrix d2 = distanceMatrix.copy();
    // System.out.println(d2.printMatrix());
    d2.ops().powScalar(2);
    // System.out.println(d2.printMatrix());
    List<NodeCluster> group_items = clusters;
    int[] group_sizes = get_sizes(group_items);
    // for (int i = 0; i < group_sizes.length; i++) { System.out.println("Group "+i+" has "+group_sizes[i]+" elements"); }
    double[] within_group_sums = get_sums(d2, group_items);
    // for (int i = 0; i < within_group_sums.length; i++) { System.out.println("Group "+i+" has sum "+within_group_sums[i]); }
    for (CyNode node: nodeList) {
      // This part can be done in parallel
      // get_distance(d2, node_index, group, group_size, clusterMemberships)
      // clusterMemberships needs to be in a critical section.
      for (NodeCluster idx1: group_items) {
        int group = idx1.getClusterNumber()-1;
        int n1 = group_sizes[group];
        double sum1 = within_group_sums[group];
        double sum12 = get_sum(d2, idx1, node);
        double term1 = sum1 / Math.pow(n1, 2);
        double term12 = sum12 / n1;
        double result_squared = term12 - term1;
        // clusterMemberships[nodeList.indexOf(node)][group] = result_squared;
        if (result_squared > 0.0d) {
          clusterMemberships[nodeList.indexOf(node)][group] = Math.sqrt(result_squared);
          // System.out.println("Node "+node+" is "+clusterMemberships[nodeList.indexOf(node)][group]+" away from group "+group);
        } else {
          clusterMemberships[nodeList.indexOf(node)][group] = Double.NaN;
        }
      }
    }

    long algTime = System.currentTimeMillis()-startTime;
    System.out.println("Algorithm took "+((double)algTime)/1000.0+" seconds");

    monitor.showMessage(TaskMonitor.Level.INFO, "Assigning fuzzy membership");

    // OK, at this point, we have a matrix with the squared distance from each node to the
    // the centroid of each cluster.  Now, we need to calculate the proportional
    // membership, which we do by normalizing all of the distances so that they sum to 1.0
    for (int node_index = 0; node_index< nelements; node_index++) {
      double sum = 0.0d;
      // System.out.println("Node: "+nodeList.get(node_index));
      for (int cluster_index = 0; cluster_index < max_cluster; cluster_index++) {
        double v = clusterMemberships[node_index][cluster_index];
        if (Double.isNaN(v))
          continue;
        sum += v;
      }
      // System.out.println("Node "+nodeList.get(node_index)+" distances sum to "+sum);
      for (int cluster_index = 0; cluster_index < max_cluster; cluster_index++) {
        double v = clusterMemberships[node_index][cluster_index];
        if (Double.isNaN(v))
          continue;
        CyNode node = nodeList.get(node_index);
        if (sum == 0.0)
          v = 1.0;
        else
          v = v/sum;
        // if (v < 1.0)
        //   v = 1.0 - v;
        clusterMemberships[node_index][cluster_index] = v;
        // System.out.println("Node "+nodeList.get(node_index)+" is "+clusterMemberships[node_index][cluster_index]+" away from group "+cluster_index);
      }
    }

    /*
    long assignTime = System.currentTimeMillis()-algTime;
    System.out.println("Assigning clusters took "+((double)assignTime)/1000.0+" seconds");
    */

    monitor.showMessage(TaskMonitor.Level.INFO, "Making cluster map");

		HashMap <CyNode, double[]> membershipMap = createMembershipMap(clusterMemberships);

		List<FuzzyNodeCluster> fuzzyClusters = new ArrayList<FuzzyNodeCluster>();

		// Adding the nodes which have memberships greater than the threshold to fuzzy node clusters
		List<CyNode> fuzzyNodeList;
		for(int i = 0 ; i < max_cluster; i++){
			fuzzyNodeList = new ArrayList<CyNode>();
			HashMap<CyNode, Double> clusterMembershipMap = new HashMap<CyNode, Double>();
			for( int node_index = 0; node_index < nodeList.size(); node_index++) {
        CyNode node = nodeList.get(node_index);
        double v = clusterMemberships[node_index][i];
				if (!Double.isNaN(v) && v > membershipThreshold) {
					fuzzyNodeList.add(node);
					clusterMembershipMap.put(node, v);
				}
			}

      if (fuzzyNodeList.size() < context.minClusterSize) 
        continue;

			FuzzyNodeCluster fCluster = new FuzzyNodeCluster(fuzzyNodeList,clusterMembershipMap);
      fCluster.setClusterNumber(i+1); // This will number this the same as the corresponding source cluster
			fuzzyClusters.add(fCluster);
		}

    long totalTime = System.currentTimeMillis()-startTime;
    System.out.println("Total time: "+((double)totalTime)/1000.0+" seconds");

		return fuzzyClusters;

	}

  private double get_sum(CyMatrix d2, NodeCluster cluster, CyNode node) {
    int n_index = nodeList.indexOf(node);
    double sum = 0.0d;
    for (CyNode c_node: cluster) {
      int c_index = nodeList.indexOf(c_node);
      sum += d2.doubleValue(n_index,c_index);
    }

    return sum;
  }

  private int[] get_sizes(List<NodeCluster> group_items) {
    int[] sizes = new int[NodeCluster.getMaxClusterNumber()];
    for (NodeCluster c: group_items) {
      sizes[c.getClusterNumber()-1] = c.size();
    }
    return sizes;
  }

  // For each cluster, sum up the distances
  private double[] get_sums(CyMatrix d2, List<NodeCluster> group_items) {
    double[] sums = new double[NodeCluster.getMaxClusterNumber()];
    List<CyNode> nodes = d2.getRowNodes();

    for (NodeCluster c: group_items) {
      int cluster_number = c.getClusterNumber()-1;
      sums[cluster_number] = 0.0d;

      int[] node_indices = node_index(c, nodes);
      for (int i = 0; i < node_indices.length; i++) {
        for (int j = i+1; j < node_indices.length; j++) {
          sums[cluster_number] += d2.doubleValue(node_indices[i], node_indices[j]);
        }
      }
    }
    return sums;
  }

  private int[] node_index(NodeCluster c, List<CyNode>nodes) {
    int[] indices = new int[c.size()];
    int index = 0;
    for (CyNode n: c) {
      indices[index++] = nodes.indexOf(n);
    }
    return indices;
  }

	/**
	 * Creates a Map from nodes to their respective membership arrays
	 * 
	 * @param membershipArray A double array of membership values
	 * @return membershipHM  A map from CyNodes to array of membership values 
	 */
	public HashMap <CyNode, double[]> createMembershipMap(double[][] membershipArray){

		HashMap<CyNode, double[]> membershipHM = new HashMap<CyNode, double[]>();
		for ( int i = 0; i<nodeList.size(); i++){
			membershipHM.put(nodeList.get(i), membershipArray[i]);
		}

		return membershipHM;
	}
}

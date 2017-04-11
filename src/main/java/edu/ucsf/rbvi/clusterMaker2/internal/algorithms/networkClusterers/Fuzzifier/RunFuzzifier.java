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

	private List<NodeCluster> Clusters = null;
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

	public RunFuzzifier (List<NodeCluster> Clusters, CyMatrix distanceMatrix, int cClusters,
			 double membershipThreshold,int maxThreads, TaskMonitor monitor ){

		this.Clusters = Clusters;
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

	public List<FuzzyNodeCluster> run(CyNetwork network, TaskMonitor monitor){

		Long networkID = network.getSUID();

		long startTime = System.currentTimeMillis();
		int nelements = distanceMatrix.nRows();
		nodeList = distanceMatrix.getRowNodes();

		//Matrix to store the temporary cluster membership values of elements 
		double [][] ClusterMemberships = new double[nelements][number_clusters];

		//Initializing all membership values to 0
		for (int i = 0; i < nelements; i++){
			for (int j = 0; j < number_clusters; j++){
				ClusterMemberships[i][j] = 0;
			}
		}

		// This matrix will store the centroid data
		CyMatrix cData = CyMatrixFactory.makeSmallMatrix(network, number_clusters, nelements);

		getFuzzyCenters(cData);

		for (CyNode node : nodeList){
			int nodeIndex = nodeList.indexOf(node);
			double sumDistances = 0;
			for (int i = 0 ; i < Clusters.size(); i++){
				sumDistances += cData.doubleValue(i, nodeIndex);
			}
			for(int i = 0 ; i < Clusters.size(); i++){
				ClusterMemberships[nodeIndex][i] = 	cData.doubleValue(i, nodeIndex)/sumDistances;
			} 
		}

		HashMap <CyNode, double[]> membershipMap = createMembershipMap(ClusterMemberships);

		List<FuzzyNodeCluster> fuzzyClusters = new ArrayList<FuzzyNodeCluster>();

		// Adding the nodes which have memberships greater than the threshold to fuzzy node clusters
		List<CyNode> fuzzyNodeList;
		for(int i = 0 ; i < number_clusters; i++){
			fuzzyNodeList = new ArrayList<CyNode>();
			HashMap<CyNode, Double> clusterMembershipMap = new HashMap<CyNode, Double>();
			for( CyNode node: nodeList){
				if (membershipMap.get(node)[i] > membershipThreshold ){
					fuzzyNodeList.add(node);
					clusterMembershipMap.put(node, membershipMap.get(node)[i]);
				}
			}
			fuzzyClusters.add(new FuzzyNodeCluster(fuzzyNodeList,clusterMembershipMap));
		}

		return fuzzyClusters;


	}

	/**
	 * The method calculates the centers of fuzzy clusters
	 * 
	 * @param cData matrix to store the data for cluster centers
	 */

	public void getFuzzyCenters(CyMatrix cData){

		// To store the sum of memberships(raised to fuzziness index) corresponding to each cluster
		int nelements = distanceMatrix.nRows();

		for (NodeCluster cluster : Clusters){
			int c = Clusters.indexOf(cluster);
			double numerator = 0;
			Double distance = 0.0;
			int i = 0;
			for (int e = 0; e < nelements; e++) {
				numerator = 0;
				for(CyNode node : cluster){
					i = nodeList.indexOf(node);
					distance = distanceMatrix.doubleValue(i,e);
					numerator += distance;
				}
				cData.setValue(c,e,(numerator/cluster.size()));
			}
		}
	}

	/**
	 * Creates a Map from nodes to their respective membership arrays
	 * 
	 * @param membershipArray A double array of membership values
	 * @return membershipHM  A map from CyNodes to array of membership values 
	 */
	public HashMap <CyNode, double[]> createMembershipMap(double[][] membershipArray){

		HashMap<CyNode, double[]> membershipHM = new HashMap<CyNode, double[]>();
		List<CyNode> nodeList = distanceMatrix.getRowNodes();
		for ( int i = 0; i<distanceMatrix.nRows(); i++){
			membershipHM.put(nodeList.get(i), membershipArray[i]);
		}

		return membershipHM;
	}
}

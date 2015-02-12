package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.subnetwork.CySubNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

import java.util.*;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 ** User: Gary Bader
 ** Date: Jan 20, 2004
 ** Time: 6:18:03 PM
 ** Description: An implementation of the MCODE algorithm
 **/

/**
 * An implementation of the MCODE algorithm
 */
public class MCODEAlgorithm {
	private boolean cancelled = false;
	private TaskMonitor taskMonitor = null;

	//data structure for storing information required for each node
	private class NodeInfo {
		double density;         //neighborhood density
		int numNodeNeighbors;   //number of node nieghbors
		List<CyNode> nodeNeighbors;    //stores node indices of all neighbors
		int coreLevel;          //e.g. 2 = a 2-core
		double coreDensity;     //density of the core neighborhood
		double score;           //node score

		public NodeInfo() {
			this.density = 0.0;
			this.numNodeNeighbors = 0;
			this.coreLevel = 0;
			this.coreDensity = 0.0;
			this.nodeNeighbors = new ArrayList<CyNode>();
		}
	}

	//data structures useful to have around for more than one cluster finding iteration
	private Map<CyNode,NodeInfo> currentNodeInfoHashMap = null;    //key is the node index, value is a NodeInfo instance
	private SortedMap<Double,List<CyNode>> currentNodeScoreSortedMap = null; //key is node score, value is nodeIndex

	//because every network can be scored and clustered several times with different parameters
	//these results have to be stored so that the same scores are used during exploration when
	//the user is switching between the various results
	//Since the network is not always rescored whenever a new result is generated (if the scoring parameters
	//haven't changed for example) the clustering method must save the current node scores under the new result
	//title for later reference
	private Map<Integer,SortedMap<Double,List<CyNode>>> nodeScoreResultsMap = 
		new HashMap<Integer, SortedMap<Double,List<CyNode>>>();//key is result, value is nodeScoreSortedMap
	private Map<Integer,Map<CyNode,NodeInfo>> nodeInfoResultsMap = 
		new HashMap<Integer, Map<CyNode,NodeInfo>>(); //key is result, value is nodeInfooHashMap

	private MCODEParameterSet params;   //the parameters used for this instance of the algorithm
	//stats
	private long lastScoreTime;
	private long lastFindTime;

	/**
	 * The constructor.  Use this to get an instance of MCODE to run.
	 *
	 * @param networkID Allows the algorithm to get the parameters of the focused network
	 */
	public MCODEAlgorithm(String networkID, TaskMonitor monitor) {
		//get current parameters
		params = MCODECurrentParameters.getInstance().getParamsCopy(networkID);
		this.taskMonitor = monitor;
	}

	public void setTaskMonitor(TaskMonitor taskMonitor, String networkID) {
		params = MCODECurrentParameters.getInstance().getParamsCopy(networkID);
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Get the time taken by the last score operation in this instance of the algorithm
	 *
	 * @return the duration of the scoring portion
	 */
	public long getLastScoreTime() {
		return lastScoreTime;
	}

	/**
	 * Get the time taken by the last find operation in this instance of the algorithm
	 *
	 * @return the duration of the finding process
	 */
	public long getLastFindTime() {
		return lastFindTime;
	}

	/**
	 * Get the parameter set used for this instance of MCODEAlgorithm
	 *
	 * @return The parameter set used
	 */
	public MCODEParameterSet getParams() {
		return params;
	}

	/**
	 * If set, will schedule the algorithm to be cancelled at the next convenient opportunity
	 *
	 * @param cancelled Set to true if the algorithm should be cancelled
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Gets the calculated node score of a node from a given result.  Used in MCODEResultsPanel
	 * during the attribute setting method.
	 *
	 * @param rootGraphIndex Integer which is used to identify the nodes in the score-sorted tree map
	 * @param resultId Id of the results for which we are retrieving a node score
	 * @return node score as a Double
	 */
	public double getNodeScore(CyNode node, int resultId) {
		Map<Double,List<CyNode>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultId);

		for (double nodeScore: nodeScoreSortedMap.keySet()) {
			List<CyNode> nodes = nodeScoreSortedMap.get(nodeScore);

			if (nodes.contains(node)) {
				return nodeScore;
			}
		}

		return 0.0;
	}

	/**
	 * Gets the highest node score in a given result.  Used in the MCODEVisualStyleAction class to
	 * re-initialize the visual calculators.
	 *
	 * @param resultTitle Title of the result
	 * @return First key in the nodeScoreSortedMap corresponding to the highest score
	 */
	public double getMaxScore(String resultTitle) {
		SortedMap<Double, List<CyNode>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultTitle);

		//Since the map is sorted, the first key is the highest value
		return nodeScoreSortedMap.firstKey();
	}

	/**
	 * Step 1: Score the graph and save scores as node attributes.  Scores are also
	 * saved internally in your instance of MCODEAlgorithm.
	 *
	 * @param inputNetwork The network that will be scored
	 * @param resultTitle Title of the result, used as an identifier in various hash maps
	 */
	public void scoreGraph(CyNetwork inputNetwork, int resultId) {
		params = getParams();
		String callerID = "MCODEAlgorithm.MCODEAlgorithm";

		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": inputNetwork was null.");
			return;
		}

		//initialize
		long msTimeBefore = System.currentTimeMillis();
		Map<CyNode, NodeInfo> nodeInfoHashMap = new HashMap<CyNode, NodeInfo>(inputNetwork.getNodeCount());

		Comparator<Double> scoreComparator = new Comparator<Double>() {
			@Override
			public int compare(Double d1, Double d2) {
				return d2.compareTo(d1);
			}
		};


		SortedMap<Double, List<CyNode>> nodeScoreSortedMap = new TreeMap<Double, List<CyNode>>(scoreComparator);

		//iterate over all nodes and calculate MCODE score
		NodeInfo nodeInfo = null;
		double nodeScore;
		List<CyNode> al;
		int i = 0;

		for (CyNode n: inputNetwork.getNodeList()) {
			if (cancelled) break;
			nodeInfo = calcNodeInfo(inputNetwork, n);
			nodeInfoHashMap.put(n, nodeInfo);
			//score node TODO: add support for other scoring functions (low priority)
			nodeScore = scoreNode(nodeInfo);

			//save score for later use in TreeMap
			//add a list of nodes to each score in case nodes have the same score
			if (nodeScoreSortedMap.containsKey(nodeScore)) {
				//already have a node with this score, add it to the list
				al = nodeScoreSortedMap.get(nodeScore);
				al.add(n);
			} else {
				al = new ArrayList<CyNode>();
				al.add(n);
				nodeScoreSortedMap.put(nodeScore, al);
			}

			if (taskMonitor != null) {
				i++;
				taskMonitor.setProgress((double)i / (double)inputNetwork.getNodeCount());
			}
		}

		nodeScoreResultsMap.put(resultId, nodeScoreSortedMap);
		nodeInfoResultsMap.put(resultId, nodeInfoHashMap);

		currentNodeScoreSortedMap = nodeScoreSortedMap;
		currentNodeInfoHashMap = nodeInfoHashMap;

		long msTimeAfter = System.currentTimeMillis();
		lastScoreTime = msTimeAfter - msTimeBefore;
	}

	/**
	 * Step 2: Find all clusters given a scored graph.  If the input network has not been scored,
	 * this method will return null.  This method is called when the user selects network scope or
	 * single node scope.
	 *
	 * @param inputNetwork The scored network to find clusters in.
	 * @param resultId Title of the result
	 * @return An array containing an MCODEClusterObj object for each cluster.
	 */
	public List<NodeCluster> findClusters(CyNetwork inputNetwork, int resultId) {
		SortedMap<Double,List<CyNode>> nodeScoreSortedMap;
		Map<CyNode, NodeInfo> nodeInfoHashMap;

		//First we check if the network has been scored under this result title (i.e. scoring
		//was required due to a scoring parameter change).  If it hasn't then we want to use the
		//current scores that were generated the last time the network was scored and store them
		//under the title of this result set for later use
		if (!nodeScoreResultsMap.containsKey(resultId)) {
			nodeScoreSortedMap = currentNodeScoreSortedMap;
			nodeInfoHashMap = currentNodeInfoHashMap;

			nodeScoreResultsMap.put(resultId, nodeScoreSortedMap);
			nodeInfoResultsMap.put(resultId, nodeInfoHashMap);
		} else {
			nodeScoreSortedMap = (TreeMap) nodeScoreResultsMap.get(resultId);
			nodeInfoHashMap = (HashMap) nodeInfoResultsMap.get(resultId);
		}
		params = getParams();
		String callerID = "MCODEAlgorithm.findClusters";
		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": inputNetwork was null.");
			return (null);
		}
		if ((nodeInfoHashMap == null) || (nodeScoreSortedMap == null)) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + 
			                        ": nodeInfoHashMap or nodeScoreSortedMap was null.");
			return (null);
		}

		//initialization
		long msTimeBefore = System.currentTimeMillis();
		Map<CyNode,Boolean> nodeSeenHashMap = 
			new HashMap<CyNode,Boolean>(); //key is nodeIndex, value is true/false

		int findingProgress = 0;
		int findingTotal = 0;
		Collection<List<CyNode>> values = nodeScoreSortedMap.values(); //returns a Collection sorted by key order (descending)
		//In order to track the progress without significant lags (for times when many nodes have the same score
		//and no progress is reported) we count all the scored nodes and track those instead
		for (List<CyNode> nodeList: values) {
			findingTotal += nodeList.size();
		}

		//stores the list of clusters as ArrayLists of node indices in the input Network
		List<MCODEClusterObj> alClusters = new ArrayList<MCODEClusterObj>();
		//iterate over node indices sorted descending by their score
		for (List<CyNode> alNodesWithSameScore: values) {
			for (CyNode currentNode: alNodesWithSameScore) {

				if (!nodeSeenHashMap.containsKey(currentNode)) {
					// Store the list of all the nodes that have already been seen and incorporated in other clusters
					Map<CyNode, Boolean> nodeSeenHashMapSnapShot = 
						new HashMap<CyNode, Boolean>(nodeSeenHashMap);

					List<CyNode> alCluster = getClusterCore(currentNode, nodeSeenHashMap, 
					                                        params.getNodeScoreCutoff(), params.getMaxDepthFromStart(), 
					                                        nodeInfoHashMap);//here we use the original node score cutoff
					if (alCluster.size() > 0) {
						//make sure seed node is part of cluster, if not already in there
						if (!alCluster.contains(currentNode)) {
							alCluster.add(currentNode);
						}
						//create an input graph for the filter and haircut methods
						CyNetwork clusterGraph = createCyNetwork(alCluster, inputNetwork);

						if (!filterCluster(clusterGraph)) {
							if (params.isHaircut()) {
								haircutCluster(clusterGraph, alCluster, inputNetwork);
							}
							if (params.isFluff()) {
								fluffClusterBoundary(alCluster, nodeSeenHashMap, nodeInfoHashMap);
							}

							clusterGraph = createCyNetwork(alCluster, inputNetwork);
							final double score = scoreCluster(clusterGraph);

							MCODEClusterObj currentCluster = new MCODEClusterObj(resultId, currentNode, clusterGraph, score, 
							                                                     alCluster, nodeSeenHashMapSnapShot);

							alClusters.add(currentCluster);
						}
					}
				}
				if (taskMonitor != null) {
					findingProgress++;
					//We want to be sure that only progress changes are reported and not
					//miniscule decimal increments so that the taskMonitor isn't overwhelmed
					double newProgress = (double)findingProgress / (double)findingTotal;
					double oldProgress = (double)(findingProgress-1) / (double)findingTotal;
					if (newProgress != oldProgress) {
						taskMonitor.setProgress(newProgress);
					}
				}
				if (cancelled) {
					break;
				}
			}
		}
		//Once the clusters have been found we either return them or in the case of selection scope, we select only
		//the ones that contain the selected node(s) and return those
		ArrayList selectedALClusters = new ArrayList();
		if (!params.getScope().equals(MCODEParameterSet.NETWORK)) {
			for (MCODEClusterObj cluster: alClusters) {
				List<CyNode> alCluster = cluster.getALCluster();
				List<CyNode> alSelectedNodes = new ArrayList<CyNode>(params.getSelectedNodes());

				//method for returning only clusters that contain all of the selected noes
				/*
				if (alCluster.containsAll(alSelectedNodes)) {
					selectedAlClusters.add(cluster);
				}
				*/
				//method for returning all clusters that contain any of the selected nodes
				boolean hit = false;
				for (CyNode node: alSelectedNodes) {
					if (alCluster.contains(node))
						hit = true;
				}
				if (hit)
					selectedALClusters.add(cluster);

			}
			alClusters = selectedALClusters;
		}
		//Finally convert the arraylist into a fixed array
		List<NodeCluster> clusters = new ArrayList<NodeCluster>();
		for (MCODEClusterObj c: alClusters) {
			clusters.add(c.getNodeCluster());
		}

		long msTimeAfter = System.currentTimeMillis();
		lastFindTime = msTimeAfter - msTimeBefore;

		return clusters;
	}

	private List<CyNode> getCyNodeList(List<Long> suidList, CyNetwork inputNetwork) {
		List<CyNode> nodeList = new ArrayList<CyNode>(suidList.size());
		for (Long suid: suidList) {
			nodeList.add(inputNetwork.getNode(suid));
		}
		return nodeList;
	}

	private CyNetwork createCyNetwork(List<CyNode> nodeList, CyNetwork inputNetwork) {
		final Set<CyEdge> edges = new HashSet<CyEdge>();

		for (final CyNode n: nodeList) {
			final Set<CyEdge> adjacentEdges = new HashSet<CyEdge>(inputNetwork.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork
			for (CyEdge e: adjacentEdges) {
				if (!params.isIncludeLoops() && e.getSource().getSUID() == e.getTarget().getSUID())
					continue;

				if (nodeList.contains(e.getSource()) && nodeList.contains(e.getTarget()))
					edges.add(e);
			}
		}
		CyNetwork gpCluster = ((CySubNetwork)inputNetwork).getRootNetwork().addSubNetwork(nodeList, edges);
		return gpCluster;
	}

	/**
	 * Score node using the formula from original MCODE paper.
	 * This formula selects for larger, denser cores.
	 * This is a utility function for the algorithm.
	 *
	 * @param nodeInfo The internal data structure to fill with node information
	 * @return The score of this node.
	 */
	private double scoreNode(NodeInfo nodeInfo) {
		if (nodeInfo.numNodeNeighbors > params.getDegreeCutoff()) {
			nodeInfo.score = nodeInfo.coreDensity * (double) nodeInfo.coreLevel;
		} else {
			nodeInfo.score = 0.0;
		}
		return (nodeInfo.score);
	}

	/**
	 * Score a cluster.  Currently this ranks larger, denser clusters higher, although
	 * in the future other scoring functions could be created
	 *
	 * @param cluster - The GINY CyNetwork version of the cluster
	 * @return The score of the cluster
	 */
	public double scoreCluster(CyNetwork clusterGraph) {
		int numNodes = 0;
		double density = 0.0, score = 0.0;

		numNodes = clusterGraph.getNodeCount();

		density = calcDensity(clusterGraph, params.isIncludeLoops());
		score = density * numNodes;

		// System.out.println("Density = "+density+", score = "+score);

		return score;
	}

	/**
	 * Calculates node information for each node according to the original MCODE publication.
	 * This information is used to score the nodes in the scoring stage.
	 * This is a utility function for the algorithm.
	 *
	 * @param inputNetwork The input network for reference
	 * @param nodeIndex    The index of the node in the input network to score
	 * @return A NodeInfo object containing node information required for the algorithm
	 */
	private NodeInfo calcNodeInfo(CyNetwork inputNetwork, CyNode node) {
		String callerID = "MCODEAlgorithm.calcNodeInfo";
		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return null;
		}

		//get neighborhood of this node (including the node)
		//long[] neighbors = inputNetwork.neighborsArray(nodeIndex);
		List<CyNode> neighborList = inputNetwork.getNeighborList(node, CyEdge.Type.ANY);
		// long[] neighbors = new long[neighborList.size()];
		// for (int i = 0; i < neighborList.size(); i++) neighbors[i] = neighborList.get(i).getSUID();

		if (neighborList.size() < 2) {
			//if there are no neighbors or just one neighbor, nodeInfo calculation is trivial
			NodeInfo nodeInfo = new NodeInfo();
			if (neighborList.size() == 1) {
				nodeInfo.coreLevel = 1;
				nodeInfo.coreDensity = 1.0;
				nodeInfo.density = 1.0;
			}
			return (nodeInfo);
		}

		//add original node to extract complete neighborhood
		/*
		Arrays.sort(neighbors);
		if (Arrays.binarySearch(neighbors, node.getSUID()) < 0) {
			neighborhood = new long[neighbors.length + 1];
			System.arraycopy(neighbors, 0, neighborhood, 1, neighbors.length);
			neighborhood[0] = node.getSUID();
		} else {
			neighborhood = neighbors;
		}
		*/
		if (!neighborList.contains(node))
			neighborList.add(node);

		//extract neighborhood subgraph
		CyNetwork gpNodeNeighborhood = createCyNetwork(neighborList, inputNetwork);
		// System.out.println("neighborhood subgraph has "+gpNodeNeighborhood.getNodeCount()+" nodes and "+
		//                    gpNodeNeighborhood.getEdgeCount()+" edges");
		if (gpNodeNeighborhood == null) {
			//this shouldn't happen
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpNodeNeighborhood was null.");
			return null;
		}

		//calculate the node information for each node
		NodeInfo nodeInfo = new NodeInfo();
		//density
		if (gpNodeNeighborhood != null) {
			nodeInfo.density = calcDensity(gpNodeNeighborhood, params.isIncludeLoops());
			// System.out.println("density is "+nodeInfo.density);
		}
		nodeInfo.numNodeNeighbors = neighborList.size();

		//calculate the highest k-core
		Object[] returnArray = getHighestKCore(gpNodeNeighborhood);
		Integer k = (Integer) returnArray[0];
		CyNetwork gpCore = (CyNetwork) returnArray[1];
		nodeInfo.coreLevel = k.intValue();
		//calculate the core density - amplifies the density of heavily interconnected regions and attenuates
		//that of less connected regions
		if (gpCore != null) {
			nodeInfo.coreDensity = calcDensity(gpCore, params.isIncludeLoops());
			// System.out.println("coreDensity is "+nodeInfo.coreDensity);
		}
		//record neighbor array for later use in cluster detection step
		nodeInfo.nodeNeighbors = neighborList;

		return (nodeInfo);
	}

	/**
	 * Find the high-scoring central region of the cluster.
	 * This is a utility function for the algorithm.
	 *
	 * @param startNode       The node that is the seed of the cluster
	 * @param nodeSeenHashMap The list of nodes seen already
	 * @param nodeScoreCutoff Slider input used for cluster exploration
	 * @param maxDepthFromStart Limits the number of recursions
	 * @param nodeInfoHashMap Provides the node scores
	 * @return A list of node IDs representing the core of the cluster
	 */
	private List<CyNode> getClusterCore(CyNode startNode, Map<CyNode,Boolean> nodeSeenHashMap,
	                                    double nodeScoreCutoff, int maxDepthFromStart, 
	                                    Map<CyNode, NodeInfo> nodeInfoHashMap) {
		List<CyNode> cluster = new ArrayList<CyNode>(); //stores Long nodeIndices
		getClusterCoreInternal(startNode, nodeSeenHashMap, 
		                       ((NodeInfo) nodeInfoHashMap.get(startNode)).score, 1, 
		                       cluster, nodeScoreCutoff, maxDepthFromStart, 
		                       nodeInfoHashMap);
		return (cluster);
	}

	/**
	 * An internal function that does the real work of getClusterCore, implemented to enable recursion.
	 *
	 * @param startNode         The node that is the seed of the cluster
	 * @param nodeSeenHashMap   The list of nodes seen already
	 * @param startNodeScore    The score of the seed node
	 * @param currentDepth      The depth away from the seed node that we are currently at
	 * @param cluster           The cluster to add to if we find a cluster node in this method
	 * @param nodeScoreCutoff   Helps determine if the nodes being added are within the given threshold
	 * @param maxDepthFromStart Limits the recursion
	 * @param nodeInfoHashMap   Provides score info
	 * @return true
	 */
	private boolean getClusterCoreInternal(CyNode startNode, Map<CyNode, Boolean> nodeSeenHashMap, double startNodeScore, 
	                                       int currentDepth, List<CyNode> cluster, double nodeScoreCutoff, 
	                                       int maxDepthFromStart,  Map<CyNode, NodeInfo> nodeInfoHashMap) {
		//base cases for recursion
		if (nodeSeenHashMap.containsKey(startNode)) {
			return true;  //don't recheck a node
		}
		nodeSeenHashMap.put(startNode, true);
		
		if (currentDepth > maxDepthFromStart) {
			return true;  //don't exceed given depth from start node
		}

		//Initialization
		for (CyNode currentNeighbor: nodeInfoHashMap.get(startNode).nodeNeighbors) {
			if ((!nodeSeenHashMap.containsKey(currentNeighbor)) &&
					(((NodeInfo) nodeInfoHashMap.get(currentNeighbor)).score >=
					(startNodeScore - startNodeScore * nodeScoreCutoff))) {
				//add current neighbor
				if (!cluster.contains(currentNeighbor)) {
					cluster.add(currentNeighbor);
				}
				//try to extend cluster at this node
				getClusterCoreInternal(currentNeighbor, nodeSeenHashMap, startNodeScore, 
				                       currentDepth + 1, cluster, nodeScoreCutoff, 
				                       maxDepthFromStart, nodeInfoHashMap);
			}
		}

		return (true);
	}

	/**
	 * Fluff up the cluster at the boundary by adding lower scoring, non cluster-core neighbors
	 * This implements the cluster fluff feature.
	 *
	 * @param cluster         The cluster to fluff
	 * @param nodeSeenHashMap The list of nodes seen already
	 * @param nodeInfoHashMap Provides neighbour info
	 * @return true
	 */
	private boolean fluffClusterBoundary(List<CyNode> cluster, Map<CyNode, Boolean> nodeSeenHashMap, 
	                                     Map<CyNode, NodeInfo> nodeInfoHashMap) {
		//create a temp list of nodes to add to avoid concurrently modifying 'cluster'
		List<CyNode> nodesToAdd = new ArrayList<CyNode>();

		//Keep a separate internal nodeSeenHashMap because nodes seen during a fluffing should not be marked as permanently seen,
		//they can be included in another cluster's fluffing step.
		Map<CyNode,Boolean> nodeSeenHashMapInternal = new HashMap<CyNode,Boolean>();

		//add all current neighbour's neighbours into cluster (if they have high enough clustering coefficients) and mark them all as seen
		for (CyNode currentNode: cluster) {
			for (CyNode nodeNeighbor: nodeInfoHashMap.get(currentNode).nodeNeighbors) {
				if ((!nodeSeenHashMap.containsKey(nodeNeighbor)) && (!nodeSeenHashMapInternal.containsKey(nodeNeighbor)) &&
						((nodeInfoHashMap.get(nodeNeighbor).density) > params.getFluffNodeDensityCutoff())) {
					nodesToAdd.add(nodeNeighbor);
					nodeSeenHashMapInternal.put(nodeNeighbor, new Boolean(true));
				}
			}
		}

		//Add fluffed nodes to cluster
		if (nodesToAdd.size() > 0) {
			cluster.addAll(nodesToAdd.subList(0, nodesToAdd.size()));
		}

		return (true);
	}

	/**
	 * Checks if the cluster needs to be filtered according to heuristics in this method
	 *
	 * @param gpClusterGraph The cluster to check if it passes the filter
	 * @return true if cluster should be filtered, false otherwise
	 */
	private boolean filterCluster(CyNetwork gpClusterGraph) {
		if (gpClusterGraph == null) {
			return (true);
		}

		//filter if the cluster does not satisfy the user specified k-core
		CyNetwork gpCore = getKCore(gpClusterGraph, params.getKCore());
		if (gpCore == null) {
			return (true);
		}

		return (false);
	}

	/**
	 * Gives the cluster a haircut (removed singly connected nodes by taking a 2-core)
	 *
	 * @param gpClusterGraph The cluster graph
	 * @param cluster        The cluster node ID list (in the original graph)
	 * @param gpInputGraph   The original input graph
	 * @return true
	 */
	private boolean haircutCluster(CyNetwork gpClusterGraph, List<CyNode> cluster, CyNetwork gpInputGraph) {
		//get 2-core
		CyNetwork gpCore = getKCore(gpClusterGraph, 2);
		if (gpCore != null) {
			//clear the cluster and add all 2-core nodes back into it
			cluster.clear();
			//must add back the nodes in a way that preserves gpInputGraph node indices
			for (CyNode node: gpCore.getNodeList()) {
				cluster.add(node);
			}
		}
		return (true);
	}

	/**
	 * Calculate the density of a network
	 * The density is defined as the number of edges/the number of possible edges
	 *
	 * @param gpInputGraph The input graph to calculate the density of
	 * @param includeLoops Include the possibility of loops when determining the number of
	 *                     possible edges.
	 * @return The density of the network
	 */
	public double calcDensity(CyNetwork gpInputGraph, boolean includeLoops) {
		double density = 0;
		int nodeCount = gpInputGraph.getNodeCount();
		int actualEdgeNum = getMergedEdgeCount(gpInputGraph.getEdgeList(), includeLoops);
		int possibleEdgeNum = 0;

		String callerID = "MCODEAlgorithm.calcDensity";
		if (gpInputGraph == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return (-1.0);
		}

		if (includeLoops) {
			possibleEdgeNum = (nodeCount * (nodeCount+1)) / 2;
		} else {
			possibleEdgeNum = (nodeCount * (nodeCount-1)) / 2;
		}

		// System.out.println(callerID+" nodeCount = "+nodeCount+", actualEdgeNum = "+actualEdgeNum+", possibleEdgeNum = "+possibleEdgeNum);

		density = possibleEdgeNum != 0 ? ((double) actualEdgeNum / (double) possibleEdgeNum) : 0;
		return (density);
	}

	/**
	 * Calculate the degree of a node taking into account whether to include loops or not.
	 *
	 * @param graph the input graph
	 * @param node the node we want the degree of
	 * @param includeLoops whether or not to consider loops
	 * @return the node degree
	 */
	private int getDegree(final CyNetwork graph, final CyNode node, final boolean includeLoops) {
		List<CyEdge> edgeList = graph.getAdjacentEdgeList(node, CyEdge.Type.ANY);

		return getMergedEdgeCount(edgeList, includeLoops);
	}

	/**
	 * Return the number of edges taking into account whether we want to include loops or not and
	 * whether we have multiple edges between the same nodes.
	 *
	 * @param edgeList the list edges we're looking at
	 * @param includeLoops whether to include loops or not
	 * @return the number of edges
	 */
	private int getMergedEdgeCount(final List<CyEdge> edgeList, final boolean includeLoops) {
		Set<String> suidPairs = new HashSet<String>();

		for (CyEdge e: edgeList) {
			Long id1 = e.getSource().getSUID();
			Long id2 = e.getTarget().getSUID();

			if (!includeLoops && id1 == id2)
				continue;

			String pair = id1 < id2 ? id1+"_"+id2 : id2+"_"+id1;
			suidPairs.add(pair);
		}

		return suidPairs.size();
	}


	/**
	 * Find a k-core of a network. A k-core is a subgraph of minimum degree k
	 *
	 * @param gpInputGraph The input network
	 * @param k            The k of the k-core to find e.g. 4 will find a 4-core
	 * @return Returns a subgraph with the core, if any was found at given k
	 */
	public CyNetwork getKCore(CyNetwork gpInputGraph, int k) {
		String callerID = "MCODEAlgorithm.getKCore";
		if (gpInputGraph == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return (null);
		}

		//filter all nodes with degree less than k until convergence
		boolean firstLoop = true;
		int numDeleted;
		CyNetwork gpOutputGraph = null;

		while (true) {
			numDeleted = 0;
			List<CyNode> alCoreNodeIndices = new ArrayList<CyNode>(gpInputGraph.getNodeCount());
			for (CyNode n: gpInputGraph.getNodeList()) {
				int degree = getDegree(gpInputGraph, n, params.isIncludeLoops());

				if (degree >= k)
					alCoreNodeIndices.add(n); //contains all nodes with degree >= k
				else
					numDeleted++;
			}

			if ((numDeleted > 0) || (firstLoop)) {
				gpOutputGraph = createCyNetwork(alCoreNodeIndices, gpInputGraph);
				if (gpOutputGraph.getNodeCount() == 0) {
					return (null);
				}

				//iterate again, but with a new k-core input graph
				gpInputGraph = gpOutputGraph;
				if (firstLoop) {
					firstLoop = false;
				}
			} else {
				//stop the loop
				break;
			}
		}

		return (gpOutputGraph);
	}

	/**
	 * Find the highest k-core in the input graph.
	 *
	 * @param gpInputGraph The input network
	 * @return Returns the k-value and the core as an Object array.
	 *         The first object is the highest k value i.e. objectArray[0]
	 *         The second object is the highest k-core as a CyNetwork i.e. objectArray[1]
	 */
	public Object[] getHighestKCore(CyNetwork gpInputGraph) {
		String callerID = "MCODEAlgorithm.getHighestKCore";
		if (gpInputGraph == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return (null);
		}

		int i = 1;
		CyNetwork gpCurCore = null, gpPrevCore = null;

		while ((gpCurCore = getKCore(gpInputGraph, i)) != null) {
			gpInputGraph = gpCurCore;
			gpPrevCore = gpCurCore;
			i++;
		}

		Integer k = new Integer(i - 1);
		Object[] returnArray = new Object[2];
		returnArray[0] = k;
		returnArray[1] = gpPrevCore;    //in the last iteration, gpCurCore is null (loop termination condition)
		// System.out.println("highest kcore = "+k);

		return (returnArray);
	}
}

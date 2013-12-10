package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.subnetwork.CySubNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

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
		long[] nodeNeighbors;    //stores node indices of all neighbors
		int coreLevel;          //e.g. 2 = a 2-core
		double coreDensity;     //density of the core neighborhood
		double score;           //node score

		public NodeInfo() {
			this.density = 0.0;
			this.numNodeNeighbors = 0;
			this.coreLevel = 0;
			this.coreDensity = 0.0;
		}
	}

	//data structures useful to have around for more than one cluster finding iteration
	private HashMap currentNodeInfoHashMap = null;    //key is the node index, value is a NodeInfo instance
	private TreeMap currentNodeScoreSortedMap = null; //key is node score, value is nodeIndex
	//because every network can be scored and clustered several times with different parameters
	//these results have to be stored so that the same scores are used during exploration when
	//the user is switching between the various results
	//Since the network is not always rescored whenever a new result is generated (if the scoring parameters
	//haven't changed for example) the clustering method must save the current node scores under the new result
	//title for later reference
	private HashMap nodeScoreResultsMap = new HashMap();//key is result, value is nodeScoreSortedMap
	private HashMap nodeInfoResultsMap = new HashMap(); //key is result, value is nodeInfroHashMap

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
	 * @param resultTitle Title of the results for which we are retrieving a node score
	 * @return node score as a Double
	 */
	public Double getNodeScore(long rootGraphIndex, String resultTitle) {
		Double nodeScore = new Double(0.0);
		TreeMap nodeScoreSortedMap = (TreeMap) nodeScoreResultsMap.get(resultTitle);

		for (Iterator score = nodeScoreSortedMap.keySet().iterator(); score.hasNext();) {
			nodeScore = (Double) score.next();
			ArrayList nodes = (ArrayList) nodeScoreSortedMap.get(nodeScore);
			if (nodes.contains(new Long(rootGraphIndex))) {
				return nodeScore;
			}
		}
		return nodeScore;
	}

	/**
	 * Gets the highest node score in a given result.  Used in the MCODEVisualStyleAction class to
	 * re-initialize the visual calculators.
	 *
	 * @param resultTitle Title of the result
	 * @return First key in the nodeScoreSortedMap corresponding to the highest score
	 */
	public double getMaxScore(String resultTitle) {
		TreeMap nodeScoreSortedMap = (TreeMap) nodeScoreResultsMap.get(resultTitle);
		//Since the map is sorted, the first key is the highest value
		Double nodeScore = (Double) nodeScoreSortedMap.firstKey();
		return nodeScore.doubleValue();
	}

	/**
	 * Step 1: Score the graph and save scores as node attributes.  Scores are also
	 * saved internally in your instance of MCODEAlgorithm.
	 *
	 * @param inputNetwork The network that will be scored
	 * @param resultTitle Title of the result, used as an identifier in various hash maps
	 */
	public void scoreGraph(CyNetwork inputNetwork, String resultTitle) {
		params = getParams();
		String callerID = "MCODEAlgorithm.MCODEAlgorithm";
		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": inputNetwork was null.");
			return;
		}

		//initialize
		long msTimeBefore = System.currentTimeMillis();
		HashMap nodeInfoHashMap = new HashMap(inputNetwork.getNodeCount());
		TreeMap nodeScoreSortedMap = new TreeMap(new Comparator() { //will store Doubles (score) as the key, Lists as values
			//sort Doubles in descending order
			public int compare(Object o1, Object o2) {
				double d1 = ((Double) o1).doubleValue();
				double d2 = ((Double) o2).doubleValue();
				if (d1 == d2) {
					return 0;
				} else if (d1 < d2) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		//iterate over all nodes and calculate MCODE score
		NodeInfo nodeInfo = null;
		double nodeScore;
		ArrayList al;
		int i = 0;
		for (CyNode n: inputNetwork.getNodeList()) {
			if (cancelled) break;
			nodeInfo = calcNodeInfo(inputNetwork, n);
			nodeInfoHashMap.put(n.getSUID(), nodeInfo);
			//score node TODO: add support for other scoring functions (low priority)
			nodeScore = scoreNode(nodeInfo);
			//save score for later use in TreeMap
			//add a list of nodes to each score in case nodes have the same score
			if (nodeScoreSortedMap.containsKey(new Double(nodeScore))) {
				//already have a node with this score, add it to the list
				al = (ArrayList) nodeScoreSortedMap.get(new Double(nodeScore));
				al.add(n.getSUID());
			} else {
				al = new ArrayList();
				al.add(n.getSUID());
				nodeScoreSortedMap.put(new Double(nodeScore), al);
			}
			if (taskMonitor != null) {
				i++;
				taskMonitor.setProgress((double)i / (double)inputNetwork.getNodeCount());
			}
		}
		nodeScoreResultsMap.put(resultTitle, nodeScoreSortedMap);
		nodeInfoResultsMap.put(resultTitle, nodeInfoHashMap);

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
	 * @param resultTitle Title of the result
	 * @return An array containing an MCODEClusterObj object for each cluster.
	 */
	public List<NodeCluster> findClusters(CyNetwork inputNetwork, String resultTitle) {
		TreeMap nodeScoreSortedMap;
		HashMap nodeInfoHashMap;
		//First we check if the network has been scored under this result title (i.e. scoring
		//was required due to a scoring parameter change).  If it hasn't then we want to use the
		//current scores that were generated the last time the network was scored and store them
		//under the title of this result set for later use
		if (!nodeScoreResultsMap.containsKey(resultTitle)) {
			nodeScoreSortedMap = currentNodeScoreSortedMap;
			nodeInfoHashMap = currentNodeInfoHashMap;
			
			nodeScoreResultsMap.put(resultTitle, nodeScoreSortedMap);
			nodeInfoResultsMap.put(resultTitle, nodeInfoHashMap);
		} else {
			nodeScoreSortedMap = (TreeMap) nodeScoreResultsMap.get(resultTitle);
			nodeInfoHashMap = (HashMap) nodeInfoResultsMap.get(resultTitle);
		}
		params = getParams();
		MCODEClusterObj currentCluster;
		String callerID = "MCODEAlgorithm.findClusters";
		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": inputNetwork was null.");
			return (null);
		}
		if ((nodeInfoHashMap == null) || (nodeScoreSortedMap == null)) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": nodeInfoHashMap or nodeScoreSortedMap was null.");
			return (null);
		}

		//initialization
		long msTimeBefore = System.currentTimeMillis();
		HashMap nodeSeenHashMap = new HashMap(); //key is nodeIndex, value is true/false
		Long currentNode;
		int findingProgress = 0;
		int findingTotal = 0;
		Collection values = nodeScoreSortedMap.values(); //returns a Collection sorted by key order (descending)
		//In order to track the progress without significant lags (for times when many nodes have the same score
		//and no progress is reported) we count all the scored nodes and track those instead
		for (Iterator iterator1 = values.iterator(); iterator1.hasNext();) {
			ArrayList value = (ArrayList) iterator1.next();
			for(Iterator iterator2 = value.iterator(); iterator2.hasNext();) {
				iterator2.next();
				findingTotal++;
			}
		}
		//stores the list of clusters as ArrayLists of node indices in the input Network
		ArrayList<MCODEClusterObj> alClusters = new ArrayList();
		//iterate over node indices sorted descending by their score
		ArrayList alNodesWithSameScore;                                                                                                                            
		for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			//each score may be associated with multiple nodes, iterate over these lists
			alNodesWithSameScore = (ArrayList) iterator.next();
			for (int j = 0; j < alNodesWithSameScore.size(); j++) {
				currentNode = (Long) alNodesWithSameScore.get(j);
				if (!nodeSeenHashMap.containsKey(currentNode)) {
					currentCluster = new MCODEClusterObj();
					currentCluster.setSeedNode(currentNode);//store the current node as the seed node
					//we store the current node seen hash map for later exploration purposes
					HashMap nodeSeenHashMapSnapShot = new HashMap((HashMap)nodeSeenHashMap.clone());

					ArrayList alCluster = getClusterCore(currentNode, nodeSeenHashMap, params.getNodeScoreCutoff(), params.getMaxDepthFromStart(), nodeInfoHashMap);//here we use the original node score cutoff
					if (alCluster.size() > 0) {
						//make sure seed node is part of cluster, if not already in there
						if (!alCluster.contains(currentNode)) {
							alCluster.add(currentNode);
						}
						//create an input graph for the filter and haircut methods
						CyNetwork gpCluster = createCyNetwork(getCyNodeList(alCluster,inputNetwork), inputNetwork);
						if (!filterCluster(gpCluster)) {
							if (params.isHaircut()) {
								haircutCluster(gpCluster, alCluster, inputNetwork);
							}
							if (params.isFluff()) {
								fluffClusterBoundary(alCluster, nodeSeenHashMap, nodeInfoHashMap);
							}
							currentCluster.setALCluster(alCluster);
							gpCluster = createCyNetwork(alCluster, inputNetwork);
							currentCluster.setGPCluster(gpCluster);
							currentCluster.setClusterScore(scoreCluster(currentCluster));
							currentCluster.setNodeSeenHashMap(nodeSeenHashMapSnapShot);//store the list of all the nodes that have already been seen and incorporated in other clusters
							currentCluster.setResultTitle(resultTitle);
							//store detected cluster for later
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
				ArrayList alCluster = cluster.getALCluster();
				ArrayList alSelectedNodes = new ArrayList();
				for (int c = 0; c < params.getSelectedNodes().length; c++) {
					alSelectedNodes.add(params.getSelectedNodes()[c]);
				}
				//method for returning only clusters that contain all of the selected noes
				/*
				if (alCluster.containsAll(alSelectedNodes)) {
					selectedAlClusters.add(cluster);
				}
				*/
				//method for returning all clusters that contain any of the selected nodes
				boolean hit = false;
				for (Iterator in = alSelectedNodes.iterator(); in.hasNext();) {
					if (alCluster.contains((Long) in.next())) {
						hit = true;
					}
				}
				if (hit) {
					selectedALClusters.add(cluster);
				}
			}
			alClusters = selectedALClusters;
		}
		//Finally convert the arraylist into a fixed array
		List<NodeCluster> clusters = new ArrayList();
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

	private CyNetwork createCyNetwork(long nodeArray[], CyNetwork inputNetwork) {
		List<CyNode> nodeList = new ArrayList<CyNode>(nodeArray.length);
		for (int i = 0; i < nodeArray.length; i++) {
			nodeList.add(inputNetwork.getNode(nodeArray[i]));
		}

		CyNetwork gpCluster = ((CySubNetwork)inputNetwork).getRootNetwork().addSubNetwork(nodeList, null);

		return gpCluster;
	}

	private CyNetwork createCyNetwork(List<CyNode> nodeList, CyNetwork inputNetwork) {
		CyNetwork gpCluster = ((CySubNetwork)inputNetwork).getRootNetwork().addSubNetwork(nodeList, null);
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
	public double scoreCluster(MCODEClusterObj cluster) {
		int numNodes = 0;
		double density = 0.0, score = 0.0;

		numNodes = cluster.getGPCluster().getNodeCount();
		density = calcDensity(cluster.getGPCluster(), true);
		score = density * numNodes;

		return (score);
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
		long[] neighborhood;

		String callerID = "MCODEAlgorithm.calcNodeInfo";
		if (inputNetwork == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return null;
		}

		//get neighborhood of this node (including the node)
		//long[] neighbors = inputNetwork.neighborsArray(nodeIndex);
		List<CyNode> neighborList = inputNetwork.getNeighborList(node, CyEdge.Type.ANY);
		long[] neighbors = new long[neighborList.size()];
		for (int i = 0; i < neighborList.size(); i++) neighbors[i] = neighborList.get(i).getSUID();

		if (neighbors.length < 2) {
			//if there are no neighbors or just one neighbor, nodeInfo calculation is trivial
			NodeInfo nodeInfo = new NodeInfo();
			if (neighbors.length == 1) {
				nodeInfo.coreLevel = 1;
				nodeInfo.coreDensity = 1.0;
				nodeInfo.density = 1.0;
			}
			return (nodeInfo);
		}
		//add original node to extract complete neighborhood
		Arrays.sort(neighbors);
		if (Arrays.binarySearch(neighbors, node.getSUID()) < 0) {
			neighborhood = new long[neighbors.length + 1];
			System.arraycopy(neighbors, 0, neighborhood, 1, neighbors.length);
			neighborhood[0] = node.getSUID();
		} else {
			neighborhood = neighbors;
		}

		List<CyNode> neighborhoodList = new ArrayList<CyNode>(neighborhood.length);
		for (int i = 0; i < neighborhood.length; i++)
			neighborhoodList.add(inputNetwork.getNode(neighborhood[i]));

		//extract neighborhood subgraph
		CyNetwork gpNodeNeighborhood = createCyNetwork(neighborhood, inputNetwork);
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
		}
		nodeInfo.numNodeNeighbors = neighborhood.length;
		//calculate the highest k-core
		CyNetwork gpCore = null;
		Integer k = null;
		Object[] returnArray = getHighestKCore(gpNodeNeighborhood);
		k = (Integer) returnArray[0];
		gpCore = (CyNetwork) returnArray[1];
		nodeInfo.coreLevel = k.intValue();
		//calculate the core density - amplifies the density of heavily interconnected regions and attenuates
		//that of less connected regions
		if (gpCore != null) {
			nodeInfo.coreDensity = calcDensity(gpCore, params.isIncludeLoops());
		}
		//record neighbor array for later use in cluster detection step
		nodeInfo.nodeNeighbors = neighborhood;

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
	private ArrayList getClusterCore(Long startNode, HashMap nodeSeenHashMap, double nodeScoreCutoff, int maxDepthFromStart, HashMap nodeInfoHashMap) {
		ArrayList cluster = new ArrayList(); //stores Long nodeIndices
		getClusterCoreInternal(startNode, nodeSeenHashMap, ((NodeInfo) nodeInfoHashMap.get(startNode)).score, 1, cluster, nodeScoreCutoff, maxDepthFromStart, nodeInfoHashMap);
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
	private boolean getClusterCoreInternal(Long startNode, HashMap nodeSeenHashMap, double startNodeScore, int currentDepth, ArrayList cluster, double nodeScoreCutoff, int maxDepthFromStart,  HashMap nodeInfoHashMap) {
		//base cases for recursion
		if (nodeSeenHashMap.containsKey(startNode)) {
			return (true);  //don't recheck a node
		}
		nodeSeenHashMap.put(startNode, new Boolean(true));
		
		if (currentDepth > maxDepthFromStart) {
			return (true);  //don't exceed given depth from start node
		}

		//Initialization
		Long currentNeighbor;
		int i = 0;
		for (i = 0; i < (((NodeInfo) nodeInfoHashMap.get(startNode)).numNodeNeighbors); i++) {
			//go through all currentNode neighbors to check their core density for cluster inclusion
			currentNeighbor = new Long(((NodeInfo) nodeInfoHashMap.get(startNode)).nodeNeighbors[i]);
			if ((!nodeSeenHashMap.containsKey(currentNeighbor)) &&
					(((NodeInfo) nodeInfoHashMap.get(currentNeighbor)).score >=
					(startNodeScore - startNodeScore * nodeScoreCutoff))) {
				//add current neighbor
				if (!cluster.contains(currentNeighbor)) {
					cluster.add(currentNeighbor);
				}
				//try to extend cluster at this node
				getClusterCoreInternal(currentNeighbor, nodeSeenHashMap, startNodeScore, currentDepth + 1, cluster, nodeScoreCutoff, maxDepthFromStart, nodeInfoHashMap);
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
	private boolean fluffClusterBoundary(ArrayList cluster, HashMap nodeSeenHashMap, HashMap nodeInfoHashMap) {
		long currentNode = 0, nodeNeighbor = 0;
		//create a temp list of nodes to add to avoid concurrently modifying 'cluster'
		ArrayList nodesToAdd = new ArrayList();

		//Keep a separate internal nodeSeenHashMap because nodes seen during a fluffing should not be marked as permanently seen,
		//they can be included in another cluster's fluffing step.
		HashMap nodeSeenHashMapInternal = new HashMap();

		//add all current neighbour's neighbours into cluster (if they have high enough clustering coefficients) and mark them all as seen
		for (int i = 0; i < cluster.size(); i++) {
			currentNode = ((Long) cluster.get(i)).longValue();
			for (int j = 0; j < ((NodeInfo) nodeInfoHashMap.get(new Long(currentNode))).numNodeNeighbors; j++) {
				nodeNeighbor = ((NodeInfo) nodeInfoHashMap.get(new Long(currentNode))).nodeNeighbors[j];
				if ((!nodeSeenHashMap.containsKey(new Long(nodeNeighbor))) && (!nodeSeenHashMapInternal.containsKey(new Long(nodeNeighbor))) &&
						((((NodeInfo) nodeInfoHashMap.get(new Long(nodeNeighbor))).density) > params.getFluffNodeDensityCutoff())) {
					nodesToAdd.add(new Long(nodeNeighbor));
					nodeSeenHashMapInternal.put(new Long(nodeNeighbor), new Boolean(true));
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
	private boolean haircutCluster(CyNetwork gpClusterGraph, ArrayList cluster, CyNetwork gpInputGraph) {
		//get 2-core
		CyNetwork gpCore = getKCore(gpClusterGraph, 2);
		if (gpCore != null) {
			//clear the cluster and add all 2-core nodes back into it
			cluster.clear();
			//must add back the nodes in a way that preserves gpInputGraph node indices
			for (CyNode node: gpCore.getNodeList()) {
				cluster.add(node.getSUID());
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
		int possibleEdgeNum = 0, actualEdgeNum = 0, loopCount = 0;
		double density = 0;

		String callerID = "MCODEAlgorithm.calcDensity";
		if (gpInputGraph == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "In " + callerID + ": gpInputGraph was null.");
			return (-1.0);
		}

		if (!includeLoops) {
			//count loops
			for (CyNode node: gpInputGraph.getNodeList()) {
				if (gpInputGraph.getConnectingEdgeList(node, node, CyEdge.Type.ANY).size() > 0)
					loopCount++;
			}
			possibleEdgeNum = (gpInputGraph.getNodeCount() * (gpInputGraph.getNodeCount()-1)) / 2;
			actualEdgeNum = gpInputGraph.getEdgeCount() - loopCount;
		} else {
			possibleEdgeNum = (gpInputGraph.getNodeCount() * (gpInputGraph.getNodeCount()-1)) / 2;
			actualEdgeNum = gpInputGraph.getEdgeCount();
		}

		density = (double) actualEdgeNum / (double) possibleEdgeNum;
		return (density);
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
				if (gpInputGraph.getNeighborList(n, CyEdge.Type.ANY).size() >= k) {
					alCoreNodeIndices.add(n); //contains all nodes with degree >= k
				} else {
					numDeleted++;
				}
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

		return (returnArray);
	}
}

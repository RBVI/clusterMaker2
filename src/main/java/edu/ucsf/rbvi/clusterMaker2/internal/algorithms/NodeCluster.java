package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;


/**
 * In it's simplist form, a Cluster is a group of nodes that represents the
 * nodes that are grouped together as the result of a clustering algorithm
 * of some sort.  A more complicated form of a cluster could include clusters
 * as part of the list, which complicates this class a little....
 */
public class NodeCluster extends ArrayList<CyNode> {
	int clusterNumber = 0;
    private int rank = 0;
    private double rankScore = 0;
	static int clusterCount = 0;
	static boolean hasScore = false;
	protected double score = 0.0;
	
	private CyNetworkView view; // keeps track of layout so that layout process doesn't have to be repeated unnecessarily
	private boolean disposed;

	public NodeCluster() {
		super();
		clusterCount++;
		clusterNumber = clusterCount;
	}

	public NodeCluster(Collection<CyNode> collection) {
		super(collection);
		clusterCount++;
		clusterNumber = clusterCount;
	}

	public boolean add(CyNode node) {
		return super.add(node);
	}

	public boolean add(List<CyNode>nodeList, int index) {
		return super.add(nodeList.get(index));
	}

	public static void init() { clusterCount = 0; hasScore = false; }
	public static boolean hasScore() { return hasScore; }

	public int getClusterNumber() { return clusterNumber; }

	public void setClusterNumber(int clusterNumber) { 
		this.clusterNumber = clusterNumber; 
	}

	public void setClusterScore(double score) { 
		this.score = score; 
		hasScore = true;
	}

	public double getClusterScore() { return score; }

    public int getRank() { return rank; }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getRankScore() { return rankScore; }

    public void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }


	public String toString() {
		String str = "("+clusterNumber+": ";
		for (Object i: this) 
			str += i.toString();
		return str+")";
	}

	public static List<Double> getScoreList(List<NodeCluster> list) {
		if (!hasScore())
			return null;

		List<Double> scoreList = new ArrayList<Double>(list.size());
		for (NodeCluster cluster: list) {
			scoreList.add(null);
		}

		for (NodeCluster cluster: list) {
			scoreList.add(cluster.getClusterNumber()-1, cluster.getClusterScore());
		}
		return scoreList;
	}

	public static List<NodeCluster> sortMap(Map<Integer, NodeCluster> map) {
		NodeCluster[] clusterArray = map.values().toArray(new NodeCluster[1]);
		Arrays.sort(clusterArray, new LengthComparator());
		return Arrays.asList(clusterArray);
	}

	public static List<NodeCluster> rankListByScore(List<NodeCluster> list) {
		NodeCluster[] clusterArray = list.toArray(new NodeCluster[1]);
		Arrays.sort(clusterArray, new ScoreComparator());
		for (int rank = 0; rank < clusterArray.length; rank++) {
			clusterArray[rank].setClusterNumber(rank+1);
		}
		return Arrays.asList(clusterArray);
	}

	static class LengthComparator implements Comparator {
		public int compare (Object o1, Object o2) {
			List c1 = (List)o1;
			List c2 = (List)o2;
			if (c1.size() > c2.size()) return -1;
			if (c1.size() < c2.size()) return 1;
			return 0;
		}
	}

	static class ScoreComparator implements Comparator {
		public int compare (Object o1, Object o2) {
			NodeCluster c1 = (NodeCluster)o1;
			NodeCluster c2 = (NodeCluster)o2;
			if (c1.getClusterScore() > c2.getClusterScore()) return -1;
			if (c1.getClusterScore() < c2.getClusterScore()) return 1;
			return 0;
		}
	}
	
	public boolean isFuzzy(){
		return false;
	}
	
	
	/* Method to get the subnetwork formed by the nodes in the cluster
	 * 
	 * @param net parent network
	 * @param nodes
	 */
	public CySubNetwork getSubNetwork(final CyNetwork net,final CyRootNetwork root, final SavePolicy policy){
		
		//final CyRootNetwork root = rootNetworkMgr.getRootNetwork(net);
		final Set<CyEdge> edges = new HashSet<CyEdge>();
		
		for (CyNode n : this) {
			Set<CyEdge> adjacentEdges = new HashSet<CyEdge>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (CyEdge e : adjacentEdges) {
				if (this.contains(e.getSource()) && this.contains(e.getTarget())) {
					edges.add(e);
				}
			}
		}

		final CySubNetwork subNet = root.addSubNetwork(this, edges, policy);

		// Not sure if this is required for a basic results panel
		/*
		// Save it for later disposal
		Set<CySubNetwork> snSet = createdSubNetworks.get(root);

		if (snSet == null) {
			snSet = new HashSet<CySubNetwork>();
			createdSubNetworks.put(root, snSet);
		}

		snSet.add(subNet);
		*/
		return subNet;
		
	}
	
	public synchronized CyNetworkView getView() {
		return view;
	}
	
	public synchronized void setView(final CyNetworkView view) {
		throwExceptionIfDisposed();

		if (this.view != null)
			this.view.dispose();

		this.view = view;
	}
	
	private void throwExceptionIfDisposed() {
		if (isDisposed())
			throw new RuntimeException("NodeCluster has been disposed and cannot be used anymore: ");
	}
	
	public synchronized boolean isDisposed() {
		return disposed;
	}
}

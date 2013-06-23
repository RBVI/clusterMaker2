package org.cytoscape.myapp.internal.algorithms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNode;


/**
 * In it's simplist form, a Cluster is a group of nodes that represents the
 * nodes that are grouped together as the result of a clustering algorithm
 * of some sort.  A more complicated form of a cluster could include clusters
 * as part of the list, which complicates this class a little....
 */
public class NodeCluster extends ArrayList<CyNode> {
	int clusterNumber = 0;
	static int clusterCount = 0;
	static boolean hasScore = false;
	protected double score = 0.0;

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

	public boolean add(List<CyNode>nodeList, int index) {
		return add(nodeList.get(index));
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


	public String toString() {
		String str = "("+clusterNumber+": ";
		for (Object i: this) 
			str += i.toString();
		return str+")";
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
}
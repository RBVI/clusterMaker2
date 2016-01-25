package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNode;
import org.apache.log4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

public class MatrixUtils {
	public static Integer[] indexSort(double[] tData,int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
	}

	public static Map<Integer, List<CyNode>> findConnectedComponents(CyMatrix matrix) {
		// Get the colt matrix
		Map<Integer, List<CyNode>> cmap = new HashMap<Integer, List<CyNode>>();
		Map<CyNode, Integer> nodeToCluster = new HashMap<CyNode, Integer>();
		int clusterNumber = 0;
		for (int row=0; row < matrix.nRows(); row++) {
			CyNode node1 = matrix.getRowNode(row);
			int colStart = 0;
			if (matrix.isSymmetrical())
				colStart = row;
			for (int col=colStart; col < matrix.nColumns(); col++) {
				CyNode node2 = matrix.getColumnNode(col);
				if (!matrix.hasValue(row, col))
					continue;
				if (nodeToCluster.containsKey(node1)) {
					if (!nodeToCluster.containsKey(node2)) {
						addNodeToCluster(nodeToCluster, cmap, nodeToCluster.get(node1), node2);
					} else {
						combineClusters(nodeToCluster, cmap, nodeToCluster.get(node1), nodeToCluster.get(node2));
					}
				} else {
					if (nodeToCluster.containsKey(node2)) {
						addNodeToCluster(nodeToCluster, cmap, nodeToCluster.get(node2), node1);
					} else {
						createCluster(nodeToCluster, cmap, clusterNumber, node1, node2);
						clusterNumber++;
					}
				}
			}
		}
		return cmap;
	}

	private static void addNodeToCluster(Map<CyNode, Integer> nodeToCluster, 
	                                     Map<Integer, List<CyNode>> clusterMap,
	                                     Integer cluster, CyNode node) {
		List<CyNode> nodeList = clusterMap.get(cluster);
		nodeList.add(node);
		nodeToCluster.put(node, cluster);
	}

	private static void createCluster(Map<CyNode, Integer> nodeToCluster, 
	                                  Map<Integer, List<CyNode>> clusterMap,
																		int clusterNumber,
																		CyNode node1, CyNode node2) {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		clusterMap.put(clusterNumber, nodeList);
		addNodeToCluster(nodeToCluster, clusterMap, clusterNumber, node1);
		addNodeToCluster(nodeToCluster, clusterMap, clusterNumber, node2);
	}

	private static void combineClusters(Map<CyNode, Integer> nodeToCluster, 
	                                    Map<Integer, List<CyNode>> clusterMap,
	                                    Integer cluster1, Integer cluster2) {
		if (cluster1.intValue() == cluster2.intValue())
			return;
		// System.out.println("Combining cluster "+cluster1+" and "+cluster2);
		List<CyNode> list1 = clusterMap.get(cluster1);
		List<CyNode> list2 = clusterMap.get(cluster2);
		clusterMap.remove(cluster2);
		for (CyNode node: list2) {
			nodeToCluster.put(node, cluster1);
		}
		list1.addAll(list2);
	}

	public static CyMatrix multiplyMatrix(CyMatrix A, CyMatrix B) {
		return null;
	}

	public static CyMatrix matrixPow(CyMatrix A, double power) {
		return null;
	}

	public static void normalizeMatrix(CyMatrix A) {
	}

	public static void normalizeMatrix(CyMatrix A, double min, double max) {
	}

	public static Integer[] indexSort(int[] tData, int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
	}

	private static class IndexComparator implements Comparator<Integer> {
		double[] data = null;
		int[] intData = null;

		public IndexComparator(double[] data) { this.data = data; }

		public IndexComparator(int[] data) { this.intData = data; }

		public int compare(Integer o1, Integer o2) {
			if (data != null) {
				if (data[o1] < data[o2]) return -1;
				if (data[o1] > data[o2]) return 1;
				return 0;
			} else if (intData != null) {
				if (intData[o1] < intData[o2]) return -1;
				if (intData[o1] > intData[o2]) return 1;
				return 0;
			}
			return 0;
		}
	}
}


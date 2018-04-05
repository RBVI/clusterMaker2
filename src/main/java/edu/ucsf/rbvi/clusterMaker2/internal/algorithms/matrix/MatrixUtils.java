package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.apache.log4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

public class MatrixUtils {

	/*
	public static Map<Integer, List<CyNode>> findConnectedComponents(CyMatrix matrix) {
		// Get the matrix
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
	*/

	/**
	 * Depth first search of graph to find connected components
	 */
	public static Map<Integer, List<CyNode>> findConnectedComponents(CyMatrix matrix) {
		Map<CyNode, Integer> nodeToCluster = new HashMap<CyNode, Integer>();
		Map<Integer, List<CyNode>> cmap = new HashMap<Integer, List<CyNode>>();
		int component = -1;

		CyNetwork network = matrix.getNetwork();
		for (CyNode node: network.getNodeList()) {
			if (!nodeToCluster.containsKey(node)) {
				component += 1;
				addNodeToMaps(nodeToCluster, cmap, node, component);
				dfs(nodeToCluster, cmap, network, node, component);
			}
		}
		return cmap;
	}

	private static void dfs(Map<CyNode, Integer> nodeToCluster, 
	                        Map<Integer, List<CyNode>> cmap, CyNetwork net, 
	                        CyNode u, int component) {

		for (CyNode v: net.getNeighborList(u, CyEdge.Type.ANY)) {
			if (!nodeToCluster.containsKey(v)) {
				addNodeToMaps(nodeToCluster, cmap, v, component);
				dfs(nodeToCluster, cmap, net, v, component);
			}
		}
	}

	private static void addNodeToMaps(Map<CyNode, Integer> nodeToCluster, 
	                                  Map<Integer, List<CyNode>> cmap, 
	                                  CyNode node, int component) {

		nodeToCluster.put(node, component);

		if (!cmap.containsKey(component)) {
			cmap.put(component, new ArrayList<CyNode>());
		}
		cmap.get(component).add(node);
	}

	public static Integer[] indexSort(double[] tData,int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
	}

	public static Integer[] indexSort(String[] tData,int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
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
		String[] stringData = null;

		public IndexComparator(String[] data) { this.stringData = data; }

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
			} else if (stringData != null) {
				return stringData[o1].compareTo(stringData[o2]);
			}
			return 0;
		}
	}
}


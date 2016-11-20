/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
// import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
// import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

// clusterMaker imports

public class RunHierarchical {
	final static int IS = 0;
	final static int JS = 1;
	String weightAttributes[];
	TaskMonitor monitor;
	HierarchicalContext context;
	DistanceMetric metric;
	CyNetwork network;
	Integer[] rowOrder;
	List<String>attrList;
	final boolean debug = false;
	CyMatrix matrix = null;

	// Instance variables
	ClusterMethod clusterMethod;

	public RunHierarchical(CyNetwork network, String[] weightAttributes, DistanceMetric metric, 
	                       ClusterMethod clusterMethod, TaskMonitor monitor, HierarchicalContext context) {
		this.weightAttributes = weightAttributes;
		this.metric = metric;
		this.clusterMethod = clusterMethod;
		this.monitor = monitor;
		this.context = context;
		this.network = network;
	}

	public Integer[] cluster(boolean transpose) { 
		String keyword = "GENE";
		if (transpose) keyword = "ARRY";

		if (debug) {
			for (int att = 0; att < weightAttributes.length; att++)
				monitor.showMessage(TaskMonitor.Level.INFO,"Attribute: '"+weightAttributes[att]+"'");
		}

		if (monitor != null) 
			monitor.setStatusMessage("Creating initial matrix");

		// Create the matrix
		matrix = CyMatrixFactory.makeSmallMatrix(network, weightAttributes, context.selectedOnly, 
		                                         context.ignoreMissing, transpose, context.isAssymetric());

		// Handle special cases
		if (context.zeroMissing)
			matrix.setMissingToZero();

		// This only makes sense for symmetrical matrices
		if (context.adjustDiagonals && matrix.isSymmetrical())
			matrix.adjustDiagonals();

		// If we have a symmetric matrix, and our weightAttribute is an edge attribute
		// then we need to force the distance metric to be "none"
		/*
		if (matrix.isSymmetrical() && weightAttributes.length == 1 && 
		    weightAttributes[0].startsWith("edge.")) {
			if (!metric.equals(DistanceMetric.VALUE_IS_CORRELATION) &&
					!metric.equals(DistanceMetric.VALUE_IS_DISTANCE))
				metric = DistanceMetric.VALUE_IS_CORRELATION;
		}
		*/

		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");

		// Cluster
		TreeNode[] nodeList = treeCluster(matrix, metric, clusterMethod);
		if (nodeList == null || nodeList.length == 0) 
			monitor.showMessage(TaskMonitor.Level.ERROR,"treeCluster returned empty tree!");

		if (metric == DistanceMetric.EUCLIDEAN || metric == DistanceMetric.CITYBLOCK) {
			// Normalize distances to between 0 and 1
			double scale = 0.0;
			for (int node = 0; node < nodeList.length; node++) {
				if (nodeList[node].getDistance() > scale) scale = nodeList[node].getDistance();
			}
			if (scale != 0.0) {
				for (int node = 0; node < nodeList.length; node++) {
					double dist = nodeList[node].getDistance();
					nodeList[node].setDistance(dist/scale);
				}
			}
		}

		if (monitor != null) 
			monitor.setStatusMessage("Creating tree");

		// Join the nodes
		double[] nodeOrder = new double[nodeList.length];
		int[] nodeCounts = new int[nodeList.length];
		String[] nodeID = new String[nodeList.length];
		attrList = new ArrayList<String>(nodeList.length);

		for (int node = 0; node < nodeList.length; node++) {
			int min1 = nodeList[node].getLeft();
			int min2 = nodeList[node].getRight();

			double order1;
			double order2;
			double counts1;
			double counts2;
			String ID1;
			String ID2;
			nodeID[node] = "GROUP"+(node+1)+"X";
			nodeList[node].setName("GROUP"+(node+1)+"X");
			if (min1 < 0) {
				int index1 = -min1-1;
				order1 = nodeOrder[index1];
				counts1 = (double) nodeCounts[index1];
				// ID1 = nodeID[index1];
				ID1 = nodeList[index1].getName();
				nodeList[node].setDistance(Math.max(nodeList[node].getDistance(), nodeList[index1].getDistance()));
			} else {
				order1 = min1;
				counts1 = 1.0;
				// ID1 = keyword+min1+"X"; // Shouldn't this be the name of the gene/condition?
				ID1 = matrix.getRowLabel(min1);
			}

			if (min2 < 0) {
				int index2 = -min2-1;
				order2 = nodeOrder[index2];
				counts2 = (double) nodeCounts[index2];
				// ID2 = nodeID[index2];
				ID2 = nodeList[index2].getName();
				nodeList[node].setDistance(Math.max(nodeList[node].getDistance(), nodeList[index2].getDistance()));
			} else {
				order2 = (double) min2;
				counts2 = 1.0;
				// ID2 = keyword+min2+"X"; // Shouldn't this be the name of the gene/condition?
				ID2 = matrix.getRowLabel(min2);
			}

			attrList.add(node, nodeList[node].getName()+"\t"+ID1+"\t"+ID2+"\t"+(1.0-nodeList[node].getDistance()));
			// System.out.println(attrList.get(node));

			nodeCounts[node] = (int)counts1 + (int)counts2;
			nodeOrder[node] = (counts1*order1 + counts2*order2) / (counts1 + counts2);
		}

		// Now sort based on tree structure
		rowOrder = treeSort(matrix, nodeList.length, nodeOrder, nodeCounts, nodeList);

		// Finally, create the group hierarchy
		// The root is the last entry in our nodeList
		if (!matrix.isTransposed()) {
			if (monitor != null) 
				monitor.setStatusMessage("Creating groups");
			ArrayList<String> groupNames = new ArrayList<String>(nodeList.length);
			if (context.createGroups) {
/*
				CyGroup top = createGroups(matrix, nodeList, nodeList[nodeList.length-1], groupNames);
				CyGroupManager.setGroupViewer(top, "namedSelection", Cytoscape.getCurrentNetworkView(), true);
*/
			}
			// Remember this in the _hierarchicalGroups attribute
			ModelUtils.createAndSetLocal(network, network, HierarchicalCluster.SHORTNAME, 
			                             groupNames, List.class, String.class);
		}

		return rowOrder;
	}

	public CyMatrix getMatrix() { return matrix; }
	public List<String> getAttributeList() { return attrList; }


	private TreeNode[] treeCluster(CyMatrix matrix, DistanceMetric metric, ClusterMethod clusterMethod) { 

		// if (debug)
		// 	matrix.printMatrix();
		if (monitor != null)
			monitor.showMessage(TaskMonitor.Level.INFO,"Getting distance matrix");

		double[][] distanceMatrix = matrix.getDistanceMatrix(metric).toArray();

		TreeNode[] result = null;
		// For debugging purposes, output the distance matrix
		// for (int row = 1; row < matrix.nRows(); row++) {
		// 	for (int col = 0; col < row; col++) {
		// 		System.out.print(distanceMatrix[row][col]+"\t");
		// 	}
		// 	System.out.println();
		// }

		switch (clusterMethod) {
			case SINGLE_LINKAGE:
				if (monitor != null) 
					monitor.showMessage(TaskMonitor.Level.INFO,"Calculating single linkage hierarchical cluster");
				result = pslCluster(matrix, distanceMatrix, metric);
				break;

			case MAXIMUM_LINKAGE:
				if (monitor != null) 
					monitor.showMessage(TaskMonitor.Level.INFO,"Calculating maximum linkage hierarchical cluster");
				result = pmlcluster(matrix.nRows(), distanceMatrix);
				break;

			case AVERAGE_LINKAGE:
				if (monitor != null) 
					monitor.showMessage(TaskMonitor.Level.INFO,"Calculating average linkage hierarchical cluster");
				result = palcluster(matrix.nRows(), distanceMatrix);
				break;

			case CENTROID_LINKAGE:
				if (monitor != null) 
					monitor.showMessage(TaskMonitor.Level.INFO,"Calculating centroid linkage hierarchical cluster");
				result = pclcluster(matrix, distanceMatrix, metric);
				break;
		}
		return result;
	}

	/**
 	 * The pslcluster routine performs single-linkage hierarchical clustering, using
 	 * either the distance matrix directly, if available, or by calculating the
 	 * distances from the data array. This implementation is based on the SLINK
 	 * algorithm, described in:
 	 * Sibson, R. (1973). SLINK: An optimally efficient algorithm for the single-link
 	 * cluster method. The Computer Journal, 16(1): 30-34.
 	 * The output of this algorithm is identical to conventional single-linkage
 	 * hierarchical clustering, but is much more memory-efficient and faster. Hence,
 	 * it can be applied to large data sets, for which the conventional single-
 	 * linkage algorithm fails due to lack of memory.
 	 *
 	 * @param matrix the data matrix containing the data and labels
 	 * @param distanceMatrix the distances that will be used to actually do the clustering.
 	 * @param metric the distance metric to be used.
 	 * @return the array of TreeNode's that describe the hierarchical clustering solution, or null if
 	 * it it files for some reason.
 	 **/

	private TreeNode[] pslCluster(CyMatrix matrix, double[][] distanceMatrix, DistanceMetric metric) {
		int nRows = matrix.nRows();
		int nNodes = nRows-1;

		int[] vector = new int[nNodes];
		TreeNode[] nodeList = new TreeNode[nNodes]; 
		// Initialize
		for (int i = 0; i < nNodes; i++) {
			vector[i] = i;
			nodeList[i] = new TreeNode(Double.MAX_VALUE);
		}

		int k = 0;
		double[] temp = new double[nNodes];

		for (int row = 0; row < nRows; row++) {
			if (distanceMatrix != null) {
				for (int j = 0; j < row; j++) temp[j] = distanceMatrix[row][j];
			} else {
				for (int j = 0; j < row; j++)
					temp[j] = metric.getMetric(matrix, matrix, row, j);
			}
			for (int j = 0; j < row; j++) {
				k = vector[j];
				if (nodeList[j].getDistance() >= temp[j]) {
					if (nodeList[j].getDistance() < temp[k]) temp[k] = nodeList[j].getDistance();
					nodeList[j].setDistance(temp[j]);
					vector[j] = row;
				} else if (temp[j] < temp[k]) temp[k] = temp[j];
			}
			for (int j = 0; j < row; j++) {
				if (vector[j] == nNodes || nodeList[j].getDistance() >= nodeList[vector[j]].getDistance()) vector[j] = row;
			}
		}


		for (int row = 0; row < nNodes; row++)
			nodeList[row].setLeft(row);

		Arrays.sort(nodeList, new NodeComparator());

		int[] index = new int[nRows];
		for (int i = 0; i < nRows; i++) index[i] = i;
		for (int i = 0; i < nNodes; i++) {
			int j = nodeList[i].getLeft();
			k = vector[j];
			nodeList[i].setLeft(index[j]);
			nodeList[i].setRight(index[k]);
			index[k] = -i-1;
		}

		return nodeList;
	}

	/**
 	 * The pclcluster routine performs clustering, using pairwise centroid-linking
 	 * on a given set of gene expression data, using the distrance metric given by metric.
 	 *
 	 * @param matrix the data matrix containing the data and labels
 	 * @param distanceMatrix the distances that will be used to actually do the clustering.
 	 * @param metric the distance metric to be used.
 	 * @return the array of TreeNode's that describe the hierarchical clustering solution, or null if
 	 * it it files for some reason.
 	 **/
	private TreeNode[] pclcluster(CyMatrix matrix, double[][] distanceMatrix, DistanceMetric metric) {
		int nRows = matrix.nRows();
		int nColumns = matrix.nColumns();
		int nNodes = nRows-1;
		double mask[][] = new double[matrix.nRows()][matrix.nColumns()];

		TreeNode[] nodeList = new TreeNode[nNodes]; 

		// Initialize
		CyMatrix newData = matrix.copy();
		// System.out.println("New matrix: ");
		// newData.printMatrix();

		int distID[] = new int[nRows];
		for (int row = 0; row < nRows; row++) {
			distID[row] = row;
			for (int col = 0; col < nColumns; col++) {
				if (newData.hasValue(row, col))
					mask[row][col] = 1.0;
				else
					mask[row][col] = 0.0;
			}
			if (row < nNodes)
				nodeList[row] = new TreeNode(Double.MAX_VALUE);
		}

		int pair[] = new int[2];

		for (int inode = 0; inode < nNodes; inode++) {
			// find the pair with the shortest distance
			pair[IS] = 1; pair[JS] = 0;
			double distance = findClosestPair(nRows-inode, distanceMatrix, pair);
			nodeList[inode].setDistance(distance);

			int is = pair[IS];
			int js = pair[JS];
			nodeList[inode].setLeft(distID[js]);
			nodeList[inode].setRight(distID[is]);
	
			// make node js the new node
			for (int col = 0; col < nColumns; col++) {
				double jsValue = newData.doubleValue(js, col);
				double isValue = newData.doubleValue(is, col);
				double newValue = 0.0;
				if (newData.hasValue(js,col)) newValue = jsValue * mask[js][col];
				if (newData.hasValue(is,col)) newValue += isValue * mask[is][col];

				if (newData.hasValue(js,col) || newData.hasValue(is,col)) {
					newData.setValue(js, col, newValue);
				}
				mask[js][col] += mask[is][col];
				if (mask[js][col] != 0) {
					newData.setValue(js, col, newValue / mask[js][col]);
				}
			}

			for (int col = 0; col < nColumns; col++) {
				mask[is][col] = mask[nNodes-inode][col];
				newData.setValue(is, col, newData.getValue(nNodes-inode, col));
			}

			// Fix the distances
			distID[is] = distID[nNodes-inode];
			for (int i = 0; i < is; i++) {
				distanceMatrix[is][i] = distanceMatrix[nNodes-inode][i];
			}

			for (int i = is+1; i < nNodes-inode; i++) {
				distanceMatrix[i][is] = distanceMatrix[nNodes-inode][i];
			}

			distID[js] = -inode-1;
			for (int i = 0; i < js; i++) {
				distanceMatrix[js][i] = metric.getMetric(newData, newData, js, i);
			}
			for (int i = js+1; i < nNodes-inode; i++) {
				distanceMatrix[i][js] = metric.getMetric(newData, newData, js, i);
			}
		}

		return nodeList;
	}

	/**
	 * The pmlcluster routine performs clustering using pairwise maximum- (complete-)
	 * linking on the given distance matrix.
	 * 
	 * @param nRows The number of rows to be clustered
	 * @param distanceMatrix The distance matrix, with rows rows, each row being filled up to the
	 * diagonal. The elements on the diagonal are not used, as they are assumed to be
	 * zero. The distance matrix will be modified by this routine.
	 * @return the array of TreeNode's that describe the hierarchical clustering solution, or null if
	 * it fails for some reason.
	 */
	private TreeNode[] pmlcluster(int nRows, double[][] distanceMatrix) {
		int[] clusterID = new int[nRows];
		TreeNode[] nodeList = new TreeNode[nRows-1]; 

		for (int j = 0; j < nRows; j++) {
			clusterID[j] = j;
		}

		int pair[] = new int[2];
		for (int n = nRows; n > 1; n--) {
			pair[0] = 1; pair[1] = 2;
			if (nodeList[nRows-n] == null)
				nodeList[nRows-n] = new TreeNode(Double.MAX_VALUE);
			nodeList[nRows-n].setDistance(findClosestPair(n, distanceMatrix, pair));
			int is = pair[0];
			int js = pair[1];

			// Fix the distances
			for (int j = 0; j < js; j++)
				distanceMatrix[js][j] = Math.max(distanceMatrix[is][j],distanceMatrix[js][j]);
			for (int j = js+1; j < is; j++)
				distanceMatrix[j][js] = Math.max(distanceMatrix[is][j],distanceMatrix[j][js]);
			for (int j = is+1; j < n; j++)
				distanceMatrix[j][js] = Math.max(distanceMatrix[j][is],distanceMatrix[j][js]);
			for (int j = 0; j < is; j++)
				distanceMatrix[is][j] = distanceMatrix[n-1][j];
			for (int j = is+1; j < n-1; j++)
				distanceMatrix[j][is] = distanceMatrix[n-1][j];

			// Update cluster IDs
			nodeList[nRows-n].setLeft(clusterID[is]);
			nodeList[nRows-n].setRight(clusterID[js]);
			clusterID[js] = n-nRows-1;
			clusterID[is] = clusterID[n-1];
		}
		return nodeList;
	}

	/**
	 * The pmlcluster routine performs clustering using pairwise average
	 * linking on the given distance matrix.
	 * 
	 * @param nRows The number of rows to be clustered
	 * @param distanceMatrix The distance matrix, with rows rows, each row being filled up to the
	 * diagonal. The elements on the diagonal are not used, as they are assumed to be
	 * zero. The distance matrix will be modified by this routine.
	 * @return the array of TreeNode's that describe the hierarchical clustering solution, or null if
	 * it fails for some reason.
	 */
	private TreeNode[] palcluster(int nRows, double[][] distanceMatrix) {
		int[] clusterID = new int[nRows];
		int[] number = new int[nRows];
		TreeNode[] nodeList = new TreeNode[nRows-1]; 

		// Setup a list specifying to which cluster a gene belongs, and keep track
		// of the number of elements in each cluster (needed to calculate the
		// average).
		for (int j = 0; j < nRows; j++) {
			number[j] = 1;
			clusterID[j] = j;
		}

		int pair[] = new int[2];
		for (int n = nRows; n > 1; n--) {
			int sum = 0;
			pair[IS] = 1; pair[JS] = 0;
			if (nodeList[nRows-n] == null)
				nodeList[nRows-n] = new TreeNode(Double.MAX_VALUE);
			double distance = findClosestPair(n, distanceMatrix, pair);
			nodeList[nRows-n].setDistance(distance);

			// Save result
			int is = pair[IS];
			int js = pair[JS];
			nodeList[nRows-n].setLeft(clusterID[is]);
			nodeList[nRows-n].setRight(clusterID[js]);

			// Fix the distances
			sum = number[is] + number[js];
			for (int j = 0; j < js; j++) {
				distanceMatrix[js][j] = (distanceMatrix[is][j]*(double)number[is] + distanceMatrix[js][j]*(double)number[js])/(double)sum;
			}

			for (int j = js+1; j < is; j++) {
				distanceMatrix[j][js] = (distanceMatrix[is][j]*(double)number[is] + distanceMatrix[j][js]*(double)number[js])/(double)sum;
			}

			for (int j = is+1; j < n; j++) {
				distanceMatrix[j][js] = (distanceMatrix[j][is]*(double)number[is] + distanceMatrix[j][js]*(double)number[js])/(double)sum;
			}

			for (int j = 0; j < is; j++) {
				distanceMatrix[is][j] = distanceMatrix[n-1][j];
			}
			for (int j = is+1; j < n-1; j++) {
				distanceMatrix[j][is] = distanceMatrix[n-1][j];
			}

			// Update number of elements in the clusters
			number[js] = sum;
			number[is] = number[n-1];

			// Update cluster IDs
			clusterID[js] = n-nRows-1;
			clusterID[is] = clusterID[n-1];
		}
		return nodeList;
	}

	/**
 	 * This function searches the distance matrix to find the pair with the shortest
 	 * distance between them. The indices of the pair are returned in ip and jp; the
 	 * distance itself is returned by the function.
 	 *
 	 * n          (input) int
 	 * The number of elements in the distance matrix.
 	 *
 	 * distanceMatrix (input) double[][]
 	 * A ragged array containing the distance matrix. The number of columns in each
 	 * row is one less than the row index.
 	 *
 	 * pair         (output) int[2]
 	 * An array with two values representing the first and second indices of the pair
 	 * with the shortest distance.
 	 */
	private double findClosestPair(int n, double[][] distanceMatrix, int[] pair) {
		int ip = 1;
		int jp = 0;
		double temp;
		double distance = distanceMatrix[1][0];
		for (int i = 1; i < n; i++) {
			for (int j = 0; j < i; j++) {
				temp = distanceMatrix[i][j];
				if (temp < distance) {
					distance = temp;
					ip = i;
					jp = j;
				}
			}
		}
		pair[IS] = ip;
		pair[JS] = jp;
		return distance;
	}

	private Integer[] treeSort(CyMatrix matrix, int nNodes, double nodeOrder[], int nodeCounts[], TreeNode nodeList[]) {
		int nElements = nNodes+1;
		double newOrder[] = new double[nElements];
		int clusterIDs[] = new int[nElements];
		double order1, order2;
		int count1, count2, i1, i2;
		
		for (int i = 0; i < nElements; i++) clusterIDs[i] = i;

		// for (int i = 0; i < nodeOrder.length; i++)
		//  	System.out.println("nodeOrder["+i+"] = "+nodeOrder[i]);

		for (int i = 0; i < nNodes; i++) {
			i1 = nodeList[i].getLeft();
			i2 = nodeList[i].getRight();
			if (i1 < 0) {
				order1 = nodeOrder[-i1-1];
				count1 = nodeCounts[-i1-1];
			} else {
				order1 = (double) i1;
				count1 = 1;
			}

			if (i2 < 0) {
				order2 = nodeOrder[-i2-1];
				count2 = nodeCounts[-i2-1];
			} else {
				order2 = (double) i2;
				count2 = 1;
			}

			// If order1 and order2 are equal, their order is determined by the
			// order in which they were clustered
			if (i1 < i2) {
				double increase = count2;
				if (order1 < order2)
					increase = count1;
				for (int j = 0; j < nElements; j++) {
					int clusterID = clusterIDs[j];
					if (clusterID == i1 && order1 >= order2) newOrder[j] += increase;
					if (clusterID == i2 && order1 < order2) newOrder[j] += increase;
					if (clusterID == i1 || clusterID == i2) clusterIDs[j] = -i-1;
				}
			} else {
				double increase = count2;
				if (order1 <= order2)
					increase = count1;
				for (int j = 0; j < nElements; j++) {
					int clusterID = clusterIDs[j];
					if (clusterID == i1 && order1 > order2) newOrder[j] += increase;
					if (clusterID == i2 && order1 <= order2) newOrder[j] += increase;
					if (clusterID == i1 || clusterID == i2) clusterIDs[j] = -i-1;
				}
			}
		}
		// for (int i = 0; i < newOrder.length; i++)
		// 	System.out.println("newOrder["+i+"] = "+newOrder[i]);

		Integer[] rowOrder = MatrixUtils.indexSort(newOrder, newOrder.length);
		if (debug) {
			/*
			for (int i = 0; i < rowOrder.length; i++) {
				monitor.showMessage(TaskMonitor.Level.INFO,""+i+": "+matrix.getRowLabel(rowOrder[i].intValue()));
			}
			*/
		}
		return rowOrder;
	}

/*
	// We can't use the common createGroups because we're creating an explicit hierarchy
	private CyGroup createGroups(Matrix matrix, TreeNode nodeList[], TreeNode node, List<String>groupNames) {
		ArrayList<CyNode>memberList = new ArrayList<CyNode>(2);
		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		// Do a right-first descend of the tree
		if (node.getRight() < 0) {
			int index = -node.getRight() - 1;
			CyGroup rightGroup = createGroups(matrix, nodeList, nodeList[index], groupNames);
			if (rightGroup != null)
				memberList.add(rightGroup.getGroupNode());
		} else {
			memberList.add(matrix.getRowNode(node.getRight()));
		}

		if (node.getLeft() < 0) {
			int index = -node.getLeft() - 1;
			CyGroup leftGroup = createGroups(matrix, nodeList, nodeList[index], groupNames);
			if (leftGroup != null)
			memberList.add(leftGroup.getGroupNode());
		} else {
			memberList.add(matrix.getRowNode(node.getLeft()));
		}

		// System.out.println("Creating group "+node.getName()+" with nodes "+memberList.get(0).getIdentifier()+" and "+memberList.get(1).getIdentifier());

		// Create the group for this level
		monitor.showMessage(TaskMonitor.Level.INFO,"Creating group "+node.getName());
		CyGroup group = CyGroupManager.createGroup(node.getName(), memberList, null);
		if (group == null) {
			monitor.showMessage(TaskMonitor.Level.INFO,"...already exists -- removing");
			// Hmmm....the group already exists -- clean it up
			group = CyGroupManager.findGroup(node.getName());
			// Remove the group
			CyGroupManager.removeGroup(group);
			// Try again to create it
			group = CyGroupManager.createGroup(node.getName(), memberList, null);
		}

		if (group != null) {
			CyGroupManager.setGroupViewer(group, "namedSelection", Cytoscape.getCurrentNetworkView(), false);
			groupNames.add(node.getName());
		}

		return group;
	}
*/
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.CyIdentifiableNameComparator;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;

public class CyMatrixFactory {
	enum MatrixType {SIMPLE, COLT, SPARSE, LARGE, OJALGO};

	/**
	 * Create an empty matrix that may be very large
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @return the empty matrix
	 */
	public static CyMatrix makeLargeMatrix(CyNetwork network, int rows, int columns) {
		return makeTypedMatrix(network, rows, columns, false, MatrixType.LARGE);
	}

	/**
	 * Create a sparse empty matrix
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @return the empty matrix
	 */
	public static CyMatrix makeSparseMatrix(CyNetwork network, int rows, int columns) {
		return makeTypedMatrix(network, rows, columns, false, MatrixType.SPARSE);
	}

	/**
	 * Create a large, possibly sparse matrix populated with data from
	 * the indicated edge attribute
	 *
	 * @param network the network that will be the source of the data
	 * @param edgeAttribute the edge attribute to pull the data from
	 * @param selectedOnly only include selected edges
	 * @param converter the edge weight converter to use
	 * @param unDirected if true, the edges are undirected
	 * @param cutOff the minimum edge value to consider
	 * @return the resulting matrix
	 */
	public static CyMatrix makeLargeMatrix(CyNetwork network, String edgeAttribute, 
	                                        boolean selectedOnly, EdgeWeightConverter converter,
																					boolean unDirected, double cutOff) {
		List<CyNode> nodes;
		List<CyEdge> edges;
		double maxAttribute = Double.MIN_VALUE;
		double minAttribute = Double.MAX_VALUE;
		if (!selectedOnly) {
			nodes = network.getNodeList();
			edges = network.getEdgeList();
		} else {
			nodes = new ArrayList<CyNode>();
			edges = new ArrayList<CyEdge>();
			nodes.addAll(CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true));
			edges.addAll(ModelUtils.getConnectingEdges(network,nodes));
		}

		CyMatrix matrix = makeTypedMatrix(network, nodes.size(), nodes.size(), false, MatrixType.LARGE);
		matrix.setRowNodes(nodes);
		matrix.setColumnNodes(nodes);
		Map<CyNode, Integer> nodeMap = new HashMap<CyNode, Integer>(nodes.size());
		for (int row = 0; row < nodes.size(); row++) {
			CyNode node = nodes.get(row);
			nodeMap.put(node, row);
			matrix.setRowLabel(row, ModelUtils.getNodeName(network, node));
			matrix.setColumnLabel(row, ModelUtils.getNodeName(network, node));
		}

		matrix.setSymmetrical(unDirected);

		CyTable edgeAttributes = network.getDefaultEdgeTable();

		// First, we need the min and max values for our converter
		if( edgeAttributes.getColumn(edgeAttribute) == null) {
			minAttribute = 1.0;
			maxAttribute = 1.0;
		} else {
			for(CyEdge edge: edges) {
    	  if (network.getRow(edge).getRaw(edgeAttribute) == null) 
					continue;

				double edgeWeight = ModelUtils.getNumericValue(network, edge, edgeAttribute);
				if (edgeWeight < cutOff)
					continue;
				minAttribute = Math.min(minAttribute, edgeWeight);
				maxAttribute = Math.max(maxAttribute, edgeWeight);
			}
		}

		for (CyEdge edge: edges) {
			double value;
			if (minAttribute == 1.0 && maxAttribute == 1.0) {
				value = 1.0;
			} else {
				value = ModelUtils.getNumericValue(network, edge, edgeAttribute);
			}

			double weight = converter.convert(value, minAttribute, maxAttribute);

			if (weight < cutOff)
				continue;

			int sourceIndex = nodeMap.get(edge.getSource());
			int targetIndex = nodeMap.get(edge.getTarget());
			matrix.setValue(targetIndex, sourceIndex, weight);
			// TODO: should we consider maybe doing this on the getValue side?
			if (unDirected)
				matrix.setValue(sourceIndex, targetIndex, weight);
		}

		// System.out.println("distance matrix: "+matrix.printMatrix());
		return matrix;
	}

	/**
	 * Create a large, possibly sparse matrix populated with data from
	 * the indicated node attributes
	 *
	 * @param network the network that will be the source of the data
	 * @param attributes the array of attributes.  If only one attribute is provided,
	 *                   it begins with "edge.", then a symmetric network is created.
	 * @param selectedOnly only include selected edges
	 * @param ignoreMissing ignore nodes/edges with missing data
	 * @param transpose transpose the nodes and attribute in the network (used for
	 *                  clustering attributes rather than nodes).
	 * @param assymetric true if we're looking at edge attributes but the matrix is not square.
	 * @return the resulting matrix
	 */
	public static CyMatrix makeLargeMatrix(CyNetwork network, String[] attributes,
	                                       boolean selectedOnly, boolean ignoreMissing,
										       							boolean transpose, boolean assymetric) {
		return makeMatrix(network, attributes, selectedOnly, ignoreMissing, transpose, 
		                  assymetric, MatrixType.LARGE);
	}

	/**
	 * Create a small empty matrix
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @return the empty matrix
	 */
	public static CyMatrix makeSmallMatrix(CyNetwork network, int rows, int columns) {
		return makeTypedMatrix(network, rows, columns, false, MatrixType.SIMPLE);
	}

	/**
	 * Convenience method for testing routines
	 */
	public static CyMatrix makeSmallMatrix(int rows, int columns, Double[] data) {
		return makeSmallMatrix(null, rows, columns, data);
	}


	/**
	 * Create a small matrix with an initial data set.  Used primarily for test cases
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @param data initial data set
	 * @return the empty matrix
	 */
	public static CyMatrix makeSmallMatrix(CyNetwork network, int rows, int columns, Double[] data) {
		CyMatrix mat = makeTypedMatrix(network, rows, columns, false, MatrixType.SIMPLE);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				mat.setValue(row, col, data[row*columns+col]);
			}
		}
		return mat;
	}

	/**
	 * Create a small matrix populated with data from
	 * the indicated node attributes
	 *
	 * @param network the network that will be the source of the data
	 * @param attributes the array of attributes.  If only one attribute is provided,
	 *                   it begins with "edge.", then a symmetric network is created.
	 *!g @param selectedOnly only include selected edges
	 * @param ignoreMissing ignore nodes/edges with missing data
	 * @param transpose transpose the nodes and attribute in the network (used for
	 *                  clustering attributes rather than nodes).
	 * @param assymetric true if we're looking at edge attributes but the matrix is not square.
	 * @return the resulting matrix
	 */
	public static CyMatrix makeSmallMatrix(CyNetwork network, String[] attributes,
	                                       boolean selectedOnly, boolean ignoreMissing,
																	       boolean transpose, boolean assymetric) {
		return makeMatrix(network, attributes, selectedOnly, 
		                  ignoreMissing, transpose, assymetric, MatrixType.SIMPLE);
	}


	private static CyMatrix makeMatrix(CyNetwork network, String[] attributes,
	                                   boolean selectedOnly, boolean ignoreMissing,
															       boolean transpose, boolean assymetric, MatrixType type) {
		// Create our local copy of the weightAtributes array
		String[] attributeArray = new String[attributes.length];

		if (attributes.length >= 1 && attributes[0].startsWith("node.")) {
			// Get rid of the leading type information
			for (int i = 0; i < attributes.length; i++) {
				attributeArray[i] = attributes[i].substring(5);
			}
			List<CyNode> nodeList = ModelUtils.getSortedNodeList(network, selectedOnly);
			Map<CyNode,Map<String,Double>>nodeCondMap = getNodeCondMap(network, nodeList, attributeArray, ignoreMissing);
			CyMatrix matrix = makeTypedMatrix(network, nodeCondMap.size(), attributeArray.length, transpose, type);
			matrix.setAssymetricalEdge(false);
			return makeAttributeMatrix(network, matrix, nodeList, nodeCondMap, attributeArray, transpose);
		} else if (attributes.length == 1 && attributes[0].startsWith("edge.")) {
			String weight = attributes[0].substring(5);
			if (!assymetric) {
				// Get the list of nodes and edges of interest
				List<CyEdge> edgeList = getEdgeList(network, selectedOnly);
				List<CyNode> nodeList = getNodesFromEdges(network, edgeList, weight, ignoreMissing);
				Collections.sort(nodeList, new CyIdentifiableNameComparator(network));
				CyMatrix matrix = makeTypedMatrix(network, nodeList.size(), nodeList.size(), false, type);
				matrix.setAssymetricalEdge(false);
				return makeSymmetricalMatrix(network, matrix, nodeList, edgeList, weight);
			} else {
				List<CyEdge> edgeList = getEdgeList(network, selectedOnly);
				List<CyNode> targetNodeList = new ArrayList<CyNode>();
				List<CyNode> sourceNodeList = getNodesFromEdges(network, edgeList, targetNodeList, weight, ignoreMissing);
				Collections.sort(targetNodeList, new CyIdentifiableNameComparator(network));
				Collections.sort(sourceNodeList, new CyIdentifiableNameComparator(network));
				CyMatrix matrix = makeTypedMatrix(network, sourceNodeList.size(), targetNodeList.size(), false, type);
				matrix.setAssymetricalEdge(true);
				return makeAssymmetricalMatrix(network, matrix, sourceNodeList, targetNodeList, edgeList, weight);
			}
		}
		return null;
	}

	private static CyMatrix makeAttributeMatrix(CyNetwork network, CyMatrix matrix, List<CyNode> nodeList,
	                                            Map<CyNode,Map<String,Double>> nodeCondMap, 
																							String attributeArray[], boolean transpose) {
		if (transpose) {
			int column = 0;
			matrix.setRowLabels(Arrays.asList(attributeArray));

			for (CyNode node: nodeList) {
				if (!nodeCondMap.containsKey(node))
					continue;

				Map<String,Double>thisCondMap = nodeCondMap.get(node);
				matrix.setColumnLabel(column, ModelUtils.getNodeName(network, node));
				matrix.setColumnNode(column, node);
				for (int row=0; row < matrix.nRows(); row++) {
					String rowLabel = matrix.getRowLabel(row);
					if (thisCondMap.containsKey(rowLabel)) {
						matrix.setValue(row, column, thisCondMap.get(rowLabel));
					}
				}
				column++;
			}
		} else {
			matrix.setColumnLabels(Arrays.asList(attributeArray));

			int row = 0;
			for (CyNode node: nodeList) {
				if (!nodeCondMap.containsKey(node))
					continue;
				matrix.setRowLabel(row, ModelUtils.getNodeName(network, node));
				matrix.setRowNode(row, node);
				Map<String,Double>thisCondMap = nodeCondMap.get(node);
				for (int column=0; column < matrix.nColumns(); column++) {
					String columnLabel = matrix.getColumnLabel(column);
					if (thisCondMap.containsKey(columnLabel)) {
						matrix.setValue(row, column, thisCondMap.get(columnLabel));
					}
				}
				row++;
			}
		}
		return matrix;
	}
	
	private static CyMatrix makeSymmetricalMatrix(CyNetwork network, CyMatrix matrix, List<CyNode> nodeList, 
	                                              List<CyEdge> edgeList, String weight) {
		// Create a map we can use to get the row for our data
		Map<CyNode, Integer> indexMap = new HashMap<CyNode, Integer>(nodeList.size());
		for (int row = 0; row < nodeList.size(); row++) {
			indexMap.put(nodeList.get(row), row);
		}

		matrix.setRowNodes(nodeList);
		matrix.setColumnNodes(nodeList);
		for (int row = 0; row < nodeList.size(); row++) {
			matrix.setRowLabel(row, ModelUtils.getNodeName(network, nodeList.get(row)));
			matrix.setColumnLabel(row, ModelUtils.getNodeName(network, nodeList.get(row)));
		}
		for (CyEdge edge: edgeList) {
			Double val = ModelUtils.getNumericValue(network, edge, weight);
			int row = indexMap.get(edge.getSource());
			int col = indexMap.get(edge.getTarget());
			if (val != null) {
				matrix.setValue(row, col, val);
				if (row != col)
					matrix.setValue(col, row, val);
			}
		}
		matrix.setSymmetrical(true);
		return matrix;
	}

	private static CyMatrix makeAssymmetricalMatrix(CyNetwork network, CyMatrix matrix, 
	                                                List<CyNode> sourceNodeList, 
	                                                List<CyNode> targetNodeList,
	                                                List<CyEdge> edgeList, String weight) {
		// Create a map we can use to get the row for our data
		Map<CyNode, Integer> rowMap = new HashMap<CyNode, Integer>(sourceNodeList.size());
		for (int row = 0; row < sourceNodeList.size(); row++) {
			rowMap.put(sourceNodeList.get(row), row);
			matrix.setRowLabel(row, ModelUtils.getNodeName(network, sourceNodeList.get(row)));
		}
		matrix.setRowNodes(sourceNodeList);

		Map<CyNode, Integer> colMap = new HashMap<CyNode, Integer>(targetNodeList.size());
		for (int col = 0; col < targetNodeList.size(); col++) {
			colMap.put(targetNodeList.get(col), col);
			matrix.setColumnLabel(col, ModelUtils.getNodeName(network, targetNodeList.get(col)));
		}
		matrix.setColumnNodes(targetNodeList);
		for (CyEdge edge: edgeList) {
			Double val = ModelUtils.getNumericValue(network, edge, weight);
			int row = rowMap.get(edge.getSource());
			int col = colMap.get(edge.getTarget());
			if (val != null) {
				matrix.setValue(row, col, val);
			}
		}
		matrix.setSymmetrical(false);
		return matrix;
	}

	private static CyMatrix makeTypedMatrix(CyNetwork network, int rows, int columns, 
	                                        boolean transpose, MatrixType type) {
		int nrows = rows;
		int ncolumns = columns;
		if (transpose) {
			nrows = columns;
			ncolumns = rows;
		}

		CyMatrix matrix = null;

		switch (type) {
			case SIMPLE:
				matrix = new CySimpleMatrix(network, nrows, ncolumns);
				break;

			// For now, we use Colt for both large and sparse matrices
			case COLT:
			case SPARSE:
				matrix = new CyColtMatrix(network, nrows, ncolumns);
				break;

			case LARGE:
			case OJALGO:
				matrix = new CyOjAlgoMatrix(network, nrows, ncolumns);
				break;
		}

		matrix.setTransposed(transpose);
		return matrix;
	}

	private static Map<CyNode, Map<String,Double>> getNodeCondMap(CyNetwork network, List<CyNode> nodeList, 
	                                                              String nodeAttributes[], boolean ignoreMissing) {
		// Make a map of the conditions, indexed by CyNode
		Map<CyNode,Map<String,Double>>nodeCondMap = new HashMap<CyNode,Map<String,Double>>();

		// Make a map of the conditions, by name
		List<String>condList = Arrays.asList(nodeAttributes);

		// Get our node attribute list
		CyTable nodeTable = network.getDefaultNodeTable();

		// Iterate over all of our nodes, getting the conditions attributes
		for (CyNode node: nodeList) {
			// Create the map for this node
			Map<String,Double>thisCondMap = new HashMap<String,Double>();

			for (String attr: condList) {
				Double value = null;
				// Get the attribute type
				if (nodeTable.getColumn(attr).getType() == Integer.class) {
					Integer intVal = nodeTable.getRow(node.getSUID()).get(attr, Integer.class);
					if (intVal != null)
						value = Double.valueOf(intVal.doubleValue());
				} else if (nodeTable.getColumn(attr).getType() == Long.class) {
					Long longVal = nodeTable.getRow(node.getSUID()).get(attr, Long.class);
					if (longVal != null)
						value = Double.valueOf(longVal.doubleValue());
				} else if (nodeTable.getColumn(attr).getType() == Float.class) {
					Float floatVal = nodeTable.getRow(node.getSUID()).get(attr, Float.class);
					if (floatVal != null)
						value = Double.valueOf(floatVal.doubleValue());
				} else if (nodeTable.getColumn(attr).getType() == Double.class) {
					value = nodeTable.getRow(node.getSUID()).get(attr, Double.class);
				} else {
					continue; // At some point, handle lists?
				}
				if (!ignoreMissing || value != null) {
					// System.out.println("Node = "+node.getIdentifier()+", attribute = "+attr+", ignoreMissing = "+ignoreMissing+" value = "+value);
					// Set it
					thisCondMap.put(attr, value);
				}
			}
			if (!ignoreMissing || thisCondMap.size() > 0)
				nodeCondMap.put(node, thisCondMap);
		}
		return nodeCondMap;
	}

	private static List<CyEdge> getEdgeList(CyNetwork network, boolean selectedOnly) {
		List<CyEdge> edgeList;
		if (selectedOnly)
			edgeList = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		else
			edgeList = network.getEdgeList();
		return edgeList;
	}

	private static List<CyNode> getNodesFromEdges(CyNetwork network, List<CyEdge> edgeList, String attribute, 
	                                              boolean ignoreMissing) {
		Set<CyNode> nodeSet = new HashSet<CyNode>();
		for (CyEdge edge: edgeList) {
			Double val = ModelUtils.getNumericValue(network, edge, attribute);
			if (ignoreMissing && val == null)
				continue;
			nodeSet.add(edge.getTarget());
			nodeSet.add(edge.getSource());
		}
		return new ArrayList<CyNode>(nodeSet);
	}

	// This version is used for assymetrical matrices
	private static List<CyNode> getNodesFromEdges(CyNetwork network, List<CyEdge> edgeList, 
	                                              List<CyNode> targetNodeList,
	                                              String attribute, boolean ignoreMissing) {
		Set<CyNode> sourceNodeSet = new HashSet<CyNode>();
		Set<CyNode> targetNodeSet = new HashSet<CyNode>();
		for (CyEdge edge: edgeList) {
			Double val = ModelUtils.getNumericValue(network, edge, attribute);
			if (ignoreMissing && val == null)
				continue;
			targetNodeSet.add(edge.getTarget());
			sourceNodeSet.add(edge.getSource());
		}
		targetNodeList.addAll(targetNodeSet);
		return new ArrayList<CyNode>(sourceNodeSet);
	}
}

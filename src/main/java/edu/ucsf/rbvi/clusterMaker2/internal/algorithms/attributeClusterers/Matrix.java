package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

// TODO: convert to sparse matrices
// import cern.colt.matrix.tdouble.DoubleFactory2D;
// import cern.colt.matrix.tdouble.DoubleMatrix2D;

// clusterMaker imports

/**
 * Matrix extends BaseMatrix to provide mechanism of importing attributes from Cytoscape network
 * TODO  Rename Matrix to CyMatrix and BaseMatrix to Matrix?
 */

public class Matrix extends BaseMatrix {
	private CyNode rowNodes[];
	private CyNode columnNodes[];
	private CyNetwork network;
	
	protected boolean ignoreMissing;
	protected boolean selectedOnly;
	
	/**
	 * Create a data matrix from the current nodes in the network.  There are two ways
	 * we construct the matrix, depending on the type.  If we are looking at expression
	 * profiles, for example, each node will represent a gene, and the expression results
	 * for each condition will be encoded in the indicated node attributes.  For our purposes,
	 * we don't pay any attention to edges when creating the matrix (there are obviously
	 * reasons why we might want to derive edges from the resulting data, but this can be
	 * done after the clustering is complete.
	 *
	 * On the other hand, if we are looking at genetic interactions, the resulting matrix will
	 * be symmetrical around the diagonal and the weightAttribute will be an edge attribute
	 * on the edges between the nodes.
	 *
	 * @param weightAttribute the edge attribute we use to get the weight (size of effect)
	 * @param transpose true if we are transposing this matrix 
	 *                  (clustering columns instead of rows)
	 */
	
	public Matrix(CyNetwork network, String[] weightAttributes, boolean transpose, boolean ignoreMissing, boolean selectedOnly) {
		this.transpose = transpose;
		this.ignoreMissing = ignoreMissing;
		this.selectedOnly = selectedOnly;
		this.network = network;

		// Create our local copy of the weightAtributes array
		String[] attributeArray = new String[weightAttributes.length];

		// If our weightAttribute is on edges, we're looking at a symmetrical matrix
		if (weightAttributes.length >= 1 && weightAttributes[0].startsWith("node.")) {
			// Get rid of the leading type information
			for (int i = 0; i < weightAttributes.length; i++) {
				attributeArray[i] = weightAttributes[i].substring(5);
			}
			buildGeneArrayMatrix(network, attributeArray, transpose, ignoreMissing, selectedOnly);
			symmetrical = false;
		} else if (weightAttributes.length == 1 && weightAttributes[0].startsWith("edge.")) {
			buildSymmetricalMatrix(network, weightAttributes[0].substring(5), ignoreMissing, selectedOnly);
			symmetrical = true;
		} else {
			// Throw an exception?
			return;
		}
	}
	public Matrix(Matrix duplicate) {
		this.nRows = duplicate.nRows();
		this.nColumns = duplicate.nColumns();
		this.matrix = new Double[nRows][nColumns];
		this.colWeights = new double[nColumns];
		this.rowWeights = new double[nRows];
		this.columnLabels = new String[nColumns];
		this.rowLabels = new String[nRows];
		this.ignoreMissing = duplicate.ignoreMissing;
		this.selectedOnly = duplicate.selectedOnly;
		this.network = duplicate.network;

		// Only one of these will actually be used, depending on whether
		// we're transposed or not
		this.rowNodes = null;
		this.columnNodes = null;

		if (duplicate.getRowNode(0) != null)
			this.rowNodes = new CyNode[nRows];
		else
			this.columnNodes = new CyNode[nColumns];

		this.transpose = duplicate.transpose;

		for (int row = 0; row < nRows; row++) {
			rowWeights[row] = duplicate.getRowWeight(row);
			rowLabels[row] = duplicate.getRowLabel(row);
			if (rowNodes != null)
				rowNodes[row] = duplicate.getRowNode(row);
			for (int col = 0; col < nColumns; col++) {
				if (row == 0) {
					colWeights[col] = duplicate.getColWeight(col);
					columnLabels[col] = duplicate.getColLabel(col);
					if (columnNodes != null)
						columnNodes[col] = duplicate.getColNode(col);
				}
				if (duplicate.getValue(row, col) != null)
					this.matrix[row][col] = new Double(duplicate.getValue(row, col));
			}
		}
	}
	public Matrix(CyNetwork network, int rows, int cols) {
		this.nRows = rows;
		this.nColumns = cols;
		this.matrix = new Double[rows][cols];
		this.colWeights = new double[cols];
		this.rowWeights = new double[rows];
		this.columnLabels = new String[cols];
		this.rowLabels = new String[rows];
		// Only one of these will actually be used
		this.rowNodes = null;
		this.columnNodes = null;
		this.transpose = false;
		this.ignoreMissing = false;
		this.selectedOnly = false;
		this.network = network;
	}
	
	public CyNode getRowNode(int row) {
		if (this.rowNodes != null)
			return rowNodes[row];
		return null;
	}

	public CyNode getColNode(int col) {
		if (this.columnNodes != null)
			return columnNodes[col];
		return null;
	}
	
	// XXX Isn't this the same as clusterMaker.algorithms.DistanceMatrix?
		@SuppressWarnings({"unchecked","deprecation"})
		private void buildSymmetricalMatrix(CyNetwork network, String weight, 
		                                    boolean ignoreMissing, boolean selectedOnly) {

			CyTable edgeAttributes = network.getDefaultEdgeTable();
			// Get the list of edges
			List<CyNode>nodeList = network.getNodeList();

			// For debugging purposes, sort the node list by identifier
			nodeList = sortNodeList(nodeList);

			this.nRows = nodeList.size();
			this.nColumns = this.nRows;
			this.matrix = new Double[nRows][nColumns];
			// this.matrix = DoubleFactory2D.sparse.make(nRows,nColumns);
			this.rowLabels = new String[nRows];
			this.columnLabels = new String[nColumns];
			this.rowNodes = new CyNode[nRows];
			this.columnNodes = null;
			this.maxAttribute = Double.MIN_VALUE;

			// For each edge, get the attribute and update the matrix and mask values
			int index = 0;
			int column;
			Class attributeType = edgeAttributes.getColumn(weight).getType(); //edgeAttributes.getType(weight);
					

			for (CyNode node: nodeList) {
				boolean found = false;
				boolean hasSelectedEdge = false;
				this.rowLabels[index] = getNodeName(node, network);
				this.rowNodes[index] = node;
				this.columnLabels[index] = getNodeName(node, network);

				// Get the list of adjacent edges
				List<CyEdge> edgeList = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
				for (CyEdge edge: edgeList) {
					if (selectedOnly && !edgeAttributes.getRow(edge).get("SELECTED", Boolean.class) /*!network.Selected(edge)*/)
					 	continue;
					hasSelectedEdge = true;
					
					Double val = null;
					if (attributeType == Float.class) {
						val = edgeAttributes.getRow(edge).get(weight, Double.class).doubleValue();
					} else {
						Integer v = edgeAttributes.getRow(edge).get(weight, Integer.class);
						if (v != null)
							val = Double.valueOf(v.toString());
					}

					if (val != null) {
						found = true;
						maxAttribute = Math.max(maxAttribute, val);
						if (edge.getSource() == node) {
							column = nodeList.indexOf(edge.getTarget());
							matrix[index][column] = val;
							//matrix.set(index,column,val);
						} else {
							column = nodeList.indexOf(edge.getSource());
							matrix[index][column] = val;
							// matrix.set(index,column,val);
						}
					}
				}
				if ((!ignoreMissing || found) && (!selectedOnly || hasSelectedEdge))
					index++;
			}

			// At this point, if we're ignoring missing values, we only have part of the matrix
			// in use.  Update nRows and nColumns to reflect the new size.
			if (ignoreMissing) {
				this.nRows = index;
				this.nColumns = index;
			}
		}

		// XXX Do we need a new constructor to clusterMaker.algorithms.DistanceMatrix?
		@SuppressWarnings("unchecked")
		private void buildGeneArrayMatrix(CyNetwork network, String[] weightAttributes, 
		                                  boolean transpose, boolean ignoreMissing,
		                                  boolean selectedOnly) {
			// Get the list of nodes
			List<CyNode>nodeList = network.getNodeList();

			if (selectedOnly) nodeList = new ArrayList<CyNode>(CyTableUtil.getNodesInState(network,"selected",true));

			// For debugging purposes, sort the node list by identifier
			nodeList = sortNodeList(nodeList);

			// Make a map of the conditions, indexed by CyNode
			Map<CyNode,Map<String,Double>>nodeCondMap = new HashMap<CyNode,Map<String,Double>>();

			// Make a map of the conditions, by name
			List<String>condList = Arrays.asList(weightAttributes);

			// Get our node attribute list
			CyTable nodeAttributes = network.getDefaultNodeTable();

			// Iterate over all of our nodes, getting the conditions attributes
			for (CyNode node: nodeList) {
				// Create the map for this node
				Map<String,Double>thisCondMap = new HashMap<String,Double>();

				for (int attrIndex = 0; attrIndex < weightAttributes.length; attrIndex++) {
					String attr = weightAttributes[attrIndex];
					Double value = null;
					// Get the attribute type
					if (nodeAttributes.getColumn(attr).getType() == Integer.class) {
						Integer intVal = nodeAttributes.getRow(node).get(attr, Integer.class);
						if (intVal != null)
							value = Double.valueOf(intVal.doubleValue());
					} else if (nodeAttributes.getColumn(attr).getType() == Long.class) {
						Long longVal = nodeAttributes.getRow(node).get(attr, Long.class);
						if (longVal != null)
							value = Double.valueOf(longVal.doubleValue());
					} else if (nodeAttributes.getColumn(attr).getType() == Float.class) {
						Float floatVal = nodeAttributes.getRow(node).get(attr, Float.class);
						if (floatVal != null)
							value = Double.valueOf(floatVal.doubleValue());
					} else if (nodeAttributes.getColumn(attr).getType() == Double.class) {
						value = nodeAttributes.getRow(node).get(attr, Double.class);
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

			// We've got all of the information, get our counts and create the
			// matrix
			if (transpose) {
				this.nRows = condList.size();
				this.nColumns = nodeCondMap.size();
				this.matrix = new Double[nRows][nColumns];
				// this.matrix = DoubleFactory2D.sparse.make(nRows,nColumns);
				this.rowLabels = new String[nRows];
				this.columnLabels = new String[nColumns];
				this.columnNodes = new CyNode[nColumns];
				setRowLabels(condList);

				int column = 0;
				for (CyNode node: nodeList) {
					if (!nodeCondMap.containsKey(node))
						continue;

					Map<String,Double>thisCondMap = nodeCondMap.get(node);
					this.columnLabels[column] = getNodeName(node, network);
					this.columnNodes[column] = node;
					for (int row=0; row < this.nRows; row++) {
						String rowLabel = this.rowLabels[row];
						if (thisCondMap.containsKey(rowLabel)) {
							matrix[row][column] = thisCondMap.get(rowLabel);
							// matrix.set(row,column,thisCondMap.get(rowLabel));
						}
					}
					column++;
				}
			} else {
				this.nRows = nodeCondMap.size();
				this.nColumns = condList.size();
				this.rowLabels = new String[nRows];
				this.rowNodes = new CyNode[nRows];
				this.columnLabels = new String[nColumns];
				this.matrix = new Double[nRows][nColumns];
				// this.matrix = DoubleFactory2D.sparse.make(nRows,nColumns);
				setColumnLabels(condList);

				int row = 0;
				for (CyNode node: nodeList) {
					if (!nodeCondMap.containsKey(node))
						continue;
					this.rowLabels[row] = getNodeName(node, network);
					this.rowNodes[row] = node;
					Map<String,Double>thisCondMap = nodeCondMap.get(node);
					for (int column=0; column < this.nColumns; column++) {
						String columnLabel = this.columnLabels[column];
						if (thisCondMap.containsKey(columnLabel)) {
							// System.out.println("Setting matrix["+rowLabels[row]+"]["+columnLabel+"] to "+thisCondMap.get(columnLabel));
							matrix[row][column] = thisCondMap.get(columnLabel);
							// matrix.set(row,column,thisCondMap.get(columnLabel));
						}
					}
					row++;
				}
			}
		}

		// sortNodeList does an alphabetical sort on the names of the nodes.
		private List<CyNode>sortNodeList(List<CyNode>nodeList) {
			Map<String,CyNode>nodeMap = new HashMap<String, CyNode>();
			// First build a string array
			String nodeNames[] = new String[nodeList.size()];
			int index = 0;
			for (CyNode node: nodeList) {
				nodeNames[index++] = getNodeName(node, network);
				nodeMap.put(getNodeName(node, network), node);
			}
			// Sort it
			Arrays.sort(nodeNames);
			// Build the node list again
			ArrayList<CyNode>newList = new ArrayList<CyNode>(nodeList.size());
			for (index = 0; index < nodeNames.length; index++) {
				newList.add(nodeMap.get(nodeNames[index]));
			}
			return newList;
		}

		private String getNodeName(CyNode node, CyNetwork network) {
			if (network.containsNode(node)) {
				return network.getRow(node).get(CyNetwork.NAME, String.class);
			}
			return null;
		}
	
	
}

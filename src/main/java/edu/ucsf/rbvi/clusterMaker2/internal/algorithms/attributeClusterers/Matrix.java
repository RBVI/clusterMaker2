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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;

// TODO: convert to sparse matrices
// import cern.colt.matrix.tdouble.DoubleFactory2D;
// import cern.colt.matrix.tdouble.DoubleMatrix2D;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

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
	 * @param ignoreMissing ignore any missing data when you cluster
	 * @param selectedOnly only include selected nodes or edges
	 */
	public Matrix(CyNetwork network, String[] weightAttributes, boolean transpose, 
	              boolean ignoreMissing, boolean selectedOnly) {
		this(network, weightAttributes, transpose, ignoreMissing, selectedOnly, false);
	}

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
	 * @param ignoreMissing ignore any missing data when you cluster
	 * @param selectedOnly only include selected nodes or edges
	 * @param assymetric for edge clusters, if they are assymetric (e.g. bipartide networks)
	 */

	public Matrix(CyNetwork network, String[] weightAttributes, boolean transpose, 
	              boolean ignoreMissing, boolean selectedOnly, boolean assymetric) {
		this.transpose = transpose;
		this.ignoreMissing = ignoreMissing;
		this.selectedOnly = selectedOnly;
		this.network = network;
		assymetricEdge = assymetric;

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
			if (!assymetricEdge) {
				buildSymmetricalMatrix(network, weightAttributes[0].substring(5), 
				                       ignoreMissing, selectedOnly);
				symmetrical = true;
			} else {
				buildAssymmetricalMatrix(network, weightAttributes[0].substring(5), 
				                         transpose, ignoreMissing, selectedOnly);
				symmetrical = false;
			}
		} else {
			// Throw an exception?
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
					this.matrix[row][col] = duplicate.getValue(row, col);
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

	public void setRowNodes(CyNode newRowNodes[]){
		this.rowNodes = newRowNodes;
	}

	public void setColumnNodes(CyNode newColumnNodes[]){
		this.columnNodes = newColumnNodes;
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

	@SuppressWarnings({"unchecked","deprecation"})
	private void buildAssymmetricalMatrix(CyNetwork network, String weight, boolean transpose,
	                                      boolean ignoreMissing, boolean selectedOnly) {
		CyTable edgeAttributes = network.getDefaultEdgeTable();
		List<CyEdge>edgeList = network.getEdgeList();
		Map<CyNode, Integer> sourceNodes = new HashMap<CyNode, Integer>();
		Map<CyNode, Integer> targetNodes = new HashMap<CyNode, Integer>();
		int sourceIndex = 0;
		int targetIndex = 0;
		for (CyEdge edge: edgeList) {
			if (selectedOnly && !network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class))
				continue;

			Double val = ModelUtils.getNumericValue(network, edge, weight);
			if (ignoreMissing && val == null)
				continue;

			if (!sourceNodes.containsKey(edge.getSource()))
				sourceNodes.put(edge.getSource(), sourceIndex++);
			if (!targetNodes.containsKey(edge.getTarget()))
				targetNodes.put(edge.getTarget(), targetIndex++);
		}

		Map<CyNode, Integer> rowNodes = sourceNodes;
		Map<CyNode, Integer> columnNodes = targetNodes;
		if (transpose) {
			rowNodes = targetNodes;
			columnNodes = sourceNodes;
		}
		this.nRows = rowNodes.size();
		this.nColumns = columnNodes.size();
		this.matrix = new Double[nRows][nColumns];
		this.rowLabels = new String[nRows];
		this.columnLabels = new String[nColumns];
		this.rowNodes = new CyNode[nRows];
		this.columnNodes = new CyNode[nColumns];

		// For each edge, get the attribute and update the matrix and mask values
		for (CyEdge edge: edgeList) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if (transpose) {
				target = edge.getSource();
				source = edge.getTarget();
			}

			if (!rowNodes.containsKey(source) ||
					!columnNodes.containsKey(target))
				continue;

			Double val = ModelUtils.getNumericValue(network, edge, weight);
			this.rowLabels[rowNodes.get(source)] = getNodeName(source, network);
			this.columnLabels[columnNodes.get(target)] = getNodeName(target, network);

			maxAttribute = Math.max(maxAttribute, val);
			matrix[rowNodes.get(source)][columnNodes.get(target)] = val;
		}

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
				if (selectedOnly && !network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class) /*!network.Selected(edge)*/)
				 	continue;
				hasSelectedEdge = true;

				Double val = ModelUtils.getNumericValue(network, edge, weight);

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

	/**
	 * The method makes a distance matrix from and instance ofDistanceMatrix
	 */
	public void buildDistanceMatrix(DistanceMatrix distanceMatrix){

		this.nRows = distanceMatrix.getNodes().size();
		this.nColumns = this.nRows;

		this.matrix = new Double[nRows][nColumns];
		this.maxAttribute = Double.MIN_VALUE;

		for(int i = 0; i < nRows; i++){
			for(int j = i; j < nRows; j++){
				matrix[i][j] = distanceMatrix.getEdgeValueFromMatrix(i, j);
			}
		}

	}

	/**
	 * Create a symmetric Matrix of distances from the current Matrix
	 */
	public Matrix makeDistanceMatrix(DistanceMetric metric) {
		int rows;
		CyNode[] nodes;
		List<String> labels;
		double symMax = Double.MIN_VALUE;


		if (transpose) {
			rows = nColumns;
			nodes = Arrays.copyOf(columnNodes, columnNodes.length);
			labels = Arrays.asList(columnLabels);
		} else {
			rows = nRows;
			nodes = Arrays.copyOf(rowNodes, rowNodes.length);
			labels = Arrays.asList(rowLabels);
		}
		System.out.println("Creating symmetrical "+rows+"x"+rows+" distance matrix");
		Matrix symMat = new Matrix(network, rows, rows);
		for (int row = 0; row < rows; row++) {
			for (int column = row; column < rows; column++) {
				double value = metric.getMetric(this, this, this.getWeights(), row, column);
				if (value > symMax) symMax = value;
				symMat.setValue(row, column, value);
				if (row != column)
					symMat.setValue(column, row, value);
			}
		}
		symMat.setRowNodes(nodes);
		symMat.setRowLabels(labels);
		symMat.setColumnNodes(null);
		symMat.setColumnLabels(labels);
		symMat.maxAttribute = symMax;
		symMat.symmetrical = true;
		return symMat;
	}

	/**
	 * NOTE: this really assumes that the matrix is symmetric!!!!!
	 */
	public double[][] getMatrix2DArray(){
		double[][] matrixArray = new double[nRows][nColumns];

		for(int i = 0; i < nRows; i++){

			for (int j = i; j < nColumns ; j++){
				if(matrix[i][j] != null){
					matrixArray[i][j] = matrix[i][j];
					matrixArray[j][i] = matrix[i][j];
				}
				else{
					matrixArray[i][j] = 0.0;
					matrixArray[j][i] = 0.0;
				}
			}
		}

		return matrixArray;

	}

	// XXX Do we need a new constructor to clusterMaker.algorithms.DistanceMatrix?
	@SuppressWarnings("unchecked")
	private void buildGeneArrayMatrix(CyNetwork network, String[] weightAttributes, 
	                                  boolean transpose, boolean ignoreMissing,
	                                  boolean selectedOnly) {
		System.out.println("Building gene array matrix");
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
					Integer intVal = nodeAttributes.getRow(node.getSUID()).get(attr, Integer.class);
					if (intVal != null)
						value = Double.valueOf(intVal.doubleValue());
				} else if (nodeAttributes.getColumn(attr).getType() == Long.class) {
					Long longVal = nodeAttributes.getRow(node.getSUID()).get(attr, Long.class);
					if (longVal != null)
						value = Double.valueOf(longVal.doubleValue());
				} else if (nodeAttributes.getColumn(attr).getType() == Float.class) {
					Float floatVal = nodeAttributes.getRow(node.getSUID()).get(attr, Float.class);
					if (floatVal != null)
						value = Double.valueOf(floatVal.doubleValue());
				} else if (nodeAttributes.getColumn(attr).getType() == Double.class) {
					value = nodeAttributes.getRow(node.getSUID()).get(attr, Double.class);
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
			System.out.println("Creating "+nRows+"x"+nColumns+" matrix");
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

	public List<CyNode> getNodes() {
		List<CyNode>nodeList = network.getNodeList();

		if (selectedOnly) 
			nodeList = new ArrayList<CyNode>(CyTableUtil.getNodesInState(network,"selected",true));

		return nodeList;
	}
}

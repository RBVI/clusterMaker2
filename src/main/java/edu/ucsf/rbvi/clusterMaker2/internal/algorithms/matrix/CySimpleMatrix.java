package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

/**
 * A wrapper around the Matrix implementations, that adds 
 * information necessary to support Cytoscape objects
 */
public class CySimpleMatrix extends SimpleMatrix implements CyMatrix {
	protected CyNetwork network;
	protected CyNode[] rowNodes;
	protected CyNode[] columnNodes;
	protected boolean assymetricalEdge = false;

	public CySimpleMatrix(CyNetwork network) {
		super();
		this.network = network;
	}

	public CySimpleMatrix(CyNetwork network, int rows, int columns) {
		super(rows, columns);
		this.network = network;
	}

	public CySimpleMatrix(CySimpleMatrix matrix) {
		super((SimpleMatrix)matrix);
		network = matrix.network;
		if (matrix.rowNodes != null)
			rowNodes = Arrays.copyOf(matrix.rowNodes, matrix.rowNodes.length);
		if (matrix.columnNodes != null)
			columnNodes = Arrays.copyOf(matrix.columnNodes, matrix.columnNodes.length);
	}

	/**
	 * Return the network associated with this matrix
	 *
	 * @return the network
	 */
	public CyNetwork getNetwork() {
		return network;
	}

	/**
	 * Set the nodes for all rows
	 *
	 * @param rowNodes array of {@link CyNode}s for the rows
	 */
	public void setRowNodes(CyNode rowNodes[]) {
		this.rowNodes = rowNodes;
	}

	/**
	 * Set the nodes for all rows
	 *
	 * @param rowNodes list of {@link CyNode}s for the rows
	 */
	public void setRowNodes(List<CyNode> rowNodes) {
		this.rowNodes = rowNodes.toArray(new CyNode[0]);
	}

	/**
	 * Set the node for a particular row
	 *
	 * @param row the row to get the node for
	 * @param node the node for that row
	 */
	public void setRowNode(int row, CyNode node) {
		if (rowNodes == null)
			rowNodes = new CyNode[nRows];
		rowNodes[row] = node;
	}

	/**
	 * Get the node for a particular row
	 *
	 * @param row the row to get the node for
	 * @return the node for that row
	 */
	public CyNode getRowNode(int row) {
		if (rowNodes == null)
			return null;
		return rowNodes[row];
	}

	/**
	 * Set the nodes for all columns
	 *
	 * @param columnNodes array of {@link CyNode}s for the columns
	 */
	public void setColumnNodes(CyNode columnNodes[]) {
		this.columnNodes = columnNodes;
	}

	/**
	 * Set the nodes for all columns
	 *
	 * @param columnNodes list of {@link CyNode}s for the columns
	 */
	public void setColumnNodes(List<CyNode> columnNodes) {
		this.columnNodes = columnNodes.toArray(new CyNode[0]);
	}

	/**
	 * Set the node for a particular column
	 *
	 * @param column the column to set the node for
	 * @param node the node for that column
	 */
	public void setColumnNode(int column, CyNode node) {
		if (columnNodes == null)
			columnNodes = new CyNode[nColumns];
		columnNodes[column] = node;
	}

	/**
	 * Get the node for a particular column
	 *
	 * @param column the column to get the node for
	 * @return the node for that column
	 */
	public CyNode getColumnNode(int column) {
		if (columnNodes == null)
			return null;
		return columnNodes[column];
	}

	/**
	 * Return true if the matrix is based on edges, but isn't
	 * symmetrical.  This will probably be very rara -- currently
	 * only Hierarchical clusters support it.
	 *
	 * @return true if the matrix is edge-based but assymetrical
	 */
	public boolean isAssymetricalEdge() { return assymetricalEdge; }

	/**
	 * Set the value of assymetrical edge.
	 *
	 * @param true if the matrix is edge-based but assymetrical
	 */
	public void setAssymetricalEdge(boolean assymetricalEdge) {
		this.assymetricalEdge = assymetricalEdge;
	}

	/**
	 * Return a copy of this matrix
	 *
	 * @return deep copy of the matrix
	 */
	public CyMatrix copy() {
		return new CySimpleMatrix(this);
	}

	/*
	public CyMatrix convertToLargeMatrix() {
		return new CyLargeMatrix(this);
	}
	*/
}

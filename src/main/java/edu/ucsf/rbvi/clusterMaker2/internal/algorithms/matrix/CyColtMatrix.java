package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

/**
 * A wrapper around the Parallel Colt matrix, with
 * information necessary to support Cytoscape objects
 */
public class CyColtMatrix extends ColtMatrix implements CyMatrix {
	protected CyNetwork network;
	protected CyNode[] rowNodes;
	protected CyNode[] columnNodes;

	public CyColtMatrix(CyNetwork network) {
		super();
		this.network = network;
	}

	public CyColtMatrix(CyNetwork network, int rows, int columns) {
		super(rows, columns);
		this.network = network;
	}

	public CyColtMatrix(CyColtMatrix matrix) {
		super((ColtMatrix)matrix);
		network = matrix.network;
		rowNodes = Arrays.copyOf(rowNodes, rowNodes.length);
		columnNodes = Arrays.copyOf(columnNodes, columnNodes.length);
	}

	public CyColtMatrix(CySimpleMatrix matrix) {
		super((SimpleMatrix)matrix);
		network = matrix.network;
		rowNodes = Arrays.copyOf(rowNodes, rowNodes.length);
		columnNodes = Arrays.copyOf(columnNodes, columnNodes.length);
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
		rowNodes[row] = node;
	}

	/**
	 * Get the node for a particular row
	 *
	 * @param row the row to get the node for
	 * @return the node for that row
	 */
	public CyNode getRowNode(int row) {
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
		columnNodes[column] = node;
	}

	/**
	 * Get the node for a particular column
	 *
	 * @param column the column to get the node for
	 * @return the node for that column
	 */
	public CyNode getColumnNode(int column) {
		return columnNodes[column];
	}

	/**
	 * Return a copy of this matrix
	 *
	 * @return deep copy of the matrix
	 */
	public CyMatrix copy() {
		return new CyColtMatrix(this);
	}

	/*
	public CyMatrix convertToLargeMatrix() {
		return new CyLargeMatrix(this);
	}
	*/
}

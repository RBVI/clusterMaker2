package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
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
	 * Get the nodes for all rows
	 *
	 * @return the nodes for all rows
	 */
	public List<CyNode> getRowNodes() {
		if (rowNodes == null)
			return null;
		return Arrays.asList(rowNodes);
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
	 * Get the nodes for all columns
	 *
	 * @return the nodes for all columns
	 */
	public List<CyNode> getColumnNodes() {
		if (columnNodes == null)
			return null;
		return Arrays.asList(columnNodes);
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

	public CyMatrix getDistanceMatrix(DistanceMetric metric) {
		CySimpleMatrix dist = new CySimpleMatrix(network, nRows, nRows);
		if (rowNodes != null) {
			dist.rowNodes = Arrays.copyOf(rowNodes, nRows);
			dist.columnNodes = Arrays.copyOf(rowNodes, nRows);
		}
		Matrix cMatrix = super.getDistanceMatrix(metric);
		return dist.copy(cMatrix);
	}

	/**
	 * Return a copy of this matrix
	 *
	 * @return deep copy of the matrix
	 */
	public CyMatrix copy() {
		return new CySimpleMatrix(this);
	}

	/**
	 * Return a copy of this matrix with the data replaced by the
	 * argument
	 *
	 * @param matrix the data matrix to insert
	 * @return new CyMatrix with new underlying data
	 */
	public CyMatrix copy(Matrix matrix) {
		SimpleMatrix sMatrix;
		if (matrix instanceof ColtMatrix) {
			sMatrix = ((ColtMatrix)matrix).getSimpleMatrix();
		} else {
			sMatrix = (SimpleMatrix)matrix;
		}
		CySimpleMatrix newMatrix = new CySimpleMatrix(this.network, nRows, nColumns);
		newMatrix.data = sMatrix.data;
		newMatrix.transposed = sMatrix.transposed;
		newMatrix.symmetric = sMatrix.symmetric;
		newMatrix.minValue = sMatrix.minValue;
		newMatrix.maxValue = sMatrix.maxValue;
		newMatrix.rowLabels = Arrays.copyOf(sMatrix.rowLabels, sMatrix.rowLabels.length);
		newMatrix.columnLabels = Arrays.copyOf(sMatrix.columnLabels, sMatrix.columnLabels.length);
		if (sMatrix.index != null)
			newMatrix.index = Arrays.copyOf(sMatrix.index, sMatrix.index.length);
		if (rowNodes != null)
			newMatrix.rowNodes = Arrays.copyOf(rowNodes, rowNodes.length);
		if (columnNodes != null)
			newMatrix.columnNodes = Arrays.copyOf(columnNodes, columnNodes.length);
		return newMatrix;
	}

	/*
	public CyMatrix convertToLargeMatrix() {
		return new CyLargeMatrix(this);
	}
	*/
}

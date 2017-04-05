package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

/**
 * A wrapper around the ojAlgo matrix, with
 * information necessary to support Cytoscape objects
 */
public class CyOjAlgoMatrix extends OjAlgoMatrix implements CyMatrix {
	protected CyNetwork network;
	protected CyNode[] rowNodes = null;
	protected CyNode[] columnNodes = null;
	protected boolean assymetricalEdge = false;

	public CyOjAlgoMatrix(CyNetwork network) {
		super();
		this.network = network;
	}

	public CyOjAlgoMatrix(CyNetwork network, int rows, int columns) {
		super(rows, columns);
		this.network = network;
	}

	public CyOjAlgoMatrix(CyOjAlgoMatrix matrix) {
		super((OjAlgoMatrix)matrix);
		network = matrix.network;
		if (matrix.rowNodes != null)
			rowNodes = Arrays.copyOf(matrix.rowNodes, matrix.rowNodes.length);
		if (matrix.columnNodes != null)
			columnNodes = Arrays.copyOf(matrix.columnNodes, matrix.columnNodes.length);
	}

	public CyOjAlgoMatrix(CySimpleMatrix matrix) {
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
		if (rowNodes == null) {
			rowNodes = new CyNode[nRows()];
		}
		rowNodes[row] = node;
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
		if (columnNodes == null) {
			columnNodes = new CyNode[nColumns()];
		}
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
		CyOjAlgoMatrix dist = new CyOjAlgoMatrix(network, nRows, nRows);
		if (rowNodes != null) {
			dist.rowNodes = Arrays.copyOf(rowNodes, nRows);
			dist.columnNodes = Arrays.copyOf(rowNodes, nRows);
		}
		Matrix cMatrix = super.getDistanceMatrix(metric);
		return dist.copy(cMatrix);
	}

	/**
	 * Return a copy of this matrix with the data replaced by the
	 * argument
	 *
	 * @param matrix the data matrix to insert
	 * @return new CyMatrix with new underlying data
	 */
	public CyMatrix copy(Matrix matrix) {
		OjAlgoMatrix cMatrix;
		if (matrix instanceof SimpleMatrix) {
			cMatrix = new OjAlgoMatrix((SimpleMatrix)matrix);
		} else {
			cMatrix = (OjAlgoMatrix)matrix;
		}
		CyOjAlgoMatrix newMatrix = new CyOjAlgoMatrix(this.network, cMatrix.nRows, cMatrix.nColumns);
		newMatrix.data = cMatrix.data;
		newMatrix.transposed = cMatrix.transposed;
		newMatrix.symmetric = cMatrix.symmetric;
		newMatrix.minValue = cMatrix.minValue;
		newMatrix.maxValue = cMatrix.maxValue;
		if (cMatrix.rowLabels != null)
			newMatrix.rowLabels = Arrays.copyOf(cMatrix.rowLabels, (int)cMatrix.rowLabels.length);
		if (cMatrix.columnLabels != null)
			newMatrix.columnLabels = Arrays.copyOf(cMatrix.columnLabels, (int)cMatrix.columnLabels.length);
		if (cMatrix.index != null)
			newMatrix.index = Arrays.copyOf(cMatrix.index, (int)cMatrix.index.length);
		if (rowNodes != null)
			newMatrix.rowNodes = Arrays.copyOf(rowNodes, (int)cMatrix.nRows);

		// Careful!  Make sure to properly account for the transition from a symmetrix matrix to
		// a vector
		if (columnNodes != null && cMatrix.nColumns > 1)
			newMatrix.columnNodes = Arrays.copyOf(columnNodes, (int)cMatrix.nColumns);
		return newMatrix;
	}

	/**
	 * Return a copy of this matrix
	 *
	 * @return deep copy of the matrix
	 */
	public CyMatrix copy() {
		return new CyOjAlgoMatrix(this);
	}

	/*
	public CyMatrix convertToLargeMatrix() {
		return new CyLargeMatrix(this);
	}
	*/
}

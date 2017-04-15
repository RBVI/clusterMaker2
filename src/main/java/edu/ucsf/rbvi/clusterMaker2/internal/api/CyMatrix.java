package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * A wrapper around the Matrix implementations, that adds 
 * information necessary to support Cytoscape objects
 */
public interface CyMatrix extends Matrix {
	/**
	 * Return the network associated with this matrix
	 *
	 * @return the network
	 */
	public CyNetwork getNetwork();

	/**
	 * Set the nodes for all rows
	 *
	 * @param rowNodes array of {@link CyNode}s for the rows
	 */
	public void setRowNodes(CyNode rowNodes[]);

	/**
	 * Set the nodes for all rows
	 *
	 * @param rowNodes list of {@link CyNode}s for the rows
	 */
	public void setRowNodes(List<CyNode> rowNodes);

	/**
	 * Set the node for a particular row
	 *
	 * @param row the row to get the node for
	 * @param node the node for that row
	 */
	public void setRowNode(int row, CyNode node);

	/**
	 * Get the node for a particular row
	 *
	 * @param row the row to set the node for
	 * @return the node for that row
	 */
	public CyNode getRowNode(int row);

	/**
	 * Get the nodes for all rows
	 *
	 * @return the nodes for all rows
	 */
	public List<CyNode> getRowNodes();

	/**
	 * Set the nodes for all columns
	 *
	 * @param columnNodes array of {@link CyNode}s for the columns
	 */
	public void setColumnNodes(CyNode columnNodes[]);

	/**
	 * Set the nodes for all columns
	 *
	 * @param columnNodes list of {@link CyNode}s for the columns
	 */
	public void setColumnNodes(List<CyNode> columnNodes);

	/**
	 * Set the node for a particular column
	 *
	 * @param column the column to set the node for
	 * @param node the node for that column
	 */
	public void setColumnNode(int column, CyNode node);

	/**
	 * Get the node for a particular column
	 *
	 * @param column the column to get the node for
	 * @return the node for that column
	 */
	public CyNode getColumnNode(int column);

	/**
	 * Get the nodes for all columns
	 *
	 * @return the nodes for all columns
	 */
	public List<CyNode> getColumnNodes();

	/**
	 * Return true if the matrix is based on edges, but isn't
	 * symmetrical.  This will probably be very rara -- currently
	 * only Hierarchical clusters support it.
	 *
	 * @return true if the matrix is edge-based but assymetrical
	 */
	public boolean isAssymetricalEdge();

	/**
	 * Set the value of assymetrical edge.
	 *
	 * @param true if the matrix is edge-based but assymetrical
	 */
	public void setAssymetricalEdge(boolean assymetricalEdge);

	/**
	 * Return the distance between rows based on the metric. Note
	 * that this overrides the getDistanceMatrix method in Matrix,
	 * which is intentional so we can get the full CyMatrix (including
	 * nodes) and not just the data matrix
	 *
	 * @param metric the metric to use to calculate the distances
	 * @return a new CyMatrix of the distances between the rows
	 */
	public CyMatrix getDistanceMatrix(DistanceMetric metric);

	/**
	 * Return a copy of this matrix
	 *
	 * @return deep copy of the matrix
	 */
	public CyMatrix copy();

	/**
	 * Sort this matrix by the row labels.  If isNumeric is "true",
	 * assume the row labels are numeric
	 *
	 * @param isNumeric true if the row labels should be numeric
	 */
	public void sortByRowLabels(boolean isNumeric);

	/**
	 * Return a copy of this matrix, but replace the data with a different
	 * Matrix
	 *
	 * @param matrix the data matrix to insert
	 * @return new CyMatrix with new underlying data
	 */
	public CyMatrix copy(Matrix matrix);
}

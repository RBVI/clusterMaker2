package edu.ucsf.rbvi.clusterMaker2.internal.api;

import org.cytoscape.model.CyNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;

public interface CyMatrixFactory {
	/**
	 * Create a large, possibly sparse empty matrix
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @return the empty matrix
	 */
	public CyMatrix makeLargeMatrix(CyNetwork network, int rows, int columns);

	/**
	 * Create a large, possibly sparse matrix populated with data from
	 * the indicated edge attribute
	 *
	 * @param network the network that will be the source of the data
	 * @param edgeAttribute the edge attribute to pull the data from
	 * @param selectedOnly only include selected edges
	 * @param converter the edge weight converter to use
	 * @return the resulting matrix
	 */
	public CyMatrix makeLargeMatrix(CyNetwork network, String edgeAttribute, 
	                                boolean selectedOnly, EdgeWeightConverter converter);

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
	public CyMatrix makeLargeMatrix(CyNetwork network, String[] attributes,
	                                boolean selectedOnly, boolean ignoreMissing,
																	boolean transpose, boolean assymetric);

	/**
	 * Create a small empty matrix
	 *
	 * @param network the network that will be the source of the data
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matix
	 * @return the empty matrix
	 */
	public CyMatrix makeSmallMatrix(CyNetwork network, int rows, int columns);

	/**
	 * Create a small matrix populated with data from
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
	public CyMatrix makeSmallMatrix(CyNetwork network, String[] attributes,
	                                boolean selectedOnly, boolean ignoreMissing,
																	boolean transpose, boolean assymetric);
}

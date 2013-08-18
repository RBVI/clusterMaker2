/*
 * Created on 25. September 2007
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;


/**
 * This Interface describes the basic properties of an object where the costs for
 * deleting or adding an edge between every two nodes i and ja are saved. There 
 * are to be different solutions as to the exact data structure of this object.
 * 
 * @author Sita Lange
 */
public interface ICCEdges {
	
	
	public ICCEdges clone();
	
	/**
	 * Initialises an edges object with the correct size and with initial null weight values.
	 * 
	 * @param size The number of nodes in the component.
	 */
	public void initCCEdges(int size);
	
	/**
	 * Normalises the values between 0 and 1.
	 *
	 */
	public void normalise();
	
	/**
	 * Undo the normalisation done by normalise()
	 *
	 */
	public void denormalise();
	
	/**
	 * Normalises the values between -1 and 1. 
	 *
	 */
	public void normaliseWithThreshold(double alpha);
	
	/**
	 * Undo the normalisation done by normaliseWithThreshold
	 *
	 */
	public void denormaliseWithThreshold();
	
	
	
	/**
	 * Sets the cost of adding or deleting the edge between nodes i and j.
	 * 
	 * @param node_i First edge node.
	 * @param node_j Second edge node.
	 * @param cost The cost of adding or deleting the edge (i,j).
	 */
	public void setEdgeCost(int node_i, int node_j, float cost);
	
	
	/**
	 * Gets the cost of adding or deleting the edge between nodes i and j.
	 * 
	 * @param node_i First edge node.
	 * @param node_j Second edge node.
	 * @return The cost of adding or deleting the edge (i,j).
	 */
	public float getEdgeCost(int node_i, int node_j);

}

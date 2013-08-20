/* 
* Created on 27. September 2007
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

/**
 * This is the interface for a class that created an initial layout for the ConnectedComponent.
 * This means that the nodes of the component are given a position according to the layout.
 * 
 * @author Sita Lange
 */
public interface ILayoutInitialiser {

	/**
	 * This method initialises the class with a ConnectedComponent. 
	 * @param cc The ConnectedComponent on which the initial layouting is to be done on.
	 */
	public void initLayoutInitialiser(ConnectedComponent cc);
	
	/**
	 * This method creates an initial layout for the nodes of the ConnectedComponent.
	 */
	public void run();
	
	
}

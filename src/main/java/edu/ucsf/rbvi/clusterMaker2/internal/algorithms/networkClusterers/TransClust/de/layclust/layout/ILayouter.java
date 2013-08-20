/* 
* Created on 27. September 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

/**
 * This is the interface for a class that does the layouting step in the clustering.
 * There are two different ways to initialise a layouter:
 * (1) It is the first time a layouter is used an the nodes have no previous positions.
 * (2) A layouter has already been used and the determined positions should be kept.
 * 
 * @author Sita Lange
 */
public interface ILayouter {
	
	/**
	 * This method initialises the layouter with an initial layout. This means
	 * a layouter is used for the first time.
	 * 
	 * @param cc The ConnectedComponent that is to be worked on.
	 * @param li An implementation of the ILayoutInitialiser to initialise a certain layout.
	 * @param parameters The parameters object with the user-defined parameters for training.
	 */
	public void initLayouter(ConnectedComponent cc, ILayoutInitialiser li, 
			IParameters parameters);
	
	/**
	 * This method initialises the layouter without an initial layout, because
	 * a layout for the nodes of the component is given by a previous layouter.
	 * 
	 * @param cc The ConnectedComponent that is to be worked on.
	 * @param layouter The layouter that was previously used.
	 * @param parameters The parameters object with the user-defined parameters for training.
	 */
	public void initLayouter(ConnectedComponent cc, ILayouter layouter, 
			IParameters parameters);
	
	/**
	 * This method initialises the layouter without setting the node positions
	 * for the given {@link ConnectedComponent}. It is for the case when the
	 * positions have previously been initialised as done in the parameter training.
	 * The implementation of this method should check whether the positions of
	 * the ConnectedComponent is not equal to null.
	 * array is known.
	 * @param cc
	 * @param parameters
	 */
	public void initLayouter(ConnectedComponent cc, IParameters parameters);
	

	/**
	 * Gets the last calculated positions for this layouter.
	 * @return The last calculated positions for this layouter.
	 */
	public double[][] getNodePositions();
	
	
	/**
	 * This method runs the layouting process.
	 */
	public void run();
	

}

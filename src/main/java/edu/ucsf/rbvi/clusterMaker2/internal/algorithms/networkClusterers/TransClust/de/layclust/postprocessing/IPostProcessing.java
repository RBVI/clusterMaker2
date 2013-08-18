/*
 *  Created on 3. December 2007
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;


/**
 * @author sita
 *
 */
public interface IPostProcessing {
	
	public void initPostProcessing(ConnectedComponent cc);

	public void run();
	
}

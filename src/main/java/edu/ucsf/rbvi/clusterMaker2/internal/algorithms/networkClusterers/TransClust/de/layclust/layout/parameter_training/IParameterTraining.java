/**
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;

/**
 * @author sita
 *
 */
public interface IParameterTraining {
	
	
	public void initialise(LayoutFactory.EnumLayouterClass enumType, int generationsSize, 
			int noOfGenerations);
	
	public IParameters run(ConnectedComponent cc);
	
	public void setMaxThreadSemaphoreAndThreadsList(Semaphore semaphore, ArrayList<Thread> allThreads);

}

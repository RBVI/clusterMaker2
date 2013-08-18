package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.IGeometricClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayouter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.IPostProcessing;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndReclusterRecursively;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndRecluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;

/**
 * This class performs the clustering, but in the parameter training mode. This means
 * no output is created and the final score is set to the {@link IParameters} object.
 * Take into account that any costs occured in the cost matrix reduction process
 * is not added to the score here, since the relative lowest score is looked for
 * and it does not matter if this factor is added or not, which stays the same for each
 * ConnectedComponent.
 * 
 * If parameter training should carry out a different clustering, then it should be changed
 * in this class.
 * 
 * @author Sita Lange
 *
 */
public class ClusterTrainingTask implements Runnable {

	private ConnectedComponent cc = null;
	private Semaphore semaphore = null;
	private IParameters parameters = null;
	private LayoutFactory.EnumLayouterClass layoutEnum = null;
	private Semaphore maxThreadSemaphore;
	
	private ArrayList<Thread> allThreads = null;
	private Thread runningThread = null;


	public ClusterTrainingTask(ConnectedComponent cc, IParameters parameters, 
			LayoutFactory.EnumLayouterClass layoutEnum) {

		this.cc = cc;
		this.parameters = parameters;
		this.layoutEnum = layoutEnum;
	}

	public void run() {
		
		/* add running thread  to list of threads*/
		if(this.allThreads != null){
			this.allThreads.add(runningThread);
		}
		
		if(this.cc.getReducedConnectedComponent()!=null){
			runClustering(this.cc.getReducedConnectedComponent());
		}else{
			runClustering(this.cc);
		}
		
		if(this.cc.getReducedConnectedComponent()!=null){
			this.cc.rebuildCC();
		}
		
//		System.out.println("before = " + this.cc.getClusteringScore());
//		this.cc.setClusteringScore(this.cc.calculateClusteringScore(this.cc.getClusters()));
//		System.out.println("after = " + this.cc.getClusteringScore());
//			System.out.println();
			
			/* ==== STUFF AT END ==== */
			/* set the score for the parameters object for the training */
			parameters.setScore(this.cc.getClusteringScore());
			
			/* release permit in semaphores if necessary */
			if(this.semaphore != null){
				semaphore.release();
			}
			if(this.maxThreadSemaphore != null){
				this.maxThreadSemaphore.release();
				this.allThreads.remove(runningThread);
			}
	}
	
	private void runClustering(ConnectedComponent cc) {
		/* ====LAYOUTING PHASE ==== */
		ILayouter layouter = layoutEnum.createLayouter();
		layouter.initLayouter(cc, this.parameters);
		layouter.run();

		
		/* ==== CLUSTERING PHASE ==== */
		IGeometricClusterer geocluster = TaskConfig.geometricClusteringEnum.createGeometricClusterer();
		geocluster.initGeometricClusterer(cc);
		geocluster.run();
		
		
		/* ====POST-PROCESSING PHASE ==== */
		if(TaskConfig.doPostProcessing){
			PostProcessingFactory.EnumPostProcessingClass ppEnum = 
				TaskConfig.postProcessingEnum;
			IPostProcessing pp = ppEnum.createPostProcessor();
			pp.initPostProcessing(cc);
			/* note: training can only be done for one layouter at a time! */
			LayoutFactory.EnumLayouterClass[] layouterTypes = {this.layoutEnum};
			IParameters[] params = {this.parameters};
			if(ppEnum == PostProcessingFactory.EnumPostProcessingClass.
					PP_DIVIDE_AND_RECLUSTER){
				((PP_DivideAndRecluster) pp).setLayoutingInfo(params, layouterTypes);
			} else if(ppEnum == PostProcessingFactory.EnumPostProcessingClass.
					PP_DIVIDE_AND_RECLUSTER_RECURSIVELY){
				((PP_DivideAndReclusterRecursively) pp).setLayoutingInfo(params, layouterTypes);
			}
			pp.run();
		}
		
	}

	/**
	 * If the use of Semaphores is wanted, then one can be set. Otherwise it is
	 * null;
	 * 
	 * @param semaphore
	 *            The semaphore to set.
	 */
	public void setSemaphore(Semaphore semaphore) {
		this.semaphore = semaphore;

		if (semaphore != null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// Thread interrupted, semaphore can't aquire
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sets the Semaphore to keep track of how many threads are allowed to run in parallel.
	 * Also tries to aquire a permit. If none is available, the thread from where this method
	 * is called has to wait as long until one running thread has finished and released a permit.
	 * 
	 * @param semaphore The Semaphore to set and to acqire.
	 */
	public void setMaxThreadSemaphore(Semaphore semaphore, ArrayList<Thread> allThreads, Thread t){
		this.maxThreadSemaphore = semaphore;
		this.allThreads = allThreads;
		this.runningThread = t;

		if (semaphore != null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// Thread interrupted, semaphore can't aquire
				e.printStackTrace();
			}
		}
	}
}

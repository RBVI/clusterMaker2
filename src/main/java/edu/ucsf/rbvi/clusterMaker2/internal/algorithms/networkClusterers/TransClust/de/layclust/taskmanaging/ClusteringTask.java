/* 
 * Created on 18. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering.FixedParameterClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.IGeometricClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy.GreedyClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayouter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.IPostProcessing;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndReclusterRecursively;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndRecluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ClusterFile;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.InfoFile;

/**
 * This class carries out the final clustering process and writes the resulting
 * clusters into a clusters file. It is possible to run this in a thread, but
 * preferable not to as the ConnectedComponent objects can get quite large and
 * if too many are created at the same time it can come to memory problems.
 * 
 * @author Sita Lange 2007
 * 
 */
public class ClusteringTask implements Runnable {
	
	// private static Logger log = Logger.getLogger(ClusteringTask.class.getName());

	/* the ConnectedComponent for which the clustering should be done */
	private ConnectedComponent cc;

	/* keeps track of the thread running status */
	private Semaphore semaphore = null;
	
	/* keeps track of number of parallel threads */
	private Semaphore maxThreadSemaphore = null;
	
	private ArrayList<Thread> allThreads = null;
	private Thread runningThread = null;
	
	private long time = -1; // starting time for this ConnectedComponent

	/*
	 * The order of layouting algorithms that should be started. Each layouting
	 * algorithm has a int value as is defined in LayoutFactory.
	 */
	private LayoutFactory.EnumLayouterClass[] layouterEnumTypes = null;

	/*
	 * IParameter objects for each layouter in the same order as in
	 * layouterIntTypes.
	 */
	private IParameters[] parameters = null;

	/* the file where the resulting clusters are to be added to */
	private ClusterFile clusterFile = null;

	public ClusteringTask(ConnectedComponent cc, IParameters[] parameters,
			LayoutFactory.EnumLayouterClass[] layouterEnumTypes,
			ClusterFile clusterFile) {
		this.cc = cc;
		this.parameters = parameters;
		this.layouterEnumTypes = layouterEnumTypes;
		this.clusterFile = clusterFile;
	}

	/**
	 * Runs the clustering process for one {@link ConnectedComponent} and prints
	 * the results into the output file.
	 */
	public void run() {
		
		/* add running thread  to list of threads*/
		if(this.allThreads != null){
			this.allThreads.add(runningThread);
		}
//		if(true){
//			this.cc.initialiseClusterInfo(1);
//			return;
//		}
		
		
//		this.cc = runClustering(this.cc);
		
		if(this.cc.getReducedConnectedComponent()!=null){
			if(this.cc.getReducedConnectedComponent().getNodeNumber()!=1){
				runClustering(this.cc.getReducedConnectedComponent());
			}else{
				this.cc.initialiseClusterInfo(1);
			}
		}	
		else{
			runClustering(this.cc);
		}
		if(this.cc.getReducedConnectedComponent()!=null){
//			double cost = this.cc.getReducedConnectedComponent().getClusteringScore()+this.cc.getReducedConnectedComponent().getReductionCost();
//			if(cost<this.cc.getClusteringScore()){
				this.cc.rebuildCC();
//			}
		}
		
		
		
		/* ====== SET SCORE ======= */
		double score = this.cc.getClusteringScore();
		/* add initial reduction cost to score */
		score += this.cc.getReductionCost();
		
		/* add clustering score to total score! */

		ClusteringManager.addClusteringScoreToSum(score);

		
		/* ====== PRINT CLUSTERING INFO ====== */

		if (TaskConfig.mode == TaskConfig.CLUSTERING_MODE) {
			String ccPath = this.cc.getCcPath();
			int ccSize = this.cc.getNodeNumber();
			int[] distribution = this.cc.getClusterInfo();
			int clusterNo = this.cc.getNumberOfClusters();

			String delimiter = "\t";
			StringBuffer resultForCCBuffer = new StringBuffer(
					distribution.length * 4);
			resultForCCBuffer.append(ccPath);
			resultForCCBuffer.append(delimiter);
			resultForCCBuffer.append("size=");
			resultForCCBuffer.append(ccSize);
			resultForCCBuffer.append(delimiter);
			resultForCCBuffer.append("score=");
			resultForCCBuffer.append(TaskUtility.round(score, 3));
			resultForCCBuffer.append(delimiter);
			resultForCCBuffer.append("clusters=");
			resultForCCBuffer.append(clusterNo);
			resultForCCBuffer.append(delimiter);

			for (int i = 0; i < distribution.length; i++) {
				resultForCCBuffer.append(distribution[i]);
				resultForCCBuffer.append(" ");
			}
			resultForCCBuffer.append(delimiter);
			resultForCCBuffer.append("time=");
			this.time = System.currentTimeMillis() - time;
			resultForCCBuffer.append(TaskUtility.convertTime(this.time));
			resultForCCBuffer.append(delimiter);
			resultForCCBuffer.append("time-ms=");
			resultForCCBuffer.append(this.time);

			
			TaskConfig.monitor.setStatusMessage(resultForCCBuffer.toString());
			InfoFile.appendLnProjectResults(resultForCCBuffer.toString());
			
		



		/* ====== PRINT RESULTS IN CLUSTERS FILE ====== */

			ArrayList<ArrayList<String>> clustersWithIDs = new ArrayList<ArrayList<String>>(
					this.cc.getNumberOfClusters());
	
			/* fill array list object with cluster array lists */
			for (int i = 0; i < this.cc.getNumberOfClusters(); i++) {
				clustersWithIDs.add(new ArrayList<String>());
			}
			/* fill cluster array list with object id's */
			for (int i = 0; i < this.cc.getNodeNumber(); i++) {
				int cluster = this.cc.getClusterNoForObject(i);
				String objectID = this.cc.getObjectID(i);
				clustersWithIDs.get(cluster).add(objectID);
			}
	
			/* print each cluster in cluster output file */
			
			for (ArrayList<String> cluster : clustersWithIDs) {
				clusterFile.printCluster(cluster);
			}
			clusterFile.flushbw();
			
		}

		
		/* ====== CLEAN UP AT END ====== */

		/* release permit in semaphores if necessary */
		if (semaphore != null) {
			semaphore.release();
		}
		if(this.maxThreadSemaphore != null){
			this.maxThreadSemaphore.release();
			this.allThreads.remove(runningThread);
		}
	}

	private void runClustering(ConnectedComponent cc) {
		
		
		ConnectedComponent ccCopy = cc.copy(true);
		new GreedyClusterer(ccCopy);
		
//		ConnectedComponent ccCopy2 = cc.copy(true);
//		new MinCutClusterer(ccCopy2);
//		System.out.println(ccCopy.getClusteringScore() + "\t" + ccCopy2.getClusteringScore());
//		ccCopy.setClusteringScore(Double.MAX_VALUE);
//		ConnectedComponent ccCopy2 = cc.copy(true);
//		ccCopy2.setClusteringScore(Double.MAX_VALUE);
//		new TreeClusterer(ccCopy2);
		
		if(!TaskConfig.fixedParameter||cc.getNodeNumber()>=TaskConfig.fixedParameterMax) TaskConfig.fpStopped = true;
		if(TaskConfig.fixedParameter && cc.getNodeNumber()<TaskConfig.fixedParameterMax){
			new FixedParameterClusterer(cc,ccCopy.getClusteringScore());
		}
		if(TaskConfig.greedy&&TaskConfig.fpStopped){
			cc.setClusteringScore(Double.MAX_VALUE);
			TaskConfig.fpStopped = false;
		}else if(TaskConfig.fpStopped){
			
//			cc.initialiseClusterInfo(ccCopy.getNumberOfClusters());
//			cc.setClusters(ccCopy.getClusters());
			/* ====== LAYOUTING PHASE ====== */
			TaskConfig.fpStopped = false;
			/* iterate over layouters */
			ILayouter previousLayouter = null;
			for (int i = 0; i < this.layouterEnumTypes.length; i++) {
				
				IParameters param = parameters[i];

				/* create correct layouter */
				ILayouter layouter = this.layouterEnumTypes[i].createLayouter();

				if (previousLayouter == null) {
					/* initialise cc positions if in clustering mode */
					if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE||TaskConfig.mode ==TaskConfig.COMPARISON_MODE||TaskConfig.mode ==TaskConfig.HIERARICHAL_MODE){
						ILayoutInitialiser li = this.layouterEnumTypes[i].createLayoutInitialiser();
						li.initLayoutInitialiser(cc);
						/* initialise and run layouter */
						layouter.initLayouter(cc, li, param);
						layouter.run();
						previousLayouter = layouter;
					} else if(TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
						ILayoutInitialiser li = this.layouterEnumTypes[i].createLayoutInitialiser();
						li.initLayoutInitialiser(cc);
						/* initialise and run layouter */
						layouter.initLayouter(cc, li, param);
						layouter.run();
						previousLayouter = layouter;
//						// else positions already set for training mode
//						layouter.initLayouter(cc, param);
//						layouter.run();
//						previousLayouter = layouter;
					} else {
					}
						
				} else {
					/*
					 * initialise and run layouter with previous calculated
					 * positions
					 */
					layouter.initLayouter(cc, previousLayouter, param);
					layouter.run();
				}
			}

			/* ====== GEOMETRIC CLUSTERING */
			IGeometricClusterer geoClust = TaskConfig.geometricClusteringEnum
					.createGeometricClusterer();
			geoClust.initGeometricClusterer(cc);
			geoClust.run();
			
		}
		

		/* ====== POST-PROCESSING ====== */
		if (TaskConfig.doPostProcessing&&cc.getClusteringScore()!=Double.MAX_VALUE) {
			PostProcessingFactory.EnumPostProcessingClass ppEnum = TaskConfig.postProcessingEnum;
			IPostProcessing pp = ppEnum.createPostProcessor();
			pp.initPostProcessing(cc);

			// TODO this had to be edited if the post processors need
			// additional parameters.
			if (ppEnum
					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER)) {
				((PP_DivideAndRecluster) pp)
						.setLayoutingInfo(this.parameters,
								this.layouterEnumTypes);
			} else if (ppEnum
					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY)) {
				((PP_DivideAndReclusterRecursively) pp).setLayoutingInfo(
						this.parameters, this.layouterEnumTypes);
			}
			/* run post processing */
			pp.run();
			
//			ppEnum = PostProcessingFactory.EnumPostProcessingClass.PP_REARRANGE_AND_MERGE_BEST;
//			pp = ppEnum.createPostProcessor();
//			pp.initPostProcessing(cc);
//			pp.run();
		}
		if (TaskConfig.doPostProcessing&&ccCopy.getClusteringScore()!=Double.MAX_VALUE) {
			PostProcessingFactory.EnumPostProcessingClass ppEnum = TaskConfig.postProcessingEnum;
			IPostProcessing pp = ppEnum.createPostProcessor();
			pp.initPostProcessing(ccCopy);

			// TODO this had to be edited if the post processors need
			// additional parameters.
			if (ppEnum
					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER)) {
				((PP_DivideAndRecluster) pp)
						.setLayoutingInfo(this.parameters,
								this.layouterEnumTypes);
			} else if (ppEnum
					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY)) {
				((PP_DivideAndReclusterRecursively) pp).setLayoutingInfo(
						this.parameters, this.layouterEnumTypes);
			}
			/* run post processing */
			pp.run();
//			ppEnum = PostProcessingFactory.EnumPostProcessingClass.PP_REARRANGE_AND_MERGE_BEST;
//			pp = ppEnum.createPostProcessor();
//			pp.initPostProcessing(cc);
//			pp.run();
		}
//		if (TaskConfig.doPostProcessing&&ccCopy2.getClusteringScore()!=Double.MAX_VALUE) {
//			PostProcessingFactory.EnumPostProcessingClass ppEnum = TaskConfig.postProcessingEnum;
//			IPostProcessing pp = ppEnum.createPostProcessor();
//			pp.initPostProcessing(ccCopy2);
//
//			// TODO this had to be edited if the post processors need
//			// additional parameters.
//			if (ppEnum
//					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER)) {
//				((PP_DivideAndRecluster) pp)
//						.setLayoutingInfo(this.parameters,
//								this.layouterEnumTypes);
//			} else if (ppEnum
//					.equals(PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY)) {
//				((PP_DivideAndReclusterRecursively) pp).setLayoutingInfo(
//						this.parameters, this.layouterEnumTypes);
//			}
//			/* run post processing */
//			pp.run();
////			ppEnum = PostProcessingFactory.EnumPostProcessingClass.PP_REARRANGE_AND_MERGE_BEST;
////			pp = ppEnum.createPostProcessor();
////			pp.initPostProcessing(cc);
////			pp.run();
//		}
		
		
	//	if(ccCopy.getClusteringScore()<cc.getClusteringScore()&&ccCopy.getClusteringScore()<=ccCopy2.getClusteringScore()){
			if(ccCopy.getClusteringScore()<cc.getClusteringScore()){
			cc.initialiseClusterInfo(ccCopy.getNumberOfClusters());
			cc.setClusters(ccCopy.getClusters());
			cc.calculateClusterDistribution();
			cc.setClusteringScore(ccCopy.getClusteringScore());
		}
//			else if(ccCopy2.getClusteringScore()<cc.getClusteringScore()&&ccCopy.getClusteringScore()>ccCopy2.getClusteringScore()){
//			cc.initialiseClusterInfo(ccCopy2.getNumberOfClusters());
//			cc.setClusters(ccCopy2.getClusters());
//			cc.calculateClusterDistribution();
//			cc.setClusteringScore(ccCopy2.getClusteringScore());
//		}
			else{
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
	 * Also sets the list with all running threads and the actual thread to be added to it.
	 * @param semaphore The Semaphore with the number of permits equals the max no. of parallel threads.
	 * @param allThreads The list with all running threads.
	 * @param t The actual thread to be added to the list of threads.
	 */
	public void setMaxThreadSemaphore(Semaphore semaphore, ArrayList<Thread> allThreads, Thread t){
		this.maxThreadSemaphore = semaphore;
		this.allThreads = allThreads;
		this.runningThread = t;
		
		if (semaphore != null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 
	 * sets the starting time for the current connectedcomponent (takes parametertraining into account) 
	 * @param time Starting time
	 * */
	public void setTime(long time){
		this.time = time;
	}

}

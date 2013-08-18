package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.util.concurrent.Semaphore;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering.FixedParameterClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.IGeometricClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy.GreedyClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayouter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.IPostProcessing;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndRecluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_DivideAndReclusterRecursively;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PP_RearrangeAndMergeBest;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;
/**
 * This class carries out the clustering procedure for post-processing. This means no output is made
 * and the post-processing used here is just {@link PP_RearrangeAndMergeBest}.
 * This is fixed here.
 * 
 * @author Sita Lange
 *
 */
public class ClusterPostProcessingTask implements Runnable {

	private ConnectedComponent cc = null;

	private Semaphore semaphore = null;

	private IParameters[] allparameters = null;

	private LayoutFactory.EnumLayouterClass[] layouterEnumTypes = null;

	public ClusterPostProcessingTask(ConnectedComponent cc,
			IParameters[] allparameters,
			LayoutFactory.EnumLayouterClass[] layouterEnumTypes) {

		this.cc = cc;
		this.allparameters = allparameters;
		this.layouterEnumTypes = layouterEnumTypes;
	}

	public void run() {
	
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
		

		/* release this thread from semaphore to signal finished status */
		if (semaphore != null) {
			semaphore.release();
		}
	}

	private void runClustering(ConnectedComponent cc) {
		ConnectedComponent ccCopy = cc.copy(true);
//		ccCopy.setClusteringScore(Double.MAX_VALUE);
		new GreedyClusterer(ccCopy);
		
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
				IParameters param = this.allparameters[i];

				/* create correct layouter */
				ILayouter layouter = this.layouterEnumTypes[i].createLayouter();

				if (previousLayouter == null) {
					/* initialise cc positions if in clustering mode */
					if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE||TaskConfig.mode ==TaskConfig.COMPARISON_MODE||TaskConfig.mode ==TaskConfig.HIERARICHAL_MODE){
						ILayoutInitialiser li = this.layouterEnumTypes[i]
						                                               .createLayoutInitialiser();
						li.initLayoutInitialiser(cc);


						/* initialise and run layouter */
						layouter.initLayouter(cc, li, param);
						layouter.run();
						previousLayouter = layouter;
					} else if(TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
						// else positions already set for training mode
						layouter.initLayouter(cc, param);
						layouter.run();
						previousLayouter = layouter;
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
			IPostProcessing pp = PostProcessingFactory.EnumPostProcessingClass.
			PP_REARRANGE_AND_MERGE_BEST.createPostProcessor();
			pp.initPostProcessing(cc);

			/* run post processing */
			pp.run();
		}
		if (TaskConfig.doPostProcessing&&ccCopy.getClusteringScore()!=Double.MAX_VALUE) {
			IPostProcessing pp = PostProcessingFactory.EnumPostProcessingClass.
			PP_REARRANGE_AND_MERGE_BEST.createPostProcessor();
			pp.initPostProcessing(ccCopy);

			/* run post processing */
			pp.run();
		}
//		if (TaskConfig.doPostProcessing&&ccCopy2.getClusteringScore()!=Double.MAX_VALUE) {
//			IPostProcessing pp = PostProcessingFactory.EnumPostProcessingClass.
//			PP_REARRANGE_AND_MERGE_BEST.createPostProcessor();
//			pp.initPostProcessing(ccCopy2);
//
//			/* run post processing */
//			pp.run();
//		}
//		if(ccCopy.getClusteringScore()<cc.getClusteringScore()&&ccCopy.getClusteringScore()<=ccCopy2.getClusteringScore()){
		if(ccCopy.getClusteringScore()<cc.getClusteringScore()){
			cc.initialiseClusterInfo(ccCopy.getNumberOfClusters());
			cc.setClusters(ccCopy.getClusters());
			cc.calculateClusterDistribution();
			cc.setClusteringScore(ccCopy.getClusteringScore());
		}
//		else if(ccCopy2.getClusteringScore()<cc.getClusteringScore()&&ccCopy.getClusteringScore()>ccCopy2.getClusteringScore()){
//			cc.initialiseClusterInfo(ccCopy2.getNumberOfClusters());
//			cc.setClusters(ccCopy2.getClusters());
//			cc.calculateClusterDistribution();
//			cc.setClusteringScore(ccCopy2.getClusteringScore());
//		}else{
//		}
		
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
}

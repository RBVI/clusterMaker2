/* 
 * Created on 14. December 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import java.util.HashSet;
import java.util.ArrayList;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusterPostProcessingTask;

/**
 * This implementation of {@link IPostProcessing} takes the clustering results 
 * from one clustering run and re-clusters each cluster that is greater than 2. This
 * is done only once and not recursively. 
 * 
 * @author Sita Lange
 *
 */
public class PP_DivideAndRecluster implements
		IPostProcessing {
	
	private ConnectedComponent cc = null;
	private double bestScore = 0;
	private IParameters[] params = null;
	private LayoutFactory.EnumLayouterClass[] layouterEnumTypes = null;

	public void initPostProcessing(ConnectedComponent cc) {
		this.cc = cc;		
		this.bestScore = cc.getClusteringScore();
	}
	
	/**
	 * This sets the information as to which layouter should be used and its parameters. Or
	 * which combination of layouters should be used.
	 * This method MUST be called before run()!!
	 * @param params The parameters for the layouting phase.
	 * @param layouterEnumTypes The enum type of the layouter to be used.
	 */
	public void setLayoutingInfo(IParameters[] params, LayoutFactory.EnumLayouterClass[] 
	                                                                                   layouterEnumTypes){
		this.params = params;
		this.layouterEnumTypes = layouterEnumTypes;
	}

	public void run() {
		
		/* print score before any post processing */
//		double scoreAtStart = this.cc.getClusteringScore();
//		System.out.println("Score before post processing: "+scoreAtStart);
		
		/* merging step */		
		ArrayList<ArrayList<Integer>> clusterObject = PostProcessingUtility.createClusterObject(this.cc, false);
		HashSet<String> alreadyCompared = new HashSet<String>();
		ClusterObjectComparator comparator = new ClusterObjectComparator();
		PostProcessingUtility.mergeCluster(clusterObject, alreadyCompared, this.cc, comparator, true);
		
		this.bestScore = PostProcessingUtility.updateClusterInfoInCC(clusterObject, this.cc);
		
		/* collection ConnectedComponent objects */
		ArrayList<ConnectedComponent> cCsOfSubgraphs = new ArrayList<ConnectedComponent>();
		
		/* start a new clustering procedure for clusters larger than 3 */
	    for(int i=0;i<clusterObject.size();i++){
			ArrayList<Integer> cluster = clusterObject.get(i);
			int clusterSize = cluster.size();

			/* if the clusters are to small, leave them in the clusters object and continue */
			if(clusterSize <= 3){
//				System.out.println("cluster too small: "+cluster.toString());
				continue;
			}
			
			/* remove cluster from cluster object and decrease i */ //TODO!!
			clusterObject.remove(i);
			--i;
			
			
			ConnectedComponent ccForCluster = this.cc.createConnectedComponentForCluster(i, cluster);
			cCsOfSubgraphs.add(ccForCluster);
			
			ClusterPostProcessingTask clusterTask = new ClusterPostProcessingTask(ccForCluster, this.params, this.layouterEnumTypes);
			clusterTask.run();
		}
	    
	    for (int i = 0; i < cCsOfSubgraphs.size(); i++) {
			ConnectedComponent subCC = cCsOfSubgraphs.get(i);
			addClustersToTotalClusters(subCC, clusterObject);
		}
	    
	    /* update clustering information */
	    this.bestScore = PostProcessingUtility.updateClusterInfoInCC(clusterObject, this.cc);
		
		/* do post post processing - merge and rearrange */
		PP_RearrangeAndMergeBest postProcess1 = new PP_RearrangeAndMergeBest();
		postProcess1.initPostProcessing(this.cc);
		postProcess1.run();
		

	}
	
	private void addClustersToTotalClusters(ConnectedComponent subCC, ArrayList<ArrayList<Integer>> clusterObject){
		int noOfClusters = subCC.getNumberOfClusters();
//		System.out.println("sub cluster size: "+noOfClusters);
		int[] subClusters = subCC.getClusters();
		
	
		/* initialise new clusters object */
		ArrayList<ArrayList<Integer>> newClusters = new ArrayList<ArrayList<Integer>>(noOfClusters);
		int[] clusterDistribution = subCC.getClusterInfo();
		for (int i = 0; i < clusterDistribution.length; i++) {
			newClusters.add(new ArrayList<Integer>(clusterDistribution[i]));
		}
		
		/* fill new clusters object */
		for (int i = 0; i < subClusters.length; i++) {
			int originalNo = Integer.parseInt(subCC.getObjectID(i));
			newClusters.get(subClusters[i]).add(originalNo);
		}
		
		/* merge new clusters object with old clusters object */
		for (int i = 0; i < noOfClusters; i++) {
			clusterObject.add(newClusters.get(i));
		}
	}

	/**
	 * Gets the score for the current clustering.
	 * @return the bestScore
	 */
	protected double getBestScore() {
		return bestScore;
	}

}

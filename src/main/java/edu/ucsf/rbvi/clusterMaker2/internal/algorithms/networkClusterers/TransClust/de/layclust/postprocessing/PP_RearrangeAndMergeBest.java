/*
 *  Created on 3. December 2007
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.CC2DArray;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ICCEdges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering.FixedParameterClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy.GreedyClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusteringManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidInputFileException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;
// import java.util.logging.Level;
// import java.util.logging.Logger;

/**
 * @author sita
 *
 */
public class PP_RearrangeAndMergeBest implements IPostProcessing {

	private ConnectedComponent cc = null;
	private double bestScore = 0;
	private ICCEdges icce;
	/**
	 * Initialises the post processing object, which needs to be done before run() us called.
	 */
	public void initPostProcessing(ConnectedComponent cc) {
		this.cc = cc;		
		this.bestScore = cc.getClusteringScore();
                this.icce = this.cc.getCCEdges();
	}
	
	/**
	 * Runs the post processing.
	 */
	public void run() {

		int[] clusters = cc.getClusters();
		
		ArrayList<ArrayList<Integer>> clusterObject = PostProcessingUtility.createClusterObject(this.cc, false); 
		
		boolean findBetterScore = true;
		double score;
		while(findBetterScore){
			score = this.bestScore;
			findBetterScore = onePostProcessingRound(clusters, clusterObject, score);
		}
		
		this.cc.initialiseClusterInfo(clusterObject.size());
		this.cc.setClusters(clusters);
		this.cc.setClusteringScore(this.cc.calculateClusteringScore(clusters)); // TODO take this out
		this.cc.calculateClusterDistribution();	
	}
	
	private boolean onePostProcessingRound(int[] clusters, ArrayList<ArrayList<Integer>> clusterObject, double oldScore){
		
	
		rearrangeSingleNodes(clusterObject, clusters);
		updateClusters(clusters, clusterObject);	
		HashSet<String> alreadyCompared = new HashSet<String>();
//        try {
//            mergeBest(clusterObject);
//        } catch (InvalidInputFileException ex) {
//            Logger.getLogger(PP_RearrangeAndMergeBest.class.getName()).log(Level.SEVERE, null, ex);
//        }
		mergeBest(clusterObject, alreadyCompared);
		updateClusters(clusters, clusterObject);	
		
		
		if(this.bestScore < oldScore){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Updates the clusters array from the information in the cluster ArrayList object.
	 * @param clusters The clusters array to be updated.
	 * @param clusterObject The cluster ArrayList object with the current information.
	 */
	protected void updateClusters(int[] clusters, ArrayList<ArrayList<Integer>> clusterObject){

		for(int i=0;i<clusterObject.size();i++){
			ArrayList<Integer> oneCluster = clusterObject.get(i);
			/* remove clusters of size 0 */
			if(oneCluster.size()==0){
				clusterObject.remove(oneCluster);
				--i;
				continue;
			}
			/* update cluster numbers for each node */
			for(int j=0;j<oneCluster.size();j++){
				clusters[oneCluster.get(j)] = i;
			}
		}
	}


	

	
	/**
	 * Looks at each single node and tries putting it in another cluster. This is checked for each cluster
	 * and the node is put into the cluster with the best score improvement, otherwise it is left in the original cluster.
	 * @param clusterObject
	 * @param clusters
	 */
	private void rearrangeSingleNodes(ArrayList<ArrayList<Integer>> clusterObject, int[] clusters ){
		
		boolean changed = false;
		int oldCluster, newCluster;
		double bestCostDiff, costDiff;
                double removeCost;
		
		/* for every node */
		for(int i=0;i<clusters.length;i++){
			bestCostDiff = 0;
			oldCluster = clusters[i];
			newCluster = clusters[i];
			removeCost=0;
                        removeCost = calculateRemoveCost(i,clusterObject.get(oldCluster));
			/* try putting node i in each other cluster*/
			costDiff = 0;
			for(int j = 0;j<clusterObject.size();j++){			
				if(oldCluster!=j){
					costDiff = calculateCostChange(i,
							clusterObject.get(j))+removeCost;
					if(costDiff < bestCostDiff){
						bestCostDiff = costDiff;
						newCluster = j;
					}
				}
			}
			
			/* try creating a new cluster for node i */
			ArrayList<Integer> newEmptyArrayList = new ArrayList<Integer>();
			costDiff = removeCost;
			if(costDiff<bestCostDiff){
				changed = true;
				bestCostDiff = costDiff;
				newCluster = clusterObject.size();
				clusterObject.add(newEmptyArrayList);
			}

			
			/* if some better clustering has been found, then move node to new cluster */
			if(oldCluster != newCluster){
				Integer node_i = Integer.valueOf(i);
				clusterObject.get(oldCluster).remove(node_i);
				clusterObject.get(newCluster).add(node_i);
				this.bestScore +=bestCostDiff;
//				System.out.println("edited clusters!!");
			}
			
			/* if a new cluster has been created break loop and start again */
			if(changed){
				updateClusters(clusters, clusterObject);
				rearrangeSingleNodes(clusterObject, clusters);
				break;
			}

		}		
		if(!changed)
			updateClusters(clusters, clusterObject);
	}
	
	/**
	 * Calculates the cost change caused by moving one node from one cluster to
	 * another. The overall costs for the graph need not be computed, just the 
	 * actual changes, which saves a lot of time.
	 * @param node_i
	 * @param oldCluster The old cluster in which node i was.
	 * @param newCluster The new cluster to which node i is to be assigned to.
	 * @return The change in costs from the old clustering to the new one.
	 */
	private double calculateCostChange(int node_i, 
			ArrayList<Integer> newCluster){
		double costChange = 0;
		/* all edges from node i to every other node in the old cluster need
		 * to be deleted.
		 */
//                for (int node_k : oldCluster) {
//                    if(node_k!=node_i){
//			costChange += icce.getEdgeCost(node_i, node_k);
//                    }
//		}
		
		/* edges from all nodes in the new cluster need to be added to the
		 * new node
		 */
                for (int node_k : newCluster) {
                    costChange -= icce.getEdgeCost(node_i, node_k);
                }
		return costChange;
        }
	
	protected void mergeBest(ArrayList<ArrayList<Integer>> clusterObject) throws InvalidInputFileException{
		
		
//		System.out.println("start merging");
		ICCEdges cc2d = TaskConfig.ccEdgesEnum.createCCEdges(clusterObject.size());
		String[] ids = new String[clusterObject.size()];
		
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i+"";
			for (int j = i+1; j < ids.length; j++) {
				cc2d.setEdgeCost(i, j, -(float) calculateCostChange(clusterObject.get(i), clusterObject.get(j)));
//				System.out.println(i + "\t" + j + "\t" + cc2d.getEdgeCost(i, j));
			}
		}
		ConnectedComponent cc2 = new ConnectedComponent(cc2d, ids, null,false);
		ClusteringManager cm = new ClusteringManager(null);
//		cm.runClusteringForOneConnectedComponent(cc2, null, null, null, System.currentTimeMillis());
		new GreedyClusterer(cc2);
//		long dummy = TaskConfig.fpMaxTimeMillis;
//		TaskConfig.fpMaxTimeMillis = Long.MAX_VALUE;
//		FixedParameterClusterer dc = new FixedParameterClusterer(cc2,cc2.getClusteringScore());
//		TaskConfig.fpMaxTimeMillis = dummy;
//		if(cc2.getClusteringScore() != this.bestScore){
//			PP_DivideAndRecluster pp = new PP_DivideAndRecluster();
//			pp.initPostProcessing(cc2);
//			pp.run();
//		}
		
		ArrayList<ArrayList<Integer>> clusterObjectNew = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < cc2.getNumberOfClusters(); i++) {
			ArrayList<Integer> v = new ArrayList<Integer>();
			clusterObjectNew.add(v);
		}
		int[] clustering = cc2.getClusters();
		for (int i = 0; i < clustering.length; i++) {
			clusterObjectNew.get(clustering[i]).addAll(clusterObject.get(i));
		}
		clusterObject = clusterObjectNew;
		this.bestScore = cc2.calculateClusteringScore(clustering);
//		System.out.println("end merging");
	}
	
	/**
	 * Searches through all pairs of clusters and merges the pair, which gives the greatest cost
	 * advantages. That means the pair, which when merged reduces the overall cost the most.
	 * This is recursively repeated until no new merges occur.
	 * @param clusterObject The object with all clusters and their respective nodes.
	 * @param alreadyCompared A HashSet that stores all clusters that have already been compared.
	 */
	protected void mergeBest(ArrayList<ArrayList<Integer>> clusterObject, 
			HashSet<String> alreadyCompared){
		double bestCostChange,costChange;
		ArrayList<Integer> best_merge;
		Integer bestMergeCluster;
		for(int i=0;i<clusterObject.size();i++){
			bestCostChange = 0;
			bestMergeCluster = i;
			best_merge = clusterObject.get(i);
			/* check all other clusters for the best merge of two clusters */
			for(int j=i+1;j<clusterObject.size();j++){
				costChange = calculateCostChange(clusterObject.get(i), clusterObject.get(j));
				if(costChange<=bestCostChange){
					bestCostChange = costChange;
					bestMergeCluster = j;
					best_merge = clusterObject.get(j);
				}
			}
			if(bestMergeCluster != i){ 
				/* merge clusters */
				clusterObject.get(i).addAll(best_merge);
				
				this.bestScore += bestCostChange;
				
				/* remove the merged cluster */
				clusterObject.remove(best_merge);
				
				/* recursive call of the method to find other merges */
				mergeBest(clusterObject, alreadyCompared);
				break;
				
			}
		}		
	}
	
	/**
	 * Calculates the cost change for merging the two given clusters and returns this.
	 * @param cluster1 First cluster to be merged.
	 * @param cluster2 Second cluster to be merged.
	 * @return The cost change for merging the two clusters.
	 */
	private double calculateCostChange(ArrayList<Integer> cluster1, ArrayList<Integer> cluster2){
            double costChange = 0;
            for (int node_i : cluster1) {
                for (int node_j : cluster2) {
                    costChange -=icce.getEdgeCost(node_i, node_j);
		}
            }
            return costChange;
	}

	/**
	 * The current best score for post processing
	 * @return the bestScore
	 */
	protected double getBestScore() {
		return bestScore;
	}

    private double calculateRemoveCost(int node_i, ArrayList<Integer> get) {
        double removeCost = 0;
        for (int node_k : get) {
            if(node_i==node_k) continue;
            removeCost+= icce.getEdgeCost(node_i, node_k);
        }
        return removeCost;
    }
}

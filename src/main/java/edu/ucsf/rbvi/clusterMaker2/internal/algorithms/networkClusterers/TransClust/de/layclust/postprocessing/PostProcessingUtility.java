/*
 * Created on 18. December 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

/**
 * This class is a collection of static methods that are needed for post processing and
 * are used in the different implementations of {@link IPostProcessing}.
 * @author sita
 *
 */
public class PostProcessingUtility {
	

	/**
	 * Merges two clusters if the overall cost decreases by doing so. Before each
	 * merging step, the clusters object is sorted according to clusters size
	 * from small to large clusters. This should improve the merging speed.
	 * As soon as a merge is carried out, the method is called recursively and
	 * stops when all cluster merging combinations have been checked without
	 * any cost improvements.
	 * 
	 * @param clusterObject The ArrayList of ArrayLists containing the actual clusters.
	 * @param comparator The comparator for comparing the size of two ArrayLists
	 * @param alreadyCompared HashSet containing combinations of clusters that have been checked.
	 * @param cc The ConnectedComponent object.
	 */
	@SuppressWarnings("unchecked")
	protected static void sortedMerge(ArrayList<ArrayList<Integer>> clusterObject, 
			ClusterObjectComparator comparator, HashSet<String> alreadyCompared,
			ConnectedComponent cc, boolean sort){
		
		if(sort){
			Collections.sort(clusterObject, comparator);
		}
		
		boolean merge = false;
		
		for (int i = 0; i < clusterObject.size(); i++) {
			ArrayList<Integer> v1 = clusterObject.get(i);
			for (int j = i+1; j < clusterObject.size(); j++) {
				ArrayList<Integer> v2 = clusterObject.get(j);
				
				/* check wether clusters have already been compared before going further */
				StringBuffer combinationBuffer = new StringBuffer();
				
				combinationBuffer.append(v1.hashCode());
				combinationBuffer.append(v2.hashCode());
				
				String combination = combinationBuffer.toString();	
//				String combination = v1.hashCode()+"#"+v2.hashCode();
				
				if(!alreadyCompared.contains(combination)){
					alreadyCompared.add(combination);
					double costChange = calculateCostChange(v1, v2, cc);
					/* merge clusters when the cost change is negative */
					if(costChange<=0){
						v1.addAll(v2);
						clusterObject.remove(v2);
						sortedMerge(clusterObject, comparator, alreadyCompared, cc, sort);
						break;
					}
				}
			}
			if(merge){
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
	protected static double calculateCostChange(ArrayList<Integer> cluster1, ArrayList<Integer> cluster2, ConnectedComponent cc){
		double costChange = 0;
		
		for (int i = 0; i < cluster1.size(); i++) {
			int node_i = cluster1.get(i);
			for (int j = 0; j < cluster2.size(); j++) {
				int node_j = cluster2.get(j);
				double cost = cc.getCCEdges().getEdgeCost(node_i, node_j);
				costChange -=cost;
			}
		}	
		return costChange;
	}
	

	protected static double calculateCostChange(HashSet<Integer> moveNodes, ArrayList<Integer> oldCluster,
			ArrayList<Integer> newCluster, ConnectedComponent cc){
		double costChange = 0;
		
		/* all edges from nodes that are to be moved to every other node in the old cluster need
		 * to be deleted.
		 */
		
		for (int n = 0; n < oldCluster.size(); n++) {
			int node_k = oldCluster.get(n);
			if(!moveNodes.contains(node_k)){
				Iterator<Integer> iterateNodes = moveNodes.iterator();
				while(iterateNodes.hasNext()){
					int node_x = iterateNodes.next();
					double cost = cc.getCCEdges().getEdgeCost(node_x, node_k);
					costChange += cost;
				}
			}
		}
		
		/* edges from all nodes in the new cluster need to be added to the
		 * new nodes 
		 */
		for (int n = 0; n < newCluster.size(); n++) {
			int node_k = newCluster.get(n);
			Iterator<Integer> iterateNodes = moveNodes.iterator();
			while(iterateNodes.hasNext()){
				int node_x = iterateNodes.next();
				double cost = cc.getCCEdges().getEdgeCost(node_x, node_k);
				costChange -= cost;	
			}
		}		
		return costChange;		
	}
	
	protected static double calculateCostChange(int node_i, int node_j, ArrayList<Integer> oldCluster, 
			ArrayList<Integer> newCluster, ConnectedComponent cc){
		double costChange = 0;
		
		/* all edges from nodes i and j to every other node in the old cluster need
		 * to be deleted.
		 */
		for (int n = 0; n < oldCluster.size(); n++) {
			int node_k = oldCluster.get(n);
			if(node_k!=node_i && node_k!=node_j){
				double cost_i = cc.getCCEdges().getEdgeCost(node_i, node_k);
				costChange += cost_i;
				double cost_j = cc.getCCEdges().getEdgeCost(node_j, node_k);
				costChange += cost_j;
			}
		}
		
		/* edges from all nodes in the new cluster need to be added to the
		 * new nodes i and j
		 */
		for (int n = 0; n < newCluster.size(); n++) {
			int node_k = newCluster.get(n);
			
			double cost_i = cc.getCCEdges().getEdgeCost(node_i, node_k);
			costChange -= cost_i;	
			double cost_j = cc.getCCEdges().getEdgeCost(node_j, node_k);
			costChange -= cost_j;
		}		
		return costChange;
	}
	
	
//	/**
//	 * Each node is put into every other cluster to see if this improves the overall cost. If it does,
//	 * then this node is removed from the old cluster and put in the new one. Also it is tested
//	 * if the overall cost is reduced by creating a new cluster for this node.
//	 * @param clusterObject
//	 * @param cc
//	 */
//	protected static void rearrangeEachNode(ArrayList<ArrayList<Integer>> clusterObject,
//			ConnectedComponent cc){
//		
//		int[] clusters = cc.getClusters();
//		
//		/* iterate over each node */
//		for (int i = 0; i < clusters.length; i++) {
//			Integer node_i = Integer.valueOf(i);
//			int oldClusterNo = clusters[i];
//			ArrayList<Integer> oldCluster = clusterObject.get(oldClusterNo);
//			
//			double costChange = 0;
//
//			/* try to put node i in each other cluster */
//			for (int c = 0; c < clusterObject.size(); c++) {
//				if(oldClusterNo!=c){
//					ArrayList<Integer> newCluster = clusterObject.get(c);
//					costChange = calculateCostChange(i,oldCluster, newCluster, cc);
//					/* move node */
//					if(costChange<0){
//						oldCluster.remove(node_i);
//						newCluster.add(node_i);
//						continue;
//					}
//				}
//			}
//			
//			/* try creating a new cluster */
//			ArrayList<Integer> newEmptyArrayList = new ArrayList<Integer>();
//			costChange = calculateCostChange(i, oldCluster, newEmptyArrayList, cc);
//			if(costChange<0){
//				oldCluster.remove(node_i);
//				newEmptyArrayList.add(node_i);
//				clusterObject.add(newEmptyArrayList);
//				rearrangeNodeToLevelTwo(clusterObject, cc);
//				break;
//			}			
//		}	
//	}
	
	
	/**
	 * Calculates the cost change caused by moving one node from one cluster to
	 * another. The overall costs for the graph need not be computed, just the 
	 * actual changes, which saves a lot of time.
	 * @param node_i
	 * @param oldCluster The old cluster in which node i was.
	 * @param newCluster The new cluster to which node i is to be assigned to.
	 * @return The change in costs from the old clustering to the new one.
	 */
	protected static double calculateCostChange(int node_i, ArrayList<Integer> oldCluster, 
			ArrayList<Integer> newCluster, ConnectedComponent cc){
		double costChange = 0;
		
		/* all edges from node i to every other node in the old cluster need
		 * to be deleted.
		 */
		for (int j = 0; j < oldCluster.size(); j++) {
			int node_k = oldCluster.get(j);
			if(node_k!=node_i){
				double cost = cc.getCCEdges().getEdgeCost(node_i, node_k);
				costChange += cost;
			}
		}
		
		/* edges from all nodes in the new cluster need to be added to the
		 * new node
		 */
		for (int j = 0; j < newCluster.size(); j++) {
			int node_k = newCluster.get(j);
			
			double cost = cc.getCCEdges().getEdgeCost(node_i, node_k);
			costChange -= cost;	
		}		
		return costChange;
	}
	
	
	/**
	 * 1. Updates the clusters array from the information in the cluster ArrayList object.
	 * 2. Sets the information related to the clustering in the ConnectedCompontent object.
	 * 3. Returns the score for the current clustering.
	 * @param clusterObject The cluster ArrayList object with the current information.
	 * @param cc The ConnectedComponent object.
	 * @return The score for the current clustering.
	 */
	public static double updateClusterInfoInCC(ArrayList<ArrayList<Integer>> clusterObject,
			ConnectedComponent cc){
		
		int[] clusters = cc.getClusters();

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
		
		/* update clustering info in cc */
		cc.initialiseClusterInfo(clusterObject.size());
		cc.setClusters(clusters);
		double score = cc.calculateClusteringScore(clusters);
		cc.setClusteringScore(score); 
		cc.calculateClusterDistribution();
		
		return score;
	}
	
	
	/**
	 * This method creates the clusters object with the needed clustering information. For each cluster
	 * a ArrayList exists with the nodes that are in this cluster. These ArrayLists are added to the
	 * overall ArrayList, which represents the collection of all clusters. If the given connected component
	 * is a sub component of the original cc, then the object IDs are not correct. In this case
	 * the correct object IDs is saved in the cc's variable objectIDs, from where they have to 
	 * be taken.
	 * 
	 * @param cc The connected component for which the cluster object should be created.
	 * @param isSubConnectedComponent If the cc is a sub component of the original cc or not.
	 * @return The clusters object.
	 */
	protected static ArrayList<ArrayList<Integer>> createClusterObject(ConnectedComponent cc, boolean isSubConnectedComponent){
		
		int noOfClusters = cc.getNumberOfClusters();
		int[] clusters = cc.getClusters();
		
		/* initialise clusters object */
		ArrayList<ArrayList<Integer>> clusterObject = new ArrayList<ArrayList<Integer>>(noOfClusters);
		int[] clusterDistribution = cc.getClusterInfo();
		
		for (int i = 0; i < clusterDistribution.length; i++) {
			clusterObject.add(new ArrayList<Integer>(clusterDistribution[i]));
		}
		
		/* fill clusters object */
		for (int i = 0; i < clusters.length; i++) {
			ArrayList<Integer> cluster = clusterObject.get(clusters[i]);
			if(isSubConnectedComponent){
				int objectInitialNo = Integer.parseInt(cc.getObjectID(i));
				cluster.add(objectInitialNo);
			} else {
				cluster.add(i);
			}
		}
		
		return clusterObject;
	}
	
	
	@SuppressWarnings("unchecked")
	protected static void mergeCluster(ArrayList<ArrayList<Integer>> h,  
			HashSet<String> alreadyCompared, ConnectedComponent cc,
			ClusterObjectComparator comparator, boolean sort) {
		
		if(sort){
			Collections.sort(h, comparator);
		}
		
		int[] test = cc.getClusters();
		
		boolean isbreak = false;
		
		for (int i = 0; i < h.size(); i++) {
			ArrayList<Integer> v1 = h.get(i);
			for (int j = i+1; j < h.size(); j++) {
				ArrayList<Integer> v2 = h.get(j);
				StringBuffer combinationBuffer = new StringBuffer();
				combinationBuffer.append(v1.hashCode());
				combinationBuffer.append(v2.hashCode());
				
				String combination = combinationBuffer.toString();	

				if(!alreadyCompared.contains(combination)){
					alreadyCompared.add(combination);
				
					for (int k = 0; k < v2.size(); k++) {
						test[v2.get(k)] = i;
					}
					double costChange = calculateCostChange(v1, v2, cc);
					if(costChange<=0){
						v1.addAll(v2);
						h.remove(j);
						mergeCluster(h,alreadyCompared, cc, comparator, sort);
						isbreak = true;
						break;
					}else{
						for (int k = 0; k < v2.size(); k++) {
							test[v2.get(k)] = j;
						}
					}
				}
			}
			if(isbreak) break;
		}
		
	}
	

}



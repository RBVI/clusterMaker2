package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ICCEdges;

public class GreedyClusterer {

	private ConnectedComponent cc;
	
	private int[] listOfElementsSortedByCosts;
	
	private boolean changed = false;	
	private ICCEdges icce;
	
	private int count;
	
	private int[] bestClusters; 
	private double bestCosts;
	
	private int k = 0;
	
	public GreedyClusterer(ConnectedComponent cc){
		bestCosts = Double.MAX_VALUE;
		this.cc = cc;
		this.listOfElementsSortedByCosts = new int[cc.getNodeNumber()];
		this.icce = cc.getCCEdges();
		this.count=cc.getNodeNumber();
		cluster2();
	}
	
	
	public GreedyClusterer(ConnectedComponent cc, int limitK) {
		bestCosts = Double.MAX_VALUE;
		this.cc = cc;
		this.listOfElementsSortedByCosts = new int[cc.getNodeNumber()];
		this.icce = cc.getCCEdges();
		this.count=cc.getNodeNumber();
		this.k=limitK;
		cluster2(k);
	}


	private void cluster2(int k2) {
		generateSortedList();
		this.count = 0;
		int startNode = this.listOfElementsSortedByCosts[0];
		Vector<Vector<Integer>> clusters = new Vector<Vector<Integer>>();
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		TreeMap<AffinityObject, Integer> affinities = new TreeMap<AffinityObject, Integer>();
		TreeMap<AffinityObject,Integer> internalAffinities = new TreeMap<AffinityObject, Integer>();
		already[startNode] = true;
		count++;
		int count2 = 0;
		for (int i = 0; i < this.listOfElementsSortedByCosts.length; i++) {
			if(already[i]) continue;
			count2++;
			double affinity = icce.getEdgeCost(i, startNode);
			AffinityObject ao = new AffinityObject(i);
			ao.affinity = affinity;
			affinities.put(ao,i);
		}
		AffinityObject startObject = new AffinityObject(startNode);
		startObject.affinity = 0;
		internalAffinities.put( startObject,startNode);
		buildCluster2(affinities,internalAffinities,already);
		Vector<Integer> cluster = new Vector<Integer>();
		cluster.addAll(internalAffinities.values());
		clusters.add(cluster);
		generateSortedList(already);
		
		for (int i = 0; i < this.listOfElementsSortedByCosts.length&&clusters.size()<this.k; i++) {
			System.out.println(clusters.size()+"\t"+ this.k);
			startNode = this.listOfElementsSortedByCosts[i];
			if(!already[startNode]){
				count++;
				already[startNode] = true;
				affinities = new TreeMap<AffinityObject, Integer>();
				internalAffinities = new TreeMap<AffinityObject, Integer>();
				for (int j = 0; j < this.listOfElementsSortedByCosts.length; j++) {
					if(already[j]) continue;
					double affinity = icce.getEdgeCost(j, startNode);
					AffinityObject ao = new AffinityObject(j);
					ao.affinity = affinity;
					affinities.put(ao,j);
				}
				startObject = new AffinityObject(startNode);
				startObject.affinity = 0;
				internalAffinities.put( startObject,startNode);
				buildCluster2(affinities,internalAffinities,already);
				cluster = new Vector<Integer>();
				cluster.addAll(internalAffinities.values());
				clusters.add(cluster);
				generateSortedList(already);
				i=0;
				if(count!=this.listOfElementsSortedByCosts.length) i = 0;
			}
		}
		
		for (int i = 0; i < already.length; i++) {
			if(already[i]) continue;
			
			double bestCosts = Double.NEGATIVE_INFINITY;
			Vector<Integer> bestcluster = new Vector<Integer>();
			for (Vector<Integer> vector : clusters) {
				double costs = 0;
				for (Integer integer : vector) {
					costs+=icce.getEdgeCost(i, integer);
				}
				if(costs<bestCosts){
					bestCosts=costs;
					bestcluster = vector;
				}
			}
			bestcluster.add(i);
		}
		
		
		
		
		
		
		int[] clusters2nodes = new int[this.cc.getNodeNumber()];
		for (int i = 0; i < clusters.size(); i++) {
			cluster = clusters.get(i);
			for (int j = 0; j < cluster.size(); j++) {
				clusters2nodes[cluster.get(j)] = i;
			}
		}
		
		bestClusters = clusters2nodes;
		int noOfClusters = 0;
		noOfClusters=clusters.size();
		this.cc.initialiseClusterInfo(noOfClusters);
		this.cc.setClusteringScore(this.cc.calculateClusteringScore(bestClusters));
		this.cc.setClusters(bestClusters);
		this.cc.calculateClusterDistribution();
//		return this.cc.getClusteringScore();
		
	}


	private double cluster2(){
		generateSortedList();
		this.count = 0;
		int startNode = this.listOfElementsSortedByCosts[0];
		Vector<Vector<Integer>> clusters = new Vector<Vector<Integer>>();
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		TreeMap<AffinityObject, Integer> affinities = new TreeMap<AffinityObject, Integer>();
		TreeMap<AffinityObject,Integer> internalAffinities = new TreeMap<AffinityObject, Integer>();
		already[startNode] = true;
		count++;
		int count2 = 0;
		for (int i = 0; i < this.listOfElementsSortedByCosts.length; i++) {
			if(already[i]) continue;
			count2++;
			double affinity = icce.getEdgeCost(i, startNode);
			AffinityObject ao = new AffinityObject(i);
			ao.affinity = affinity;
			affinities.put(ao,i);
		}
		AffinityObject startObject = new AffinityObject(startNode);
		startObject.affinity = 0;
		internalAffinities.put( startObject,startNode);
		buildCluster2(affinities,internalAffinities,already);
		Vector<Integer> cluster = new Vector<Integer>();
		cluster.addAll(internalAffinities.values());
		clusters.add(cluster);
//		generateSortedList(already);
		for (int i = 0; i < this.listOfElementsSortedByCosts.length; i++) {
			startNode = this.listOfElementsSortedByCosts[i];
			if(!already[startNode]){
				count++;
				already[startNode] = true;
				affinities = new TreeMap<AffinityObject, Integer>();
				internalAffinities = new TreeMap<AffinityObject, Integer>();
				for (int j = 0; j < this.listOfElementsSortedByCosts.length; j++) {
					if(already[j]) continue;
					double affinity = icce.getEdgeCost(j, startNode);
					AffinityObject ao = new AffinityObject(j);
					ao.affinity = affinity;
					affinities.put(ao,j);
				}
				startObject = new AffinityObject(startNode);
				startObject.affinity = 0;
				internalAffinities.put( startObject,startNode);
				buildCluster2(affinities,internalAffinities,already);
				cluster = new Vector<Integer>();
				cluster.addAll(internalAffinities.values());
				clusters.add(cluster);
//				generateSortedList(already);
//				i=0;
				if(count!=this.listOfElementsSortedByCosts.length) i = 0;
			}
		}
		
		int[] clusters2nodes = new int[this.cc.getNodeNumber()];
		for (int i = 0; i < clusters.size(); i++) {
			cluster = clusters.get(i);
			for (int j = 0; j < cluster.size(); j++) {
				clusters2nodes[cluster.get(j)] = i;
			}
		}
		
		bestClusters = clusters2nodes;
		int noOfClusters = 0;
		noOfClusters=clusters.size();
		this.cc.initialiseClusterInfo(noOfClusters);
		this.cc.setClusteringScore(this.cc.calculateClusteringScore(bestClusters));
		this.cc.setClusters(bestClusters);
		this.cc.calculateClusterDistribution();
		return this.cc.getClusteringScore();
	}
	
	
	private void buildCluster2(TreeMap<AffinityObject, Integer> affinities, TreeMap<AffinityObject, Integer> internalAffinities,
			boolean[] already) {
		
		boolean change = false;
		if(!affinities.isEmpty()){
			while(affinities.lastKey().affinity>0){
				AffinityObject ao = affinities.lastKey();
				int id = affinities.pollLastEntry().getValue();
				already[id] = true;
				count++;
				TreeMap<AffinityObject,Integer> aos = (TreeMap<AffinityObject, Integer>) affinities.clone();
				affinities.clear();
				for (Iterator<AffinityObject> iterator = aos.keySet().iterator(); iterator.hasNext();) {
					AffinityObject key = iterator.next();
					key.affinity +=icce.getEdgeCost(key.id, id);
					affinities.put(key,key.id);
				}
				aos = (TreeMap<AffinityObject, Integer>) internalAffinities.clone();
				internalAffinities.clear();
				for (Iterator<AffinityObject> iterator = aos.keySet().iterator(); iterator.hasNext();) {
					AffinityObject key = iterator.next();
					key.affinity +=icce.getEdgeCost(key.id, id);
					internalAffinities.put(key,key.id);
				}
				internalAffinities.put(ao, ao.id);
				change = true;
				if(affinities.isEmpty()) break;
			}
		}
		
		if(internalAffinities.size()>1){
			while(internalAffinities.firstKey().affinity<0){
				AffinityObject ao =internalAffinities.firstKey();
				int id = internalAffinities.pollFirstEntry().getValue();
				already[id] = false;
				count--;
				TreeMap<AffinityObject,Integer> aos = (TreeMap<AffinityObject, Integer>) affinities.clone();
				affinities.clear();
				for (Iterator<AffinityObject> iterator = aos.keySet().iterator(); iterator.hasNext();) {
					AffinityObject key = iterator.next();
					key.affinity -=icce.getEdgeCost(key.id, id);
					affinities.put(key,key.id);
				}
				aos = (TreeMap<AffinityObject, Integer>) internalAffinities.clone();
				internalAffinities.clear();
				for (Iterator<AffinityObject> iterator = aos.keySet().iterator(); iterator.hasNext();) {
					AffinityObject key = iterator.next();
					key.affinity -=icce.getEdgeCost(key.id, id);
					internalAffinities.put(key,key.id);
				}
				affinities.put(ao, ao.id);
				change = true;
				if(internalAffinities.size()==1) break;
			}
		}
		
		if(change){
			buildCluster2(affinities, internalAffinities, already);
		}
		
	}


	private void cluster(){
		generateSortedList();
		calculateCostsForClusterStartingWithSpecificNode(this.listOfElementsSortedByCosts[0]);
	}
	
	private void buildCluster(boolean[] already, int startNode, Vector<Vector<Integer>> clusters){	
		Vector<Integer> cluster = new Vector<Integer>();
		cluster.add(startNode);
		already[startNode] = true;
		this.count--;
		addNodesToClusterRecursivly(already,cluster);
		clusters.add(cluster);		
	}
	
	private double calculateCostsForClusterStartingWithSpecificNode(int node_i) {
		int startNode = this.listOfElementsSortedByCosts[0];
		this.count = this.cc.getNodeNumber();
		Vector<Vector<Integer>> clusters = new Vector<Vector<Integer>>();
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		buildCluster(already, startNode, clusters);
		for (int i = 0; i < this.listOfElementsSortedByCosts.length; i++) {
			startNode = this.listOfElementsSortedByCosts[i];
			if(!already[startNode]){
				buildCluster(already, startNode, clusters);
			}
		}
		
		int[] clusters2nodes = new int[this.cc.getNodeNumber()];
		for (int i = 0; i < clusters.size(); i++) {
			Vector<Integer> cluster = clusters.get(i);
			for (int j = 0; j < cluster.size(); j++) {
				clusters2nodes[cluster.get(j)] = i;
			}
		}
		
		bestClusters = clusters2nodes;
		int noOfClusters = 0;
		for (int i = 0; i < bestClusters.length; i++) {
			if(bestClusters[i]>noOfClusters) noOfClusters = bestClusters[i];
		}
		noOfClusters++;
		this.cc.initialiseClusterInfo(noOfClusters);
		this.cc.setClusteringScore(this.cc.calculateClusteringScore(bestClusters));
		this.cc.setClusters(bestClusters);
		this.cc.calculateClusterDistribution();
		return this.cc.getClusteringScore();
	}


	private void addNodesToClusterRecursivly(boolean[] already,
			Vector<Integer> cluster) {
		
		double minCosts = Double.MAX_VALUE;
		int minNode = -1;
		for (int i = 0; i < this.cc.getNodeNumber(); i++) {	
			if(already[i]) continue;
			double costs = calculateCostsForAddingNode(cluster, i);		
			if(costs<minCosts){
				minCosts = costs;
				minNode = i;
			}		
		}
		if(minCosts>0){
			removeWorstRecursively(already,cluster);
			return;
		}
		
		count--;
		cluster.add(minNode);
		already[minNode] = true;
		addNodesToClusterRecursivly(already, cluster);
	}


	private void removeWorstRecursively(boolean[] already,
			Vector<Integer> cluster) {

		double minCost = Double.POSITIVE_INFINITY;
		int minInt = -1;
		
		for (int i = 0; i < cluster.size(); i++) {
			
			double costs = calculateCostsForRemovingNode(cluster, cluster.get(i));
			
			if(costs<minCost){
				minCost=costs;
				minInt = i;
			}
			
		}
		if(minCost<0){
			count++;
			already[cluster.get(minInt)]=false;
			cluster.remove(minInt);
			this.changed = true;
			removeWorstRecursively(already, cluster);
			this.changed=false;
		}
		if(changed){
			changed=false;
			addNodesToClusterRecursivly(already, cluster);
		}
		return;
	}
	
	
	private double calculateCostsForRemovingNode(Vector<Integer> cluster, int j) {
		double costs = 0;
		for (int i = 0; i < cluster.size(); i++) {
			int node_i = cluster.get(i);
			if(node_i==j)continue;
			costs += this.icce.getEdgeCost(node_i, j);
		}
		return costs;
	}	
	
	private double calculateCostsForAddingNode(Vector<Integer> clusters, int j) {
		
		double costs = 0;
		for (int i = 0; i < clusters.size(); i++) {
			int node_i = clusters.get(i);
			costs-= this.icce.getEdgeCost(node_i, j);
		}
		return costs;
	}


	private void generateSortedList(){
		
		ICCEdges icce = this.cc.getCCEdges();
		
		double costs[] = new double[cc.getNodeNumber()];
		
		for (int i = 0; i < cc.getNodeNumber(); i++) {

			double cost = 0;
			
			for (int j = 0; j < cc.getNodeNumber(); j++) {
				
				if(i==j) continue;
				
				cost+= icce.getEdgeCost(i, j);
				
				
			}
			costs[i] = cost;
	
		}
		
		double[] costsClone = Arrays.copyOf(costs, costs.length);
		
		Arrays.sort(costs);
		
		boolean[] already = new boolean[costs.length];
		for (int i = costs.length-1; i >= 0; i--) {
			
			int position = 0;
			for (int j = 0; j < costsClone.length; j++) {
				if(costs[i]!=costsClone[j]||already[j]) continue;
				
				position = j;
				already[j] = true;
				break;
			}
			
			this.listOfElementsSortedByCosts[costs.length-1-i] = position;
			
		}
		
	}
	
	private void generateSortedList(boolean already2[]){
		
		ICCEdges icce = this.cc.getCCEdges();
		
		double costs[] = new double[cc.getNodeNumber()];
		
		for (int i = 0; i < cc.getNodeNumber(); i++) {

			if(already2[i]){
				costs[i] = Double.POSITIVE_INFINITY;
				continue;
			}
			
			double cost = 0;
			
			for (int j = 0; j < cc.getNodeNumber(); j++) {
				
				if(i==j) continue;
				if(already2[j]) continue;
				
				
				cost+= icce.getEdgeCost(i, j);
				
				
			}
			costs[i] = cost;
	
		}
		
		double[] costsClone = Arrays.copyOf(costs, costs.length);
		
		Arrays.sort(costs);
		
		boolean[] already = new boolean[costs.length];
		for (int i = costs.length-1; i >= 0; i--) {
			
			int position = 0;
			for (int j = 0; j < costsClone.length; j++) {
				if(costs[i]!=costsClone[j]||already[j]) continue;
				
				position = j;
				already[j] = true;
				break;
			}
			
			this.listOfElementsSortedByCosts[costs.length-1-i] = position;
			
		}
		
	}
	
	
	
}


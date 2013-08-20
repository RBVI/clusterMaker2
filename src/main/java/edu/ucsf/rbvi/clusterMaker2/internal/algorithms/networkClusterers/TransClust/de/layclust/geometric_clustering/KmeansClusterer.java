package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ICCEdges;

public class KmeansClusterer implements IGeometricClusterer {
	
	private ConnectedComponent cc;
	
	private int maxK;
	
	private int bestK;
	
	private double bestCosts;
	
	private int[] bestCluster;
	
	private double[] center;
	
	private double span;
	
	private int maxRuns;
	
	private int initMethod = 3;
	
	private int[] listOfElementsSortedByCosts;
	
	public KmeansClusterer(){}
	
	
	/**
	 * Initialises the object with a {@link ConnectedComponent} and 
	 * also the other global properties from the Config file.
	 */
	public void initGeometricClusterer(ConnectedComponent cc) {
		this.cc = cc;
		this.maxK = cc.getNodeNumber();
		if(this.maxK>GeometricClusteringConfig.kLimit){
			this.maxK = GeometricClusteringConfig.kLimit;
		}
		this.bestCosts = Double.MAX_VALUE;
		this.center = new double[cc.getCCPostions(0).length];
		this.span = calculateCenterAndSpan(cc.getCCPositions(), center);
		this.maxRuns = GeometricClusteringConfig.maxInitStartConfigs;
		this.listOfElementsSortedByCosts = new int[this.cc.getNodeNumber()];
	}

	public void run() {
		
		int[] clusters = new int[this.cc.getNodeNumber()];
		for (int i = 1; i <= maxK; i++) {
			
			kmeans(i,clusters);
			
			double costs = this.cc.calculateClusteringScore(clusters);
			
			double bestCostWithK = costs;
			
			int run = 0;
			
			// multiple runs for the same k.  best costs for fixed k are stored in bestCostWithK and overall best costs as usual in bestCosts
			while(run<this.maxRuns){
				if(costs<bestCosts){
					bestCosts = costs;
					bestK = i;
					bestCluster = copyClusters(clusters);
				}
				
				kmeans(i,clusters);
				costs = this.cc.calculateClusteringScore(clusters);
				if(costs<bestCostWithK){
					bestCostWithK = costs;
				}
				run++;
				
			}
			
			if(bestCostWithK>(bestCosts*bestCosts)) break;					
		}
		
		int numberOfCluster = bestK;
		this.cc.initialiseClusterInfo(numberOfCluster);
		this.cc.setClusteringScore(bestCosts);
		this.cc.setClusters(bestCluster);
		this.cc.calculateClusterDistribution();
		
	}
	
	private void kmeans(int k, int[] clusters){
		
		double nodePositions[][] = this.cc.getCCPositions();
		
		double seedPositions[][] = new double[k][nodePositions[0].length];
		
		int[] clustersOld = copyClusters(clusters);
		
		clustersOld[0] = -1;

		
		// different initialize methods
		
		if(initMethod==0){
			initializeSeedpositions(seedPositions, nodePositions);	
		}else if(initMethod==1){
			initializeSeedpositionsInCenter(seedPositions, nodePositions);
		}else if(initMethod==2){
			initializeRandomSeedpositions(seedPositions, nodePositions);
		}else  if(initMethod==3){
			initializeFixedSeedpositions(seedPositions, nodePositions);
		}
		
		while(!isClusterEqual(clusters, clustersOld)){
			
			clustersOld = copyClusters(clusters);
			
			calculateClusters(seedPositions, clusters, nodePositions);	
			
			calculateNewSeedPositions(seedPositions, clusters, nodePositions);
			
		}
		
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
	
	private void initializeFixedSeedpositions(double[][] seedPositions, double nodePositions[][]){
		generateSortedList();
		seedPositions[0] = this.cc.getCCPostions(this.listOfElementsSortedByCosts[0]).clone();
		HashSet<Integer> already = new HashSet<Integer>();
		already.add(this.listOfElementsSortedByCosts[0]);
		for (int i = 1; i < seedPositions.length; i++) {
			int next = findFarestElement(already);
			already.add(next);
			seedPositions[i] = this.cc.getCCPostions(next).clone();
		}
	}
	
	private int findFarestElement(HashSet<Integer> already) {
		
		float bestcosts=Float.MAX_VALUE;
		int best = -1;
		
		for (int i = 0; i < cc.getNodeNumber(); i++) {
			if(already.contains(i)) continue;
			float costs=0;
			for (Integer j : already) {
				costs += cc.getCCEdges().getEdgeCost(i, j);
			}
			if(costs<bestcosts){
				bestcosts = costs;
				best =i;
			}
		}
		
		return best;
	}


	private void initializeSeedpositions(double[][] seedPositions,double[][] nodePositions){
		
		Hashtable<Integer, Boolean> h = new Hashtable<Integer, Boolean>();
		
		h.put(-1, true);
		
		Random r = new Random();
		
		for (int i = 0; i < seedPositions.length; i++) {
			
			int number = -1;
			
			while(h.containsKey(number)){
				number = r.nextInt(nodePositions.length);
			}
			
			h.put(number, true);
			
			seedPositions[i] = this.cc.getCCPostions(number).clone();
					
		}
		
	}
	
	private void initializeSeedpositionsInCenter(double[][] seedPositions,double[][] nodePositions){
		
		Random r = new Random();
		
		for (int i = 0; i < seedPositions.length; i++) {
			
			for (int j = 0; j < seedPositions[i].length; j++) {
				
				double epsilon = r.nextDouble()/this.span;
				
				epsilon = r.nextDouble();
				
				boolean sigma = r.nextBoolean();
				
				if(sigma){
					
					seedPositions[i][j] = this.center[j] + epsilon; 
					
				}else{
					
					seedPositions[i][j] = this.center[j] - epsilon; 
					
				}	
				
			}
					
		}
		
	}
	
	
	private void initializeRandomSeedpositions(double[][] seedPositions,double[][] nodePositions){
		
		Random r = new Random();
		
		for (int i = 0; i < seedPositions.length; i++) {
			
			for (int j = 0; j < seedPositions[i].length; j++) {
				
				double epsilon = r.nextDouble()*(span/2);
				
				epsilon = r.nextDouble();
				
				boolean sigma = r.nextBoolean();
				
				if(sigma){
					
					seedPositions[i][j] = this.center[j] + epsilon; 
					
				}else{
					
					seedPositions[i][j] = this.center[j] - epsilon; 
					
				}	
				
			}
					
		}
		
	}
	
	
	private double calculateCenterAndSpan(double[][] nodePositions,double[] center) {
		
		double span = 0;
		
		double[] min = new double[nodePositions[0].length];
		
		double[] max = new double[nodePositions[0].length];
		
		for (int i = 0; i < max.length; i++) {
			
			min[i] = Double.POSITIVE_INFINITY;
			max[i] = Double.NEGATIVE_INFINITY;
			
		}
		
		
		for (int i = 0; i < nodePositions.length; i++) {
			
			double[] position = nodePositions[i];
			
			for (int j = 0; j < position.length; j++) {
				
				if(position[j]<min[j]) min[j] = position[j];
				
				if(position[j]>max[j]) max[j] = position[j];
				
			}
			
		}
		
		for (int i = 0; i < center.length; i++) {
			
			center[i] = (min[i] + max[i])/2;
			
		}
		
		span = Math.sqrt(calculateEuclidianDistance(min, max));
				
		return span;
		
	}

	private void calculateClusters(double[][] seedPositions, int[] clusters, double[][] nodePositions){
		
		for (int i = 0; i < clusters.length; i++) {
			
			double[] position = nodePositions[i];
			
			int bestSeed = -1;
			
			double bestDistance = Double.MAX_VALUE;
			
			for (int j = 0; j < seedPositions.length; j++) {
				
				double[] seedPosition = seedPositions[j];
				
				double distance = calculateEuclidianDistance(position, seedPosition);

				if(distance<bestDistance){

					bestDistance = distance;
					bestSeed = j;
					
				}
				
				
			}//end for seedPositions
			
			clusters[i] = bestSeed;
			
		}//end for clusters
		
	}
	
	private void calculateNewSeedPositions(double[][] seedPositions, int[] clusters,double[][] nodePositions){
		
		int[] clusterSizes = new int[seedPositions.length];		
		
		for (int i = 0; i < seedPositions.length; i++) {
			seedPositions[i] = new double[nodePositions[0].length];
			for (int j = 0; j < seedPositions[i].length; j++) {
				seedPositions[i][j]=0;
			}
		}
		
		
		for (int i = 0; i < clusters.length; i++) {
			
			double position[] = nodePositions[i];
			
			clusterSizes[clusters[i]]++;
			
			seedPositions[clusters[i]] = positionAdd( position,  seedPositions[clusters[i]]);
			
		}
		
		for (int i = 0; i < seedPositions.length; i++) {
			seedPositions[i] = dividePosition(seedPositions[i],clusterSizes[i]);
		}
		
		
	}
	

	
	private double[] dividePosition(double[] position, int divisor) {
		
		double[] resultingPosition = new double[position.length];
		
		for (int i = 0; i < resultingPosition.length; i++) {
			resultingPosition[i] = position[i]/((double) divisor);
		}
		
		return resultingPosition;
	}

	private double[] positionAdd(double[] position1, double[] position2) {
		
		double[] resultingPosition = new double[position1.length];
		
		for (int i = 0; i < resultingPosition.length; i++) {
			resultingPosition[i] = position1[i] + position2[i];
		}
		
		return resultingPosition;
	}

	private boolean isClusterEqual(int[] clusters1, int[] clusters2){
		
		for (int i = 0; i < clusters2.length; i++) {
			if(clusters1[i]!=clusters2[i]){
				return false;
			}
		}
		
		return true;
		
	}
	
	private int[] copyClusters(int[] clusters){
		
		int[] clustersCopy = new int[clusters.length];
		
		for (int i = 0; i < clustersCopy.length; i++) {
			clustersCopy[i] = clusters[i];
		}
		
		return clustersCopy;
		
	}
	
	private double calculateEuclidianDistance(double[] node1, double[] node2){
		
		double euclidianDistance = 0;
		
		for (int i = 0; i < node1.length; i++) {
			euclidianDistance += ((node1[i]-node2[i])*(node1[i]-node2[i])); 		
		}
		
		return euclidianDistance;
	}
}

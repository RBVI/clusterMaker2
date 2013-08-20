package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class SingleLinkageClusterer implements IGeometricClusterer{

	private double minDistance;
	
	private double maxDistance;
	
	private double stepsize;
	
	private double stepsizeFactor;
	
	private int noOfClusters;
	
	private double bestDistance;
	
	private ConnectedComponent cc;
	
	private float[][] distances;
	
	private float sortedDistances[];
	
	private ExecutorService es;
	
	/**
	 * Creates instance of SingleLinkageClusterer with no parameters. This
	 * still needs to be initialised.
	 *
	 */
	public SingleLinkageClusterer(){}
	
	
	public void initGeometricClusterer(ConnectedComponent cc){
		if(TaskConfig.useThreads){
			es = java.util.concurrent.Executors.newFixedThreadPool(TaskConfig.maxNoThreads);	
		}else{
			es = java.util.concurrent.Executors.newFixedThreadPool(1);
		}
		this.cc = cc;
		this.minDistance = GeometricClusteringConfig.minDistance;
		this.maxDistance = GeometricClusteringConfig.maxDistance;
		this.stepsize = GeometricClusteringConfig.stepsize;
		this.stepsizeFactor = GeometricClusteringConfig.stepsizeFactor;
		this.distances = new float[this.cc.getNodeNumber()][this.cc.getNodeNumber()];
		this.sortedDistances = new float[(((this.cc.getNodeNumber()-1)*this.cc.getNodeNumber())/2)+1];
		int k =0;
		this.sortedDistances[k] = -1;
		k++;
		for (int i = 0; i < distances.length; i++) {
			for (int j = i+1; j < distances.length; j++) {
				distances[i][j] = distances[j][i] = calculateEuclidianDistance(this.cc.getCCPostions(i), this.cc.getCCPostions(j));
				this.sortedDistances[k] = distances[i][j];
				k++;
			}
		}
		Arrays.sort(this.sortedDistances);
	}
	
	public void run3(){
		long time = System.currentTimeMillis();
		double bestScore = Double.MAX_VALUE;
		int bestClustersArray[] = new int[this.cc.getNodeNumber()];
		int clustersArray[] = new int[this.cc.getNodeNumber()];
		int bestClusterNr = -1;
		float distOld[][] = this.distances;
		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < distOld.length; i++) {
			ArrayList<Integer> cluster = new ArrayList<Integer>();
			cluster.add(i);
			clusters.add(cluster);
		}
		for (int j = 0; j < clusters.size(); j++) {
			ArrayList<Integer> cluster = clusters.get(j);
			for (Integer integer : cluster) {
				clustersArray[integer] = j;
			}
		}
		
		double score = this.cc.calculateClusteringScore(clustersArray);
		if(score<bestScore){
			bestScore = score;
			bestClustersArray = clustersArray.clone();
			bestClusterNr = (cc.getNodeNumber());
		}
		for (int i = cc.getNodeNumber()-1; i>0 ; i--) {
			
			float dist2[][] = new float[i][i];
			float min = Float.MAX_VALUE;
			int minj =-1;
			int mink = -1;
			for (int j = 0; j < distOld.length; j++) {
				for (int k = j+1; k < distOld.length; k++) {
					if(distOld[j][k]<min){
						min=distOld[j][k];
						minj = j;
						mink = k;
					}
				}
			}
			
			clusters.get(minj).addAll(clusters.get(mink));
			clusters.remove(mink);
			for (int j = 0; j < clusters.size(); j++) {
				ArrayList<Integer> cluster = clusters.get(j);
				for (Integer integer : cluster) {
					clustersArray[integer] = j;
				}
			}
			
			score = this.cc.calculateClusteringScore(clustersArray);
			if(score<bestScore){
				bestScore = score;
				bestClustersArray = clustersArray.clone();
				bestClusterNr = i;
			}
			
			int mappingOld2New[] = new int[distOld.length];
			for (int l = 0, m = 0; l < distOld.length; l++) {
				if ( l == mink)
					continue;
				mappingOld2New[l] = m;
				m++;
			}
			
			for (int j = 0; j < mappingOld2New.length; j++) {
				if ( j == mink)continue;
				for (int k = j + 1; k < mappingOld2New.length; k++) {
					if (k == mink)continue;
					if(j==minj){
						dist2[mappingOld2New[j]][mappingOld2New[k]]=dist2[mappingOld2New[k]][mappingOld2New[j]]=Math.min(distOld[minj][k], distOld[mink][k]);
					}else{
						dist2[mappingOld2New[j]][mappingOld2New[k]]=dist2[mappingOld2New[k]][mappingOld2New[j]]=distOld[j][k];
					}
				}
			}
			distOld = dist2.clone();
		}
		
		this.cc.initialiseClusterInfo(bestClusterNr);
		this.cc.setClusteringScore(bestScore);
		this.cc.setClusters(bestClustersArray);
		this.cc.calculateClusterDistribution();
		System.out.println("time for single linkage " + (System.currentTimeMillis()-time));
	}
	
	public void run(){
		try {
			runForSortedArray(0,this.sortedDistances.length);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] clusters = new int[this.cc.getNodeNumber()];
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		int clusterNr = calculateClusters(bestDistance,clusters,already);
		this.cc.initialiseClusterInfo(clusterNr);
		this.cc.setClusteringScore(this.cc.calculateClusteringScore(clusters));
		this.cc.setClusters(clusters);
		this.cc.calculateClusterDistribution();
	}
	
	public void runForSortedArray(int a, int b) throws InterruptedException, ExecutionException{
		ArrayList<CalculateClustersTask> test = new ArrayList<CalculateClustersTask>();
		if((b-a)<20){
			if(TaskConfig.useThreads){
				es = java.util.concurrent.Executors.newFixedThreadPool(TaskConfig.maxNoThreads);	
			}else{
				es = java.util.concurrent.Executors.newFixedThreadPool(1);
			}
			double bestScore = Double.MAX_VALUE;
			double bestScoreParallel = Double.MAX_VALUE;
			double bestDistanceParallel = -1;
			for (int i = a; i <b; i++) {
//				boolean already[] = new boolean[this.cc.getNodeNumber()];
//				int clusters[] = new int[this.cc.getNodeNumber()];
				float distance =this.sortedDistances[i];
//				calculateClusters(distance,clusters,already);
				CalculateClustersTask cct = new CalculateClustersTask(distance, distances, cc);
				test.add(cct);
				es.execute(cct);
//				double score = this.cc.calculateClusteringScore(clusters);
//				if(score<bestScore){
//					bestScore = score;
//					bestDistance = distance;
//				}
			}
			es.shutdown(); 
			try {
				es.awaitTermination(5, java.util.concurrent.TimeUnit.HOURS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (CalculateClustersTask calculateClustersTask : test) {
				if(calculateClustersTask.score<bestScore){
					bestScore=calculateClustersTask.score;
					bestDistance = calculateClustersTask.distance;
				}
			}
			
			
		}else{
			int step = (int) Math.floor(((double) (b-a))/20);
			int bestStep = -1;
			double bestScore = Double.MAX_VALUE;
			int[] clusters = new int[this.cc.getNodeNumber()];
			for (int i = 0, j = a; i < 20; i++, j+=step) {
//				boolean already[] = new boolean[this.cc.getNodeNumber()];
				float distance =this.sortedDistances[j];
				CalculateClustersTask cct = new CalculateClustersTask(distance, distances, cc);
				test.add(cct);
				es.execute(cct);
//				calculateClusters(distance,clusters,already);
//				double score = this.cc.calculateClusteringScore(clusters);
//				if(score<bestScore){
//					bestScore = score;
//					bestStep = j;
//				}
			}
			es.shutdown(); 
			try {
				es.awaitTermination(5, java.util.concurrent.TimeUnit.HOURS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (CalculateClustersTask calculateClustersTask : test) {
				if(calculateClustersTask.score<bestScore){
					bestScore=calculateClustersTask.score;
					bestStep = Arrays.binarySearch(this.sortedDistances, (float) calculateClustersTask.distance);
				}
			}
			if(TaskConfig.useThreads){
				es = java.util.concurrent.Executors.newFixedThreadPool(TaskConfig.maxNoThreads);	
			}else{
				es = java.util.concurrent.Executors.newFixedThreadPool(1);
			}
			if(bestStep==0){
				runForSortedArray( 0,(2*step));
			}else if(bestStep==(19*step)){
				runForSortedArray( (18*step),(b-a));
			}else{
				runForSortedArray(Math.max(0, bestStep-step),bestStep+step);
			}
			
			
		}
		
		
	}
	
	
	public void run4(){
		long time = System.currentTimeMillis();
		
		bestDistance = 0;
		int[] clusters = new int[this.cc.getNodeNumber()];
		for (int i = 0; i < clusters.length; i++) {
			clusters[i] = i;
		}
		double bestScore = this.cc.calculateClusteringScore(clusters);
		double count = 0;
		for (int i = 0; i < this.sortedDistances.length; i++) {
//			if(clusters[this.distance2sources.get(this.sortedDistances[i])]==clusters[this.distance2targets.get(this.sortedDistances[i])]) continue;
			boolean already[] = new boolean[this.cc.getNodeNumber()];
			double distance = this.sortedDistances[i];
			calculateClusters(distance,clusters,already);
			double score = this.cc.calculateClusteringScore(clusters);
			if(score<bestScore){
				bestScore = score;
				bestDistance = distance;
			}
		}
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		double distance = bestDistance;
		this.noOfClusters = calculateClusters(distance,clusters,already);
		this.cc.initialiseClusterInfo(this.noOfClusters);
		this.cc.setClusteringScore(bestScore);
		this.cc.setClusters(clusters);
		this.cc.calculateClusterDistribution();
		System.out.println("time for single linkage " + (System.currentTimeMillis()-time));
	}
	
	
	
	
	
	private int calculateClusters(double distance, int[] clusters,
			boolean[] already) {
		
		int clusterNr = 0;
		for (int i = 0; i < clusters.length; i++) {
			if(already[i]) continue;
			clusters[i] = clusterNr;
			already[i] = true;
			assignRecursivly(clusterNr,already,distance,i,clusters);
			clusterNr++;
		}
		
		return clusterNr;
	}


	private void assignRecursivly(int clusterNr,
			boolean[] already, double distance, int seed, int[] clusters) {
		

		
		for (int i = 0; i < already.length; i++) {
			if(already[i]) continue;
			if(distances[i][seed]<=distance){
				clusters[i] = clusterNr;
				already[i] = true;
				assignRecursivly(clusterNr, already, distance, i,clusters);
			}
		}
	}


	/**
	 * This method determines the best clustering obtained from geometric single linkage clustering
	 * within a range between minDistance and maxDistance
	 */
	@SuppressWarnings("unchecked")
	public void run2(){
		try{
			long time = System.currentTimeMillis();
			double bestScore = Double.MAX_VALUE;
			bestDistance = 0;
			int[] clusters = new int[this.cc.getNodeNumber()];
			double currentDistance = this.minDistance;
			
			Vector<Integer> putativeNeighbors[] = new Vector[clusters.length];
			Vector<Integer> putativeNeighbors_orig[] = new Vector[clusters.length];
			
			for (int i = 0; i < putativeNeighbors.length; i++) {
				putativeNeighbors[i] = new Vector<Integer>();
				putativeNeighbors_orig[i] = new Vector<Integer>();
			}
			
			for (int i = 0; i < clusters.length; i++) {	
				for (int j = i+1; j < clusters.length; j++) {
					double distance = distances[i][j];
					if(distance<(this.maxDistance*this.maxDistance)){
						putativeNeighbors[i].add(j);
						putativeNeighbors[j].add(i);
						putativeNeighbors_orig[i].add(j);
						putativeNeighbors_orig[j].add(i);
					}
				}
			}
			
			Vector<Double> distances = new Vector<Double>();
			while(currentDistance<this.maxDistance){
				distances.add(currentDistance);
				currentDistance +=this.stepsize;
				this.stepsize+=(this.stepsizeFactor*this.stepsize);
			}
			distances.add(Double.MAX_VALUE);
			
			
			// start with minDistance and increase upto maxDistance. Save distance which produces minimal costs 
			for (int i = distances.size()-1; i >= 0; i--) {
				currentDistance = distances.get(i);
			
				double currentScore = 0;
				calculateClusters((currentDistance*currentDistance), clusters,putativeNeighbors);
				currentScore = this.cc.calculateClusteringScore(clusters);
				if(currentScore<=bestScore){
					bestScore = currentScore;
					bestDistance = currentDistance;
				}
			
			}
			this.noOfClusters = calculateClusters((bestDistance*bestDistance), clusters,putativeNeighbors_orig);
			this.cc.initialiseClusterInfo(this.noOfClusters);
			this.cc.setClusteringScore(bestScore);
			this.cc.setClusters(clusters);
			this.cc.calculateClusterDistribution();
			time = System.currentTimeMillis() - time;
//		System.out.println("Time for geometric clustering: "+TaskUtility.convertTime(time));
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * This method determines the best clustering obtained from geometric single linkage clustering
	 * for a fixed distance
	 * @param distance The maximal distance between two nodes to be assigned to one cluster
	 * @param clusters Array of integers, where position equals a proteinNumber and value is the assigned clusternumber
	 * @return number of clusters
	 */
	private int calculateClusters(double distance, int[] clusters, Vector<Integer>[] putativeNeighbors) throws Exception{
		
		int clusterNumber = 0;

		boolean[] remaining2 = new boolean[clusters.length];
		
		
		for (int i = 0; i < remaining2.length; i++) {
			if(remaining2[i]) continue;
			recursiveClusterCalculate(i, remaining2, clusterNumber, clusters, distance,putativeNeighbors);
			clusterNumber++;
		}
		return clusterNumber;
	}
	
	
	
	
	
	
	
	
	
	
	private void recursiveClusterCalculate(int seed, boolean[] remaining2, int currentClusterNumber, int[] clusters, double distance, Vector<Integer>[] putativeNeighbors) {
		
		if(remaining2[seed]) return;
		
		clusters[seed] = currentClusterNumber;
		
		remaining2[seed] = true;
		
		Vector<Integer> v = putativeNeighbors[seed];
		
		for (int i = 0; i < v.size(); i++) {
			int node = v.get(i);
			if(remaining2[node]) continue;
			
			if(distances[seed][node]<distance){
			
				recursiveClusterCalculate(node, remaining2, currentClusterNumber, clusters, distance, putativeNeighbors);
				
			}else{
				v.remove(i);
				putativeNeighbors[i].removeElement(seed);
				i--;
			}		
		}
	}


	/**
	 * This method simply calculates the euclidian distance of two arrays. 
	 * @param node1 positionarray of first node
	 * @param node2 positionarray of second node
	 */
	private float calculateEuclidianDistance(double[] node1, double[] node2){
				
		float euclidianDistance = 0;
		
		for (int i = 0; i < node1.length; i++) {
			euclidianDistance += ((node1[i]-node2[i])*(node1[i]-node2[i])); 		
		}
		euclidianDistance = (float) Math.sqrt(euclidianDistance);
		
		return euclidianDistance;
	}

	public double getBestDistance() {
		return bestDistance;
	}

	public void setBestDistance(double bestDistance) {
		this.bestDistance = bestDistance;
	}
	
	
}

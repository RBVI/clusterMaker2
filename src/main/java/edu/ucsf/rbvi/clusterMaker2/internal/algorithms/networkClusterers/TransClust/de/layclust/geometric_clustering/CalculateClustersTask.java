package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

public class CalculateClustersTask implements Runnable{
	private float[][] distances;
	private ConnectedComponent cc;
	public double distance;
	public double score;
	public CalculateClustersTask(double distance, float[][] distances,ConnectedComponent cc) {
		this.distances = distances;
		this.cc=cc;
		this.distance = distance;
	}
	

	public void run() {
		boolean already[] = new boolean[this.cc.getNodeNumber()];
		int[] clusters = new int[this.cc.getNodeNumber()];
		int clusterNr = 0;
		for (int i = 0; i < clusters.length; i++) {
			if(already[i]) continue;
			clusters[i] = clusterNr;
			already[i] = true;
			assignRecursivly(clusterNr,already,distance,i,clusters);
			clusterNr++;
		}
		this.score= this.cc.calculateClusteringScore(clusters);
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

}

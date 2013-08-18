package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class FixedParameterClusterer2 {
	private ConnectedComponent cc;
	private double maxK;
	private long startTime;
	private float graph[][];
	private double costs;
	private double solutionCost;
	private float solution[][];
	private long treesize;
	private int depth;
	
	public FixedParameterClusterer2(ConnectedComponent cc, double maxK){
		this.cc = cc;
		this.maxK = maxK/2;
		this.startTime = System.currentTimeMillis();
		this.graph = new float[this.cc.getNodeNumber()][this.cc.getNodeNumber()];
		this.solutionCost = -1;
		this.costs = this.cc.getClusteringScore();
		this.costs=0;
		this.depth = 0;
		if(costs==-1) costs=0;
		this.treesize = 0;
		for (int i = 0; i < this.cc.getNodeNumber(); i++) {
			for (int j = i+1; j < this.cc.getNodeNumber(); j++) {
				this.graph[i][j] = this.graph[j][i]=this.cc.getCCEdges().getEdgeCost(i, j);
			}
		}
		run();
	}
	
	private void run(){
		
		findNextEdge();
		while(this.solutionCost<0){
			if(System.currentTimeMillis()-startTime>TaskConfig.fpMaxTimeMillis){
				TaskConfig.fpStopped = true;
				return;
			}
			cluster();
			this.maxK += maxK/10;
		}
		
		buildSolution();
		
		TaskConfig.fpStopped = true;
		
	}

	private void buildSolution() {
		
		
		float edges[][] = solution;

		int[] nodes2clustersReduced = new int[solution.length];
		int clusterNr = 0;
		boolean[] already = new boolean[this.cc.getNodeNumber()];

		for (int i = 0; i < edges.length; i++) {
			if (already[i])
				continue;
			nodes2clustersReduced[i] = clusterNr;
			already[i] = true;
			assingCluster(nodes2clustersReduced, clusterNr, i, already, edges);
			clusterNr++;
		}

		this.cc.initialiseClusterInfo(clusterNr);
		this.cc.setClusteringScore(this.cc
				.calculateClusteringScore(nodes2clustersReduced));
		this.cc.setClusters(nodes2clustersReduced);
		this.cc.calculateClusterDistribution();
		
		
	}
	public void assingCluster(int[] nodes2clusters, int clusterNr, int node_i,
			boolean[] already, float[][] edges) {

		for (int i = 0; i < edges.length; i++) {
			if (already[i])
				continue;
			if (edges[node_i][i] > 0) {
				nodes2clusters[i] = clusterNr;
				already[i] = true;
				assingCluster(nodes2clusters, clusterNr, i, already, edges);
			}
		}
	}
	private void cluster() {
		treesize++;
		if(System.currentTimeMillis()-startTime>TaskConfig.fpMaxTimeMillis){
			TaskConfig.fpStopped = true;
			return;
		}
		reductionicf();
		int[] edge = findNextEdge();
		if (edge == null) {
			maxK = costs;
			solutionCost = costs;
			solution = graph.clone();
//			solution = new int[3]; //TODO
			return;
		}
		// branch 1 (merge)
		float costsForMerging = calculateCostsForMerging(edge[0], edge[1]);
		if (costsForMerging + costs <= maxK) {
			float oldvalue = graph[edge[0]][edge[1]]; 
			if(graph[edge[0]][edge[1]]<0){
				costs-=oldvalue;
			}
			float[][] graphCopy = graph.clone();
			graph[edge[0]][edge[1]]=graph[edge[1]][edge[0]] = Float.POSITIVE_INFINITY;
			float dum = 0;
			for (int i = 0; i < graph.length; i++) {
				if(i==edge[0]||i==edge[1]) continue;
				if(graph[edge[0]][i]==Float.POSITIVE_INFINITY&&graph[edge[1]][i]!=Float.POSITIVE_INFINITY){
					if(graph[edge[1]][i]<0){
						dum-=graph[edge[1]][i];
					}
					graph[edge[1]][i] = Float.POSITIVE_INFINITY;
				}else if(graph[edge[1]][i]==Float.POSITIVE_INFINITY&&graph[edge[0]][i]!=Float.POSITIVE_INFINITY){
					if(graph[edge[0]][i]<0){
						dum-=graph[edge[0]][i];
					}
					graph[edge[0]][i] = Float.POSITIVE_INFINITY;
				}
			}
			if(dum<Float.POSITIVE_INFINITY){
				costs+=dum;
				depth++;
				cluster();
				depth--;
				costs-=dum;
			}else{
			}
			
			graph = graphCopy;
			graph[edge[0]][edge[1]]=graph[edge[1]][edge[0]] = oldvalue;
			if(oldvalue<0){
				costs+=oldvalue;
			}
			
		}
		// branch 2 (forbidden)
		float costsForSetForbidden = calculateCostsForSetForbidden(edge[0], edge[1]);
		if (costs + costsForSetForbidden <= maxK) {
			float oldvalue = graph[edge[0]][edge[1]]; 
			if(graph[edge[0]][edge[1]]>0){
				costs+=oldvalue;
			}
			graph[edge[0]][edge[1]]=graph[edge[1]][edge[0]]=Float.NEGATIVE_INFINITY;
			depth++;
			cluster();
			depth--;
			graph[edge[0]][edge[1]]=graph[edge[1]][edge[0]] = oldvalue;
			if(oldvalue>0){
				costs-=oldvalue;
			}
		}
		
	}

	private void reductionicf() {
		// TODO Auto-generated method stub
		
	}

	public int[] findNextEdge() {
		int[] bestEdge = new int[2];
//		for (int i = 0; i < graph.length; i++) {
//			for (int j = i+1; j < graph.length; j++) {
//				for (int k = j+1; k < graph.length; k++) {
//					int sum = 0;
//					if(graph[i][j]>0) sum++;
//					if(graph[i][k]>0) sum++;
//					if(graph[k][j]>0) sum++;
//					if(sum==2){
//						if(graph[i][j]!=Float.POSITIVE_INFINITY&&graph[i][j]!=Float.NEGATIVE_INFINITY){
//							bestEdge[0]=i;
//							bestEdge[1]=j;
//							return bestEdge;
//						}else if(graph[k][j]!=Float.POSITIVE_INFINITY&&graph[k][j]!=Float.NEGATIVE_INFINITY){
//							bestEdge[0]=k;
//							bestEdge[1]=j;
//							return bestEdge;
//						}if(graph[i][k]!=Float.POSITIVE_INFINITY&&graph[i][k]!=Float.NEGATIVE_INFINITY){
//							bestEdge[0]=i;
//							bestEdge[1]=k;
//							return bestEdge;
//						}else{
////							System.out.println(graph[i][j] + "\t" + graph[k][j] + "\t" + graph[i][k]);
////							System.out.println(visited[i][j] + "\t" + visited[k][j] + "\t" + visited[i][k]);
////							System.out.println();
//						}
//					}
//				}
//			}
//		}
//		return null;
		
		float[][] numberOfOccurencesInConflictTriples = new float[graph.length][graph.length];
		for (int i = 0; i < graph.length; i++) {
			for (int j = i + 1; j < graph.length; j++) {
				if(graph[i][j]==Float.POSITIVE_INFINITY||graph[i][j] ==Float.NEGATIVE_INFINITY) continue;
				if (graph[i][j]>0) {
					numberOfOccurencesInConflictTriples[i][j] = numberOfOccurencesInConflictTriples[j][i] = Math
							.abs(calculateCostsForMerging(i, j)
									- calculateCostsForSetForbidden(i, j));
				}
			}
		}
		
		float highestOccurence = 0;
		for (int i = 0; i < graph.length; i++) {
			for (int j = i + 1; j < graph.length; j++) {
				if (numberOfOccurencesInConflictTriples[i][j] > highestOccurence) {
					highestOccurence = numberOfOccurencesInConflictTriples[i][j];
					bestEdge[0] = i;
					bestEdge[1] = j;
				}
			}
		}
		if (highestOccurence == 0)
			return null;

		return bestEdge;
	}

	private float calculateCostsForMerging(int node_i, int node_j) {
		float costsForMerging = 0;
		
		for (int i = 0; i < graph.length; i++) {
			if (i == node_i || i == node_j)
				continue;
			if((graph[i][node_i]>0&&graph[i][node_j]>0)||(graph[i][node_i]<=0&&graph[i][node_j]<=0)) continue;
			costsForMerging += Math.min(Math
					.abs(graph[i][node_i]), Math.abs(graph[i][node_j]));
		}

		return costsForMerging;
	}

	private float calculateCostsForSetForbidden(int node_i, int node_j) {
		float costs = 0;
		for (int i = 0; i < graph.length; i++) {
			if(graph[i][node_i]>0&&graph[i][node_j]>0) costs+=Math.min(graph[i][node_i], graph[i][node_j]);
		}

		costs += graph[node_i][node_j];
		return costs;
	}
}

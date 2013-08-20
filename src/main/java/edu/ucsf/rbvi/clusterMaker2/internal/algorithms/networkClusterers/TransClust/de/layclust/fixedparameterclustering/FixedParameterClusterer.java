package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class FixedParameterClusterer {

	private ConnectedComponent cc;
	private double maxK;
	private FixedParameterTreeNode solution;
	private long startTime;
	
	

	public FixedParameterClusterer(ConnectedComponent cc) {
		this.cc = cc;
		this.maxK = 0;
		startTime = System.currentTimeMillis();
		while (solution == null) {
			if(System.currentTimeMillis()-startTime>TaskConfig.fpMaxTimeMillis){
				TaskConfig.fpStopped = true;
				return;
			}
			FixedParameterTreeNode fptn = initFirstTreeNode();
			cluster(fptn);
			this.maxK += 10;
		}
		buildClusters(solution);
	}

	public FixedParameterClusterer(ConnectedComponent cc, double maxK) {
		this.cc = cc;
		this.maxK = maxK/2;
		startTime = System.currentTimeMillis();
		while (solution == null) {
			if(System.currentTimeMillis()-startTime>TaskConfig.fpMaxTimeMillis){
				TaskConfig.fpStopped = true;
				return;
			}
			FixedParameterTreeNode fptn = initFirstTreeNode();
			cluster(fptn);
			this.maxK += maxK/10;
		}
		buildClusters(solution);
//		System.out.println("mergeCount " + mergeCount + " setForbiddenCount " + setForbiddenCount + " calculateMergeCount " + calculateMergeCostCount + " calculateForbiddenCount " + calculateForbiddenCostCount );
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

	public void buildClusters(FixedParameterTreeNode solution) {

		float edges[][] = solution.edgeCosts;

		int[] nodes2clustersReduced = new int[solution.size];
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

		int[] nodes2clusters = new int[this.cc.getNodeNumber()];
		for (int i = 0; i < nodes2clustersReduced.length; i++) {
			for (int j = 0; j < solution.clusters[i].length; j++) {
				if (solution.clusters[i][j])
					nodes2clusters[j] = nodes2clustersReduced[i];
			}
		}

		this.cc.initialiseClusterInfo(clusterNr);
		this.cc.setClusteringScore(this.cc
				.calculateClusteringScore(nodes2clusters));
		this.cc.setClusters(nodes2clusters);
		this.cc.calculateClusterDistribution();
//		System.out.println(cc.getNodeNumber() + "\t" + cc.getClusteringScore() + "\t" + treesize + "\t" + mergetime);
	}

	public float calculateCostsForMerging(FixedParameterTreeNode fptn,
			int node_i, int node_j) {
//		long dum = System.currentTimeMillis();
		float costsForMerging = 0;
		
		for (int i = 0; i < fptn.size; i++) {
                    if(i==node_i) continue;
                    else if(i==node_j) continue;
                    else if((fptn.edgeCosts[i][node_i] > 0 && fptn.edgeCosts[i][node_j] <= 0)) costsForMerging+=Math.min(fptn.edgeCosts[i][node_i],-fptn.edgeCosts[i][node_j]);
                    else if((fptn.edgeCosts[i][node_i] <= 0 && fptn
							.edgeCosts[i][node_j] > 0))costsForMerging+=Math.min(-fptn.edgeCosts[i][node_i],fptn.edgeCosts[i][node_j]);
		}
		return costsForMerging;
	}

	public float calculateCostsForSetForbidden(FixedParameterTreeNode fptn,
			int node_i, int node_j) {
		float costs = 0;

		for (int i = 0; i < fptn.size; i++) {
			if (fptn.edgeCosts[node_i][i] > 0
					&& fptn.edgeCosts[node_j][i] > 0) {
				costs += Math.min(fptn.edgeCosts[node_i][i], fptn
						.edgeCosts[node_j][i]);
			}
		}

		costs += fptn.edgeCosts[node_i][node_j];
		return costs;
	}

	public void cluster(FixedParameterTreeNode fptn) {
//		          System.out.println("fptn.costs = " + fptn.costs);
		if(System.currentTimeMillis()-startTime>TaskConfig.fpMaxTimeMillis){
			TaskConfig.fpStopped = true;
			return;
		}
//		fptn = reductionicf(fptn);
//		long dum = System.currentTimeMillis();
		int[] edge = findNextConflictTriple2(fptn);
//		mergetime+=(System.currentTimeMillis()-dum);
		if (edge == null) {
			maxK = fptn.costs;
			solution = fptn.copy();
			return;
		}

                // branch 2 (forbidden)
		float costsForSetForbidden = calculateCostsForSetForbidden(fptn,
				edge[0], edge[1]);
		if (fptn.costs+ costsForSetForbidden <= maxK) {
			setForbiddenAndCluster(fptn, edge[0], edge[1],
					fptn.edgeCosts[edge[0]][edge[1]]);
		}

		// branch 1 (merge)
		float costsForMerging = calculateCostsForMerging(fptn, edge[0], edge[1]);
		if (costsForMerging + fptn.costs <= maxK) {
			FixedParameterTreeNode fptn2 = mergeNodes(fptn, edge[0], edge[1],
					costsForMerging);
			cluster(fptn2);
		}
		
	}

	public int[] findNextConflictTriple2(FixedParameterTreeNode fptn) {
		int[] bestEdge = new int[2];
		float highestOccurence = 0;
		float occurence = 0;
//		float[][] numberOfOccurencesInConflictTriples = new float[fptn.size][fptn.size];
		for (int i = 0; i < fptn.size; i++) {
			for (int j = i + 1; j < fptn.size; j++) {
				
				if (fptn.edgeCosts[i][j] > 0) {
//                                    bestEdge[0] = i;
//						bestEdge[1] = j;
//                                                return bestEdge;
					occurence = Math.abs(calculateCostsForMerging(fptn, i, j) - calculateCostsForSetForbidden(fptn, i, j));
					if (occurence > highestOccurence) {
						highestOccurence = occurence;
						bestEdge[0] = i;
						bestEdge[1] = j;
//                                                return bestEdge;
					}
				}
				
			}
		}
		
//		for (int i = 0; i < fptn.size; i++) {
//			for (int j = i + 1; j < fptn.size; j++) {
//				if (numberOfOccurencesInConflictTriples[i][j] > highestOccurence) {
//					highestOccurence = numberOfOccurencesInConflictTriples[i][j];
//					bestEdge[0] = i;
//					bestEdge[1] = j;
//				}
//			}
//		}
		if (highestOccurence == 0)
			return null;

		return bestEdge;
	}
	
	
	public int[] findNextConflictTriple3(FixedParameterTreeNode fptn) {

		float[][] numberOfOccurencesInConflictTriples = new float[fptn.size][fptn.size];
		
		
		for (int i = 0; i < fptn.size; i++) {
			for (int j = i + 1; j < fptn.size; j++) {
				for (int k = j+1; k < fptn.size; k++) {
					if(fptn.edgeCosts[i][j]+fptn.edgeCosts[i][k]+fptn.edgeCosts[k][j]==2){
						numberOfOccurencesInConflictTriples[i][j]++;
						numberOfOccurencesInConflictTriples[i][k]++;
						numberOfOccurencesInConflictTriples[j][k]++;
					}
				}
			}
		}
		
		for (int i = 0; i < fptn.size; i++) {
			for (int j = i + 1; j < fptn.size; j++) {
				if (fptn.edgeCosts[i][j] > 0) {
					numberOfOccurencesInConflictTriples[i][j] = numberOfOccurencesInConflictTriples[j][i] = Math
							.abs(calculateCostsForMerging(fptn, i, j)
									- calculateCostsForSetForbidden(fptn, i, j));
				}
			}
		}
		int[] bestEdge = new int[2];
		float highestOccurence = 0;
		for (int i = 0; i < fptn.size; i++) {
			for (int j = i + 1; j < fptn.size; j++) {
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

	public FixedParameterTreeNode initFirstTreeNode() {

		FixedParameterTreeNode fptn = new FixedParameterTreeNode(this.cc
				.getNodeNumber(), 0, this.cc.getNodeNumber());

		for (int i = 0; i < fptn.size; i++) {
			fptn.clusters[i][i] = true;
			for (int j = i + 1; j < fptn.size; j++) {
				fptn.edgeCosts[i][j] = fptn.edgeCosts[j][i] = this.cc
						.getCCEdges().getEdgeCost(i, j);
			}
		}
		fptn = reductionicf(fptn);
		return fptn;
	}

	public FixedParameterTreeNode mergeNodes(FixedParameterTreeNode fptn,
			int node_i, int node_j, float costsForMerging) {

		FixedParameterTreeNode fptnNew = new FixedParameterTreeNode(
				fptn.size - 1, fptn.costs, this.cc.getNodeNumber());
		fptnNew.costs = (fptn.costs + costsForMerging);

		int mappingOld2New[] = new int[fptn.size];
		for (int i = 0, j = 0; i < fptn.size; i++) {
			if (i == node_i || i == node_j)
				continue;
			mappingOld2New[i] = j;
			fptnNew.clusters[j] = fptn.clusters[i];
			j++;
		}

		for (int i = 0; i < mappingOld2New.length; i++) {
			if (i == node_i || i == node_j)
				continue;
			for (int j = i + 1; j < mappingOld2New.length; j++) {
				if (j == node_i || j == node_j)
					continue;
				fptnNew.edgeCosts[mappingOld2New[i]][mappingOld2New[j]] = fptnNew
						.edgeCosts[mappingOld2New[j]][mappingOld2New[i]] = fptn
						.edgeCosts[i][j];
			}
		}

		for (int i = 0; i < this.cc.getNodeNumber(); i++) {
			fptnNew.clusters[fptnNew.size - 1][i] = (fptn.clusters[node_i][i] || fptn
					.clusters[node_j][i]);
		}

		for (int i = 0; i < fptn.size; i++) {
			if (i == node_i || i == node_j)
				continue;
			fptnNew.edgeCosts[mappingOld2New[i]][fptnNew.size - 1] = fptnNew
					.edgeCosts[fptnNew.size - 1][mappingOld2New[i]] = fptn
					.edgeCosts[i][node_i]
					+ fptn.edgeCosts[i][node_j];
		}
		return fptnNew;
	}

	public FixedParameterTreeNode reductionicf(FixedParameterTreeNode fptnNew) {

		if (fptnNew.costs > maxK) {
			return fptnNew;
		}

		for (int i = 0; i < fptnNew.size; i++) {
			for (int j = i + 1; j < fptnNew.size; j++) {
				if (fptnNew.edgeCosts[i][j] <= 0)
					continue;
				float sumIcf = calculateCostsForSetForbidden(fptnNew, i, j);
				float sumIcp = calculateCostsForMerging(fptnNew, i, j);
				if (sumIcf + fptnNew.costs > maxK
						&& sumIcp + fptnNew.costs > maxK) {
					fptnNew.costs = (Float.POSITIVE_INFINITY);
					return fptnNew;
				} else if (sumIcf + fptnNew.costs > maxK) {
					float costsForMerging = calculateCostsForMerging(fptnNew,
							i, j);
					FixedParameterTreeNode fptnNew2 = mergeNodes(fptnNew, i, j,
							costsForMerging);
					fptnNew2 = reductionicf(fptnNew2);
					return fptnNew2;
				} else if (sumIcp + fptnNew.costs > maxK) {
					fptnNew.costs = (fptnNew.costs
							+ fptnNew.edgeCosts[i][j]);
					fptnNew.edgeCosts[i][j] = fptnNew.edgeCosts[j][i] = Float.NEGATIVE_INFINITY;
					fptnNew = reductionicf(fptnNew);
					return fptnNew;
				}
			}
		}
		return fptnNew;
	}

	public void setForbiddenAndCluster(FixedParameterTreeNode fptn,
			int node_i, int node_j, float costsForSetForbidden) {
		fptn.costs = (fptn.costs + fptn.edgeCosts[node_i][node_j]);
		fptn.edgeCosts[node_i][node_j] = fptn.edgeCosts[node_j][node_i] = Float.NEGATIVE_INFINITY;
		cluster(fptn);
		fptn.costs = (fptn.costs - costsForSetForbidden);
		fptn.edgeCosts[node_i][node_j] = fptn.edgeCosts[node_j][node_i] = costsForSetForbidden;
	}

}

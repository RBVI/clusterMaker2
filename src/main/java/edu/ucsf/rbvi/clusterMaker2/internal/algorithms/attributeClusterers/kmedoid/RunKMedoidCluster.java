/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmedoid;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

// Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.TaskMonitor;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;

public class RunKMedoidCluster extends AbstractKClusterAlgorithm {
	KMedoidContext context;

	public RunKMedoidCluster(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
	                   TaskMonitor monitor, KMedoidContext context, AbstractClusterAlgorithm parentTask) {
		super(network, weightAttributes, metric, monitor, parentTask);
		this.context = context;
	}

	// The kmeans implementation of a k-clusterer
	public int kcluster(int nClusters, int nIterations, CyMatrix matrix, DistanceMetric metric, int[] clusterID) {
		// System.out.println("Running kmedoid with "+nClusters+" clusters");
		if (monitor != null)
			monitor.setProgress(0);

		int iteration = 0;

		// Start by calculating the pairwise distances
		double[][] distances = new double[matrix.nRows()][matrix.nRows()];
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nRows(); j++) {
				distances[i][j] = metric.getMetric(matrix, matrix, i, j);
			}
		}

		int[] centers;
		if (context.kcluster.initializeNearCenter) {
			centers = chooseCentralElementsAsCenters(matrix.nRows(), nClusters, distances, null);
		} else {
			centers = chooseRandomElementsAsCenters(matrix.nRows(), nClusters);
		}
		int[] oldCenters = null;
		// outputCenters(centers);

		while (centersChanged(oldCenters, centers)) {
			oldCenters = centers;
			// outputClusterId(clusterID);
			assignPointsToClosestCenter(oldCenters, distances, clusterID);
			centers = calculateCenters(nClusters, matrix, metric, clusterID);
			// outputCenters(centers);

			if (iteration++ >= nIterations) break;
		}

		// System.out.println("ifound = "+ifound+", error = "+error);
		// outputCenters(centers);
  	return 1;
	}


	private void assignPointsToClosestCenter(int[] centers, double[][] distances, int[] clusterId) {
		for (int row = 0; row < distances.length; row++) {
			double minDistance = Double.MAX_VALUE;
			for (int cluster = 0; cluster < centers.length; cluster++) {
				// We could have clusters that are also 0 distance from
				// our medoid, so we need to make sure that our medoid gets
				// assigned to itself
				if (centers[cluster] == row) {
					clusterId[row] = cluster;
					break;
				}
				double distance = distances[row][centers[cluster]];
				if (distance < minDistance) {
					clusterId[row] = cluster;
					minDistance = distance;
				}
			}
		}
	} 

	private int[] calculateCenters(int nClusters, CyMatrix matrix, DistanceMetric metric, int[] clusterId) {
		int[] newCenters = new int[nClusters];
		// CyMatrix cData = new CyMatrix(network, nClusters, matrix.nRows());
		// CyMatrix cData = new CyMatrix(network, nClusters, matrix.nColumns());

		// Calculate all of the cluster centers
		// getClusterMeans(nClusters, matrix, cData, clusterId);

		// For each cluster, find the closest row
		for (int cluster = 0; cluster < nClusters; cluster++) {
			// newCenters[cluster] = findMedoid(matrix, cData, cluster, clusterId);
			newCenters[cluster] = findMedoid(matrix, cluster, clusterId);
		}
		return newCenters;
	}

	private int findMedoid(CyMatrix matrix, int cluster, int[] clusterid) {
		double minDistance = Double.MAX_VALUE;
		int medoid = -1;
		// System.out.println("Looking for cluster "+cluster);
		for (int row = 0; row < matrix.nRows(); row++) {
			if (clusterid[row] == cluster) {
				double distance = metric.getMetric(matrix, matrix, row, cluster);
				if (distance < minDistance) {
					medoid = row;
					minDistance = distance;
				}
			}
		}
		return medoid;
	}

	private boolean centersChanged(int[] oldCenters, int[] centers) {
		if (oldCenters == null || centers == null) return true;

		for (int i = 0; i < oldCenters.length; i++) {
			if (oldCenters[i] != centers[i]) return true;
		}
		return false;
	}

	private void outputCenters(int[] centers) {
		System.out.println("Centroid points: ");
		for (int i = 0; i < centers.length; i++) {
			System.out.println(" "+i+": "+centers[i]);
		}
	}

	private void outputClusterId(int[] clusterId) {
		System.out.println("Cluster IDs: ");
		for (int i = 0; i < clusterId.length; i++) {
			System.out.println(" "+i+": "+clusterId[i]);
		}
	}
}

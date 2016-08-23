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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans;

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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

public class RunKCluster extends AbstractKClusterAlgorithm {
	KMeansContext context;

	public RunKCluster(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
	                   TaskMonitor monitor, KMeansContext context, AbstractClusterAlgorithm parentTask) {
		super(network, weightAttributes, metric, monitor, parentTask);
		this.context = context;
	}

	// The kmeans implementation of a k-clusterer
	public int kcluster(int nClusters, int nIterations, CyMatrix matrix, DistanceMetric metric, int[] clusterID) {
		// System.out.println("Running kmeans with "+nClusters+" clusters");

		int nelements = matrix.nRows();
		int ifound = 1;

		int[] tclusterid = new int[nelements];

		int[] saved = new int[nelements];

		int[] mapping = new int[nClusters];
		int[] counts = new int[nClusters];

		double error = Double.MAX_VALUE;

		if (monitor != null)
			monitor.setProgress(0);

		// System.out.println("Creating matrix for "+nClusters);
		// This matrix will store the centroid data
		// Matrix cData = new Matrix(network, nClusters, matrix.nColumns());
		CyMatrix cData = CyMatrixFactory.makeSmallMatrix(network, nClusters, matrix.nColumns());

		// Outer initialization
		if (nIterations <= 1) {
			for (int i=0; i < clusterID.length; i++) {
				tclusterid[i] = clusterID[i];
			}
			nIterations = 1;
		} else {
			for (int i = 0; i < nelements; i++) 
				clusterID[i] = 0;
		}

		// System.out.println("Entering do loop for "+nClusters);
		int iteration = 0;
		do {
			// System.out.println("do loop iteration "+iteration+" for "+nClusters);

			if (monitor != null)
				monitor.setProgress(((double)iteration/(double)nIterations));

			double total = Double.MAX_VALUE;
			int counter = 0;
			int period = 10;

			// System.out.println("Assigning elements "+nClusters);

			// Randomly assign elements to clusters
			if (nIterations != 0) {
				if (!context.kcluster.initializeNearCenter) {
					// System.out.println("Randomly assigning elements "+nClusters);
					// Use the cluster 3.0 version to be consistent
					chooseRandomElementsAsCenters(nelements, nClusters, tclusterid);
					// System.out.println("Done randomly assigning elements "+nClusters);
					// if (nIterations != 0) debugAssign(nClusters, nelements, tclusterid);
				} else {
					int centers[] = chooseCentralElementsAsCenters(nelements, nClusters, 
					                                               matrix.getDistanceMatrix(metric).toArray(), tclusterid);
				}
			}
			// System.out.println("Done assigning elements "+nClusters);

			// Initialize
			for (int i = 0; i < nClusters; i++) counts[i] = 0;
			for (int i = 0; i < nelements; i++) counts[tclusterid[i]]++;

			// System.out.println("Inner loop starting "+nClusters);
			while (true) {
				double previous = total;
				total = 0.0;
				if (counter % period == 0) // Save the current cluster assignments
				{
					for (int i = 0; i < nelements; i++)
						saved[i] = tclusterid[i];
					if (period < Integer.MAX_VALUE / 2) 
						period *= 2;
				}
				counter++;

				// Find the center
				// System.out.println("Assigning cluster means "+nClusters);
				getClusterMeans(nClusters, matrix, cData, tclusterid);

				/*
				for (int i = 0; i < nClusters; i++) {
					System.out.print("cluster "+i+": ");
					for (int j = 0; j < matrix.nColumns(); j++) {
						System.out.print(cData.getValue(i,j)+"\t");
					}
					System.out.println();
				}
				*/

				for (int i = 0; i < nelements; i++) {
					// Calculate the distances
					double distance;
					int k = tclusterid[i];
					if (counts[k]==1) continue;

					// Get the distance
					// distance = metric(ndata,data,cdata,mask,cmask,weight,i,k,transpose);
					distance = metric.getMetric(matrix, cData, i, k);
					for (int j = 0; j < nClusters; j++) { 
						double tdistance;
						if (j==k) continue;
						// tdistance = metric(ndata,data,cdata,mask,cmask,weight,i,j,transpose);
						tdistance = metric.getMetric(matrix, cData, i, j);
						if (tdistance < distance) 
						{ 
							distance = tdistance;
            	counts[tclusterid[i]]--;
            	tclusterid[i] = j;
            	counts[j]++;
						}
          }
        	total += distance;
        }
				// System.out.println("total = "+total+", previous = "+previous+" nClusters="+nClusters);
      	if (total>=previous) break;
      	/* total>=previous is FALSE on some machines even if total and previous
				 * are bitwise identical. */
				int i;
	      for (i = 0; i < nelements; i++)
	        if (saved[i]!=tclusterid[i]) break;
	      if (i==nelements)
	        break; /* Identical solution found; break out of this loop */
    	}

			if (nIterations<=1)
			{ error = total;
				break;
			}

			for (int i = 0; i < nClusters; i++) mapping[i] = -1;

			int element = 0;
			for (element = 0; element < nelements; element++)
			{ 
				int j = tclusterid[element];
				int k = clusterID[element];
				if (mapping[k] == -1) 
					mapping[k] = j;
				else if (mapping[k] != j)
      	{ 
					if (total < error)
        	{ 
						ifound = 1;
          	error = total;
						// System.out.println("Mapping tclusterid to clusterid nClusters = "+nClusters);
          	for (int i = 0; i < nelements; i++) clusterID[i] = tclusterid[i];
        	}
        	break;
      	}
    	}
    	if (element==nelements) ifound++; /* break statement not encountered */
  	} while (++iteration < nIterations);
		// System.out.println("Do loop complete for "+nClusters);

		// System.out.println("ifound = "+ifound+", error = "+error);
  	return ifound;
	}

	// Debug version of "randomAssign" that isn't random
	private void debugAssign (int nClusters, int nElements, int[] clusterID) {
		for (int element = 0; element < nElements; element++) {
			clusterID[element] = element%nClusters;
		}
	}

}

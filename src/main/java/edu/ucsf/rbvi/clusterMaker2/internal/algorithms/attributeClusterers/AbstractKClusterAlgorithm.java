/* vim: set ts=2: */
/**
 * Copyright (c) 2011 The Regents of the University of California.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This abstract class is the base class for all of the attribute clusterers provided by
 * clusterMaker.  Fundamentally, an attribute clusterer is an algorithm which functions to
 * partition either nodes or attributes based on the similarity between them.
 */
public abstract class AbstractKClusterAlgorithm {
    protected List<String>attrList;
    protected CyNetwork network;
    protected String[] weightAttributes;
    protected DistanceMetric metric;
    protected TaskMonitor monitor;
    protected boolean debug = false;
    protected Matrix matrix;
    protected boolean ignoreMissing = true;
    protected boolean selectedOnly = false;
    protected boolean useSilhouette = false;
    protected boolean cancelled = false;
    protected Integer[] rowOrder;
    private Silhouettes[] silhouetteResults = null;


    /**
     * Common code for the k-cluster algorithms with silhouette
     */
    public AbstractKClusterAlgorithm(CyNetwork network, String weightAttributes[],
                                     DistanceMetric metric, TaskMonitor monitor) {
        this.network = network;
        this.weightAttributes = weightAttributes;
        this.metric = metric;
        this.monitor = monitor;
    }

    // This should be overridden by any k-cluster implementation
    public abstract int kcluster(int nClusters, int nIterations, Matrix matrix,
                                 DistanceMetric metric, int[] clusters);

    /**
     * This is the common entry point for k-cluster algorithms.
     *
     * @param nClusters the number of clusters (k)
     * @param nIterations the number of iterations to use
     * @param transpose whether we're doing rows (GENE) or columns (ARRY)
     * @param algorithm the algorithm type
     * @return a string with all of the results
     */
    public Integer[] cluster(ClusterManager clusterManager,
                             int nClusters, int nIterations, boolean transpose,
                             String algorithm, KClusterAttributes context, boolean createGroups) {
        String keyword = "GENE";
        if (transpose) keyword = "ARRY";

        for (int att = 0; att < weightAttributes.length; att++)
            if (debug)
                monitor.showMessage(TaskMonitor.Level.INFO,"Attribute: '"+weightAttributes[att]+"'");

        if (monitor != null)
            monitor.setStatusMessage("Creating distance matrix");

        // Create the matrix
        matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
        monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
        int kMax = Math.min(context.kMax, matrix.nRows());

        // Create a weight vector of all ones (we don't use individual weighting, yet)
        matrix.setUniformWeights();

        if (monitor != null)
            monitor.setStatusMessage("Clustering...");

        if (context.useSilhouette) {
            TaskMonitor saveMonitor = monitor;
            monitor = null;

            silhouetteResults = new Silhouettes[kMax];

            int nThreads = Runtime.getRuntime().availableProcessors()-1;
            if (nThreads > 1)
                runThreadedSilhouette(kMax, nIterations, nThreads, saveMonitor);
            else
                runLinearSilhouette(kMax, nIterations, saveMonitor);

            if (cancelled()) return null;

            // Now get the results and find our best k
            double maxSil = Double.MIN_VALUE;
            for (int kEstimate = 2; kEstimate < kMax; kEstimate++) {
                double sil = silhouetteResults[kEstimate].getMean();
                saveMonitor.showMessage(TaskMonitor.Level.INFO,"Average silhouette for "+kEstimate+" clusters is "+sil);
                if (sil > maxSil) {
                    maxSil = sil;
                    nClusters = kEstimate;
                }
            }
            monitor = saveMonitor;
            // System.out.println("maxSil = "+maxSil+" nClusters = "+nClusters);
        }

        int[] clusters = new int[matrix.nRows()];

        if (cancelled()) return null;

        // Cluster
        int nClustersFound = kcluster(nClusters, nIterations, matrix, metric, clusters);
        if (cancelled()) return null;

        // TODO Change other algorithms s.t. the number of clusters found is returned
        if (nClusters == 0) nClusters = nClustersFound;

        // OK, now run our silhouette on our final result
        Silhouettes sResult = SilhouetteCalculator.calculate(matrix, metric, clusters);
        // System.out.println("Average silhouette = "+sResult.getAverageSilhouette());
        // SilhouetteUtil.printSilhouette(sResult, clusters);

        if (!matrix.isTransposed())
            createGroups(clusterManager, nClusters, clusters, algorithm, createGroups);

	/*
 		Ideally, we would sort our clusters based on size, but for some reason
		this isn't working...
		renumberClusters(nClusters, clusters);
	*/
        // NB  HOPACH clusters should not be re-ordered

        rowOrder = matrix.indexSort(clusters, clusters.length);
        // System.out.println(Arrays.toString(rowOrder));
        // Update the network attributes

        // FIXME For HOPACH, nClusters is determined by the algorithm, and is neither estimated nor predefined...

        String resultString =  "Created "+nClusters+" clusters with average silhouette = "+sResult.getMean();
        monitor.showMessage(TaskMonitor.Level.INFO,resultString);

		/*
		String s = "Clusters: ";
		for (int i = 0; i < clusters.length; ++i) {
			s += clusters[i] + ", ";
		}
		monitor.showMessage(TaskMonitor.Level.INFO,s);
		*/

        return rowOrder;
    }

    public Matrix getMatrix() { return matrix; }
    public List<String> getAttributeList() { return attrList; }

    /**
     * This protected method is called to create all of our groups (if desired).
     * It is used by all of the k-clustering algorithms.
     *
     * @param nClusters the number of clusters we created
     * @param cluster the list of values and the assigned clusters
     */

    protected void createGroups(ClusterManager clusterManager, int nClusters, int[] clusters,
                                String algorithm, boolean createGroups) {
        if (matrix.isTransposed()) {
            return;
        }

        attrList = new ArrayList<String>(matrix.nRows());
        // Create the attribute list
        for (int cluster = 0; cluster < nClusters; cluster++) {
            List<CyNode> memberList = new ArrayList<CyNode>();
            for (int i = 0; i < matrix.nRows(); i++) {
                if (clusters[i] == cluster) {
                    attrList.add(matrix.getRowLabel(i)+"\t"+cluster);
                    memberList.add(matrix.getRowNode(i));
                    ModelUtils.createAndSetLocal(network, matrix.getRowNode(i), algorithm+" Cluster",
                            new Integer(cluster), Integer.class, null);
                }
            }
            if (createGroups) {
                CyGroup group = clusterManager.createGroup(network, "Cluster_"+cluster, memberList, null, true);
            }
        }
    }

    public void getClusterMeans(int nClusters, Matrix data, Matrix cdata, int[] clusterid) {

        double[][]cmask = new double[nClusters][cdata.nColumns()];

        for (int i = 0; i < nClusters; i++) {
            for (int j = 0; j < data.nColumns(); j++) {
                cdata.setValue(i, j, null);
                cmask[i][j] = 0.0;
            }
        }

        for (int k = 0; k < data.nRows(); k++) {
            int i = clusterid[k];
            for (int j = 0; j < data.nColumns(); j++) {
                if (data.hasValue(k,j)) {
                    double cValue = 0.0;
                    double dataValue = data.getValue(k, j);
                    if (cdata.hasValue(i,j)) {
                        cValue = cdata.getValue(i, j);
                    }
                    cdata.setValue(i,j, Double.valueOf(cValue+dataValue));
                    cmask[i][j] = cmask[i][j] + 1.0;
                }
            }
        }
        for (int i = 0; i < nClusters; i++) {
            for (int j = 0; j < data.nColumns(); j++) {
                if (cmask[i][j] > 0.0) {
                    double cData = cdata.getValue(i, j) / cmask[i][j];
                    cdata.setValue(i,j,Double.valueOf(cData));
                }
            }
        }
    }

    public boolean cancelled() {
        return cancelled;
    }

    protected int[] chooseRandomElementsAsCenters(int nElements, int nClusters) {
        int[] centers = new int[nClusters];
        List<Integer> candidates = new ArrayList<Integer>(nElements);
        for (int i = 0; i < nElements; i++)
            candidates.add(i);

        for (int i = 0; i < nClusters; i++) {
            int index = (int) Math.floor(Math.random() * candidates.size());
            centers[i] = candidates.get(index);
            candidates.remove(index);
        }
		/*
		for (int i = 0; i < nClusters; i++) {
			System.out.println("nClusters = "+nClusters+", i = " + i + ", center = " + centers[i]);
		}
		*/
        return centers;
    }

    protected int[] chooseCentralElementsAsCenters(int nElements, int nClusters, double[][] distances, int[] tclusterid) {
        int[] centers = new int[nClusters];

        // calculate normalized distances
        double[][] normalized = new double[nElements][nElements];
        for (int i = 0; i < nElements; i++) {
            double sum = 0;
            for (int j = 0; j < nElements; j++) {
                double x = distances[i][j];
                normalized[i][j] = x;
                sum += x;
            }
            for (int j = 0; j < nElements; j++) {
                normalized[i][j] /= sum;
            }
        }

        // sum the normalized distances across all rows
        // setup key-value pairs with summed normalized distances as keys
        // and element indices as values
        KeyValuePair[] pairs = new KeyValuePair[nElements];
        for (int i = 0; i < nElements; i++) {
            pairs[i] = new KeyValuePair(0.0, i);
            for (int j = 0; j < nElements; j++) {
                pairs[i].key += normalized[i][j];
            }
        }

        // sort the summed normalized distances
        // for choosing the elements that are closest overall to all other elements
        Comparator<KeyValuePair> comparator = new KeyValuePairComparator();
        Arrays.sort(pairs, comparator);

        // initialize the centers
        for (int i = 0; i < nClusters; i++) {
            centers[i] = pairs[i].value;
            // System.out.println("nClusters = "+nClusters+", i = " + i + ", center = " + centers[i]);
        }

        // Now, if we've been provided a tclusterid array, assign each element to it's closest center
        if (tclusterid != null) {
            for (int j = 0; j < nElements; j++) {
                double distance = Double.MAX_VALUE;
                for (int cluster = 0; cluster < nClusters; cluster++) {
                    if (normalized[j][centers[cluster]] < distance) {
                        distance = normalized[j][centers[cluster]];
                        tclusterid[j] = cluster;
                    }
                }
            }
        }

        return centers;
    }

    private void renumberClusters(int nClusters, int [] clusters) {
        int[] clusterSizes = new int[nClusters];
        Arrays.fill(clusterSizes, 0);
        for (int cluster : clusters) {
            clusterSizes[cluster] += 1;
        }

        Integer[] sortedClusters = new Integer[nClusters];
        for (int cluster = 0; cluster < nClusters; cluster++) {
            sortedClusters[cluster] = cluster;
        }


        // OK, now sort
        Arrays.sort(sortedClusters, new SizeComparator(clusterSizes));
        int[] clusterIndex = new int[nClusters];
        for (int cluster = 0; cluster < nClusters; cluster++) {
            clusterIndex[sortedClusters[cluster]] = cluster;
        }
        for (int row = 0; row < clusters.length; row++) {
            // System.out.println("Setting cluster for row "+ row+" to "+sortedClusters[clusters[row]]+" was "+clusters[row]);
            clusters[row] = clusterIndex[clusters[row]];
        }

    }

    private void runThreadedSilhouette(int kMax, int nIterations, int nThreads, TaskMonitor saveMonitor) {
        // Set up the thread pools
        ExecutorService[] threadPools = new ExecutorService[nThreads];
        for (int pool = 0; pool < threadPools.length; pool++)
            threadPools[pool] = Executors.newFixedThreadPool(1);

        // Dispatch a kmeans calculation to each pool
        for (int kEstimate = 2; kEstimate < kMax; kEstimate++) {
            int[] clusters = new int[matrix.nRows()];
            Runnable r = new RunKMeans(matrix, clusters, kEstimate, nIterations, saveMonitor);
            threadPools[(kEstimate-2)%nThreads].submit(r);
            // threadPools[0].submit(r);
        }

        // OK, now wait for each thread to complete
        for (ExecutorService threadPool : threadPools) {
            threadPool.shutdown();
            try {
                boolean result = threadPool.awaitTermination(7, TimeUnit.DAYS);
            } catch (Exception ignored) {
            }
        }
    }

    private void runLinearSilhouette(int kMax, int nIterations, TaskMonitor saveMonitor) {
        for (int kEstimate = 2; kEstimate < kMax; kEstimate++) {
            int[] clusters = new int[matrix.nRows()];
            if (cancelled()) return;
            if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a k estimate of "+kEstimate);
            int ifound = kcluster(kEstimate, nIterations, matrix, metric, clusters);
            silhouetteResults[kEstimate] = SilhouetteCalculator.calculate(matrix, metric, clusters);
        }
    }

    // private class pairing key and and value
    // abandon generic here and hard-code types, since arrays and generics do not work well in Java!
    private class KeyValuePair {
        public double key;
        public int value;

        public KeyValuePair(double key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // private class comparator for sorting key-value pairs
    private class KeyValuePairComparator implements Comparator<KeyValuePair> {
        public int compare(KeyValuePair a, KeyValuePair b) {
            if ((Double)a.key < (Double)b.key) {
                return -1;
            }
            return 1;
        }
    }

    private class SizeComparator implements Comparator <Integer> {
        int[] sizeArray = null;
        public SizeComparator(int[] a) { this.sizeArray = a; }

        public int compare(Integer o1, Integer o2) {
            if (sizeArray[o1] > sizeArray[o2]) return 1;
            if (sizeArray[o1] < sizeArray[o2]) return -1;
            return 0;
        }
    }

    private class RunKMeans implements Runnable {
        Matrix matrix;
        int[] clusters;
        int kEstimate;
        int nIterations;
        TaskMonitor saveMonitor = null;

        public RunKMeans (Matrix matrix, int[] clusters, int k, int nIterations, TaskMonitor saveMonitor) {
            this.matrix = matrix;
            this.clusters = clusters;
            this.kEstimate = k;
            this.nIterations = nIterations;
            this.saveMonitor = saveMonitor;
        }

        public void run() {
            int[] clusters = new int[matrix.nRows()];
            if (cancelled()) return;
            if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a k estimate of "+kEstimate);
            try {
                int ifound = kcluster(kEstimate, nIterations, matrix, metric, clusters);
                silhouetteResults[kEstimate] = SilhouetteCalculator.calculate(matrix, metric, clusters);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}

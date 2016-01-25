package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;


/**
 * Utility class for Silhouette calculation and related functions.
 * @author lucasyao, djh.shih
 *
 */
public class SilhouetteCalculator {
	
	/**
	 * This method calculates the silhouette for a given data matrix using a metric as it's string.  The current
	 * cluster is provided.
	 *
	 * @param matrix the data matrix
	 * @param metric the distance metric we're using
	 * @param labels the labels for each of clusters
	 * @return the resulting silhouette
	 */
	public static Silhouettes calculate(CyMatrix matrix, DistanceMetric metric, int[] labels)
	{
		double[][] distanceMatrix = matrix.getDistanceMatrix(metric).toArray();
		return calculate(distanceMatrix, labels);
	}
	
	public static Silhouettes calculate(CyMatrix matrix, DistanceMetric metric, Clusters clusters)
	{
		double[][] distanceMatrix = matrix.getDistanceMatrix(metric).toArray();
		return calculate(distanceMatrix, clusters);
	}
	
	/**
	 * This method calculates the silhouette for a given matrix and the current cluster labels.
	 * @param distancematrix is 2-D double arrays for the pair-wise distances
	 * @param labels the labels for each of clusters
	 * @return the resulting silhouette
	 */
	public static Silhouettes calculate(double[][] distancematrix, int[] labels)
	{
		// System.out.println("Have "+labels.length+" labels");
		return calculate(distancematrix, new Clusters(labels));
	}
	
	public static Silhouettes calculate(double[][] distancematrix, Clusters clusters)
	{
		
		Silhouettes silresult = new Silhouettes();
		int samplenum = clusters.size();
		
		int[] clusterSizes = clusters.getSizes();

		// OK, now calculate the silhouette
		for(int i=0;i<samplenum;i++)
		{
			double silhouettevalue=0;
			double a=0;
			double b=0;
			Integer classlabel = clusters.getClusterIndex(i);
			int clusterSize = clusterSizes[classlabel];
		
			//initializing
			HashMap<Integer, Double> bvalues = new HashMap<Integer, Double>();
		
			//calculate distance by different classes
			for(int j=0;j<samplenum;j++)
			{
				if (i == j) continue;
				Integer currentclasslabel = clusters.getClusterIndex(j);
				double distancevalue = 0.0;
				if(bvalues.containsKey(currentclasslabel))
					distancevalue = bvalues.get(currentclasslabel).doubleValue();
				// System.out.println("i,j = "+ i +","+ j+"dmatij= " + distancematrix[i][j]);
				distancevalue = distancevalue + distancematrix[i][j];
				bvalues.put(currentclasslabel, Double.valueOf(distancevalue));
			}
			
			//calculate a b and silhouette
			double mindis = Double.MAX_VALUE;
			Integer minlabel = null;
			for(Integer kLabel: bvalues.keySet())
			{
				int count = clusterSizes[kLabel];
				double value = bvalues.get(kLabel).doubleValue();
				
				if (kLabel.equals(classlabel))
					// when calculating average distance to all elements in own cluster,
					// do not consider distance to itself
					a = value / (count-1);
				else if (value/count < mindis) {
					mindis = value/count;
					minlabel = kLabel;
				}
			}
			b = mindis;
			
			if (clusterSize == 1) {
				// element is in singleton cluster: set silhouette to 0, by definition
				// this could be done earlier, if minlabel is not of interest...
				silresult.addSilhouette(0, minlabel);
			}

			if(a>b) {
				silhouettevalue = (b-a)/a;
			} else  {
				silhouettevalue = (b-a)/b;
			}
			// System.out.println("silhouetteValue for "+i+" = "+silhouettevalue+", a = "+a+", b = "+b);
			
			silresult.addSilhouette(silhouettevalue, minlabel);
			
		}
		return silresult;
	}
	
	/**
	 * Calculate the silhouette profile, given a matrix of distances between data points and clusters (segregation matrix),
	 * or a matrix of distances between clusters and clusters (segregation matrix)
	 * @param S matrix of distances
	 * @param clusters
	 */
	public static Silhouettes silhouettes(double[][] S, Clusters clusters) {
		int m = S.length;
		int k = S[0].length;
		
		Silhouettes sils = new Silhouettes();
		
		int[] sizes = clusters.getSizes();
		
		// calculate silhouettes
		for (int i = 0; i < m; ++i) {
			int c = clusters.getClusterIndex(i);
			if (sizes[c] == 1) {
				// element is in a singleton class: silhouette is 0 by definition
				sils.addSilhouette(0, -1);
				continue;
			}
			// distance to own cluster
			double a = S[i][c];
			// distance to nearest cluster
			double b = Double.POSITIVE_INFINITY;
			int nearest = -1;
			for (int j = 0; j < k; ++j) {
				if (j != c && S[i][j] < b) {
					b = S[i][j];
					nearest = j;
				}
			}
			if (b < Double.POSITIVE_INFINITY) {
				double max;
				if (a < b) {
					max = b;
				} else {
					max = a;
				}
				sils.addSilhouette((b-a)/max, nearest);
			} else {
				// no other cluster is available: set silhouette to 0
				sils.addSilhouette(0, -1);
			}
		}
		
		return sils;
	}
	
	/**
	 * This method prints out the silhouette profile for a given result.  In typical silhouette display,
	 * the values are organized by cluster with the silhouette values ranked largest to smallest.
	 *
	 * @param result the result that we're displaying
	 * @param labels the clustering
	 */
	public static void print(Silhouettes result, int[] labels) {
		// Divide the indices into clusters
		TreeMap<Integer, SortedSet<Double>> clusters = new TreeMap<Integer, SortedSet<Double>>();
		for (int row = 0; row < labels.length; row++) {
			if (!clusters.containsKey(labels[row]))
				clusters.put(labels[row], new TreeSet<Double>());
			clusters.get(labels[row]).add(result.getSilhouette(row));
		}
		// For each cluster, output the profile
		for (Integer cluster: clusters.keySet()) {
			System.out.println("Cluster #"+cluster);
			for (Double sil: clusters.get(cluster)) {
				System.out.println("Silhouette "+sil);
			}
		}

	}
	
}


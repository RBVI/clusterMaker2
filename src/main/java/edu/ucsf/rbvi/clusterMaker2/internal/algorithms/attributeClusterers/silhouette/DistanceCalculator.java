package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;


/**
 * DistanceCalculator constructs different types of distance matrices.
 * @author djh.shih
 *
 */
public class DistanceCalculator {
	
	/**
	 * Make a n x k matrix of distances between n data points and k clusters.
	 * @param distances distance matrix
	 * @param clusters cluster assignments
	 * @return matrix
	 */
	public static double[][] segregations(Matrix distances, Clusters clusters) {
		int m = distances.nRows();
		int k = clusters.getNumberOfClusters();
		
		// allocate space
		double [][] S = new double[m][k];
		
		// get cluster sizes
		int[] sizes = clusters.getSizes();
		
		// calculate the average distances from data point i to data point, for each cluster
		for (int i = 0; i < m; ++i) {
			// accumulate sum, assuming distance to self is 0
			for (int j = 0; j < m; ++j) {
				double v = distances.doubleValue(i,j);
				if (!Double.isNaN(v))
					S[i][ clusters.getClusterIndex(j) ] += v;
			}
			// derive mean via division by cluster sizes
			for (int jj = 0; jj < k; ++jj) {
				if (sizes[jj] > 1) {
					S[i][jj] /= sizes[jj];
				} else if (sizes[jj] == 0) {
					// special case: empty cluster
					S[i][jj] = Double.POSITIVE_INFINITY;
				}
			}
			// correct mean for own cluster (divide by size-1 instead of size)
			// element in singleton cluster has 0 distance to itself (and hence 0 distance to own cluster)
			int c = clusters.getClusterIndex(i);
			int size = sizes[c];
			if (size > 1) {
				S[i][c] *= size / (size - 1);
			}
		}
		
		return S;
	}
	
	// TODO medoid-segregation
	
	/**
	 * Make a k x k matrix of distances between k clusters.
	 * TODO  add options to use average, median, or centroid inter-cluster distances
	 * @param distances distance matrix
	 * @param clusters clustering result
	 * @return matrix
	 */
	public static double[][] separations(Matrix distances, Clusters clusters) {
		final int k = clusters.getNumberOfClusters();
		
		// allocate space
		double[][] S = new double[k][k];
		
		if (k == 1) {
			// special case: only one cluster
			S[0][0] = 0;
			return S;
		}
		
		int[][] partitions = clusters.getPartitions();
		
		// calculate distance for lower and upper triangles together
		for (int i = 0; i < k; ++i) {
			for (int j = i+1; j < k; ++j) {
				// calculate average of distances from elements in partition_i to partition_j
				double d = 0.0;
				int n = 0;
				for (int c1 = 0; c1 < partitions[i].length; ++c1) {
					for (int c2 = 0; c2 < partitions[j].length; ++c2) {
						double v = distances.doubleValue(partitions[i][c1], 
						                                 partitions[j][c2]);
						if (!Double.isNaN(v))
							d += v;
						++n;
					}
				}
				S[i][j] = S[j][i] = d / n;
			}
		}
		
		return S;
	}
	
	/**
	 * Make a k x k matrix of distances between k medoids.
	 * @param distances distance matrix
	 * @param medoids medoid assignments
	 * @return matrix
	 */
	public static double[][] separations(Matrix distances, int[] medoids) {
		int k = medoids.length;
		
		// allocate space
		double[][] S = new double[k][k];
		
		if (k == 1) {
			// special case: only one medoid/cluster
			S[0][0] = 0;
			return S;
		}
		
		for (int i = 0; i < k; ++i) {
			for (int j = i+1; j < k; ++j) {
				// determine inter-medoid distance by subsetting distances
				double v = distances.doubleValue(medoids[i], medoids[j]);
				if (!Double.isNaN(v))
					S[i][j] = S[j][i] = v;
			}
		}
		
		return S;
	}
}

	
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;


/**
 * Hopachable is an interface for partitioners usable by Hopach.
 * @author djh.shih
 *
 */
public interface Hopachable extends Subsegregatable {

	// Split elements into partitions
	// A class implementing Hopachable is free to choose the best k, and can use simply choose k = argmin_k MSS
	//   by calling a separately provided functions to calculate min MSS
	Clusters split(boolean forceSplit);

	// Return a matrix with distances from each element to each cluster
	double[][] segregations(Clusters clusters);

	// Return a matrix with distances between clusters (required for ordering of clusters)
	double[][] separations(Clusters clusters);

	// Collapse a pair of clusters indexed by i and j, given a split
	Clusters collapse(int i, int j, Clusters clusters);

	// Order elements (elements of the same class should belong together and classes can be rearranged)
	int[] order(Clusters clusters);

	// Subset
	Hopachable subset(int[] index);

}
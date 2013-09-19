package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import java.util.HashSet;
import java.util.Iterator;

import org.cytoscape.model.CyNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.KClusterable;



/**
 * Partitioning Around Medoids. Cluster data elements by aiming to minimize the average
 * dissimilarity of objects to their closest selected element (medoid).
 * Data elements are indexable.
 * Independent of Cytoscape.
 * NB   This class is an implementation of the PAM algorithm described in the course notes
 *      at <www.cs.umb.edu/cs738/pam1.pdf> (accessed 2012-07-31).
 *      There appears to be different variants of the PAM algorithm varying in details
 *      within the build and swap phases.
 *      The original algorithm is described in chapter 2 of Kaufman and Rousseeuw (1990),
 *      whose original Fortran code was translated and augmented as part of the cluster R package.
 *      The cluster results from current implementation can differ from the implementation 
 *      in R's cluster::pam. (See PAMTest for details.)
 * @author djh.shih
 * @comment
 *
 */
public class PAM implements KClusterable {
	
	protected BaseMatrix data;
	protected DistanceMetric metric;
	protected DistanceMatrix distances;
	protected int nClusters;
	
	protected Clusters clusters;
	
	// ordered index of element subset
	// all immediate data and output should have the same size as idx
	// (only the input BaseMatrix data has the full original set of data elements)
	// (indexing of DistanceMatrix is handled by the class itself)
	int[] idx;
	
	// clustering cost, to be minimized
	double cost;
	
	// distance between element and closest medoid
	protected double[] nearestDistances;
	// distance between element and second closest medoid
	double[] nextNearestDistances;
	
	// nearest medoid of each element
	int[] nearestMedoids;
	// next-nearest medoid of each element 
	int[] nextNearestMedoids;
	
	// set of medoids
	HashSet<Integer> medoids;
	
	// set of non-meoids (maintain for finding swap candidates)
	HashSet<Integer> nonmedoids;
	
	// set of all indexed elements
	// required since Java's HashSet cannot use native types
	Integer[] elements;
	
	int maxSwaps = 1000;
	private CyNetwork network;
	
	public PAM(CyNetwork network, BaseMatrix data, DistanceMetric metric) {
		this(data, metric, null, null);
		this.network = network;
	}
	
	public PAM(BaseMatrix data, DistanceMetric metric, DistanceMatrix distances, int[] idx) {
		this.data = data;
		this.metric = metric;
		
		if (data == null || data.nRows() == 0) {
			throw new IllegalArgumentException("Data matrix is empty.");
		}
		
		if (idx == null) {
			// initially, use index all data elements in original order
			int m = data.nRows();
			idx = new int[m];
			for (int i = 0; i < m; ++i) {
				idx[i] = i;
			}
		}
		this.idx = idx;
		
		if (distances == null) {
			this.distances = new DistanceMatrix(data, metric, idx);
		} else {
			this.distances = distances.subset(idx);
		}
		
		this.clusters = null;
	}

	//@Override
	public Clusters cluster(int k) {
		int n = size();
		if (n == 0) {
			throw new IllegalArgumentException("No data elements are indexed.");
		}
		if (k > n) {
			throw new IllegalArgumentException("Number of clusters must be less than the number of data elements.");
		} else if (k == n) {
			// build trivial single clusters
			return new Clusters(k);
		}
		
		this.nClusters = k;
		
		initialize();
		buildPhase();
		swapPhase();
		clusters = new Clusters(nearestMedoids, getCost());
		
		return clusters;
	}

	/**
	 * Size. Number of data elements.
	 */
	public int size() {
		return idx.length;
	}
	
	/**
	 * Calculate the clustering cost: sum of distances to cluster medoids.
	 * @return cost
	 */
	private double getCost() {
		double c = 0;
		for (int i = 0; i < nearestDistances.length; ++i) {
			c += nearestDistances[i];
		}
		return c;
	}
	
	private void initialize() {
		int m = size();
		nearestDistances = new double[m];
		nextNearestDistances = new double[m];
		nearestMedoids = new int[m];
		nextNearestMedoids = new int[m];
		
		
		elements = new Integer[m];
		medoids = new HashSet<Integer>();
		nonmedoids = new HashSet<Integer>();
		
		for (int ii = 0; ii < m; ++ii) {
			// initialize distances to infinity
			nearestDistances[ii] = nextNearestDistances[ii] = Double.POSITIVE_INFINITY;
			// initialize medoids to non-valid indices, s.t. unexpected bugs trigger indexing error
			nearestMedoids[ii] = nextNearestMedoids[ii] = -1;
			
			elements[ii] = new Integer(ii);
			
			// all (indexed) data elements are initially non-medoids
			nonmedoids.add( elements[ii] );
		}
	}
	
	/**
	 * BUILD phase. Select a initial set of k medoids.
	 */
	private void buildPhase() {
		int m = size();
		
		// select first medoid
		
		// find element with minimum total distance to all other elements
		double[] totalDistances = new double[m];
		for (int ii = 0; ii < m; ++ii) {
			// sum distances to all other elements
			// assume distance to itself is 0
			double d = 0;
			for (int jj = 0; jj < m; ++jj) {
				d += distances.getValue(ii, jj);
			}
			totalDistances[ii] = d;
		}
		double minDistance = totalDistances[0];
		int minIndex = 0;
		for (int ii = 0; ii < m; ++ii) {
			if (totalDistances[ii] < minDistance) {
				minDistance = totalDistances[ii];
				minIndex = ii;
			}
		}
		// add element to medoid set
		addMedoid(minIndex);
		
		
		// select remaining k - 1 medoids
		
		double[] gains = new double[m];
		
		for (int kk = 1; kk < nClusters; ++kk) {
		
			// consider each i as medoid candidate
			for (int ii = 0; ii < m; ++ii) {
				// if ii is already a medoid, it has negative gain to prevent it from being selected again
				if (medoids.contains(elements[ii])) {
					gains[ii] = -1.0;
				} else {
					double gain = 0;
					// for each non-medoid j != i, calculate the gain
					for (int jj = 0; jj < m; ++jj) {
						if (jj == ii || medoids.contains(elements[jj]) ) continue;
						if (nearestDistances[jj] > distances.getValue(ii, jj)) {
							// add i will improve j's nearest distances
							// (if selected, i will be the new nearest neighbour of j)
							gain += nearestDistances[jj] - distances.getValue(ii, jj);
						}
					}
					gains[ii] = gain;
				}
			}
			// select candidate with maximum gain
			double maxGain = Double.NEGATIVE_INFINITY;
			int maxIndex = -1;
			for (int ii = 0; ii < m; ++ii) {
				if (gains[ii] > maxGain) {
					maxGain = gains[ii];
					maxIndex = ii;
				}
			}
			// add element to medoid set
			addMedoid(maxIndex);
			
		}
		
		// check that the number of medoids match the expected
		if (nClusters != medoids.size()) {
			throw new RuntimeException("Expected error in BUILD phase: Number of medoids does not match parameter k.");
		}
		
	}
	
	/**
	 * SWAP phase. Attempt to improve clustering quality by exchanging medoids with non-medoids.
	 */
	private void swapPhase() {
		while (true) {
			double bestChange = 0;
			int bestii = -1, besthh = -1;
			
			Iterator<Integer> medIt = medoids.iterator();
			while (medIt.hasNext()) {
				int ii = medIt.next().intValue();
				
				Iterator<Integer> nonmedIt = nonmedoids.iterator();
				while (nonmedIt.hasNext()) {
					int hh = nonmedIt.next().intValue();

					// Consider swapping medoid i and nonmedoid h
					// by calculating gains by all other elements
					
					// Calculate cumulative change to distance to nearest medoid for all nonmedoids j != h
					double change = 0;
					Integer[] nonmedIt2 = new Integer[nonmedoids.size() + 1];
					nonmedoids.toArray(nonmedIt2);
					nonmedIt2[nonmedIt2.length - 1] = ii;
					for (int jj: nonmedIt2) {
					//	if (jj == hh) continue;
						
						double d = nearestDistances[jj];
						if (distances.getValue(ii, jj) > d) {
							// if removed, i will have no impact
							if (distances.getValue(jj, hh) < d) {
								// if selected, h will improve nearest distance for j
								change += distances.getValue(jj, hh) - d;
							}
						} else {
							// i cannot be closer than the nearest neighbour for j;
							// therefore, distances[i][j] == d
							// and i is currently the nearest neighbour for j
							double e = nextNearestDistances[jj];
							if (distances.getValue(jj, hh) < e) {
								// if i and h are swapped, h will become the nearest neighbour
								// nearest distance for j may improve or worsen
								change += distances.getValue(jj, hh) - d;
							} else {
								// if i is removed, the current next-nearest of j will be promoted to nearest
								change += e - d;
							}
						}
					}
					if (change < bestChange) {
						bestChange = change;
						bestii = ii;
						besthh = hh;
					}
					
				}
				
			}
			if (bestChange == 0) break;
			else {
			//	System.out.println("bestChange: " + bestChange);
				swap(besthh,bestii);
			}
		}
	}
	
	private void addMedoid(int add) {
		medoids.add( elements[add] );
		nonmedoids.remove( elements[add] );
		updateNearest(add, -1);
	}
	
	private void swap(int add, int remove) {
		medoids.add( elements[add] );
		nonmedoids.remove( elements[add] );
		medoids.remove( elements[remove] );
		nonmedoids.add( elements[remove] );
		updateNearest(add, remove);
	}
	
	/**
	 * Update nearest and next-nearest distances.
	 * Does not check whether {@code added} or {@ removed} have been added to or removed from the medoid set.
	 * FIXME  optimize
	 * @param added Index of element added to medoid set (-1 for none)
	 * @param removed Index of element removed from medoid set (-1 for none)
	 */
	private void updateNearest(int added, int removed) {
		int m = size();
		
		if (removed >= 0) {
			// removed index is valid
			
			// check if the removed medoid is the nearest or next-nearest of any element
			for (int ii = 0; ii < m; ++ii) {
				if (nearestMedoids[ii] == removed) {
					// promote next-nearest to nearest
					nearestMedoids[ii] = nextNearestMedoids[ii];
					nearestDistances[ii] = nextNearestDistances[ii];
					// find new next-nearest
					updateNextNearest(ii);
				} else if (nextNearestMedoids[ii] == removed) {
					// find new next-nearest
					updateNextNearest(ii);
				}
			}
			
		}

		if (added >= 0) {
			// added index is valid
			
			// check if any nearest distance improves
			for (int ii = 0; ii < m; ++ii) {
				double d = distances.getValue(ii, added);
				if (d < nearestDistances[ii]) {
					// element i is nearer to added medoid than previous nearest: update
					double oldDistance = nearestDistances[ii];
					int oldMedoid = nearestMedoids[ii];
					nearestMedoids[ii] = added;
					nearestDistances[ii] = d;
					// pump nearest distance to next-nearest distance
					nextNearestMedoids[ii] = oldMedoid;
					nextNearestDistances[ii] = oldDistance;
				} else if (d < nextNearestDistances[ii]) {
					// element i is nearer to added medoid than previous next-nearest: update
					nextNearestMedoids[ii] = added;
					nextNearestDistances[ii] = d;
				}
			}
			
		}
		Integer [] a = new Integer[medoids.size()];
	/*	System.out.print("medoids: ");
		for (int i: medoids.toArray(a))
			System.out.print(i + " ");
		System.out.print("\n"); */
	}
	
	/**
	 * Update next nearest for element i.
	 * Assume nearest medoid is already set.
	 * @param ii element index to be updated
	 */
	private void updateNextNearest(int ii) {
		int nearestMedoid = nearestMedoids[ii];
		
		// find the next-nearest
		Iterator<Integer> it = medoids.iterator();
		double minDistance = Double.POSITIVE_INFINITY;
		int nextNearestMedoid = -1;
		while (it.hasNext()) {
			int jj = it.next().intValue();
			// ignore if j is the nearestMedoid, since we are interested in the next-nearest
			if (jj == nearestMedoid) continue;
			if (distances.getValue(ii, jj) < minDistance) {
				minDistance = distances.getValue(ii, jj);
				nextNearestMedoid = jj;
			}
		}
	
		// update
		nextNearestDistances[ii] = minDistance;
		nextNearestMedoids[ii] = nextNearestMedoid;
	}

}

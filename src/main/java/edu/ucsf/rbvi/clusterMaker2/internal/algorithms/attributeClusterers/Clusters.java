package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;


import java.util.Arrays;
import java.util.TreeMap;

/**
 * Clusters store clustering results.
 * Independent of Cytoscape.
 * @author djh.shih
 *
 */
public class Clusters {
	protected int[] index;
	protected int[] orderedLabels;
	protected int k;
	private double cost;
	
	// cache
	private int[] sizes;
	private int[][] partitions;
	
	/**
	 * Constructor.
	 * @param assignments cluster index/labels of each element (cluster labels will be re-assigned)
	 * @param k number of clusters
	 */
	public Clusters(int[] assignments) {
		init(assignments, 0.0);
	}
	
	public Clusters(int[] assignments, double cost) {
		init(assignments, cost);
	}
	
	/**
	 * Special constructor for building singleton clusters.
	 * @param k number of singleton clusters
	 */
	public Clusters(int k) {
		int[] t = new int[k];
		for (int i = 0; i < k; ++i) {
			t[i] = k;
		}
		this.index = t;
		this.orderedLabels = Arrays.copyOf(t, t.length);
		this.k = k;
		this.cost = 0.0;
	}
	
	/**
	 * Constructor. No cluster label reassignment. No error checking.
	 * @param assignments cluster index/labels of each element (clusters must be labeled 0 to k-1)
	 * @param k number of clusters
	 * @param cost cost of the clustering
	 */
	public Clusters(int[] index, int nClusters, double cost) {
		this.index= Arrays.copyOf(index, index.length);
		this.k = nClusters;
		this.cost = cost;
		
		int[] orderedLabels = new int[nClusters];
		for (int c = 0; c < nClusters; ++c) {
			orderedLabels[c] = c;
		}
		this.orderedLabels =orderedLabels;
	}
	
	/**
	 * Copy constructor.
	 * @param other
	 */
	public Clusters(Clusters other) {
		this.index = Arrays.copyOf(other.index, other.index.length);
		this.k = other.k;
		this.cost = other.cost;
		this.orderedLabels = Arrays.copyOf(other.orderedLabels, other.orderedLabels.length);
	}
	
	/**
	 * Re-assign clusterIndex s.t. clusters are labeled 0 to k-1.
	 * Labels assigned in the order in which the cluster appear in the cluster assignment
	 * TODO Ensure stable label assignment?
	 *      Sort original labels; calculate intervals between labels; iterate through clusterIndex, subtracting intervals as appropriate; O(n)
	 * @param assignments
	 * @param cost
	 */
	protected void init(int[] assignments, double cost) {
		int m = assignments.length;
		
		Integer[] elements = new Integer[m];
		for (int ii = 0; ii < m; ++ii) {
			elements[ii] = new Integer(ii);
		}

		/*
		System.out.println("Cluster init assignments:");
		for (int ii = 0; ii < m; ++ii) {
			System.out.println("    assignments["+ii+"] = "+assignments[ii]);
		}
		*/
		
		// assign new cluster 0-based index
		Integer k = new Integer(0);
		TreeMap<Integer, Integer> clusterLabels = new TreeMap<Integer, Integer>();
		for (int ii = 0; ii < m; ++ii) {
			if (!clusterLabels.containsKey( elements[assignments[ii]] )) {
				// assign new cluster to next available index
				clusterLabels.put(elements[assignments[ii]], k++);
			}
		}
		
		int[] newClusterIndex = new int[m];
		for (int ii = 0; ii < m; ++ii) {
			newClusterIndex[ii] = clusterLabels.get( elements[assignments[ii]] ).intValue();
		}
		
		// store the label of each cluster
		// the atrocious inefficiency here is likely inconsequential
		int[] labels = new int[k];
		for (int i = 0; i < m; ++i) {
			labels[ newClusterIndex[i] ] = assignments[i];
		}
		
		this.orderedLabels = labels;
		this.index = newClusterIndex;
		this.k = clusterLabels.size();
		this.cost = cost;
	}
	
	/**
	 * Get cluster index for an element
	 * @param i element index
	 * @return class index
	 */
	public int getClusterIndex(int i) {
		return index[i];
	}
	
	/**
	 * Get the cluster label for an element
	 * @param i element index
	 * @return class label
	 */
	public int getLabel(int i) {
		return orderedLabels[ index[i] ];
	}
	
	public int getClusterLabel(int c) {
		return orderedLabels[c];
	}
	
	public int[] getClusterLabels() {
		return orderedLabels;
	}
	
	/**
	 * Order clusters based on specified index.
	 * @param clusterOrderIndex new cluster order
	 */
	public void order(int[] clusterOrderIndex) {
		int k = clusterOrderIndex.length;
		if (k != this.k) {
			throw new IllegalArgumentException("clusterOrderIndex must be a permutation of the cluster index (and its size must be equal to the number of clusters).");
		}
		
		// mapping from old cluster index to new index
		TreeMap<Integer, Integer> oldToNew = new TreeMap<Integer, Integer>();
		for (int c = 0; c < k; ++c) {
			oldToNew.put(clusterOrderIndex[c], c);
		}
		
		// create new index
		int m = index.length;
		int[] newIndex = new int[m];
		for (int i = 0; i < m; ++i) {
			newIndex[i] = oldToNew.get(index[i]).intValue();
		}
	
		// create new labels used on order
		int[] newOrderedLabels = new int[k];
		// iterate through new cluster index
		for (int d = 0; d < k; ++d) {
			newOrderedLabels[d] = orderedLabels[ clusterOrderIndex[d] ];
		}
		
		this.index = newIndex;
		this.orderedLabels = newOrderedLabels;
		
		clear();
	}
	
	/**
	 * Merge one cluster to another
	 * @param c1 destination cluster
	 * @param c2 source cluster
	 */
	public void merge(int c1, int c2) {
		
		// modify assignment index array
		for (int i = 0; i < index.length; ++i) {
			// assign all elements of c2 to c1
			if (index[i] == c2) {
				index[i] = c1;
			}
			// shift all subsequent cluster index down
			if (index[i] > c2) {
				--(index[i]);
			}
		}
		
		// modify create new orderedLabels array
		int[] newOrderedLabels = new int[k-1];
		// copy labels before deleted cluster verbatim
		for (int c = 0; c < c2; ++c) {
			newOrderedLabels[c] = orderedLabels[c];
		}
		// shift labels after deleted cluster
		for (int c = c2; c < k-1; ++c) {
			newOrderedLabels[c] = orderedLabels[c+1];
		}
		this.orderedLabels = newOrderedLabels;
		
		--k;
		
		// clear cached results
		clear();
	}
	
	/**
	 * Sizes of each cluster.
	 * @return array of cluster sizes
	 */
	public int[] getSizes() {
		if (sizes != null) {
			// return cached result
			return sizes;
		}
		
		sizes = new int[k];
		for (int i = 0; i < index.length; ++i) {
			sizes[index[i]]++;
		}
		
		return sizes;
	}
	
	/**
	 * Partitioning of the clustered elements.
	 * @return array of array containing elements in each cluster
	 */
	public int[][] getPartitions() {
		// allocate space
		int[] sizes = getSizes();
		partitions = new int[k][];
		for (int i = 0; i < k; ++i) {
			partitions[i] = new int[sizes[i]];
		}
		// running size of each partition
		int[] counters = new int[k];
		
		// determine partitions
		for (int i = 0; i < index.length; ++i) {
			int cl = index[i];
			partitions[cl][counters[cl]] = i;
			++counters[cl];
		}
		
		return partitions;
	}
	
	/**
	 * Number of clusters.
	 * @return number of clusters
	 */
	public int getNumberOfClusters() { return k; }
	
	/**
	 * Cost of clustering.
	 * @return cost
	 */
	public double getCost() { return cost; }
	
	/**
	 * Set cost of clustering.
	 * @param cost new cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	/**
	 * Clear cached results.
	 */
	public void clear() {
		sizes = null;
		partitions = null;
	}
	
	/**
	 * Number of elements.
	 * @return number of elements
	 */
	public int size() {
		return index.length;
	}
	
}

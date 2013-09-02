package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;

import java.util.ArrayList;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Hopachable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.Numeric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.PrimitiveMeanSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.PrimitiveSummarizer;


/**
 * Hopach performs HOPACH using a Hopachable partitioner.
 * Independent of Cytoscape.
 * @author djh.shih
 *
 */
public class Hopach {
	
	protected Hopachable partitioner;
	
	// resulting split
	Clusters split;
	
	// resulting level
	int level;
	
	// flattened splits at each level
	// FIXME consider using native array?
	ArrayList<Clusters> splits;
	
	// maximum number of partitioning levels
	int maxLevel = 9;
	
	// minimum cost reduction
	double minCostReduction = 0;
	
	// whether to force split the initial level
	boolean forceInitSplit = false;
	
	// summarizes array of values
	PrimitiveSummarizer psummarizer = new PrimitiveMeanSummarizer();
	
	/*
	 * Initial set of parameters implemented
	 * clusters = best
	 * coll = seq
	 * newmed = NA  (HopachablePAM)
	 * mss = med    (HopachablePAM)
	 * initord = co
	 * ord = neighbour
	 */
	
	public Hopach(Hopachable partitioner) {
		this.partitioner = partitioner;
		initialize();
	}
	
	public void setParameters(int maxLevel, double minCostReduction, boolean forceInitSplit, PrimitiveSummarizer psummarizer) {
		this.maxLevel = maxLevel;
		this.minCostReduction = minCostReduction;
		this.forceInitSplit = forceInitSplit;
		this.psummarizer = psummarizer;
		initialize();
	}
	
	public Clusters run() {
		runDown();
		//int[] order = partitioner.order(split);
		
		// TODO construct linkage tree from splits and order
		
		return split;
	}
	
	public void printResults() {
		System.out.format("level: %d%n", level);
		for (int i = 0; i < split.size(); ++i) {
			System.out.format("%d ", split.getLabel(i));
		}
		System.out.println();
		
		// TODO print linkage tree
	}
	
	public Hopachable getPartitioner() {
		return partitioner;
	}
	
	void initialize() {
		// pre-allocate space-holders
		this.splits = new ArrayList<Clusters>(maxLevel);
		for (int i = 0; i < maxLevel; ++i) {
			splits.add(null);
		}
	}
	
	void runDown() {
		Clusters split = initLevel();
		if (splitIsFinal(split)) {
			// return initial split results
			this.split = split;
			this.level = 0;
			return;
		}
	
		// set optimal split to initial level
		int optLevel = 0;
		Clusters optSplit = split;
		
		int level = 0;
		while (true) {
			split = collapse(level);
	
			// store optimal split
			if (split.getCost() < optSplit.getCost()) {
				optSplit = split;
				optLevel = level;
			}
			
			// proceed to next level
			++level;
			
			// break if max level exceeded, or splitting has converged
			if (level >= maxLevel || nextLevel(level)) {
				// last split is ignored, since it did not do anything
				break;
			}
		}
		
		// store optimal split and level
		this.split = optSplit;
		this.level = optLevel;
	}
	
	/**
	 * Split the initial level.
	 * Pollard DIFF:  no option to force split
	 * @return possibility of continuing split
	 */
	Clusters initLevel() {
		Clusters split = partitioner.split(forceInitSplit);
		sortInitLevel(split);
		splits.set(0, split);
		return split;
	}
	
	/**
	 * Attempt to collapse clusters at the specified level.
	 * @param level
	 */
	Clusters collapse(int level) {
		
		// split will be collapsed in-place
		Clusters split = splits.get(level);
		
		// a valid split must already be stored at the specified level
		if (split == null) {
			throw new IllegalArgumentException("Specified split level for collapse does not exist.");
		}
		
		int k = split.getNumberOfClusters();
		if (k <= 2) {
			// if k == 1, no collapse is possible
			// if k == 2, no improvement will occur from k = 2 to k = 1,
			//            since partitioning into k =2 was chosen over k =1 during the partitioning step
			// therefore, the correct action is: do nothing and return original split
			return split;
		}
		
		int maxCollapses = k - 2;
		
		for (int nCollapses = 0; nCollapses < maxCollapses; ++nCollapses) {
			Pair nearest = nearestClusters(split);
			Clusters collapsedSplit = partitioner.collapse(nearest.x, nearest.y, split);
			
			// reduction in cost
			double r = split.getCost() - collapsedSplit.getCost();
			
			if (r >= minCostReduction) {
				// save collapsed split, and continue
				split = collapsedSplit;
			} else {
				// insufficient cost reduction: reject collapse and stop
				break;
			}
		}
		
		return split;
	}
	
	private static class Pair {
		public int x, y;
		public Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	Pair nearestClusters(Clusters split) {
		// distances of clusters from one another
		double[][] S = partitioner.separations(split);
		int k = S.length;
		
		// find minimum non-self distance
		double minDist = Double.POSITIVE_INFINITY;
		int mi = 0, mj = 1;
		for (int i = 0; i < k; ++i) {
			for (int j = i+1; j < k; ++j) {
				if (S[i][j] < minDist) {
					minDist = S[i][j];
					mi = i;
					mj = j;
				}
			}
		}
		
		// return closest pair
		return new Pair(mi, mj);
	}
	
	/**
	 * Attempt to split the next level.
	 * @param level next level to split
	 * @return convergence
	 */
	boolean nextLevel(int level) {
		// nextLevel can only be invoked for level >= 1
		if (level < 1) {
			throw new IllegalArgumentException("nextlevel can only be invoked for level >= 1");
		}
		
		// clusters in parent level
		Clusters prevSplit = splits.get(level-1);
		int[][] partitions = prevSplit.getPartitions();
		int nClusters = prevSplit.getNumberOfClusters();
		
		double[][] segregations = partitioner.segregations(prevSplit);
		
		// flattened array of all cluster index for each partition
		int[] clusterIndex = new int[partitioner.size()];
		// j indexes the first element of each cluster (global index)
		int j = 0;
		// running total number of partitions
		int k = 0;
		// cost of each subsplit
		double[] costs = new double[nClusters];
		
		// Attempt to split each partition
		for (int i = 0; i < nClusters; ++i) {
			
			// neighbour is on the right unless current partition is the last partition
			boolean rightNeighbour = j < nClusters - 1;
			
			int[] partition = partitions[i];
			if (partition.length == 0) {
				// partition is empty (partitioner returned fewer partitions than requested)
				continue;
			}
			
			int neighbourIndex = rightNeighbour ? i+1 : i-1;
			
			// split partition
			// TODO cache sub-partitioner here and in MSS calculator
			//      or, cache partition and split results
			Hopachable sub = partitioner.subset(partition);
			Clusters subsplit = sub.split(false);
			
			int subk = subsplit.getNumberOfClusters();
			
			if (subk > 1) {
			
				int[][] subpartitions = subsplit.getPartitions();
				
				// create separation matrix for distance from each sub-cluster to neighbouring cluster
				// NB Pollard used medoid-separations for ordering initial level, but average-separations for subsequent levels (implemented here)
				// TODO Allow partitioner to handle different options of summarizing distance from new subclusters to neighbouring cluster?
				//      e.g. Calculate distances between medoids instead of average distances
				//      Consider: HopachablePAM will only only one type of separations (medoid-separations).
				double[] separations = new double[subpartitions.length];
				for (int c = 0; c < subpartitions.length; ++c) {
					// average distance across elements of sub-cluster
					double d = 0.0;
					for (int jj = 0; jj < subpartitions[c].length; ++jj) {
						d += segregations[ subpartitions[c][jj] + j ][neighbourIndex];
					}
					separations[c] = d / subpartitions[c].length;
				}
				
				// order sub-clusters
				sortSplit(subsplit, separations, rightNeighbour);
				// NB  ordered labels now stores the index of the medoids based on local index
				
			}
			
			costs[i] = subsplit.getCost();
			
			// copy over new cluster index
			for (int jj = 0; jj < sub.size(); ++jj) {
				clusterIndex[ partition[jj] ] = subsplit.getClusterIndex(jj) + k;
			}
			j += sub.size();
			k += subk;
			
		}
		
		// store results for new level
		Clusters newSplit = new Clusters(clusterIndex, k, psummarizer.summarize(costs));
		// NB  now the orderedLabels store trivial labels...
		this.split = newSplit;
		this.splits.set(level, newSplit);
		
		// splitting has converged if k has not changed
		if (k == nClusters) {
			return true;
		}
		
		// splitting has converged if new split is final
		return splitIsFinal(newSplit);
	}
	
	void sortSplit(Clusters split, double[] segregationsFromNeighbour, boolean rightNeighbour) {
		int[] order;
		if (rightNeighbour) {
			// order clusters by decreasing segregation from right neighbour
			order = Numeric.order(segregationsFromNeighbour, true);
		} else {
			// order clusters by increasing segregation from left neighbour
			order = Numeric.order(segregationsFromNeighbour, false);
		}
		
		split.order(order);
	}
	
	/**
	 * Re-label cluster index in sorted order given the split of the first level.
	 * The clusters are sorted, but the elements are not.
	 * @param split clustering for initial level
	 */
	void sortInitLevel(Clusters split) {
		if (split.getNumberOfClusters() <= 2) {
			// order of 2 or fewer clusters cannot be optimized
			return;
		}
		
		double[][] S = partitioner.separations(split);
		int k = S.length;
		
		// original order
		int[] order = new int[k];
		for (int i = 0; i < k; ++i) {
			order[i] = i;
		}
		
		// order clusters using inter-cluster distances
		optimizeOrderingCorrelation(S, order);
		
		split.order(order);
	}
	
	/**
	 * Check if split is final.
	 * @param split splitting results
	 * @return possibility of continuing split
	 */
	boolean splitIsFinal(Clusters split) {
		final int minSize = 3;
		int[] sizes = split.getSizes();
		boolean isFinal = true;
		for (int clusterSize : sizes) {
			if (clusterSize >= minSize) {
				isFinal = false;
			}
		}
		return isFinal;
	}
	
	/**
	 * Optimize ordering correlation.
	 * @param d matrix of distances
	 * @param order original order (to be modified in-place)
	 * @return ordering correlation ({@code order} is re-organized as an side-effect)
	 */
	double optimizeOrderingCorrelation(double[][] d, int[] order) {
		// number of data elements
		int m = d.length;
		
		double[] v = distanceMatrixToVector(d);
		
		// original correlation
		double r = orderingCorrelation(v, m);
		
		for (int step = 1; step <= m-1; ++step) {
			boolean changed = true;
			while (changed) {
				changed = false;
				for (int j = 0; j < m - step; ++j) {
					int jj = j + step;
					// swap elements
					int t = order[j];
					order[j] = order[jj];
					order[jj] = t;
					double r2 = orderingCorrelation( reorderDistanceVector(v, order, m), m );
					if (r2 <= r) {
						// N.B. differs from Pollar's implementation
						// correlation did not improve: revert order
						t = order[j];
						order[j] = order[jj];
						order[jj] = t;
					} else {
						changed = true;
						r = r2;
					}
				}
			}
		}
		
		return r;
	}
	
	/**
	 * Compute correlation of ordering as specified by the row order of a lower triangle distance matrix.
	 * @param x distance matrix in lower triangle vector form
	 * @param n number of node elements
	 * @return correlation
	 */
	double orderingCorrelation(double[] x, int n) {
		// Construct lower triangle matrix (in vector form) with perfect ordering and and equal intervals
		double[] y = new double[x.length];
		int value = 1, end = n - 1;
		for (int i = 0; i < x.length; ++i) {
			y[i] = (double)value;
			++value;
			if (value > end) {
				// start next column
				value = 1;
				end--;
			}
		}
	
		return Numeric.correlation(x, y);
	}
	
	double[] distanceMatrixToVector(double[][] d) {
		int m = d.length;
		
		int n = m * (m - 1) / 2;
		double[] v = new double[n];
		
		int ii = 1, jj = 0;
		for (int i = 0; i < n; ++i) {
			v[i] = d[ii][jj];
			// descend down current column
			++ii;
			if (ii >= m) {
				// start next column
				++jj;
				// jump to element below diagonal
				ii = jj - 1;
			}
		}
		
		return v;
	}
	
	int indexLTMatrixToVector(int i, int j, int m) {
		// ensure i < j (i == j is invalid)
		if (j > i) {
			// swap indices
			int t = i;
			i = j;
			j = t;
		}
		return (j * (m- j)) + ((j - 1) * j / 2) + (i - j) - 1;
	}
	
	double[] reorderDistanceVector(double[] v, int[] idx, int m) {
		int n = v.length;
		double[] w = new double[n];
		
		// fill in vector, starting at d[1,0]
		int ii = 1, jj = 0;
		for (int i = 0; i < n; ++i) {
			w[i] = v[ indexLTMatrixToVector(idx[ii], idx[jj], m) ];
			// descend down current column
			++ii;
			if (ii >= m) {
				// start next column
				++jj;
				// jump to element below diagonal
				ii = jj + 1;
			}
		}
		
		return w;
	}
	
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;


// TODO Make DistanceMatrix more memory efficient using a lower triangular matrix representation

/**
 * Distance matrix. Matrix of distances between data elements.
 * Independent of Cytoscape.
 * @author djh.shih
 */
public class DistanceMatrix {
	protected double matrix[][];
	protected DistanceMetric metric;
	// index array (for efficient subsetting/permuting/bootstrapping)
	protected int[] idx;
	
	public DistanceMatrix(BaseMatrix data, DistanceMetric metric) {
		this(data, metric, null);
	}
	
	/**
	 * Constructor.
	 * @param data matrix of data
	 * @param metric distance metric
	 */
	public DistanceMatrix(BaseMatrix data, DistanceMetric metric, int[] idx) {
		this.matrix = data.getDistanceMatrix(metric);
		this.metric = metric;
		
		if (idx == null) {
			// initialize indexing array to original order
			idx = new int[matrix.length];
			for (int i = 0; i < matrix.length; ++i) {
				idx[i] = i;
			}
		}
		this.idx = idx;
	}
	
	/**
	 * Constructor. For use by DistanceMatrix.subset(.)
	 * @param matrix distance matrix
	 * @param metric metric
	 * @param idx index array
	 */
	private DistanceMatrix(double[][] matrix, DistanceMetric metric, int[] idx) {
		this.matrix = matrix;
		this.metric = metric;
		this.idx = idx;
	}
	
	/**
	 * Get value at specified position.
	 * @param i row index
	 * @param j column index
	 * @return value at position
	 */
	public double getValue(int i, int j) {
		return matrix[ idx[i] ][ idx[j] ];
	}
	
	/**
	 * Subset distance matrix.
	 * @param idx index array for subsetting (or potentially permuting, bootstrapping, etc.) the matrix
	 * @return subset of current distance matrix
	 */
	public DistanceMatrix subset(int[] idx) {
		// copy reference to matrix and metric, and use new index array
		return new DistanceMatrix(this.matrix, this.metric, idx);
	}

	/**
	 * Getter for size of array.
	 * @return size of array
	 */
	public int size() {
		return idx.length;
	}
	
	/**
	 * Getter for distance metric.
	 * @return distance metric
	 */
	public DistanceMetric getDistanceMetric() {
		return metric;
	}
}

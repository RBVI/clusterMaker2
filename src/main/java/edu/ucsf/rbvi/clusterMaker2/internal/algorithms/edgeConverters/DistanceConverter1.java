package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters;

public class DistanceConverter1 implements EdgeWeightConverter {

	/**
 	 * Get the short name of this converter
 	 *
 	 * @return short-hand name for converter
 	 */
	public String getShortName() { return "1/value";}
	public String toString() { return "1/value";}

	/**
 	 * Get the name of this converter
 	 *
 	 * @return name for converter
 	 */
	public String getName() { return "Edge weights are distances (1/value)"; }

	/**
 	 * Convert an edge weight
 	 *
 	 * @param weight the edge weight to convert
 	 * @param minValue the minimum value over all edge weights
 	 * @param maxValue the maximum value over all edge weights
 	 * @return the converted edge weight
 	 */
	public double convert(double weight, double minValue, double maxValue) {
		if (weight == 0.0)
			return Double.MAX_VALUE;

		return 1.0/weight;
	}
}

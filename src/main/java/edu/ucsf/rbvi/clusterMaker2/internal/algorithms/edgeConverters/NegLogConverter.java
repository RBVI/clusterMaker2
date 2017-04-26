package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters;

public class NegLogConverter implements EdgeWeightConverter {

	/**
 	 * Get the short name of this converter
 	 *
 	 * @return short-hand name for converter
 	 */
	public String getShortName() { return "-LOG(value)";}
	public String toString() { return "-LOG(value)";}

	/**
 	 * Get the name of this converter
 	 *
 	 * @return name for converter
 	 */
	public String getName() { return "Edge weights are expectation values (-LOG(value))"; }

	/**
 	 * Convert an edge weight
 	 *
 	 * @param weight the edge weight to convert
 	 * @param minValue the minimum value over all edge weights
 	 * @param maxValue the maximum value over all edge weights
 	 * @return the converted edge weight
 	 */
	public double convert(double weight, double minValue, double maxValue) {
		// If our minumum value is < 0, we want to shift everything up
		if (minValue < 0.0) 
			weight += Math.abs(weight);

		if(weight != 0.0 && weight != Double.MAX_VALUE && !Double.isNaN(weight))
			weight = -Math.log10(weight);
		else
			weight = 500; // Assume 1e-500 as a reasonble upper bound

		return weight;
	}
}

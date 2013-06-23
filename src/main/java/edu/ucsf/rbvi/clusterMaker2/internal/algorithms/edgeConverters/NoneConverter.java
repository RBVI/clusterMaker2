package org.cytoscape.myapp.internal.algorithms.edgeConverters;

public class NoneConverter implements EdgeWeightConverter {

	/**
 	 * Get the short name of this converter
 	 *
 	 * @return short-hand name for converter
 	 */
	public String getShortName() { return "None";}
	public String toString() { return "None";}

	/**
 	 * Get the name of this converter
 	 *
 	 * @return name for converter
 	 */
	public String getName() { return "Edge values are not converted"; }

	/**
 	 * Convert an edge weight
 	 *
 	 * @param weight the edge weight to convert
 	 * @param minValue the minimum value over all edge weights
 	 * @param maxValue the maximum value over all edge weights
 	 * @return the converted edge weight
 	 */
	public double convert(double weight, double minValue, double maxValue) {
		return weight;
	}
}

package org.cytoscape.myapp.internal.algorithms.edgeConverters;

import java.lang.Math;


public class SCPSConverter implements EdgeWeightConverter {

	/**
 	 * Get the short name of this converter
 	 *
 	 * @return short-hand name for converter
 	 */
	public String getShortName() { return "SCPS";}
	public String toString() { return "SCPS";}

	/**
 	 * Get the name of this converter
 	 *
 	 * @return name for converter
 	 */
	public String getName() { return "SCPS convertion (assumes edge weights are BLAST e-values)"; }

	/**
 	 * Convert an edge weight
 	 *
 	 * @param weight the edge weight to convert
 	 * @param minValue the minimum value over all edge weights
 	 * @param maxValue the maximum value over all edge weights
 	 * @return the converted edge weight
 	 */
	public double convert(double weight, double minValue, double maxValue) {

	    double w = 6.1302;
	    double b = 1.2112;
	    
	    weight = 1/(1 + Math.exp(w*Math.log10(weight) + b));
	    return weight;
	}
}

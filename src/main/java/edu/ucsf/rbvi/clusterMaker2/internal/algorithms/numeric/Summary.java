package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;


import java.lang.Math;

/**
 * Data summary statistics. Calculates mean and standard deviation
 * @author djh.shih
 *
 */
public class Summary {
	double mean;
	double devsq;
	double n;
	
	public Summary() {
		mean = 0;
		devsq = 0;
		n = 0;
	}
	
	/**
	 * Add value to summarizer. Calculate running statistics.
	 * @param x value
	 */
	void add(double x) {
		n++;
		double t = x - mean;
		mean += t / n;
		devsq += t * (x - mean);
	}
	
	/**
	 * Population variance.
	 * @return population variance
	 */
	double variance() {
		if (n > 1) {
			return devsq / n;
		}
		return 0.0;
	}
	
	/**
	 * Population standard deviation.
	 * @return population standard deviation
	 */
	double sd() {
		if (n > 1) {
			return Math.sqrt( devsq / n );
		}
		return 0.0;
	}
	
	/**
	 * Mean.
	 * @return mean
	 */
	double mean() {
		return mean;
	}
	
	/**
	 * Number of data elements summarized.
	 * @return size
	 */
	int size() {
		return (int)n;
	}
}
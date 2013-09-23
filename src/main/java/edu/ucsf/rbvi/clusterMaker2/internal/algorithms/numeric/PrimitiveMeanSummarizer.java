package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

public class PrimitiveMeanSummarizer implements PrimitiveSummarizer {
	public double summarize(double[] a) {
		return Numeric.mean(a);
	}
}

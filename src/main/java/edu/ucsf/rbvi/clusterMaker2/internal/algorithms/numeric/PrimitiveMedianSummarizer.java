package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

public class PrimitiveMedianSummarizer implements PrimitiveSummarizer {
	@Override
	public double summarize(double[] a) {
		return Numeric.median(a);
	}
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

public class MedianSummarizer implements Summarizer {
	@Override
	public Double summarize(Double[] a) {
		return Numeric.median(a);
	}
}

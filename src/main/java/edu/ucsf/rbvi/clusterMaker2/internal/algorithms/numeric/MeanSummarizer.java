package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

public class MeanSummarizer implements Summarizer {
	@Override
	public Double summarize(Double[] a) {
		return Numeric.mean(a);
	}
}

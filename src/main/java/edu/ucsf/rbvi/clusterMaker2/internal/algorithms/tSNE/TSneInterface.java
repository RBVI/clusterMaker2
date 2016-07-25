package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

public interface TSneInterface {

	double [][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca);

	R Hbeta (double [][] D, double beta);
	
	R x2p(double [][] X,double tol, double perplexity);

	static class R {
		double [][] P;
		double [] beta;
		double H;
	}
}

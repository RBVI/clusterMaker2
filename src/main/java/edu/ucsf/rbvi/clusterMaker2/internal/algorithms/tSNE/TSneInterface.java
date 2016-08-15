package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public interface TSneInterface {

	Matrix tsne(Matrix matrix, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca);

	R Hbeta (Matrix D, double beta);
	
	R x2p(Matrix X,double tol, double perplexity);

	static class R {
		Matrix P;
		double [] beta;
		double H;
	}
}

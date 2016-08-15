package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;



import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.abs;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.addColumnVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.addRowVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.assignAllLessThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.assignAtIndex;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.assignValuesToRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.biggerThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.diag;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.equal;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.exp;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.fillMatrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.getValuesFromRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.log;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.maximum;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.negate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.plus;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.replaceNaN;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.rnorm;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarDivide;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarMult;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarPlus;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.sqrt;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.square;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.sum;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.tile;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.times;
public class tSNECalculation implements TSneInterface{

	CalculationMatrix mo = new CalculationMatrix();

	public Matrix tsne(Matrix X, int k, int initial_dims, double perplexity) {
		return tsne(X,k,initial_dims, perplexity, 2000, true);
	}

	public Matrix tsne(Matrix X, int k, int initial_dims, double perplexity, int maxIterations) {
		return tsne(X,k,initial_dims, perplexity, maxIterations, true);
	}

	public Matrix tsne(Matrix matrix, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca) {
		double X[][]=matrix.toArray();
		String IMPLEMENTATION_NAME = this.getClass().getSimpleName();
		System.out.println("X:Shape is = " + X.length + " x " + X[0].length);
		System.out.println("Running " + IMPLEMENTATION_NAME + ".");
		if(use_pca && matrix.nColumns() > initial_dims && initial_dims > 0) {
			System.out.println("Using pca");
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			double trmpmatrix[][] = pca.pca(matrix.toArray(), initial_dims);
			matrix= CalculationMatrix.arrayToCyMatrix(trmpmatrix);
			System.out.println("X:Shape after PCA is = " + matrix.nRows() + " x " + matrix.nColumns());
		}
		
		int n = matrix.nRows();
		double momentum = .5;
		double initial_momentum = 0.5;
		double final_momentum   = 0.8;
		int eta                 = 500;
		double min_gain         = 0.01;
		Matrix Y           = rnorm(n,no_dims);
		
		Matrix dY          = fillMatrix(n,no_dims,0.0);
		Matrix iY          = fillMatrix(n,no_dims,0.0);
		Matrix gains       = fillMatrix(n,no_dims,1.0);

		// Compute P-values
		Matrix P = x2p(matrix, 1e-5, perplexity).P;
		P = plus(P , mo.transpose(P));
		P = scalarDivide(P,sum(P));
		P = scalarMult(P , 4);					// early exaggeration
		P = maximum(P, 1e-12);

		System.out.println("Y:Shape is = " + Y.nRows() + " x " + Y.nColumns());
		
		// Run iterations
		for (int iter = 0; iter < max_iter; iter++) {
			// Compute pairwise affinities
			Matrix sum_Y = mo.transpose(sum(square(Y), 1));
			Matrix num = scalarInverse(scalarPlus(addRowVector(mo.transpose(addRowVector(scalarMult(
					times(Y, mo.transpose(Y)),
					-2),
					sum_Y)),
					sum_Y),
					1));
			assignAtIndex(num, range(n), range(n), 0);
			Matrix Q = scalarDivide(num , sum(num));

			Q = maximum(Q, 1e-12);

			// Compute gradient
			Matrix L = mo.scalarMultiply(mo.minus(P , Q), num);
		    dY = scalarMult(times(mo.minus(diag(sum(L, 1)),L) , Y), 4);
			
			// Perform the update
			if (iter < 20)
				momentum = initial_momentum;
			else
				momentum = final_momentum;
			gains = plus(mo.scalarMultiply(scalarPlus(gains,0.2), abs(negate(equal(biggerThan(dY,0.0),biggerThan(iY,0.0))))),
					mo.scalarMultiply(scalarMult(gains,0.8), abs(equal(biggerThan(dY,0.0),biggerThan(iY,0.0)))));

			assignAllLessThan(gains, min_gain, min_gain);
			iY = mo.minus(scalarMult(iY,momentum) , scalarMult(mo.scalarMultiply(gains , dY),eta));
			Y = plus(Y , iY);
			//double [][] tile = tile(mean(Y, 0), n, 1);
			Y = mo.minus(Y , tile(mean(Y, 0), n, 1));

			// Compute current value of cost function
			if ((iter % 100 == 0))   {
				Matrix logdivide = log(scalarDivide(P , Q));
				logdivide = replaceNaN(logdivide,0);
				double C = sum(mo.scalarMultiply(P , logdivide));
				System.out.println("Iteration " + (iter + 1) + ": error is " + C);
			} else if((iter + 1) % 10 == 0) {
				System.out.println("Iteration " + (iter + 1));
			}

			// Stop lying about P-values
			if (iter == 100)
				P = scalarDivide(P , 4);
		}

		// Return solution
		return Y;
	}

	public R Hbeta (Matrix D, double beta){
		Matrix P = exp(scalarMult(scalarMult(D,beta),-1));
		double sumP = sum(P);   // sumP confirmed scalar
		double H = Math.log(sumP) + beta * sum(mo.scalarMultiply(D,P)) / sumP;
		P = scalarDivide(P,sumP);
		R r = new R();
		r.H = H;
		r.P = P;
		return r;
	}

	public R x2p(Matrix X,double tol, double perplexity){
		int n               = X.nRows();
		Matrix sum_X   = sum(square(X), 1);
		Matrix times   = scalarMult(times(X, mo.transpose(X)), -2);
		Matrix prodSum = addColumnVector(mo.transpose(times), sum_X);
		Matrix D       = addRowVector(prodSum, mo.transpose(sum_X));
		
		// D seems correct at this point compared to Python version
		Matrix P       = fillMatrix(n,n,0.0);
		double [] beta      = fillMatrix(n,n,1.0).toArray()[0];
		double logU         = Math.log(perplexity);
		System.out.println("Starting x2p...");
		for (int i = 0; i < n; i++) {
			if (i % 500 == 0)
				System.out.println("Computing P-values for point " + i + " of " + n + "...");
			double betamin = Double.NEGATIVE_INFINITY;
			double betamax = Double.POSITIVE_INFINITY;
			Matrix Di = getValuesFromRow(D, i,concatenate(range(0,i),range(i+1,n)));

			R hbeta = Hbeta(Di, beta[i]);
			double H = hbeta.H;
			Matrix thisP = hbeta.P;

			// Evaluate whether the perplexity is within tolerance
			double Hdiff = H - logU;
			int tries = 0;
			while(Math.abs(Hdiff) > tol && tries < 50){
				if (Hdiff > 0){
					betamin = beta[i];
					if (Double.isInfinite(betamax))
						beta[i] = beta[i] * 2;
					else 
						beta[i] = (beta[i] + betamax) / 2;
				} else{
					betamax = beta[i];
					if (Double.isInfinite(betamin))  
						beta[i] = beta[i] / 2;
					else 
						beta[i] = ( beta[i] + betamin) / 2;
				}

				hbeta = Hbeta(Di, beta[i]);
				H = hbeta.H;
				thisP = hbeta.P;
				Hdiff = H - logU;
				tries = tries + 1;
			}
			assignValuesToRow(P, i,concatenate(range(0,i),range(i+1,n)),thisP.toArray()[0]);
		}

		R r = new R();
		r.P = P;
		r.beta = beta;
		double sigma = mean(sqrt(scalarInverse(beta)));

		System.out.println("Mean value of sigma: " + sigma);

		return r;
	}

}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps;

import java.util.stream.IntStream;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.printArray;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.sqrt;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.addColumnVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.addRowVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.addScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.columnMean;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.copy;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.diag;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.divideScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.exp;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.log;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.multiplyMatrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.multiplyScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.normalize;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.powScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.rowSum;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.subtractScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.sum;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixUtils.transpose;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.assignAtIndex;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.assignValuesToRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.getValuesFromRow;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.BoolMatrixUtils.abs;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.BoolMatrixUtils.biggerThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.BoolMatrixUtils.equal;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.BoolMatrixUtils.negate;

/*
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.abs;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.assignAllLessThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.biggerThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.diag;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.equal;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.exp;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.log;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.negate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.replaceNaN;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.scalarDivide;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.scalarMult;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.scalarPlus;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.sqrt;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.square;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEMatrixOps.CalculationMatrix.tile;
*/

import java.util.Arrays;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

public class tSNECalculation implements TSneInterface{

	// CalculationMatrix mo = new CalculationMatrix();
	
	TaskMonitor monitor;
	
		public tSNECalculation(TaskMonitor monitor) {
			this.monitor = monitor;
		}

	public CyMatrix tsne(CyMatrix X, int k, int initial_dims, double perplexity) {
		return tsne(X,k,initial_dims, perplexity, 2000, true);
	}

	public CyMatrix tsne(CyMatrix X, int k, int initial_dims, double perplexity, int maxIterations) {
		return tsne(X,k,initial_dims, perplexity, maxIterations, true);
	}

	public CyMatrix tsne(CyMatrix matrix, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca) {

		//double X[][]=matrix.toArray();
		monitor.setProgress(0.0);

		String IMPLEMENTATION_NAME = this.getClass().getSimpleName();
		//System.out.println("X:Shape is = " + matrix.nRows() + " x " + matrix.nColumns());
		//System.out.println("Running " + IMPLEMENTATION_NAME + ".");
		monitor.showMessage(TaskMonitor.Level.INFO, "Running " + IMPLEMENTATION_NAME + ".");
		if(use_pca && matrix.nColumns() > initial_dims && initial_dims > 0) {
			//System.out.println("Using pca");
			monitor.showMessage(TaskMonitor.Level.INFO, "Using pca");
			// PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
	//		double trmpmatrix[][] = pca.pca(matrix.toArray(), initial_dims);

//			matrix= CalculationMatrix.arrayToCyMatrix(matrix, trmpmatrix);

			//System.out.println("X:Shape after PCA is = " + matrix.nRows() + " x " + matrix.nColumns());
			monitor.showMessage(TaskMonitor.Level.INFO, "X:Shape after PCA is = " + matrix.nRows() + " x " + matrix.nColumns());
		}
		
		int n = matrix.nRows();
		double momentum = .5;
		double initial_momentum = 0.5;
		double final_momentum   = 0.8;
		int eta                 = 500;
		double min_gain         = 0.01;

		Matrix Y           = matrix.like(n, no_dims, Matrix.DISTRIBUTION.NORMAL);
		Matrix dY          = matrix.like(n, no_dims, 0.0);
		Matrix iY          = matrix.like(n, no_dims, 0.0);
		Matrix gains       = matrix.like(n, no_dims, 1.0);

		// Compute P-values
		Matrix P = x2p(matrix, 1e-5, perplexity).P;

		P = addScalar(P , transpose(P));
		P = divideScalar(P, sum(P));
		P = multiplyScalar(P, 4.0);		// early exaggeration
		P = maximum(P, 1e-12);

		//System.out.println("Y:Shape is = " + Y.nRows() + " x " + Y.nColumns());
		monitor.showMessage(TaskMonitor.Level.INFO, "Y:Shape is = " + Y.nRows() + " x " + Y.nColumns());
		
		double progress = 0.0;
		// Run iterations
		for (int iter = 0; iter < max_iter; iter++) {
			progress = (double)iter/(double)max_iter;
			monitor.setProgress(progress);

			// P.writeMatrix("P-MatrixOps-"+iter);

			// Compute pairwise affinities
			Matrix sum_Y = transpose(rowSum(powScalar(copy(Y), 2)));

			// Matrix num = scalarInverse(scalarPlus(addRowVector(transpose(
			//                            addRowVector(scalarMult( 
			//                            times(Y, transpose(Y)), -2), 
			//                            sum_Y)), sum_Y), 1));
			Matrix mat1 = multiplyScalar(multiplyMatrix(Y, transpose(Y)), -1);
			Matrix mat2 = addRowVector(transpose(addRowVector(mat1, sum_Y)),sum_Y);
			Matrix num = scalarInverse(addScalar(mat2, 1));

			// Set the diagonals to 0
			assignAtIndex(num, range(n), range(n), 0.0);
			Matrix Q = divideScalar(copy(num), sum(num));

			Q = maximum(Q, 1e-12);
			// Q.writeMatrix("Q-MatrixOps-"+iter);

			// Compute gradient
			// Matrix L = mo.scalarMultiply(mo.minus(P , Q), num);
			Matrix L = multiplyScalar(subtractScalar(copy(P), Q), num);
			// L.writeMatrix("L-MatrixOps-"+iter);
			// dY = scalarMult(times(mo.minus(diag(sum(L, 1)),L) , Y), 4);
			dY = multiplyScalar(multiplyMatrix(subtractScalar(diag(rowSum(L)),L), Y), 4);

			// Perform the update
			if (iter < 20)
				momentum = initial_momentum;
			else
				momentum = final_momentum;

			// gains = plus(mo.scalarMultiply(scalarPlus(gains,0.2), 
			// 						 abs(gains,negate(equal(biggerThan(dY,0.0),biggerThan(iY,0.0))))),
			// 		         mo.scalarMultiply(scalarMult(gains,0.8), 
			// 						 abs(gains,equal(biggerThan(dY,0.0),biggerThan(iY,0.0)))));
			/*
			Matrix t1 = addScalar(copy(gains), 0.2);
			Matrix t2 = abs(gains,negate(equal(biggerThan(dY,0.0),biggerThan(iY,0.0))));
			Matrix t3 = multiplyScalar(t1, t2);

			Matrix t11 = multiplyScalar(copy(gains), 0.8);
			Matrix t12 = abs(gains,equal(biggerThan(dY,0.0),biggerThan(iY,0.0)));
			Matrix t13 = multiplyScalar(t11, t12);

			gains = addScalar(t3, t13);
			*/

			gains = addScalar(multiplyScalar(addScalar(copy(gains),0.2), 
			 					     	  abs(copy(gains),negate(equal(biggerThan(dY,0.0),biggerThan(iY,0.0))))),
			 		              multiplyScalar(multiplyScalar(copy(gains),0.8), 
			 						      abs(copy(gains),equal(biggerThan(dY,0.0),biggerThan(iY,0.0)))));

			// gains.writeMatrix("gains-MatrixOps-"+iter);

			assignAllLessThan(gains, min_gain, min_gain);
			iY = subtractScalar(multiplyScalar(iY,momentum), multiplyScalar(multiplyScalar(copy(gains), dY), eta));
			// iY.writeMatrix("iY-MatrixOps-"+iter);
			Y = addScalar(Y, iY);
			//double [][] tile = tile(mean(Y, 0), n, 1);
			Matrix cMean = columnMean(Y);
			// cMean.writeMatrix("cMean-MatrixOps-"+iter);
			// System.out.println("Y: "+Y.printMatrixInfo());
			Y = subtractScalar(Y , tile(columnMean(Y), n, 1));

			// Y.writeMatrix("Y-MatrixOps-"+iter);

			// Compute current value of cost function
			if ((iter % 100 == 0))   {
				Matrix logdivide = log(divideScalar(copy(P) , Q));
				logdivide = replaceNaN(logdivide,0.0);
				// logdivide.writeMatrix("logdivide-MatrixOps-"+iter);
				double C = sum(multiplyScalar(copy(P) , logdivide));
				//System.out.println("Iteration " + (iter + 1) + ": error is " + C);
				monitor.showMessage(TaskMonitor.Level.INFO, "Iteration " + (iter + 1) + ": error is " + C);
			} else if((iter + 1) % 10 == 0) {
				//System.out.println("Iteration " + (iter + 1));
				// monitor.showMessage(TaskMonitor.Level.INFO, "Iteration " + (iter + 1));
			}

			// Stop lying about P-values
			if (iter == 100)
				P = divideScalar(P , 4);
		}

		return matrix.copy(Y);
	}

	public R Hbeta (Matrix D, double beta){
		Matrix P = exp(multiplyScalar(multiplyScalar(copy(D),beta),-1));
		double sumP = sum(P);   // sumP confirmed scalar
		double H = Math.log(sumP) + beta * sum(multiplyScalar(copy(D),P)) / sumP;
		P = divideScalar(P,sumP);
		R r = new R();
		r.H = H;
		r.P = P;
		return r;
	}

	public R x2p(Matrix X,double tol, double perplexity){
		int n               = X.nRows();
		// Matrix square_X   = square(X);
		Matrix square_X   = powScalar(copy(X), 2);
		square_X.setRowLabels(Arrays.asList(X.getRowLabels()));
		square_X.setColumnLabels(Arrays.asList(X.getColumnLabels()));

		// Matrix sum_X   = sum(square_X, 1);
		Matrix sum_X = rowSum(square_X);
		sum_X.setRowLabels(Arrays.asList(X.getRowLabels()));

		// Matrix times   = scalarMult(times(X, mo.transpose(X)), -2);
		Matrix times   = multiplyScalar(multiplyMatrix(X, transpose(X)), -2);

		Matrix prodSum = addColumnVector(transpose(times), sum_X);

		Matrix D       = addRowVector(prodSum, transpose(sum_X));

		// D seems correct at this point compared to Python version
		Matrix P          = X.like(n, n, 0.0);
		double [] beta    = X.like(n, n, 1.0).toArray()[0];

		double logU         = Math.log(perplexity);
		//System.out.println("Starting x2p...");
		monitor.showMessage(TaskMonitor.Level.INFO, "Starting x2p...");
		for (int i = 0; i < n; i++) {
			if (i % 500 == 0)
				monitor.showMessage(TaskMonitor.Level.INFO, "Computing P-values for point " + i + " of " + n + "...");
				//System.out.println("Computing P-values for point " + i + " of " + n + "...");
				
			double betamin = Double.NEGATIVE_INFINITY;
			double betamax = Double.POSITIVE_INFINITY;
			Matrix Di = getValuesFromRow(D, i, concatenate(range(0,i),range(i+1,n)));


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
	
		//System.out.println("Mean value of sigma: " + sigma);
		monitor.showMessage(TaskMonitor.Level.INFO, "Mean value of sigma: " + sigma);

		return r;
	}

	/**
	 * Modify a matrix such that all of the values are greater than
	 * some minimum value.
	 */
	private Matrix maximum(Matrix matrix, double minvalue) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					if (matrix.doubleValue(row, col) < minvalue)
						matrix.setValue(row, col, minvalue);
				})
			);
		return matrix;
	}

	private void assignAllLessThan(Matrix matrix, double lessthan, double assign) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					if(matrix.doubleValue(row, col) < lessthan) {
						matrix.setValue(row, col, assign);
					}
				})
			);
	}

	// FIXME: should be able to stream this
	private Matrix tile(Matrix matrix, int rowtimes, int coltimes) {
		
		Matrix result=matrix.like(matrix.nRows()*rowtimes,matrix.nColumns()*coltimes);
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (int j = 0; j < matrix.nRows(); j++) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix.nColumns(); l++) {
						result.setValue(resultrow, resultcol++, matrix.doubleValue(j, l));
					}
				}
				resultrow++;
			}
		}
		return result;
	}

	private Matrix replaceNaN(Matrix matrix, double repl) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					if(Double.isNaN(matrix.doubleValue(row, col))) {
						matrix.setValue(row, col, repl);
					} 
				})
			);
		return matrix;
	}

}

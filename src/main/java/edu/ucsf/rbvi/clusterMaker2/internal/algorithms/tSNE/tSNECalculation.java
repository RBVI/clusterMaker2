package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.IntStream;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.printArray;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.ArrayUtils.sqrt;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.addColumnVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.addRowVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.addElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.addScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.columnMean;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.copy;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.diag;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.divideElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.divideScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.expElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.logElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.multiplyElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.multiplyMatrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.multiplyScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.normalize;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.powScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.rowSum;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.setDiag;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.subtractElement;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.subtractScalar;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.sum;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.transpose;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.assignAtIndex;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.assignValuesToRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixIndexUtils.getValuesFromRow;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.BoolMatrixUtils.abs;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.BoolMatrixUtils.biggerThan;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.BoolMatrixUtils.equal;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.BoolMatrixUtils.negate;

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

	public CyMatrix tsne(CyMatrix matrix, int no_dims, int initial_dims, 
	                     double perplexity, int max_iter, boolean use_pca) {

		monitor.setProgress(0.0);

		// For debugging purposes only!
		// matrix.sortByRowLabels(true);

		String IMPLEMENTATION_NAME = this.getClass().getSimpleName();
		monitor.showMessage(TaskMonitor.Level.INFO, "Running " + IMPLEMENTATION_NAME + ".");

		long end = System.currentTimeMillis();
		long start = end;
		long total = end;

		// FIXME: Add prefiltering with PCA
		if(use_pca && matrix.nColumns() > initial_dims && initial_dims > 0) {
			//System.out.println("Using pca");
			monitor.showMessage(TaskMonitor.Level.INFO, "Using pca");
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			matrix = pca.pca(matrix, initial_dims);

			//System.out.println("X:Shape after PCA is = " + matrix.nRows() + " x " + matrix.nColumns());
			monitor.showMessage(TaskMonitor.Level.INFO, 
			                    "X:Shape after PCA is = " + matrix.nRows() + " x " + matrix.nColumns());
		}
		
		int n = matrix.nRows();
		double momentum = .5;
		double initial_momentum = 0.5;
		double final_momentum   = 0.8;
		int eta                 = 500;
		double min_gain         = 0.01;

		Matrix Y           = matrix.like(n, no_dims, Matrix.DISTRIBUTION.NORMAL);
		// Matrix Y           = matrix.like(n, no_dims, 0.5);
		Matrix Ysqlmul     = matrix.like(n, n);
		Matrix dY          = matrix.like(n, no_dims, 0.0);
		Matrix iY          = matrix.like(n, no_dims, 0.0);
		Matrix gains       = matrix.like(n, no_dims, 1.0);
		Matrix btNeg       = matrix.like(n, no_dims);
		Matrix bt          = matrix.like(n, no_dims);

		// Compute P-values
		Matrix P           = x2p(matrix, 1e-5, perplexity).P;
		Matrix Ptr         = transpose(P);
		Matrix diag        = matrix.like(n, n, 0.0);

		addElement(P, Ptr);

		// P.writeMatrix("P1");

		// System.out.format("sum(P) = %.20f\n",sum(P));
		divideScalar(P, sum(P));
		// P.writeMatrix("P2");
		replaceNaN(P, Double.MIN_VALUE);
		// P.writeMatrix("P3");
		multiplyScalar(P, 4.0);		// early exaggeration
		// P.writeMatrix("P4");
		maximum(P, 1e-12);

		// P.writeMatrix("P5");

		//System.out.println("Y:Shape is = " + Y.nRows() + " x " + Y.nColumns());
		monitor.showMessage(TaskMonitor.Level.INFO, "Y:Shape is = " + Y.nRows() + " x " + Y.nColumns());
		
		double progress = 0.0;
		// Run iterations
		for (int iter = 0; iter < max_iter; iter++) {
			// The following matrices are updated (rather than replaced)
			// each cycle:
			// P, Y, num(?), Ysqlmul
			progress = (double)iter/(double)max_iter;
			monitor.setProgress(progress);

			// Compute pairwise affinities
			Matrix sqed = powScalar(copy(Y), 2);
			Matrix sum_Y = transpose(rowSum(sqed));

			// Ysqlmul = Ysqlmul+(-2.0)*Y*trans(Y)
			Ysqlmul = addElement(multiplyScalar(multiplyMatrix(Y, transpose(Y)), -2), Ysqlmul);
			addRowVector(Ysqlmul, sum_Y);
			Ysqlmul = transpose(Ysqlmul);
			addRowVector(Ysqlmul, sum_Y);

			addScalar(Ysqlmul, 1.0);
			divideElement(1.0, Ysqlmul);

			// Ysqlmul.writeMatrix("Ysqlmul");

			Matrix num = copy(Ysqlmul);

			// Set the diagonals to 0
			assignAtIndex(num, range(n), range(n), 0.0);
			// num.writeMatrix("num");
			// System.out.println("sum(num) = "+sum(num));
			Matrix Q = divideScalar(copy(num), sum(num));
			// Q.writeMatrix("Q1");

			Q = maximum(Q, 1e-12);
			// Q.writeMatrix("Q2");

			// Compute gradient
			Matrix L = subtractElement(copy(P), Q);
			L = multiplyElement(L, num);
			// L.writeMatrix("L");
			Matrix rowsum = rowSum(L);
			// rowsum.writeMatrix("rowsum");
			setDiag(diag, rowsum);
			// diag.writeMatrix("diag");
			L = subtractElement(copy(diag), L);
			dY = multiplyMatrix(L, Y);
			multiplyScalar(dY, 4.0);

			// Perform the update
			if (iter < 20)
				momentum = initial_momentum;
			else
				momentum = final_momentum;

			// dY.writeMatrix("dY");
			// iY.writeMatrix("iY");
			boolean[][] boolMtrx = equal(biggerThan(dY, 0.0), biggerThan(iY, 0.0));
			// writeMatrix("boolMtrx-m", boolMtrx);
			btNeg.initialize(boolMtrx.length, boolMtrx[0].length, abs(negate(boolMtrx)));
			bt.initialize(boolMtrx.length, boolMtrx[0].length, abs(boolMtrx));

			// gains.writeMatrix("gains1");
			// btNeg.writeMatrix("btNeg");
			// bt.writeMatrix("bt");

			Matrix gainsSmall = copy(gains);
			Matrix gainsBig = copy(gains);
			addScalar(gainsSmall, 0.2);
			multiplyScalar(gainsBig, 0.8);

			multiplyElement(gainsSmall, btNeg);
			multiplyElement(gainsBig, bt);
			gains = addElement(copy(gainsSmall), gainsBig);
			// gains.writeMatrix("gains2");

			assignAllLessThan(gains, min_gain, min_gain);

			// gains.writeMatrix("gains3");

			multiplyScalar(iY, momentum);
			Matrix gainsdY = multiplyElement(copy(gains), dY);
			// gainsdY.writeMatrix("gainsdY");
			multiplyScalar(gainsdY, eta);
			subtractElement(iY, gainsdY);
			// iY.writeMatrix("iY");
			addElement(Y, iY);
			// Y.writeMatrix("Y2");
			Matrix colMeanY = columnMean(Y);
			// colMeanY.writeMatrix("colMeanY");
			Matrix meanTile = tile(colMeanY, n, 1);
			// meanTile.writeMatrix("meanTile");
			subtractElement(Y, meanTile);

			// Y.writeMatrix("Y3");

			// Compute current value of cost function
			if ((iter % 100 == 0))   {
				Matrix Pdiv = copy(P);
				divideElement(Pdiv, Q);
				Matrix logdivide = logElement(Pdiv);
				replaceNaN(logdivide,Double.MIN_VALUE);
				multiplyElement(logdivide, P);
				replaceNaN(logdivide,Double.MIN_VALUE);
				double C = sum(logdivide);
				end = System.currentTimeMillis();
				//System.out.println("Iteration " + (iter + 1) + ": error is " + C);
				monitor.showMessage(TaskMonitor.Level.INFO, 
					String.format("Iteration %d: error is %f"+
					" (100 iterations in %4.2f seconds)", iter, C, (end-start)/1000.0));
				start = System.currentTimeMillis();
			} /* else if(iter % 10 == 0) {
				end = System.currentTimeMillis();
				monitor.showMessage(TaskMonitor.Level.INFO, 
					"Iteration %d: (10 iterations in %4.2f seconds)",iter,(end-start)/1000);
				//System.out.println("Iteration " + (iter + 1));
				// monitor.showMessage(TaskMonitor.Level.INFO, "Iteration " + (iter + 1));
				start = System.currentTimeMillis();
			} */

			// Stop lying about P-values
			if (iter == 100)
				P = divideScalar(P , 4);
		}

		// Y.writeMatrix("Y");

		end = System.currentTimeMillis();
		monitor.showMessage(TaskMonitor.Level.INFO, 
					String.format("Completed in %4.2f seconds)",(end-total)/1000.0));

		Y.updateMinMax();
		System.out.format("min = %f, max = %f\n",Y.getMinValue(), Y.getMaxValue());
		return matrix.copy(Y);
	}

	public R Hbeta (Matrix D, double beta) {
		Matrix P = copy(D);
		multiplyScalar(P, -beta);
		P = expElement(P);
		double sumP = sum(P);   // sumP confirmed scalar
		Matrix Dd = copy(D);
		multiplyElement(Dd, P);
		double H = Math.log(sumP) + beta * sum(Dd) / sumP;
		P = multiplyScalar(P,1/sumP);
		R r = new R();
		r.H = H;
		r.P = P;
		return r;
	}

	public R x2p(Matrix X,double tol, double perplexity){
		int n               = X.nRows();
		Matrix sum_X        = rowSum(powScalar(copy(X), 2));
		Matrix times        = multiplyScalar(multiplyMatrix(X, transpose(X)), -2);
		Matrix prodSum      = addColumnVector(transpose(times), sum_X);
		Matrix D            = addRowVector(prodSum, transpose(sum_X));
		Matrix P            = X.like(n, n, 0.0);
		double [] beta      = X.like(n, n, 1.0).toArray()[0];
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

		// P.writeMatrix("x2p.P");

		R r = new R();
		r.P = P;
		r.beta = beta;
		double sigma = mean(sqrt(scalarInverse(beta)));
		System.out.println("Mean value of sigma: " + sigma);
	
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
						matrix.setValue(col, col, minvalue);
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

  public static void writeMatrix(String fileName, double[] vector) {
    String filePath = "/tmp/" + fileName;
    try{
      File file = new File(filePath);
      if(!file.exists()) {
        file.createNewFile();
      }
      PrintWriter writer = new PrintWriter(filePath, "UTF-8");
      writer.write(printMatrix(vector));
      writer.close();
    }catch(IOException e){
      e.printStackTrace(System.out);
    }
  }

  public static String printMatrix(double[] vector) {
    StringBuilder sb = new StringBuilder();
    int n = vector.length;
    sb.append("OjAlgo Matrix("+n+")\n\t");
    for (int i = 0; i < n; i++) {
      sb.append("null:\t"); //node.getIdentifier()
      sb.append(""+vector[i]+"\t");
      sb.append("\n");
    }
    return sb.toString();
  }

	public static void writeMatrix(String fileName, boolean[][] matrix) {
    String filePath = "/tmp/" + fileName + "-m";
    try{
      File file = new File(filePath);
      if(!file.exists()) {
        file.createNewFile();
      }
      PrintWriter writer = new PrintWriter(filePath, "UTF-8");
      writer.write(printMatrix(matrix));
      writer.close();
    }catch(IOException e){
      e.printStackTrace(System.out);
    }
  }


	public static String printMatrix(boolean[][] matrix) {
    StringBuilder sb = new StringBuilder();
    int nRows = matrix.length;
    int nColumns = matrix[0].length;
    sb.append("EJML Matrix("+nRows+", "+nColumns+")\n\t");
    for (int col = 0; col < nColumns; col++) {
      sb.append("null\t");
    }
    sb.append("\n");
    for (int row = 0; row < nRows; row++) {
      sb.append("null:\t"); //node.getIdentifier()
      for (int col = 0; col < nColumns; col++) {
        sb.append(""+matrix[row][col]+"\t");
      }
      sb.append("\n");
    }
    return sb.toString();

  }

}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.function.tdouble.DoubleFunction;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleSingularValueDecomposition;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleStatistic;

import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixOps;

public class ColtOps implements MatrixOps {
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	private DenseDoubleEigenvalueDecomposition decomp = null;
	private DenseDoubleSingularValueDecomposition svdDecomp = null;
	private int nThreads = -1;
	final Logger logger = Logger.getLogger(CyUserLog.NAME);

	// For debugging messages
	private static DecimalFormat scFormat = new DecimalFormat("0.###E0");
	private static DecimalFormat format = new DecimalFormat("0.###");
	private final ColtMatrix matrix;

	public ColtOps(ColtMatrix matrix) {
		nThreads = Runtime.getRuntime().availableProcessors()-1;
		this.matrix = matrix;
	}

	public void threshold() {
		threshold(EPSILON);
	}

	public void threshold(final double thresh) {
		getData().forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					if (value <= thresh)
						matrix.setValue(row, column, 0.0);
					return value;
				}
			}
		);
	}

	/**
	 * Create a new matrix that is the transpose of this matrix
	 */
	public Matrix transpose() {
		DenseDoubleAlgebra dda = new DenseDoubleAlgebra();
		DoubleMatrix2D trans = dda.transpose(matrix.data);
		ColtMatrix result = new ColtMatrix(matrix, trans);
		result.transposed = true;
		return result;
	}

	/**
	 * Return the rank order of the columns in a row
	 *
	 * @param row the row to rank the columns in
	 * @return the rank order of the columns
	 */
	public double[] getRank(int row) {
		// Get the masked row
		double[] tData = new double[matrix.nColumns()];
		int nVals = 0;
		for (int column = 0; column < matrix.nColumns(); column++) {
			if (matrix.hasValue(row,column))
				tData[nVals++] = matrix.doubleValue(row,column);
		}
		//System.out.println("Inside getRank; nVals: "+nVals);
		if (nVals == 0)
			return null;

		// Sort the data
		Integer index[] = MatrixUtils.indexSort(tData, nVals);

		// Build a rank table
		double[] rank = new double[nVals];
		for (int i = 0; i < nVals; i++) rank[index[i]] = i;

		// Fix for equal ranks
		int i = 0;
		while (i < nVals) {
			int m = 0;
			double value = tData[index[i]];
			int j = i+1;
			while (j < nVals && tData[index[j]] == value) j++;
			m = j - i; // Number of equal ranks found
			value = rank[index[i]] + (m-1)/2.0;
			for (j = i; j < i+m; j++) rank[index[j]] = value;
			i += m;
		}

		return rank;
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (!matrix.isSymmetrical()) {
			logger.warn("clusterMaker2 ColtMatrix: attempt to invert an assymetric network");
		}

		DenseDoubleAlgebra dda = new DenseDoubleAlgebra();
		DoubleMatrix2D inverse = dda.inverse(getData());
		((ColtMatrix)matrix).data = inverse;
	}

	/**
	 * Normalize the matrix in place
	 * 
	 * TODO: This isn't really matrix normalization.  Do we want to
	 * do real normalization here?
	 */
	public void normalize() {
		double minValue = matrix.getMinValue();
		double span = matrix.getMaxValue() - minValue;
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;
		for (int row = 0; row < matrix.nRows(); row++) {
			for (int col = ((ColtMatrix)matrix).colStart(row); col < matrix.nColumns(); col++) {
				double d = matrix.doubleValue(row, col);
				if (Double.isNaN(d))
					continue;
				double val = (d-minValue)/span;
				matrix.setValue(row, col, val);
				if (matrix.isSymmetrical() && col != row)
					matrix.setValue(col, row, val);
			}
		}
	}

	public void normalizeMatrix() {
		getData().normalize();
		matrix.updateMinMax();
	}

	public double normalizeRow(int row) {
		getData().viewRow(row).normalize();
		return getData().viewRow(row).zSum();
		// matrix.updateMinMax();
	}

	public double normalizeColumn(int column) {
		getData().viewColumn(column).normalize();
		return getData().viewColumn(column).zSum();
		// matrix.updateMinMax();
	}

	public void standardizeRow(int row) {
		double mean = rowMean(row);
		double variance = rowVariance(row, mean);
		double stdev = Math.sqrt(variance);
		for (int column = 0; column < matrix.nColumns(); column++) {
			double cell = matrix.doubleValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	public void standardizeColumn(int column) {
		double mean = columnMean(column);
		double variance = columnVariance(column, mean);
		double stdev = Math.sqrt(variance);
		for (int row = 0; row < matrix.nRows(); row++) {
			double cell = matrix.doubleValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	public void centralizeColumns() {
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;
		for(int col=0;col<matrix.nColumns();col++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int row=0;row<matrix.nRows(); row++){
				double cell = matrix.doubleValue(row, col);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= matrix.nRows();
			for(int row=0;row<matrix.nRows();row++){
				double cell = matrix.doubleValue(row, col);
				if (!Double.isNaN(cell)) {
					cell = cell-mean;
				} else {
					cell = 0.0d;
				}
				matrix.setValue(row, col, cell);
			}
		}
	}

	public void centralizeRows() {
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;
		for(int i=0;i<matrix.nRows();i++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int j=0;j<matrix.nColumns(); j++){
				double cell = matrix.doubleValue(i, j);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= matrix.nColumns();
			for(int j=0;j<matrix.nColumns();j++){
				double cell = matrix.doubleValue(i, j);
				if (!Double.isNaN(cell))
					cell = cell-mean;
				else
					cell = 0.0d;

				matrix.setValue(j, i, cell);
			}
		}
	}

	public double sum() {
		return getData().zSum();
	}

	public double columnSum(int column) {
		return getData().viewColumn(column).zSum();
	}

	public double columnSum2(int column) {
		DoubleMatrix1D colMat = getData().viewColumn(column);
		double rSum2 = colMat.aggregate(DoubleFunctions.plus, DoubleFunctions.square);
		return rSum2;
	}
	
	public double rowSum(int row) {
		return getData().viewRow(row).zSum();
	}
	
	public double rowSum2(int row) {
		DoubleMatrix1D rowMat = getData().viewRow(row);
		double rSum2 = rowMat.aggregate(DoubleFunctions.plus, DoubleFunctions.square);
		return rSum2;
	}

	public double columnMean(int column) {
		double mean = 0.0;
		for(int j=0;j<matrix.nRows(); j++){
			double cell = matrix.doubleValue(j, column);
			if (!Double.isNaN(cell))
				mean += cell;
		}
		return mean/matrix.nRows();
	}

	public double rowMean(int row) {
		double mean = 0.0;
		for(int j=0;j<matrix.nColumns(); j++){
			double cell = matrix.doubleValue(row, j);
			if (!Double.isNaN(cell))
				mean += cell;
		}
		return mean/matrix.nColumns();
	}
	
	public double columnVariance(int column) {
		double mean = columnMean(column);
		return columnVariance(column, mean);
	}

	public double columnVariance(int column, double mean) {
		double variance = 0.0;
		for(int j=0;j<matrix.nRows(); j++){
			double cell = matrix.doubleValue(j, column);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nRows();
	}
	
	public double rowVariance(int row) {
		double mean = rowMean(row);
		return rowVariance(row, mean);
	}

	public double rowVariance(int row, double mean) {
		double variance = 0.0;
		for(int j=0;j<matrix.nColumns(); j++){
			double cell = matrix.doubleValue(row, j);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nColumns();
	}

	// For some reason, the parallelcolt version of zMult doesn't
	// really take advantage of the available cores.  This version does, but
	// it seems like it only works for multiplying matrices of the same
	// size.
	public Matrix multiplyMatrix(Matrix matrix2) {
		// return mult(matrix);
		// if (matrix2.nRows() != matrix.nRows() || matrix2.nColumns() != matrix.nColumns()())
		// 	return mult(matrix);

		DoubleMatrix2D A = getData();
		DoubleMatrix2D B = matrix2.getColtMatrix();

		int m = A.rows();
		int n = A.columns();
		int p = B.columns();

		// Create views into B
		final DoubleMatrix1D[] Brows= new DoubleMatrix1D[n];
		for (int i = n; --i>=0; ) Brows[i] = B.viewRow(i);

		// Create a series of 1D vectors
		final DoubleMatrix1D[] Crows= new DoubleMatrix1D[m];
		for (int i = m; --i>=0; ) Crows[i] = B.like1D(p);

		// Create the thread pools
		final ExecutorService[] threadPools = new ExecutorService[nThreads];
		for (int pool = 0; pool < threadPools.length; pool++) {
				threadPools[pool] = Executors.newFixedThreadPool(1);
		}

		A.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {

					Runnable r = new ThreadedDotProduct(value, Brows[column], Crows[row]);
					threadPools[row%nThreads].submit(r);
					return value;
				}
			}
		);

		for (int pool = 0; pool < threadPools.length; pool++) {
			threadPools[pool].shutdown();
			try {
				boolean result = threadPools[pool].awaitTermination(7, TimeUnit.DAYS);
			} catch (Exception e) {}
		}
		// Recreate C
		return new ColtMatrix(matrix, create2DMatrix(Crows));
	}

	/**
	 * add a value to all cells in the matrix
	 * 
	 * @param value to add to each cell
	 */
	public void addScalar(double value) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					return v+value;
				}
			}
		);
	}

	/**
	 * add two matrices together
	 * 
	 * @param addend the matrix to add to our matrix
	 */
	public void addElement(Matrix addend) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					double addendValue = addend.doubleValue(row, column);
					if (!Double.isNaN(addendValue))
						return v+addendValue;
					else
						return v;
				}
			}
		);
	}

	/**
	 * subtract a value from all cells in the matrix
	 * 
	 * @param value to subtract from each cell
	 */
	public void subtractScalar(double value) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					return v-value;
				}
			}
		);
	}

	/**
	 * subtract a matrix from this matrix
	 * 
	 * @param subtrahend the matrix to subtract from our matrix
	 */
	public void subtractElement(Matrix subtrahend) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					double subValue = subtrahend.doubleValue(row, column);
					if (!Double.isNaN(subValue))
						return v-subValue;
					else
						return v;
				}
			}
		);
	}

	/**
	 * multiple all cells in the matrix by a value
	 * 
	 * @param matrix our matrix
	 * @param value to multiply each cell by
	 */
	public void multiplyScalar(double value) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					return v*value;
				}
			}
		);
	}

	/**
	 * divide all cells in the matrix by a value
	 * 
	 * @param matrix our matrix
	 * @param value to divide each cell by
	 */
	public void divideScalar(double value) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					return v/value;
				}
			}
		);
	}

	/**
	 * divide all cells in a column by a value.  This is used
	 * primarily for normalization when the current sum
	 * of the column is already known.
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param column the column we're dividing
	 * @param value to divide each cell in the column by
	 */
	public void divideScalarColumn(int column, double value) {
		DoubleMatrix1D col = getData().viewColumn(column);
		col.assign(new DoubleFunction() {
					public double apply(double v) {
						return v/value;
					}
				}
			);
	}

	/**
	 * raise all cells in the matrix by a power
	 * 
	 * @param matrix our matrix
	 * @param value power to raise to each cell
	 */
	public void powScalar(double value) {
		DoubleMatrix2D data = getData();
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double v) {
					return Math.pow(v,value);
				}
			}
		);
	}

	private DoubleMatrix2D create2DMatrix (DoubleMatrix1D[] rows) {
		int columns = (int)rows[0].size();
		DoubleMatrix2D C = DoubleFactory2D.sparse.make(rows.length, columns);
		for (int row = 0; row < rows.length; row++) {
			for (int col = 0; col < columns; col++) {
				double value = rows[row].getQuick(col);
				if (value != 0.0)
					C.setQuick(row, col, value);
			}
		}
		return C;
	}

	public Matrix covariance() {
		// We want a dense matrix for this
		DoubleMatrix2D data = DoubleFactory2D.dense.make(getData().toArray());
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(data);
		return matrix.copyDataFromMatrix(matrix2D);
	}

	public Matrix correlation() {
		DoubleMatrix2D data = DoubleFactory2D.dense.make(getData().toArray());
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(data);
		matrix2D = DoubleStatistic.correlation(matrix2D);
		return matrix.copyDataFromMatrix(matrix2D);
	}

	public void eigenInit(){
		decomp = null;
	}

	public double[] eigenValues(boolean nonZero){
		if (decomp == null) {
			decomp = new DenseDoubleEigenvalueDecomposition(getData());
		}

		double[] allValues = decomp.getRealEigenvalues().toArray();
		if (!nonZero)
			return allValues;

		int size = 0;
		for (double d: allValues) {
			if (Math.abs(d) > EPSILON)size++;
		}
		double [] nonZ = new double[size];
		int index = 0;
		for (double d: allValues) {
			if (Math.abs(d) > EPSILON)
				nonZ[index++] = d;
		}

		return nonZ;
	}

	public double[][] eigenVectors() {
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(getData());

		DoubleMatrix2D eigv = decomp.getV();
		System.out.println("Found "+eigv.columns()+" eigenvectors");
		return eigv.toArray();
	}

	public void svdInit(){
		svdDecomp = null;
	}

	public Matrix svdU() {
		if (svdDecomp == null) {
			svdDecomp= new DenseDoubleSingularValueDecomposition(getData(), true, false);
		}
		return wrap(svdDecomp.getU());
	}

	public Matrix svdS() {
		if (svdDecomp == null) {
			svdDecomp= new DenseDoubleSingularValueDecomposition(getData(), true, false);
		}
		return wrap(svdDecomp.getS());
	}

	public Matrix svdV() {
		if (svdDecomp == null) {
			svdDecomp= new DenseDoubleSingularValueDecomposition(getData(), true, false);
		}
		return wrap(svdDecomp.getV());
	}

	public int cardinality() { return getData().cardinality(); }

	public Matrix mult(Matrix b) {
		/*
		DoubleMatrix2D aMat = data;
		DoubleMatrix2D bMat = b.getColtMatrix();
		DoubleMatrix2D cMat = DoubleFactory2D.sparse.make(nRows, nColumns);
		// System.out.println("aMat ("+aMat.rows()+", "+aMat.columns()+")");
		// System.out.println("bMat ("+bMat.rows()+", "+bMat.columns()+")");
		// System.out.println("cMat ("+cMat.rows()+", "+cMat.columns()+")");
		blas.dgemm(false, false, 1.0, aMat, bMat, 0.0, cMat);
		ColtMatrix c = new ColtMatrix(this, cMat);
		*/
		DoubleMatrix2D cMat;
		if (getData().getClass().getName().indexOf("Sparse") >= 0)
			cMat = DoubleFactory2D.sparse.make(matrix.nRows(), b.nColumns());
		else
			cMat = DoubleFactory2D.dense.make(matrix.nRows(), b.nColumns());
		getData().zMult(b.getColtMatrix(), cMat);
		ColtMatrix c = new ColtMatrix(matrix, cMat);
		return c;
	}

	private DoubleMatrix2D getData() {
		return matrix.getColtMatrix();
	}

	private Matrix wrap(DoubleMatrix2D mat) {
		ColtMatrix result = new ColtMatrix();
		result.data = mat;
		result.nRows = mat.rows();
		result.nColumns = mat.columns();
		return result;
	}

	private class ThreadedDotProduct implements Runnable {
		double value;
		DoubleMatrix1D Bcol;
		DoubleMatrix1D Crow;
		// final cern.jet.math.PlusMult fun = cern.jet.math.PlusMult.plusMult(0);

		ThreadedDotProduct(double value, DoubleMatrix1D Bcol, 
		                   DoubleMatrix1D Crow) {
			this.value = value;
			this.Bcol = Bcol;
			this.Crow = Crow;
		}

		public void run() {
			// fun.multiplicator = value;
			for (int k = 0; k < Bcol.size(); k++) {
				if (Bcol.getQuick(k) != 0.0) {
					Crow.setQuick(k, Crow.getQuick(k)+Bcol.getQuick(k)*value);
				}
			}
			// Crow.assign(Bcol, fun);
		}
	}
}

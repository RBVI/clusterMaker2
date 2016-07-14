package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleStatistic;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class ColtMatrix implements Matrix {
	protected DoubleMatrix2D data;
	protected SmpDoubleBlas blas;
	protected int[] index = null;
	protected int nRows;
	protected int nColumns;
	protected String[] rowLabels = null;
	protected String[] columnLabels = null;
	protected double maxValue = Double.MIN_VALUE;
	protected double minValue = Double.MAX_VALUE;
	protected boolean symmetric = false;
	protected boolean transposed = false;
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	private DenseDoubleEigenvalueDecomposition decomp = null;
	private int nThreads = -1;
	final Logger logger = Logger.getLogger(CyUserLog.NAME);

	// For debugging messages
	private static DecimalFormat scFormat = new DecimalFormat("0.###E0");
	private static DecimalFormat format = new DecimalFormat("0.###");

	public ColtMatrix() {
		blas = new SmpDoubleBlas();
		nThreads = Runtime.getRuntime().availableProcessors()-1;
	}

	public ColtMatrix(ColtMatrix mat) {
		this();
		data = mat.data.copy();
		nRows = data.rows();
		nColumns = data.columns();
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		minValue = mat.minValue;
		maxValue = mat.maxValue;
		if (mat.rowLabels != null)
			rowLabels = Arrays.copyOf(mat.rowLabels, mat.rowLabels.length);
		if (mat.columnLabels != null)
			columnLabels = Arrays.copyOf(mat.columnLabels, mat.columnLabels.length);

		if (mat.index != null)
			index = Arrays.copyOf(mat.index, mat.index.length);
	}

	public ColtMatrix(int rows, int columns) {
		this();
		data = DoubleFactory2D.sparse.make(rows,columns);
		nRows = rows;
		nColumns = columns;
		rowLabels = new String[rows];
		columnLabels = new String[columns];
		index = null;
	}

	public ColtMatrix(ColtMatrix mat, DoubleMatrix2D data) {
		this();
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		if (mat.rowLabels != null)
			rowLabels = Arrays.copyOf(mat.rowLabels, mat.rowLabels.length);
		if (mat.columnLabels != null)
			columnLabels = Arrays.copyOf(mat.columnLabels, mat.columnLabels.length);

		this.data = data;
		nRows = data.rows();
		nColumns = data.columns();
		updateMinMax();
	}

	public ColtMatrix(SimpleMatrix mat) {
		this();
		data = DoubleFactory2D.sparse.make(mat.toArray());
		nRows = data.rows();
		nColumns = data.columns();
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		minValue = mat.minValue;
		maxValue = mat.maxValue;
		if (mat.index != null)
			index = Arrays.copyOf(mat.index, mat.index.length);
	}

	public void initialize(int rows, int columns, double[][] arrayData) {
		if (arrayData != null) {
			data = DoubleFactory2D.sparse.make(arrayData);
		} else {
			data = DoubleFactory2D.sparse.make(rows, columns);
		}
		nRows = data.rows();
		nColumns = data.columns();
		transposed = false;
		symmetric = false;
		rowLabels = new String[nRows];
		columnLabels = new String[nColumns];
		updateMinMax();
	}

	public void initialize(int rows, int columns, Double[][] arrayData) {
		data = DoubleFactory2D.sparse.make(rows, columns);
		nRows = data.rows();
		nColumns = data.columns();
		if (arrayData != null) {
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < columns; col++) {
					setValue(row, col, arrayData[row][col]);
				}
			}
		}
		transposed = false;
		symmetric = false;
		rowLabels = new String[nRows];
		columnLabels = new String[nColumns];
		updateMinMax();
	}

	/**
	 * Return the number of rows in this matrix.
	 *
	 * @return number of rows
	 */
	public int nRows() { 
		if (index != null)
			return index.length;
		return nRows; 
	}

	/**
	 * Return the number of columns in this matrix.
	 *
	 * @return number of columns
	 */
	public int nColumns() { return nColumns; }

	/**
	 * Return the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return the (possibly null) value at that location
	 */
	public Double getValue(int row, int column) { 
		Double d;
		if (index == null)
			d = data.getQuick(row, column);
		else
			d = data.getQuick(index[row], index[column]);
		if (Double.isNaN(d))
			return null;
		return d;
	}

	/**
	 * Return the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return the value at that location, if it was set, otherwise, return Double.NaN.
	 */
	public double doubleValue(int row, int column) {
		Double d = getValue(row, column);
		if (d == null) return Double.NaN;
		return d.doubleValue();
	}

	/**
	 * Set the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @param value the value to set
	 */
	public void setValue(int row, int column, double value) {
		if (value < minValue) minValue = value;
		if (value > maxValue) maxValue = value;

		if (index != null) {
			row = index[row];
			column = index[column];
		}

		data.setQuick(row, column, value);
	}

	/**
	 * Set the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @param value the value to set
	 */
	public void setValue(int row, int column, Double value) {
		if (value < minValue) minValue = value;
		if (value > maxValue) maxValue = value;

		if (index != null) {
			row = index[row];
			column = index[column];
		}

		if (value == null)
			data.setQuick(row, column, Double.NaN);
		else
			data.setQuick(row, column, value);
	}

	/**
	 * Return true if the location has a value
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return true if this location has a value, false otherwise
	 */
	public boolean hasValue(int row, int column) {
		Double d = getValue(row, column);
		if (d == null)
			return false;
		return true;
	}
	
	/**
	 * Return an array of column labels
	 *
	 * @return the column labels
	 */
	public String[] getColumnLabels() {
		return columnLabels;
	}

	/**
	 * Return a column label
	 *
	 * @param col the column to get the label for
	 * @return the column label
	 */
	public String getColumnLabel(int col) {
		if (index != null)
			col = index[col];
		return columnLabels[col];
	}

	/**
	 * Set a column label
	 *
	 * @param col the column to set the label for
	 * @param label the column label
	 */
	public void setColumnLabel(int col, String label) {
		if (index != null)
			col = index[col];
		columnLabels[col] = label;
	}

	/**
	 * Set the column labels
	 *
	 * @param labelList the list of column labels
	 */
	public void setColumnLabels(List<String>labelList) {
		columnLabels = labelList.toArray(new String[0]);
	}

	/**
	 * Return an array of row labels
	 *
	 * @return the row labels
	 */
	public String[] getRowLabels() {
		return rowLabels;
	}
	
	/**
	 * Return a row label
	 *
	 * @param row the row to get the label for
	 * @return the row label
	 */
	public String getRowLabel(int row) {
		if (index != null)
			row = index[row];
		return rowLabels[row];
	}

	/**
	 * Set a row label
	 *
	 * @param row the row to set the label for
	 * @param label the row label
	 */
	public void setRowLabel(int row, String label) {
		if (index != null)
			row = index[row];
		rowLabels[row] = label;
	}
	
	/**
	 * Set the row labels
	 *
	 * @param labelList the list of row labels
	 */
	public void setRowLabels(List<String>labelList) {
		rowLabels = labelList.toArray(new String[0]);
	}

	/**
	 * Return the distance between rows based on the metric.
	 *
	 * @param metric the metric to use to calculate the distances
	 * @return a new Matrix of the distances between the rows
	 */
	public Matrix getDistanceMatrix(DistanceMetric metric) {
		ColtMatrix mat = new ColtMatrix(nRows, nRows);
		mat.transposed = false;
		mat.symmetric = true;
		mat.rowLabels = Arrays.copyOf(rowLabels, rowLabels.length);
		mat.columnLabels = Arrays.copyOf(rowLabels, rowLabels.length);

		for (int row = 0; row < nRows; row++) {
			for (int column = row; column < this.nRows; column++) {
				mat.setValue(row, column, metric.getMetric(this, this, row, column));
				if (row != column)
					mat.setValue(column, row, metric.getMetric(this, this, row, column));
			}
		}
		return mat;
	}
 
	/**
	 * Return a 2D array with all of the values in the matrix.  The missing
	 * values are set to Double.NaN
	 *
	 * @return the data in the matrix
	 */
	public double[][] toArray() {
		double doubleData[][] = new double[nRows][nColumns];
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				doubleData[row][col] = doubleValue(row, col);
				if (symmetric && row != col)
					doubleData[col][row] = doubleValue(row, col);
			}
		}
		return doubleData;
	}

	/**
	 * Return the maximum value in the matrix
	 *
	 * @return the max value
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Return the minimum value in the matrix
	 *
	 * @return the min value
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * Return true if the matrix is transposed
	 *
	 * @return true if the matrix is transposed
	 */
	public boolean isTransposed() {
		return transposed;
	}

	/**
	 * Set true if the matrix is transposed
	 *
	 * @param transposed true if the matrix is transposed
	 */
	public void setTransposed(boolean transposed) {
		this.transposed = transposed;
	}

	/**
	 * Return true if the matrix is symmetraical
	 *
	 * @return true if the matrix is symmetraical
	 */
	public boolean isSymmetrical() {
		return symmetric;
	}

	/**
	 * Set true if the matrix is symmetrical
	 *
	 * @param symmetrical true if the matrix is symmetrical
	 */
	public void setSymmetrical(boolean symmetrical) {
		this.symmetric = symmetrical;
	}

	/**
	 * Set all missing values to zero
	 */
	public void setMissingToZero() {
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				if (getValue(row, col) == null) {
					data.setQuick(row, col, 0.0d);
					if (symmetric && row != col)
						data.setQuick(col, row, 0.0d);
				}
			}
		}
	}

	/**
	 * Adjust the diagonals
	 */
	public void adjustDiagonals() {
		for (int row = 0; row < nRows; row++ ) {
			double max = 0.0;
			for (int col = 0; col < nColumns; col++ ) {
				if (data.get(row,col) > max) max = data.get(row,col);
			}
			data.setQuick(row, row, max);
		}
	}

	public void threshold() {
		threshold(EPSILON);
	}

	public void threshold(final double thresh) {
		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					if (value <= thresh)
						setValue(row, column, 0.0);
					return value;
				}
			}
		);
	}

	/**
	 * Return the rank order of the columns in a row
	 *
	 * @param row the row to rank the columns in
	 * @return the rank order of the columns
	 */
	public double[] getRank(int row) {
		// Get the masked row
		double[] tData = new double[nColumns];
		int nVals = 0;
		for (int column = 0; column < nColumns; column++) {
			if (hasValue(row,column))
				tData[nVals++] = doubleValue(row,column);
		}
		//System.out.println("Inside getRank; nVals: "+nVals);
		if (nVals == 0)
			return null;

		// Sort the data
		Integer index[] = MatrixUtils.indexSort(tData,nVals);

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
	 * Create an index on the matrix
	 */
	public void index() {
		if (!symmetric) {
			// Can't index a non-symmetric matrix!
			logger.warn("clusterMaker2 ColtMatrix: attempt to index an assymetric network");
			return;
		}

		// initialize indexing array to original order
		index = new int[nRows];
		for (int i = 0; i < index.length; ++i) {
			index[i] = i;
		}
	}
	
	/**
	 * Create a shallow copy of the matrix with an alternative
	 * index.  This is an efficient way to access submatrices
	 */
	public Matrix submatrix(int[] index) {
		ColtMatrix mat = new ColtMatrix();
		mat.data = data;
		mat.index = index;
		mat.nRows = nRows;
		mat.nColumns = nColumns;
		mat.symmetric = symmetric;
		mat.transposed = transposed;
		mat.rowLabels = rowLabels;
		mat.columnLabels = columnLabels;
		mat.maxValue = maxValue;
		mat.minValue = minValue;
		return mat;
	}

	/**
	 * Return a submatrix
	 * 
	 * @param row the starting row of the submatrix
	 * @param col the starting column of the submatrix
	 * @param rows the number of rows
	 * @param cols the number of columnss
	 * @return submatrix
	 */
	public Matrix submatrix(int row, int col, int rows, int cols) {
		ColtMatrix newMatrix = new ColtMatrix(rows, cols);
		newMatrix.data = data.viewPart(row, col, rows, cols);
		double newMin = Double.MAX_VALUE;
		double newMax = Double.MIN_VALUE;
		for (int r = 0; r < rows; r++) {
			newMatrix.setRowLabel(r, rowLabels[r+row]);
			for (int c = 0; c < cols; c++) {
				if (r == 0)
					newMatrix.setColumnLabel(c, columnLabels[c+col]);
			}
		}
		newMatrix.minValue = newMin;
		newMatrix.maxValue = newMax;

		if (transposed)
			newMatrix.setTransposed(transposed);
		if (symmetric && rows == cols && row == col)
			newMatrix.setSymmetrical(symmetric);
		return newMatrix;
	}

	/**
	 * Return a copy of the Matrix
	 *
	 * @return matrix copy
	 */
	public Matrix copy() {
		return new ColtMatrix(this);
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (!symmetric) {
			logger.warn("clusterMaker2 ColtMatrix: attempt to invert an assymetric network");
		}

		DenseDoubleAlgebra dda = new DenseDoubleAlgebra();
		DoubleMatrix2D inverse = dda.inverse(data);
		data = inverse;
	}

	/**
	 * Normalize the matrix in place
	 * 
	 * TODO: This isn't really matrix normalization.  Do we want to
	 * do real normalization here?
	 */
	public void normalize() {
		double span = maxValue - minValue;
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				Double d = getValue(row, col);
				if (d == null)
					continue;
				setValue(row, col, (d-minValue)/span);
				if (symmetric && col != row)
					setValue(col, row, (d-minValue)/span);
			}
		}
		updateMinMax();
	}

	public void normalizeMatrix() {
		data.normalize();
		updateMinMax();
	}

	public void normalizeRow(int row) {
		data.viewRow(row).normalize();
		updateMinMax();
	}

	public void normalizeColumn(int column) {
		data.viewColumn(column).normalize();
		updateMinMax();
	}

	public void standardizeRow(int row) {
		double mean = rowMean(row);
		double variance = rowVariance(row, mean);
		double stdev = Math.sqrt(variance);
		for (int column = 0; column < nColumns; column++) {
			double cell = this.getValue(row, column);
			this.setValue(row, column, (cell-mean)/stdev);
		}
		updateMinMax();
	}

	public void standardizeColumn(int column) {
		double mean = columnMean(column);
		double variance = columnVariance(column, mean);
		double stdev = Math.sqrt(variance);
		for (int row = 0; row < nRows; row++) {
			double cell = this.getValue(row, column);
			this.setValue(row, column, (cell-mean)/stdev);
		}
		updateMinMax();
	}

	public void centralizeColumns() {
		for(int i=0;i<nColumns;i++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int j=0;j<nRows; j++){
				double cell = this.getValue(j, i);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= nRows;
			for(int j=0;j<nRows;j++){
				double cell = this.getValue(j, i);
				if (!Double.isNaN(cell))
					this.setValue(j, i, cell - mean);
				else
					this.setValue(i, j, 0.0d);
			}
		}
		updateMinMax();
	}

	public void centralizeRows() {
		for(int i=0;i<nRows;i++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int j=0;j<nColumns; j++){
				double cell = this.getValue(i, j);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= nColumns;
			for(int j=0;j<nColumns;j++){
				double cell = this.getValue(i, j);
				if (!Double.isNaN(cell))
					this.setValue(i, j, cell - mean);
				else
					this.setValue(i, j, 0.0d);
			}
		}
		updateMinMax();
	}

	public double columnSum(int column) {
		return data.viewColumn(column).zSum();
	}
	
	public double rowSum(int row) {
		return data.viewRow(row).zSum();
	}

	public double columnMean(int column) {
		double mean = 0.0;
		for(int j=0;j<nRows; j++){
			double cell = this.getValue(j, column);
			if (!Double.isNaN(cell))
				mean += cell;
		}
		return mean/nRows;
	}

	public double rowMean(int row) {
		double mean = 0.0;
		for(int j=0;j<nColumns; j++){
			double cell = this.getValue(row, j);
			if (!Double.isNaN(cell))
				mean += cell;
		}
		return mean/nColumns;
	}
	
	public double columnVariance(int column) {
		double mean = columnMean(column);
		return columnVariance(column, mean);
	}

	public double columnVariance(int column, double mean) {
		double variance = 0.0;
		for(int j=0;j<nRows; j++){
			double cell = this.getValue(j, column);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/nRows;
	}
	
	public double rowVariance(int row) {
		double mean = rowMean(row);
		return rowVariance(row, mean);
	}

	public double rowVariance(int row, double mean) {
		double variance = 0.0;
		for(int j=0;j<nColumns; j++){
			double cell = this.getValue(row, j);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/nColumns;
	}

	// For some reason, the parallelcolt version of zMult doesn't
	// really take advantage of the available cores.  This version does, but
	// it seems like it only works for multiplying matrices of the same
	// size.
	public Matrix multiplyMatrix(Matrix matrix) {
		// return mult(matrix);
		// if (matrix.nRows() != nRows() || matrix.nColumns() != nColumns())
		// 	return mult(matrix);

		DoubleMatrix2D A = data;
		DoubleMatrix2D B = matrix.getColtMatrix();

		int m = A.rows();
		int n = A.columns();
		int p = B.columns();

		System.out.println("multiplyMatrix: m="+m+", n="+n+", p="+p);

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
		return new ColtMatrix(this, create2DMatrix(Crows));
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
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(data);
		return copyDataFromMatrix(matrix2D);
	}

	public Matrix correlation() {
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(data);
		matrix2D = DoubleStatistic.correlation(matrix2D);
		return copyDataFromMatrix(matrix2D);
	}

	public double[] eigenValues(boolean nonZero){
		if (decomp == null) {
			decomp = new DenseDoubleEigenvalueDecomposition(data);
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

	public double[][] eigenVectors(){
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(data);

		DoubleMatrix2D eigv = decomp.getV();
		System.out.println("Found "+eigv.columns()+" eigenvectors");
		return eigv.toArray();
	}

	public DoubleMatrix2D getColtMatrix() {
		return data;
	}

	public int cardinality() { return data.cardinality(); }

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
		DoubleMatrix2D cMat = DoubleFactory2D.sparse.make(nRows, b.nColumns());
		data.zMult(b.getColtMatrix(), cMat);
		ColtMatrix c = new ColtMatrix(this, cMat);
		return c;
	}

	public Matrix gowers() {
		// Create the Identity matrix
		DoubleMatrix2D I = DoubleFactory2D.sparse.identity(this.nRows());

		// Create the ones matrix.  This is equivalent to 11'/n
		DoubleMatrix2D one = DoubleFactory2D.dense.make(this.nRows(), this.nRows(), 1.0/this.nRows());

		// Create the subtraction matrix (I-11'/n)
		DoubleMatrix2D mat = I.assign(one, DoubleFunctions.minus);

		// Create our data matrix
		final DoubleMatrix2D A = DoubleFactory2D.sparse.make(this.nRows(), this.nRows());

		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					A.setQuick(row, column, -Math.pow(value,2)/2.0);
					return value;
				}
			}
		);

		ColtMatrix cMat = new ColtMatrix(this, mat);
		ColtMatrix cA = new ColtMatrix(this, A);

		// Finally, the Gower's matrix is mat*A*mat
		
		Matrix mat1 = cMat.multiplyMatrix(cA);
		return mat1.multiplyMatrix(cMat);
	}

	/**
	 * Debugging routine to print out information about a matrix
	 *
	 * @param matrix the matrix we're going to print out information about
	 */
	public String printMatrixInfo() {
		String s = "Colt Matrix("+data.rows()+", "+data.columns()+")\n";
		if (data.getClass().getName().indexOf("Sparse") >= 0)
			s += " matrix is sparse\n";
		else
			s += " matrix is dense\n";
		s += " cardinality is "+data.cardinality()+"\n";
		return s;
	}

	public String printMatrix() {
		StringBuilder sb = new StringBuilder();
		sb.append("ColtMatrix("+nRows+", "+nColumns+")\n");
		if (data.getClass().getName().indexOf("Sparse") >= 0)
			sb.append(" matrix is sparse\n");
		else
			sb.append(" matrix is dense\n");
		sb.append(" cardinality is "+data.cardinality()+"\n\t");

		for (int col = 0; col < nColumns; col++) {
			sb.append(getColumnLabel(col)+"\t");
		}
		sb.append("\n");
		for (int row = 0; row < nRows; row++) {
			sb.append(getRowLabel(row)+":\t"); //node.getIdentifier()
			for (int col = 0; col < nColumns; col++) {
				double value = getValue(row, col);
				if (value < 0.001)
					sb.append(""+scFormat.format(value)+"\t");
				else
					sb.append(""+format.format(value)+"\t");
			} 
			sb.append("\n");
		} 
		return sb.toString();
	}

	public void writeMatrix(String fileName) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String filePath = tmpDir + File.separator + fileName;
		try{
			File file = new File(filePath);
			if(!file.exists()) {
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");
			writer.write(printMatrix());
			writer.close();
		}catch(IOException e){
			e.printStackTrace(System.out);
		}
	}

	public SimpleMatrix getSimpleMatrix() {
		SimpleMatrix sm = new SimpleMatrix(nRows, nColumns);
		double[][] inputData = toArray();
		for (int row = 0; row < nRows; row++) {
			for (int column = 0; column < nColumns; column++) {
				sm.data[row][column] = inputData[row][column];
			}
		}
		sm.transposed = this.transposed;
		sm.symmetric = this.symmetric;
		sm.minValue = this.minValue;
		sm.maxValue = this.maxValue;
		sm.index = Arrays.copyOf(this.index, this.index.length);
		return sm;
	}
	
	private Matrix copyDataFromMatrix(DoubleMatrix2D matrix2D) {
		ColtMatrix mat = new ColtMatrix(matrix2D.rows(), matrix2D.columns());
		mat.symmetric = true;
		mat.data = matrix2D;
		String[] labels;
		if (this.transposed)
			labels = rowLabels;
		else
			labels = columnLabels;
		if (labels != null) {
			mat.rowLabels = Arrays.copyOf(labels, labels.length);
			mat.columnLabels = Arrays.copyOf(labels, labels.length);
		}
		mat.updateMinMax();
		return mat;
	}

	private void updateMinMax() {
		double[] min = data.getMinLocation();
		double[] max = data.getMaxLocation();
		minValue = min[0];
		maxValue = max[0];
	}

	private int colStart(int row) {
		if (!symmetric) return 0;
		return row;
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

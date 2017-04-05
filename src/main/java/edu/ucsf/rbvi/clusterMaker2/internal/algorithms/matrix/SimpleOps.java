package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixOps;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleStatistic;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;

public class SimpleOps implements MatrixOps {
	protected SmpDoubleBlas blas;
	private DenseDoubleEigenvalueDecomposition decomp = null;
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	final Logger logger = Logger.getLogger(CyUserLog.NAME);
	private final SimpleMatrix matrix;

	public SimpleOps(SimpleMatrix mat) {
		this.matrix = mat;
		blas = new SmpDoubleBlas();
	}

	public void threshold() {
		threshold(EPSILON);
	}

	public void threshold(double thresh) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					if (matrix.getValue(row, column) <= thresh) {
						matrix.setValue(row, column, 0.0);
					}
				}));
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (matrix.nRows() != matrix.nColumns()) {
			logger.warn("clusterMaker2 SimpleMatrix: attempt to invert an assymetric network");
		}
		int nRows = matrix.nRows();
		int nColumns = matrix.nColumns();

		double b[][] = new double[nRows][nColumns];
		double x[][] = new double[nRows][nColumns];
		int idx[] = new int[nRows];

		// Create identity matrix
		IntStream.range(0, nRows).parallel()
			.forEach(row -> IntStream.range(0, nColumns)
				.forEach(column -> {
					if (row == column)
						b[row][column] = 1.0d;
					else
						b[row][column] = 0.0d;
				}));

		// Transform the matrix into an upper triangle
		gaussian(matrix.data, idx);

		// Update the matrix b[i][j] with the ratios stored
		for (int i = 0; i < nRows-1; ++i) {
			for (int j=i+1; j < nRows; ++j) {
				for (int k=0; k < nRows; ++k) {
					b[idx[j]][k] -= matrix.data[idx[j]][i]*b[idx[i]][k];
				}
			}
		}

		// Perform backward substitution
		for (int i = 0; i < nRows; ++i) {
			x[nRows-1][i] = b[idx[nRows-1]][i]/
			                      matrix.data[idx[nRows-1]][nRows-1];
			for (int j = nRows-2; j >=0; --j) {
				x[j][i] = b[idx[j]][i];
				for (int k = j+1; k < nRows; ++k) {
					x[j][i] -= matrix.data[idx[j]][k]*x[k][i];
				}

				x[j][i] /= matrix.data[idx[j]][j];
			}
		}
		matrix.data = x;
	}

	/**
	 * Normalize the matrix in place
	 */
	public void normalize() {
		double span = matrix.maxValue - matrix.minValue;
		double min = matrix.minValue;
		double max = matrix.maxValue;
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(col -> {
					double d = matrix.getValue(row, col);
					if (!Double.isNaN(d)) {
						matrix.setValue(row, col, (d-min)/span);
						if (matrix.symmetric && col != row)
							matrix.setValue(col, row, (d-min)/span);
					}
				}));
	}

	/**
	 * Normalize the matrix in place.  This is actual matrix normalization,
	 * i.e. all cells sum to 1.0
	 */
	public void normalizeMatrix() {
		double sum = sum();
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;

		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(col -> {
						double d = matrix.getValue(row, col);
						if (!Double.isNaN(d)) {
							matrix.setValue(row, col, d/sum);
							if (matrix.symmetric && col != row)
								matrix.setValue(col, row, d/sum);
						}
					}));
	}

	/**
	 * Normalize a matrix row in place (all columns in the row sum to 1.0)
	 *
	 * @param row the row to normalize
	 */
	public void normalizeRow(int row) {
		double sum = Arrays.stream(matrix.data[row])
									.filter(v -> !Double.isNaN(v))
									.sum();

		IntStream.range(0, matrix.nColumns())
						.forEach(col -> {
										double val = matrix.getValue(row, col);
										if (!Double.isNaN(val))
											matrix.setValue(row, col, val/sum);
						});

		matrix.updateMinMax();
	}

	/**
	 * Normalize a matrix column in place (all rows in the column sum to 1.0)
	 *
	 * @param column the column to normalize
	 */
	public void normalizeColumn(int column) {
		double sum = Arrays.stream(matrix.data)
									.mapToDouble(arr -> arr[column])
									.filter(v -> !Double.isNaN(v))
									.sum();

		IntStream.range(0, matrix.nRows())
						.forEach(row -> {
										double val = matrix.getValue(row, column);
										if (!Double.isNaN(val))
											matrix.setValue(row, column, val/sum);
						});
		matrix.updateMinMax();
	}

	public void standardizeRow(int row) {
		double mean = rowMean(row);
		double variance = rowVariance(row, mean);
		double stdev = Math.sqrt(variance);
		for (int column = 0; column < matrix.nColumns(); column++) {
			double cell = matrix.getValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	public void standardizeColumn(int column) {
		double mean = columnMean(column);
		double variance = columnVariance(column, mean);
		double stdev = Math.sqrt(variance);
		for (int row = 0; row < matrix.nRows(); row++) {
			double cell = matrix.getValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	public void centralizeColumns() {
		for(int i=0;i<matrix.nColumns();i++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int j=0;j<matrix.nRows(); j++){
				double cell = matrix.getValue(j, i);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= matrix.nRows();
			for(int j=0;j<matrix.nRows();j++){
				double cell = matrix.getValue(j, i);
				if (!Double.isNaN(cell))
					matrix.setValue(j, i, cell - mean);
				else
					matrix.setValue(i, j, 0.0d);
			}
		}
	}

	public void centralizeRows() {
		for(int i=0;i<matrix.nRows();i++){
			// Replace with parallel function?
			double mean = 0.0;
			for(int j=0;j<matrix.nColumns(); j++){
				double cell = matrix.getValue(i, j);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= matrix.nColumns();
			for(int j=0;j<matrix.nColumns();j++){
				double cell = matrix.getValue(i, j);
				if (!Double.isNaN(cell))
					matrix.setValue(i, j, cell - mean);
				else
					matrix.setValue(i, j, 0.0d);
			}
		}
	}

	public double columnSum(int column) {
		return Arrays.stream(matrix.data)
									.mapToDouble(arr -> arr[column])
									.filter(v -> !Double.isNaN(v))
									.sum();
	}
	
	public double rowSum(int row) {
		return Arrays.stream(matrix.data[row])
										.filter(v -> !Double.isNaN(v))
										.sum();
	}

	public double columnMean(int column) {
		return Arrays.stream(matrix.data)
									.mapToDouble(arr -> arr[column])
									.filter(v -> !Double.isNaN(v))
									.average().getAsDouble();
	}

	public double rowMean(int row) {
		return Arrays.stream(matrix.data[row])
										.filter(v -> !Double.isNaN(v))
										.average().getAsDouble();
	}
	
	public double columnVariance(int column) {
		double mean = columnMean(column);
		return columnVariance(column, mean);
	}

	public double columnVariance(int column, double mean) {
		double variance = 0.0;
		for(int j=0;j<matrix.nRows(); j++){
			double cell = matrix.getValue(j, column);
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
			double cell = matrix.getValue(row, j);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nColumns();
	}

	public DoubleMatrix2D getColtMatrix() {
		DoubleMatrix2D mat = DoubleFactory2D.dense.make(matrix.nRows(), matrix.nColumns());
		mat.assign(matrix.toArray());
		return mat;
	}

	public Matrix multiplyMatrix(Matrix b) {
		return mult(b);
	}

	public void add(double v) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					double value = matrix.getValue(row, column);
					if (!Double.isNaN(value))
						matrix.setValue(row, column, value+v);
				}));
	}

	public void subtract(double v) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					double value = matrix.getValue(row, column);
					if (!Double.isNaN(value))
						matrix.setValue(row, column, value-v);
				}));
	}

	public void multiply(double v) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					double value = matrix.getValue(row, column);
					if (!Double.isNaN(value))
						matrix.setValue(row, column, value*v);
				}));
	}

	public void divide(double v) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					double value = matrix.getValue(row, column);
					if (!Double.isNaN(value))
						matrix.setValue(row, column, value/v);
				}));
	}

	public void pow(double v) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(matrix.colStart(row), matrix.nColumns())
				.forEach(column -> {
					double value = matrix.getValue(row, column);
					if (!Double.isNaN(value))
						matrix.setValue(row, column, Math.pow(value,v));
				}));
	}

	public Matrix covariance() {
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(getColtMatrix());
		return matrix.copyDataFromMatrix(matrix2D);
	}

	public Matrix correlation() {
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(getColtMatrix());
		matrix2D = DoubleStatistic.correlation(matrix2D);
		return matrix.copyDataFromMatrix(matrix2D);
	}

	public double[] eigenValues(boolean nonZero){
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(getColtMatrix());

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
			decomp = new DenseDoubleEigenvalueDecomposition(getColtMatrix());
		return decomp.getV().toArray();
	}

	public Matrix mult(Matrix b) {
		double[][] data = Arrays.stream(matrix.data).map(r ->
			IntStream.range(0, b.nColumns()).mapToDouble(i ->
				IntStream.range(0, b.nRows()).mapToDouble(j -> r[j] * b.getValue(j, i))
					.sum()).toArray()).toArray(double[][]::new);
		return new SimpleMatrix(matrix, data);
	}

	public int cardinality() {
		long cardinality = Arrays.stream(matrix.data).parallel()
														.flatMapToDouble(arr -> Arrays.stream(arr))
														.filter(v -> !Double.isNaN(v))
														.count();
		return (int)cardinality;
	}

	private void gaussian(double a[][], int idx[]) {
		int n = idx.length;
		double c[] = new double[n];

		// Initialize the index
		for (int i=0; i < n; ++i)
			idx[i] = i;

		// Find the rescaling factors, one from each row
		for (int i= 0; i < n; ++i) {
			double c1 = 0;
			for (int j=0; j<n; ++j) {
				double c0 = Math.abs(a[i][j]);
				if (c0 > c1) c1 = c0;
			}
			c[i] = c1;
		}

		// Search the pivoting element from each column
		int k = 0;
		for (int j=0; j<n-1; ++j) {
			double pi1 = 0;
			for (int i=j; i<n; ++i) {
				double pi0 = Math.abs(a[idx[i]][j]);
				pi0 /= c[idx[i]];
				if (pi0 > pi1) {
					pi1 = pi0;
					k = i;
				}
			}

			// Interchange rows according to the pivoting order
			int itmp = idx[j];
			idx[j] = idx[k];
			idx[k] = itmp;

			for (int i = j+1; i<n; ++i) {
				double pj = a[idx[i]][j]/a[idx[j]][j];
				// Record pivoting ratios below the diagonal
				a[idx[i]][j] = pj;

				// Modify other elements accordingly
				for (int l=j+1; l<n; ++l) {
					a[idx[i]][l] -= pj*a[idx[j]][l];
				}
			}
		}
	}

	private double sum() {
		return Arrays.stream(matrix.data).parallel()
						.flatMapToDouble(arr -> Arrays.stream(arr))
						.filter(v -> !Double.isNaN(v))
						.sum();
	}
}

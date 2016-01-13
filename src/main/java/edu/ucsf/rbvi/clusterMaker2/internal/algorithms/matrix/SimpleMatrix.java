package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class SimpleMatrix implements Matrix {
	protected Double[][] data;
	protected int[] index;
	protected int nRows;
	protected int nColumns;
	protected String[] rowLabels;
	protected String[] columnLabels;
	protected double maxValue = Double.MIN_VALUE;
	protected double minValue = Double.MAX_VALUE;
	protected boolean symmetric = false;
	protected boolean transposed = false;
	final Logger logger = Logger.getLogger(CyUserLog.NAME);

	public SimpleMatrix() {
	}

	public SimpleMatrix(SimpleMatrix mat) {
		this(mat.nRows, mat.nColumns);
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		minValue = mat.minValue;
		maxValue = mat.maxValue;
		index = Arrays.copyOf(mat.index, mat.index.length);
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				data[row][col] = mat.data[row][col];
				if (symmetric && row != col)
					data[col][row] = mat.data[col][row];
			}
		}
	}

	public SimpleMatrix(int rows, int columns) {
		data = new Double[rows][columns];
		nRows = rows;
		nColumns = columns;
		rowLabels = new String[rows];
		columnLabels = new String[columns];
		index = null;
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
		if (index == null)
			return data[row][column]; 
		else
			return data[index[row]][index[column]]; 
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

		if (Double.isNaN(value))
			data[row][column] = null;
		else
			data[row][column] = value;
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

		data[row][column] = value;
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
		System.out.println("getDistanceMatrix.  Metric = "+metric.toString());
		SimpleMatrix mat = new SimpleMatrix(nRows, nRows);
		mat.transposed = false;
		mat.symmetric = true;
		mat.rowLabels = Arrays.copyOf(rowLabels, rowLabels.length);
		mat.columnLabels = Arrays.copyOf(rowLabels, rowLabels.length);

		long totalTime = System.currentTimeMillis();
		long metricTime = 0L;
		for (int row = 0; row < nRows; row++) {
			for (int column = row; column < this.nRows; column++) {
				long metricStart = System.currentTimeMillis();
				double metValue = metric.getMetric(this, this, row, column);
				metricTime += System.currentTimeMillis() - metricStart;
				mat.setValue(row, column, metValue);
				if (row != column)
					mat.setValue(column, row, metValue);
			}
		}
		System.out.println("... getDistanceMatrix done");
		System.out.println("Total time = "+(System.currentTimeMillis()-totalTime)/100);
		System.out.println("Metric time = "+metricTime/100);
		return mat;
	}
 
	/**
	 * Return a 2D array with all of the values in the matrix.  The missing
	 * values are set to Double.NaN
	 *
	 * @return the data in the matrix
	 */
	public double[][] toArray() {
		System.out.println("toArray");
		double doubleData[][] = new double[nRows][nColumns];
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				doubleData[row][col] = doubleValue(row, col);
				if (symmetric && row != col)
					doubleData[col][row] = doubleValue(row, col);
			}
		}
		System.out.println("... toArray done");
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
				if (data[row][col] == null) {
					data[row][col] = 0.0d;
					data[col][row] = 0.0d;
				}
			}
		}
	}

	/**
	 * Adjust the diagonals
	 */
	public void adjustDiagonals() {
		for (int col = 0; col < nColumns; col++ ) {
			data[col][col] = maxValue;
		}
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
				tData[nVals++] = data[row][column];
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
			logger.warn("clusterMaker2 SimpleMatrix: attempt to index an assymetric network");
			return;
		}

		// initialize indexing array to original order
		index = new int[data.length];
		for (int i = 0; i < data.length; ++i) {
			index[i] = i;
		}
	}
	
	/**
	 * Create a shallow copy of the matrix with an alternative
	 * index.  This is an efficient way to access submatrices
	 */
	public Matrix submatrix(int[] index) {
		SimpleMatrix mat = new SimpleMatrix();
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
		SimpleMatrix newMatrix = new SimpleMatrix(rows, cols);
		double newMin = Double.MAX_VALUE;
		double newMax = Double.MIN_VALUE;
		for (int r = 0; r < rows; r++) {
			newMatrix.setRowLabel(r, rowLabels[r+row]);
			for (int c = 0; c < cols; c++) {
				if (r == 0)
					newMatrix.setColumnLabel(c, columnLabels[c+col]);

				newMatrix.data[r][c] = data[r+row][c+col];
				if (newMatrix.data[r][c] != null) {
					if (newMatrix.data[r][c] < newMin)
						newMin = newMatrix.data[r][c];
					if (newMatrix.data[r][c] > newMax)
						newMax = newMatrix.data[r][c];
				}
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
		return new SimpleMatrix(this);
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (!symmetric) {
			logger.warn("clusterMaker2 SimpleMatrix: attempt to invert an assymetric network");
		}
		Double b[][] = new Double[nRows][nColumns];
		Double x[][] = new Double[nRows][nColumns];
		int idx[] = new int[nRows];

		// Create identity matrix
		for (int row = 0; row < nRows; row++) {
			for (int col = 0; col < nColumns; col++) {
				if (row == col)
					b[row][col] = 1.0d;
				else
					b[row][col] = 0.0d;
			}
		}

		// Transform the matrix into an upper triangle
		gaussian(data, idx);

		// Update the matrix b[i][j] with the ratios stored
		for (int i = 0; i < nRows-1; ++i) {
			for (int j=i+1; j < nRows; ++j) {
				for (int k=0; k < nRows; ++k) {
					b[idx[j]][k] -= data[idx[j]][i]*b[idx[i]][k];
				}
			}
		}

		// Perform backward substitution
		for (int i = 0; i < nRows; ++i) {
			x[nRows-1][i] = b[idx[nRows-1]][i]/data[idx[nRows-1]][nRows-1];
			for (int j = nRows-2; j >=0; --j) {
				x[j][i] = b[idx[j]][i];
				for (int k = j+1; k < nRows; ++k) {
					x[j][i] -= data[idx[j]][k]*x[k][i];
				}

				x[j][i] /= data[idx[j]][j];
			}
		}
		data = x;
	}

	/**
	 * Normalize the matrix in place
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
	}

	private void gaussian(Double a[][], int idx[]) {
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

	private void updateMinMax() {
		maxValue = Double.MIN_VALUE;
		minValue = Double.MAX_VALUE;
		for (int row = 0; row < nRows; row++) {
			for (int col = 0; col < nColumns; col++) {
				Double d = data[row][col];
				if (d == null) continue;
				if (d > maxValue) maxValue = d;
				if (d < minValue) minValue = d;
			}
		}
	}

	private int colStart(int row) {
		if (!symmetric) return 0;
		return row;
	}
}

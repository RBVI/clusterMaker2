package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix.DISTRIBUTION;
import edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixOps;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleStatistic;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;

public class SimpleMatrix implements Matrix {
	protected double[][] data;
	protected SimpleMatrix distanceMatrix = null;
	protected DistanceMetric distanceMetric = null;
	protected int[] index;
	protected int nRows;
	protected int nColumns;
	protected String[] rowLabels;
	protected String[] columnLabels;
	protected double maxValue = Double.MIN_VALUE;
	protected double minValue = Double.MAX_VALUE;
	protected boolean symmetric = false;
	protected boolean transposed = false;
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	final Logger logger = Logger.getLogger(CyUserLog.NAME);
	public final SimpleOps ops;

	public SimpleMatrix() {
		ops = new SimpleOps(this);
	}

	public SimpleMatrix(SimpleMatrix mat) {
		this(mat.nRows, mat.nColumns);
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		minValue = mat.minValue;
		maxValue = mat.maxValue;
		if (mat.index != null)
			index = Arrays.copyOf(mat.index, mat.index.length);
		else
			mat.index = null;
		data = Arrays.stream(mat.data).map(e1->e1.clone()).toArray($->data.clone());
		/*
		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				data[row][col] = mat.data[row][col];
				if (symmetric && row != col)
					data[col][row] = mat.data[col][row];
			}
		}
		*/
	}

	public SimpleMatrix(int rows, int columns) {
		this();
		// System.out.println("rows="+rows+", columns="+columns);
		Thread.dumpStack();
		data = new double[rows][columns];
		nRows = rows;
		nColumns = columns;
		rowLabels = new String[rows];
		columnLabels = new String[columns];
		IntStream.range(0, data.length).forEach(x->Arrays.fill(data[x], Double.NaN));
		/*
		for (int row = 0; row < rows; row++) {
			Arrays.fill(data[row], Double.NaN);
		}
		*/
		index = null;
	}

	public SimpleMatrix(int rows, int columns, double initialValue) {
		this();
		data = new double[rows][columns];
		nRows = rows;
		nColumns = columns;
		rowLabels = new String[rows];
		columnLabels = new String[columns];
		IntStream.range(0, data.length).forEach(x->Arrays.fill(data[x], initialValue));
		index = null;
	}

	public SimpleMatrix(SimpleMatrix mat, double[][] inputData) {
		this();
		transposed = mat.transposed;
		symmetric = mat.symmetric;
		minValue = mat.minValue;
		maxValue = mat.maxValue;
		rowLabels = Arrays.copyOf(mat.rowLabels, mat.rowLabels.length);
		columnLabels = Arrays.copyOf(mat.columnLabels, mat.columnLabels.length);
		nRows = inputData.length;
		nColumns = inputData[0].length;
		data = Arrays.stream(inputData).map(e1->e1.clone()).toArray($->inputData.clone());
	}

	public void initialize(int rows, int columns, double[][] arrayData) {
		nRows = rows;
		nColumns = columns;
		data = Arrays.stream(arrayData).map(e1->e1.clone()).toArray($->arrayData.clone());
		transposed = false;
		symmetric = false;
		rowLabels = new String[nRows];
		columnLabels = new String[nColumns];
	}

	public void initialize(int rows, int columns, Double[][] arrayData) {
		nRows = rows;
		nColumns = columns;
		data = new double[rows][columns];
		if (arrayData != null) {
			IntStream.range(0, rows).parallel()
				.forEach(r -> IntStream.range(0, columns)
					.forEach(c -> {
									if (arrayData[r][c] == null)
										data[r][c] = Double.NaN;
									else
										data[r][c] = arrayData[r][c];
					}));
		}
		transposed = false;
		symmetric = false;
		rowLabels = new String[nRows];
		columnLabels = new String[nColumns];
	}

	public MatrixOps ops() { return ops; }

	public Matrix like() {
		return new SimpleMatrix();
	}

	public Matrix like(int rows, int columns) {
		return new SimpleMatrix(rows, columns);
	}

	public Matrix like(int rows, int columns, double initialValue) {
		return new SimpleMatrix(rows, columns, initialValue);
	}

	public Matrix like(int rows, int columns, DISTRIBUTION dist) {
		OjAlgoMatrix ojMat = new OjAlgoMatrix(rows, columns, dist);
		Matrix newMat = like();
		newMat.initialize(rows, columns, ojMat.toArray());
		return newMat;
	}

	public Matrix like(Matrix initial) {
		Matrix newMat = like();
		newMat.initialize(initial.nRows(), initial.nColumns(), initial.toArray());
		return newMat;
	}

	public Matrix like(int rows, int columns, double[][] initial) {
		Matrix newMat = like();
		newMat.initialize(rows, columns, initial);
		return newMat;
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
		double v = doubleValue(row, column);
		if (Double.isNaN(v))
			return null;
		return v;
	}

	/**
	 * Return the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return the value at that location, if it was set, otherwise, return Double.NaN.
	 */
	public double doubleValue(int row, int column) {
		double v;
		if (index == null)
			v = data[row][column]; 
		else
			v = data[index[row]][index[column]]; 
		return v;
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
		if (value != null) {
			if (value < minValue) minValue = value;
			if (value > maxValue) maxValue = value;
		}
		if (index != null) {
			row = index[row];
			column = index[column];
		}

		if (value == null || Double.isNaN(value))
			data[row][column] = Double.NaN;
		else
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
		double d = doubleValue(row, column);
		if (Double.isNaN(d))
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
		// First, see if we've already got the distance matrix
		if (distanceMatrix != null && metric == distanceMetric)
			return distanceMatrix;

		SimpleMatrix mat = new SimpleMatrix(nRows, nRows);
		mat.transposed = false;
		mat.symmetric = true;
		mat.rowLabels = Arrays.copyOf(rowLabels, rowLabels.length);
		mat.columnLabels = Arrays.copyOf(rowLabels, rowLabels.length);

		IntStream.range(0, nRows).parallel()
			.forEach(row -> IntStream.range(row, nRows)
				.forEach(column -> {
						mat.setValue(row, column, metric.getMetric(this, this, row, column));
						if (row != column)
							mat.setValue(column, row, metric.getMetric(this, this, row, column));
				}));
		distanceMatrix = mat;
		distanceMetric = metric;
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
		IntStream.range(0, nRows).parallel()
			.forEach(row -> doubleData[row] = Arrays.copyOf(data[row], nColumns));

		for (int row = 0; row < nRows; row++) {
			for (int col = colStart(row); col < nColumns; col++) {
				doubleData[row][col] = doubleValue(row, col);
				if (symmetric && row != col)
					doubleData[col][row] = doubleValue(row, col);
			}
		}
		return doubleData;
	}

	public double[] getRow(int row) {
		double rowData[] = new double[nColumns];
		IntStream.range(0, nColumns).parallel()
			.forEach(col -> rowData[col] = data[row][col]);
		return rowData;
	}

	public double[] getColumn(int col) {
		double columnData[] = new double[nRows];
		IntStream.range(0, nRows).parallel()
			.forEach(row -> columnData[row] = data[row][col]);
		return columnData;
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
		IntStream.range(0, nRows).parallel()
			.forEach(row -> IntStream.range(colStart(row), nColumns)
				.forEach(column -> {
					if (data[row][column] == Double.NaN) {
						data[row][column] = 0.0d;
						if (symmetric && row != column)
							data[column][row] = 0.0d;
					}
				}));
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
		double[] tData = new double[nColumns()];
		int nVals = 0;
		for (int column = 0; column < nColumns(); column++) {
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
				if (!Double.isNaN(newMatrix.data[r][c])) {
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

	public DoubleMatrix2D getColtMatrix() {
		DoubleMatrix2D mat = DoubleFactory2D.dense.make(nRows, nColumns);
		mat.assign(toArray());
		return mat;
	}

	/**
	 * Debugging routine to print out information about a matrix
	 *
	 * @param matrix the matrix we're going to print out information about
	 */
	public String printMatrixInfo() {
		String s = "Simple Matrix("+nRows+", "+nColumns+")\n";
		s += " cardinality is "+ops.cardinality()+"\n";
		return s;
	}

	public String printMatrix() {
		StringBuilder sb = new StringBuilder();
		sb.append("SimpleMatrix("+nRows+", "+nColumns+")\n\t");
		for (int col = 0; col < nColumns; col++) {
			sb.append(getColumnLabel(col)+"\t");
		}
		sb.append("\n");
		for (int row = 0; row < nRows; row++) {
			sb.append(getRowLabel(row)+":\t"); //node.getIdentifier()
			for (int col = 0; col < nColumns; col++) {
				sb.append(""+doubleValue(row,col)+"\t");
			} 
			sb.append("\n");
		} 
		return sb.toString();
	}
	
	public void writeMatrix(String fileName) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		try{
			File file = new File(tmpDir + fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(tmpDir + fileName, "UTF-8");
			writer.write(printMatrix());
			writer.close();
		}catch(IOException e){
			e.printStackTrace(System.out);
		}
	}

	protected Matrix copyDataFromMatrix(DoubleMatrix2D matrix2D) {
		SimpleMatrix mat = new SimpleMatrix();
		double[][]inputData = matrix2D.toArray();
		mat.initialize(matrix2D.rows(), matrix2D.columns(), inputData);
		mat.symmetric = true;
		mat.transposed = this.transposed;
		String[] labels;
		if (this.transposed)
			labels = rowLabels;
		else
			labels = columnLabels;
		if (labels != null) {
			mat.rowLabels = Arrays.copyOf(labels, labels.length);
			mat.columnLabels = Arrays.copyOf(labels, labels.length);
		}
		return mat;
	}

	public void updateMinMax() {
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

	protected int colStart(int row) {
		if (!symmetric) return 0;
		return row;
	}
}

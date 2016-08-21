package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.List;
import cern.colt.matrix.tdouble.DoubleMatrix2D;


/**
 * A generic wrapper around Matrix implementations, from the most simple
 * (a two-dimensional array) to more complex sparse implementations.
 *
 * In general, a Matrix contains the following pieces of information:
 * 	o	The data
 * 	o	Row and column weights
 * 	o Row and column labels
 * 	o Informational flags:
 * 		o The matrix is symmetrical (symmetrical)
 * 		o The matrix is dense
 * 		o The matrix is separately indexed
 */
public interface Matrix {

	/**
	 * Initialize the matrix data
	 *
	 * @param rows the number of rows
	 * @param columns the number of columns
	 * @param data a (possibly null) matrix of data
	 */
	public void initialize(int rows, int columns, double[][] data);

	/**
	 * Initialize the matrix data
	 *
	 * @param rows the number of rows
	 * @param columns the number of columns
	 * @param data a (possibly null) matrix of data
	 */
	public void initialize(int rows, int columns, Double[][] data);
	
	/**
	 * Return the number of rows in this matrix.
	 *
	 * @return number of rows
	 */
	public int nRows();

	/**
	 * Return the number of columns in this matrix.
	 *
	 * @return number of columns
	 */
	public int nColumns();

	/**
	 * Return the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return the (possibly null) value at that location
	 */
	public Double getValue(int row, int column);
	
	/**
	 * Return the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return the value at that location, if it was set, otherwise, return Double.NaN.
	 */
	public double doubleValue(int row, int column);
	
	/**
	 * Set the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @param value the value to set
	 */
	public void setValue(int row, int column, double value);

	/**
	 * Set the value at a specific location.
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @param value the value to set
	 */
	public void setValue(int row, int column, Double value);

	/**
	 * Return true if the location has a value
	 *
	 * @param row the row number of the value
	 * @param column the coulmn number of the value
	 * @return true if this location has a value, false otherwise
	 */
	public boolean hasValue(int row, int column);
	
	/**
	 * Return an array of column labels
	 *
	 * @return the column labels
	 */
	public String[] getColumnLabels();

	/**
	 * Return a column label
	 *
	 * @param col the column to get the label for
	 * @return the column label
	 */
	public String getColumnLabel(int col);

	/**
	 * Set a column label
	 *
	 * @param col the column to set the label for
	 * @param label the column label
	 */
	public void setColumnLabel(int col, String label);

	/**
	 * Set the column labels
	 *
	 * @param labelList the list of column labels
	 */
	public void setColumnLabels(List<String>labelList);

	/**
	 * Return an array of row labels
	 *
	 * @return the row labels
	 */
	public String[] getRowLabels();
	
	/**
	 * Return a row label
	 *
	 * @param row the row to get the label for
	 * @return the row label
	 */
	public String getRowLabel(int row);

	/**
	 * Set a row label
	 *
	 * @param row the row to set the label for
	 * @param label the row label
	 */
	public void setRowLabel(int row, String label);
	
	/**
	 * Set the row labels
	 *
	 * @param labelList the list of row labels
	 */
	public void setRowLabels(List<String>labelList);

	/**
	 * Return the distance between rows based on the matric.
	 *
	 * @param metric the metric to use to calculate the distances
	 * @return a new Matrix of the distances between the rows
	 */
	public Matrix getDistanceMatrix(DistanceMetric metric);
 
	/**
	 * Return a 2D array with all of the values in the matrix.  The missing
	 * values are set to Double.NaN
	 *
	 * @return the data in the matrix
	 */
	public double[][] toArray();

	/**
	 * Return the maximum value in the matrix
	 *
	 * @return the max value
	 */
	public double getMaxValue();

	/**
	 * Return the minimum value in the matrix
	 *
	 * @return the min value
	 */
	public double getMinValue();

	/**
	 * Return true if the matrix is transposed
	 *
	 * @return true if the matrix is transposed
	 */
	public boolean isTransposed();

	/**
	 * Set true if the matrix is transposed
	 *
	 * @param transposed true if the matrix is transposed
	 */
	public void setTransposed(boolean transposed);

	/**
	 * Return true if the matrix is symmetraical
	 *
	 * @return true if the matrix is symmetraical
	 */
	public boolean isSymmetrical();

	/**
	 * Set true if the matrix is symmetrical
	 *
	 * @param symmetrical true if the matrix is symmetrical
	 */
	public void setSymmetrical(boolean symmetrical);

	/**
	 * Set all missing values to zero
	 */
	public void setMissingToZero();

	/**
	 * Adjust the diagonals
	 */
	public void adjustDiagonals();

	/**
	 * Return the rank order of the columns in a row
	 *
	 * @param row the row to rank the columns in
	 * @return the rank order of the columns
	 */
	public double[] getRank(int row);

	/**
	 * Return a copy of the matrix
	 *
	 * @return matrix copy
	 */
	public Matrix copy();
	
	/**
	 * Create an index on the matrix
	 */
	public void index();
	
	/**
	 * Create copy of the matrix with an alternative
	 * index.  This is an efficient way to access submatrices
	 */
	public Matrix submatrix(int[] index);

	/**
	 * Return a submatrix
	 * 
	 * @param row the starting row of the submatrix
	 * @param col the starting column of the submatrix
	 * @param rows the number of rows
	 * @param cols the number of columnss
	 * @return submatrix
	 */
	public Matrix submatrix(int row, int col, int rows, int cols);

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix();

	/**
	 * Normalize the matrix in place.  This actually doesn't do matrix
	 * normalization -- it just uses the min and max values to bound
	 * the matrix
	 */
	public void normalize();

	/**
	 * Normalize the matrix in place.  This is actual matrix normalization,
	 * i.e. all cells sum to 1.0
	 */
	public void normalizeMatrix();

	/**
	 * Normalize a matrix row in place (all columns in the row sum to 1.0)
	 *
	 * @param row the row to normalize
	 */
	public void normalizeRow(int row);

	/**
	 * Normalize a matrix column in place (all rows in the column sum to 1.0)
	 *
	 * @param column the column to normalize
	 */
	public void normalizeColumn(int column);

	/**
	 * Standardize the data in a row
	 *
	 * @param row the row to standardize
	 */
	public void standardizeRow(int row);

	/**
	 * Standardize the data in a column
	 *
	 * @param column the column to standardize
	 */
	public void standardizeColumn(int column);

	/**
	 * Normalize a matrix column in place (all rows in the column sum to 1.0)
	 *
	 * @param column the column to normalize

	/**
	 * Centralize the rows of a matrix around the mean of the row
	 *
	 */
	public void centralizeRows();

	/**
	 * Centralize the column of a matrix around the mean of the column
	 *
	 */
	public void centralizeColumns();

	/**
	 * Return the sum of a row
	 *
	 * @param row the row to get the sum of
	 * @return the sum
	 */
	public double rowSum(int row);

	/**
	 * Return the sum of a column
	 *
	 * @param column the column to get the sum of
	 * @return the sum
	 */
	public double columnSum(int row);

	/**
	 * Return the variance of a row
	 *
	 * @param row the row to get the variance of
	 * @return the variance
	 */
	public double rowVariance(int row);

	/**
	 * Return the variance of a column
	 *
	 * @param column the column to get the variance of
	 * @return the variance
	 */
	public double columnVariance(int row);

	/**
	 * Return the mean of a row
	 *
	 * @param row the row to get the mean of
	 * @return the mean
	 */
	public double rowMean(int row);

	/**
	 * Return the mean of a column
	 *
	 * @param column the column to get the mean of
	 * @return the mean
	 */
	public double columnMean(int column);

	/**
	 * Return the cardinality (number of non-null values) of this matrix
	 *
	 * @return cardinality
	 */
	public int cardinality();

	/**
	 * Multiply two matrices together
	 *
	 * @param matrix the matrix to multiple with our matrix
	 * @return the multiplied matrix
	 */
	public Matrix multiplyMatrix(Matrix matrix);

	/**
	 * Return the covariance of a matrix
	 *
	 * @return the covariance of the matrix
	 */
	public Matrix covariance();

	/**
	 * Return the correlation of a matrix
	 *
	 * @return the correlation of the matrix
	 */
	public Matrix correlation();

	/**
	 * Threshold the matrix.  This will remove all of the
	 * values less than a default threshold to increase the sparsity
	 * of the matrix (decrease the cardinality).
	 */
	public void threshold();

	/**
	 * Threshold the matrix.  This will remove all of the
	 * very, very small values to increase the sparsity
	 * of the matrix (decrease the cardinality).
	 *
	 * @param threshold the actual threshold to use
	 */
	public void threshold(double thresh);

	/**
	 * Transpose the matrix.  This will return a new
	 * matrix that is the transpose of this matrix.
	 *
	 * @return a new matrix that is the transpose of this matrix
	 */
	// public Matrix transpose();

	/**
	 * Return the eigenvalues of a matrix
	 *
	 * @param nonZero if true, only return the non-zero eigenvalues
	 * @return the eigenvalues of the matrix
	 */
	public double[] eigenValues(boolean nonZero);

	/**
	 * Return the eigenvectors of a matrix
	 *
	 * @return the eigenvectors of the matrix
	 */
	public double[][] eigenVectors();

	/**
	 * Return some information about the matrix
	 *
	 * @return string information
	 */
	public String printMatrixInfo();

	/**
	 * Print the matrix out in a reasonable format
	 *
	 * @return string representation of the matrix
	 */
	public String printMatrix();

	/**
	 * Write the matrix out in a reasonable format
	 *
	 * @param filename the name of the output file
	 */
	public void writeMatrix(String filename);

	/**
	 * Temporary solution to providing access to Colt Matrices.  Eventually,
	 * we want to bury all of this in MatrixUtils and the matrix classes
	 * themselves.
	 *
	 * @return the internal (or created) colt 2D matrix
	 */
	public DoubleMatrix2D getColtMatrix();
}

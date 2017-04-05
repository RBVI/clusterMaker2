package edu.ucsf.rbvi.clusterMaker2.internal.api;

/**
 * A generic wrapper around Matrix various matrix operations, from the most simple
 * (a two-dimensional array) to more complex sparse implementations.
 */
public interface MatrixOps {

	/**
	 * Invert the matrix in place
	 *
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
	 * Centralize all the rows of a matrix around the mean of that row
	 */
	public void centralizeRows();

	/**
	 * Centralize all the columns of a matrix around the mean of that column
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
	 * Return the sum of squares of a row
	 *
	 * @param row the row to get the sum of squares of
	 * @return the sum
	 */
	public double rowSum2(int row);

	/**
	 * Return the sum of a column
	 *
	 * @param column the column to get the sum of
	 * @return the sum of squares
	 */
	public double columnSum(int column);

	/**
	 * Return the sum of squares of a column
	 *
	 * @param column the column to get the sum of squares of
	 * @return the sum of squares
	 */
	public double columnSum2(int column);

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
	 * add a value to all cells in this matrix
	 * 
	 * @param value to add to each cell
	 */
	public void addScalar(double value);

	/**
	 * subtract a value from all cells in this matrix
	 * 
	 * @param value to subtract from each cell
	 */
	public void subtractScalar(double value);

	/**
	 * multiple all cells in this matrix by a value
	 * 
	 * @param value to multiply each cell by
	 */
	public void multiplyScalar(double value);

	/**
	 * divide all cells in this matrix by a value
	 * 
	 * @param value to divide each cell by
	 */
	public void divideScalar(double value);

	/**
	 * raise all cells in this matrix by a power
	 * 
	 * @param value power to raise to each cell
	 */
	public void powScalar(double value);

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
	 * very, very small values larger than the threashold
	 * to increase the sparsity of the matrix (decrease 
	 * the cardinality).
	 *
	 * @param thresh the actual threshold to use
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
	 * Return the Gowers matrix
	 */
}

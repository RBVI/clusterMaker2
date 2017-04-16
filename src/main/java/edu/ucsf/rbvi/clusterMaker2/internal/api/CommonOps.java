package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.stream.IntStream;

/**
 * This utility class provides a number of methods that allow short-hand
 * access to the various MatrixOps.  
 */
public class CommonOps {

	/**
	 * Return a copy of a matrix
	 * 
	 * @param matrix the matrix to be copied
	 * @return the new matrix
	 */
	public static Matrix copy(Matrix matrix) {
		return matrix.copy();
	}
	

	/**
	 * Invert the matrix in place
	 * 
	 * @param matrix the matrix to be inverted
	 * @return the same matrix
	 */
	public static Matrix invertMatrix(Matrix matrix) {
		matrix.ops().invertMatrix();
		return matrix;
	}

	/**
	 * Normalize the matrix in place.  This actually doesn't do matrix
	 * normalization -- it just uses the min and max values to bound
	 * the matrix
	 * @param matrix the matrix to be normalized
	 * @return the same matrix
	 */
	public static Matrix normalize(Matrix matrix) {
		matrix.ops().normalize();
		return matrix;
	}

	/**
	 * Normalize the matrix in place.  This is actual matrix normalization,
	 * i.e. all cells sum to 1.0
	 *
	 * @param matrix the matrix to be normalized
	 * @return the same matrix
	 */
	public static Matrix normalizeMatrix(Matrix matrix) {
		matrix.ops().normalizeMatrix();
		return matrix;
	}

	/**
	 * Normalize a matrix row in place (all columns in the row sum to 1.0)
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix to be normalized
	 * @param row the row to normalize
	 * @return the same matrix
	 */
	public static Matrix normalizeRow(Matrix matrix, int row) {
		matrix.ops().normalizeRow(row);
		return matrix;
	}

	/**
	 * Normalize a matrix column in place (all rows in the column sum to 1.0)
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix to be normalized
	 * @param column the column to normalize
	 * @return the same matrix
	 */
	public static Matrix normalizeColumn(Matrix matrix, int column) {
		matrix.ops().normalizeColumn(column);
		return matrix;
	}

	/**
	 * Standardize the data in a row
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix with the row to be standardized
	 * @param row the row to standardize
	 * @return the same matrix
	 */
	public static Matrix standardizeRow(Matrix matrix, int row) {
		matrix.ops().standardizeRow(row);
		return matrix;
	}

	/**
	 * Standardize the data in a column
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix with the column to be standardized
	 * @param column the column to standardize
	 * @return the same matrix
	 */
	public static Matrix standardizeColumn(Matrix matrix, int column) {
		matrix.ops().standardizeColumn(column);
		return matrix;
	}

	/**
	 * Centralize all the rows of a matrix around the mean of that row
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix with the rows to be centralized
	 * @return the same matrix
	 */
	public static Matrix centralizeRows(Matrix matrix) {
		matrix.ops().centralizeRows();
		return matrix;
	}

	/**
	 * Centralize all the columns of a matrix around the mean of that column
	 * Note: does not update matrix min/max values.  
	 *
	 * @param matrix the matrix with the columns to be centralized
	 * @return the same matrix
	 */
	public static Matrix centralizeColumns(Matrix matrix) {
		matrix.ops().centralizeColumns();
		return matrix;
	}

	/**
	 * Return the sum of a row
	 *
	 * @param matrix the matrix with the row
	 * @param row the row to get the sum of
	 * @return the sum
	 */
	public static double rowSum(Matrix matrix, int row) {
		return matrix.ops().rowSum(row);
	}

	/**
	 * Return a matrix containing the sum of all rows
	 *
	 * @param matrix the matrix to sum
	 * @return a new matrix of size nRows,1
	 */
 	public static Matrix rowSum(Matrix matrix) {
		Matrix result = matrix.like(matrix.nRows(), 1);
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> {
				result.setValue(row, 0, rowSum(matrix, row));
			});
		return result;
	}	

	/**
	 * Return the sum of squares of a row
	 *
	 * @param matrix the matrix with the row
	 * @param row the row to get the sum of squares of
	 * @return the sum of squares
	 */
	public static double rowSum2(Matrix matrix, int row) {
		return matrix.ops().rowSum2(row);
	}

	/**
	 * Return the sum of a column
	 *
	 * @param matrix the matrix with the column
	 * @param column the column to get the sum of
	 * @return the sum
	 */
	public static double columnSum(Matrix matrix, int column) {
		return matrix.ops().columnSum(column);
	}

	/**
	 * Return a matrix containing the sum of all columns
	 *
	 * @param matrix the matrix to sum
	 * @return a new matrix of size 1,nColumns
	 */
 	public static Matrix columnSum(Matrix matrix) {
		Matrix result = matrix.like(1, matrix.nColumns());
		IntStream.range(0, matrix.nColumns()).parallel()
			.forEach(col -> {
				result.setValue(0, col, columnSum(matrix, col));
			});
		return result;
	}

	/**
	 * Return the sum of squares of a column
	 *
	 * @param matrix the matrix with the column
	 * @param column the column to get the sum of squares of
	 * @return the sum of squares
	 */
	public static double columnSum2(Matrix matrix, int column) {
		return matrix.ops().columnSum2(column);
	}

	/**
	 * Return the sum of a matrix
	 *
	 * @param matrix to sum
	 * @return the sum of the the matrix
	 */
	public static double sum(Matrix matrix) {
		return matrix.ops().sum();
	}

	/**
	 * Return the variance of a row
	 *
	 * @param matrix with the row of interest
	 * @param row the row to get the variance of
	 * @return the variance
	 */
	public static double rowVariance(Matrix matrix, int row) {
		return matrix.ops().rowVariance(row);
	}

	/**
	 * Return the variance of a column
	 *
	 * @param matrix with the column of interest
	 * @param column the column to get the variance of
	 * @return the variance
	 */
	public static double columnVariance(Matrix matrix, int column) {
		return matrix.ops().columnVariance(column);
	}

	/**
	 * Return the mean of a row
	 *
	 * @param matrix with the row of interest
	 * @param row the row to get the mean of
	 * @return the mean
	 */
	public static double rowMean(Matrix matrix, int row) {
		return matrix.ops().rowMean(row);
	}

	/**
	 * Return an n by 1 matrix containing the means of the rows
	 *
	 * @param matrix the source matrix
	 * @return a matrix with the mean of the rows
	 */
	public static Matrix rowMean(Matrix matrix) {
		Matrix result = matrix.like(matrix.nRows(), 1);
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> {
				result.setValue(row, 0, matrix.ops().rowMean(row));
			});
		return result;
	}

	/**
	 * Return the mean of a column
	 *
	 * @param matrix with the column of interest
	 * @param column the column to get the mean of
	 * @return the mean
	 */
	public static double columnMean(Matrix matrix, int column) {
		return matrix.ops().columnMean(column);
	}

	/**
	 * Return a 1 by n matrix containing the means of the columns
	 *
	 * @param matrix the source matrix
	 * @return a matrix with the mean of the columns
	 */
	public static Matrix columnMean(Matrix matrix) {
		Matrix result = matrix.like(1, matrix.nColumns());
		IntStream.range(0, matrix.nColumns()).parallel()
			.forEach(col -> {
				result.setValue(0, col, matrix.ops().columnMean(col));
			});
		return result;
	}

	/**
	 * Return the cardinality (number of non-null values) of this matrix
	 *
	 * @param matrix to get the cardinality of
	 * @return cardinality
	 */
	public static int cardinality(Matrix matrix) {
		return matrix.ops().cardinality();
	}

	/**
	 * Multiply two matrices together
	 *
	 * @param matrix1 the first matrix 
	 * @param matrix2 the second matrix to multiply
	 * @return a new multiplied matrix
	 */
	public static Matrix multiplyMatrix(Matrix matrix1, Matrix matrix2) {
		return matrix1.ops().multiplyMatrix(matrix2);
	}

	/**
	 * add a value to all cells in this matrix
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix to add the value to
	 * @param value to add to each cell
	 * @return the matrix
	 */
	public static Matrix addScalar(Matrix matrix, double value) {
		matrix.ops().addScalar(value);
		return matrix;
	}

	/**
	 * add a matrix to an existing matrix
	 * 
	 * @param matrix to add the values to
	 * @param addend the matrix to add
	 * @return the matrix with the added values
	 */
	public static Matrix addElement(Matrix matrix, Matrix addend) {
		matrix.ops().addElement(addend);
		return matrix;
	}

	/**
	 * subtract a value from all cells in this matrix
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix to subtract the value from
	 * @param value to subtract from each cell
	 * @return the matrix
	 */
	public static Matrix subtractScalar(Matrix matrix, double value) {
		matrix.ops().subtractScalar(value);
		return matrix;
	}

	/**
	 * subtract a matrix from an existing matrix
	 * 
	 * @param matrix to add the values to
	 * @param sub the matrix to subtract
	 * @return the matrix with the subtracted values
	 */
	public static Matrix subtractElement(Matrix matrix, Matrix sub) {
		/*
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (matrix.doubleValue(row, col)-sub.doubleValue(row,col)));
				})
			);
		*/
		matrix.ops().subtractElement(sub);
		return matrix;
	}

	/**
	 * multiple all cells in this matrix by a value
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix to multiply by the value
	 * @param value to multiply each cell by
	 * @return the matrix
	 */
	public static Matrix multiplyScalar(Matrix matrix, double value) {
		matrix.ops().multiplyScalar(value);
		return matrix;
	}

	/**
	 * multiply a matrix by the values in another matrix
	 * 
	 * @param matrix to multiply
	 * @param multiplier the matrix to get the mutipliers from
	 * @return the matrix multiplied by the values in the multiplier
	 */
	public static Matrix multiplyElement(Matrix matrix, Matrix multiplier) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (matrix.doubleValue(row, col)*multiplier.doubleValue(row,col)));
				})
			);
		return matrix;
	}

	/**
	 * divide all cells in this matrix by a value
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix to divide by the value
	 * @param value to divide each cell by
	 * @return the matrix
	 */
	public static Matrix divideScalar(Matrix matrix, double value) {
		matrix.ops().divideScalar(value);
		return matrix;
	}

	/**
	 * divide a matrix by the values in another matrix
	 * 
	 * @param numerator to divide
	 * @param denom the matrix to get the denominators from
	 * @return the numerator matrix divided by the denominator
	 */
	public static Matrix divideElement(Matrix numerator, Matrix denom) {
		IntStream.range(0, numerator.nRows()).parallel()
			.forEach(row -> IntStream.range(0, numerator.nColumns())
				.forEach(col -> {
					numerator.setValue(row, col, (numerator.doubleValue(row, col)/denom.doubleValue(row,col)));
				})
			);
		return numerator;
	}

	/**
	 * divide all cells in a column by a value.  This is used
	 * primarily for normalization when the current sum
	 * of the column is already known.
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix the matrix with the column
	 * @param column the column we're dividing
	 * @param value to divide each cell in the column by
	 * @return the matrix
	 */
	public static Matrix divideScalarColumn(Matrix matrix, int column, double value) {
		matrix.ops().divideScalarColumn(column, value);
		return matrix;
	}

	/**
	 * inverse (value/cell) all cells in matrix.  
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param value the numerator
	 * @param matrix the matrix to get the denominators
	 * @return the matrix
	 */
	public static Matrix divideElement(double value, Matrix matrix) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (value/matrix.doubleValue(row, col)));
				})
			);
		return matrix;
	}

	public static Matrix logElement(Matrix matrix) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (Math.log(matrix.doubleValue(row, col))));
				})
			);
		return matrix;
	}

	public static Matrix expElement(Matrix matrix) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (Math.exp(matrix.doubleValue(row, col))));
				})
			);
		return matrix;
	}

	/**
	 * raise all cells in this matrix by a power
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param matrix the matrix to raise to a power
	 * @param value power to raise to each cell
	 * @return the matrix
	 */
	public static Matrix powScalar(Matrix matrix, double value) {
		matrix.ops().powScalar(value);
		return matrix;
	}

	/**
	 * Return the covariance of a matrix
	 *
	 * @param matrix the matrix to get the covariance of
	 * @return the covariance of the matrix
	 */
	public static Matrix covariance(Matrix matrix) {
		return matrix.ops().covariance();
	}

	/**
	 * Return the correlation of a matrix
	 *
	 * @param matrix the matrix to get the correlation of
	 * @return the correlation of the matrix
	 */
	public static Matrix correlation(Matrix matrix) {
		return matrix.ops().correlation();
	}

	/**
	 * Threshold the matrix.  This will remove all of the
	 * values less than a default threshold to increase the sparsity
	 * of the matrix (decrease the cardinality).
	 *
	 * @param matrix the matrix to threshold
	 * @return the matrix
	 */
	public static Matrix threshold(Matrix matrix) {
		matrix.ops().threshold();
		return matrix;
	}

	/**
	 * Threshold the matrix.  This will remove all of the
	 * very, very small values larger than the threashold
	 * to increase the sparsity of the matrix (decrease 
	 * the cardinality).
	 *
	 * @param matrix the matrix to threshold
	 * @param thresh the actual threshold to use
	 * @return the matrix
	 */
	public static Matrix threshold(Matrix matrix, double thresh) {
		matrix.ops().threshold(thresh);
		return matrix;
	}

	/**
	 * Transpose the matrix.  This will return a new
	 * matrix that is the transpose of this matrix.
	 *
	 * @param matrix the matrix to transpose
	 * @return a new matrix that is the transpose of this matrix
	 */
	public static Matrix transpose(Matrix matrix) {
		return matrix.ops().transpose();
	}

	/**
	 * Create a diagonal matrix 
	 *
	 * @param matrix the matrix to transpose
	 * @return a new matrix that is the transpose of this matrix
	 */
	public static Matrix diag(Matrix matrix) {
		boolean isLong = matrix.nRows() > matrix.nColumns();
		int dim = Math.max(matrix.nRows(), matrix.nColumns());
		Matrix result = matrix.like(dim, dim);
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					if (row == col) {
						if (isLong) {
							result.setValue(row, col, matrix.doubleValue(row, 0));
						} else {
							result.setValue(row, col, matrix.doubleValue(0, col));
						}
					}
				})
			);
		return result;
	}

	/**
	 * Set the diagonal of a matrix from a vector
	 *
	 * @param matrix the matrix to transpose
	 * @return a new matrix that is the transpose of this matrix
	 */
	public static void setDiag(Matrix matrix, Matrix vector) {
		if (vector.nRows() == 1) {
			for (int col = 0; col < vector.nColumns(); col++) {
				matrix.setValue(col, col, vector.doubleValue(0, col));
			}
		} else {
			for (int row = 0; row < vector.nRows(); row++) {
				matrix.setValue(row, row, vector.doubleValue(row, 0));
			}
		}
	}

	/**
	 * Return the eigenvalues of a matrix
	 *
	 * @param matrix to get the eigenvalues of
	 * @param nonZero if true, only return the non-zero eigenvalues
	 * @return the eigenvalues of the matrix
	 */
	public static double[] eigenValues(Matrix matrix, boolean nonZero) {
		return matrix.ops().eigenValues(nonZero);
	}

	/**
	 * Return the eigenvectors of a matrix
	 *
	 * @param matrix to get the eigenvectors of
	 * @return the eigenvectors of the matrix
	 */
	public static double[][] eigenVectors(Matrix matrix) {
		return matrix.ops().eigenVectors();
	}

	/**
	 * Add a matrix containing a row vector to another matrix
	 * NOTE: this does *not* create a new matrix
	 *
	 * @param matrix the matrix to add the row vector to
	 * @param rowvector the matrix containing the row vector
	 * @return the original matrix with the row vector added
	 */
	public static Matrix addRowVector(Matrix matrix, Matrix rowvector) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (matrix.doubleValue(row, col)+rowvector.doubleValue(0, col)));
				})
			);
		return matrix;
	}

	/**
	 * Add a matrix containing a column vector to another matrix
	 * NOTE: this does *not* create a new matrix
	 *
	 * @param matrix the matrix to add the column vector to
	 * @param rowvector the matrix containing the column vector
	 * @return the original matrix with the column vector added
	 */
	public static Matrix addColumnVector(Matrix matrix, Matrix colvector) {
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					matrix.setValue(row, col, (matrix.doubleValue(row, col)+colvector.doubleValue(row, 0)));
				})
			);
		return matrix;
	}
}

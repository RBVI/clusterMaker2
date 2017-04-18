package edu.ucsf.rbvi.clusterMaker2.internal.api;

/**
 * This utility class provides a number of methods that support accessing
 * Matrix data through indices
 */
public class MatrixIndexUtils {
	
	/**
	 * Fill a section of a matrix with a specific value
	 *
	 * @param matrix the matrix we're modifying
	 * @param rowRange the array of row values
	 * @param columnRange the array of column values
	 * @param value the value to set at those locations
	 */
	public static void assignAtIndex(Matrix num, int[] rowRange, int[] columnRange, double value) {
		for (int index = 0; index < rowRange.length; index++)
			num.setValue(rowRange[index], columnRange[index], value);
	}

	/**
	 * Create a new matrix with indexed values from a row
	 *
	 * @param matrix the source matrix
	 * @param row the row we're getting the data from
	 * @param indicies the array of indicies
	 * @return a new matrix with the selected values
	 */
	public static Matrix getValuesFromRow(Matrix matrix, int row, int[] indicies) {
		Matrix values=matrix.like(1,indicies.length);
		for (int index = 0; index < indicies.length; index++)
			values.setValue(0, index, matrix.getValue(row, indicies[index]));
		return values;
	}

	/**
	 * Create a new matrix with indexed values from a column
	 *
	 * @param matrix the source matrix
	 * @param col the column we're getting the data from
	 * @param indicies the array of indicies
	 * @return a new matrix with the selected values
	 */
	public static Matrix getValuesFromColumn(Matrix matrix, int col, int[] indicies) {
		Matrix values=matrix.like(indicies.length,1);
		for (int index = 0; index < indicies.length; index++)
			values.setValue(index, 0, matrix.getValue(indicies[index], col));
		return values;
	}

	/**
	 * Assign the values from an arrow to a row in a matrix using an array of
	 * indices for the offsets.
	 * Note that the array of indices and the array of values must be the same
	 * length
	 *
	 * @param matrix the target matrix
	 * @param row the row we're writing into
	 * @param indicies the array of indicies
	 * @param values the array of values
	 */
	public static void assignValuesToRow(Matrix matrix, int row, int[] indicies, double [] values) {
		if( indicies.length != values.length ) {
			throw new IllegalArgumentException("Length of indicies and values have to be equal");
		}
		for (int index = 0; index < indicies.length; index++) {
			matrix.setValue(row, indicies[index], values[index]);
		}
	}

}

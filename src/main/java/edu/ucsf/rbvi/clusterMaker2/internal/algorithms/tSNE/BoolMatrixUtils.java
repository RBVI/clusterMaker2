package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import java.util.stream.IntStream;

import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class BoolMatrixUtils {
	
	public static boolean [][] equal(boolean [][] matrix1, boolean [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int row = 0; row < matrix1.length; row++)
			for (int col = 0; col < matrix1[0].length; col++)
				equals[row][col] = (matrix1[row][col] == matrix2[row][col]);
		/*
		IntStream.range(0, matrix1.length).parallel()
			.forEach(row -> IntStream.range(0, matrix1[0].length)
				.forEach(col -> {
					equals[row][col] = (matrix1[row][col] == matrix2[row][col]);
			}));
		*/
		return equals;
	}

	public static boolean [][] biggerThan(Matrix matrix, double value) {
		boolean [][] equals = new boolean[matrix.nRows()][matrix.nColumns()];
		for (int row = 0; row < equals.length; row++)
			for (int col = 0; col < equals[0].length; col++)
				equals[row][col] = Double.compare(matrix.doubleValue(row, col), value) == 1;
		/*
		IntStream.range(0, matrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, matrix.nColumns())
				.forEach(col -> {
					equals[row][col] = Double.compare(matrix.doubleValue(row, col), value) == 1;
			}));
		*/
		return equals;
	}
	
	public static boolean [][] negate(boolean [][] booleans) {
		boolean [][] negates = new boolean[booleans.length][booleans[0].length];
		for (int row = 0; row < booleans.length; row++)
			for (int col = 0; col < booleans[0].length; col++)
				negates[row][col] = !booleans[row][col];
		/*
		IntStream.range(0, booleans.length).parallel()
			.forEach(row -> IntStream.range(0, booleans[0].length)
				.forEach(col -> {
					negates[row][col] = !booleans[row][col];
			}));
		*/
		return negates;
	}

	public static double[][] abs(boolean [][] booleans) {
		double [][] absolutes = new double[booleans.length][booleans[0].length];
		for (int row = 0; row < booleans.length; row++)
			for (int col = 0; col < booleans[0].length; col++)
				absolutes[row][col] = (booleans[row][col] ? 1 : 0);
		/*
		IntStream.range(0, booleans.length)
			.forEach(row -> IntStream.range(0, booleans[0].length)
				.forEach(col -> {
					absolutes[row][col] = (booleans[row][col] ? 1 : 0);
			}));
		*/
		return absolutes;
	}
}

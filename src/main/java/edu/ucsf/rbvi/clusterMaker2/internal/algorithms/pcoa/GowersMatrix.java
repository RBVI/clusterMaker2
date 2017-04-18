package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.text.DecimalFormat;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import static edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps.subtractElement;

public class GowersMatrix  {
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens

	public static Matrix getGowersMatrix(CyMatrix distanceMatrix) {
		
		// Create the Identity matrix
		// DoubleMatrix2D I = DoubleFactory2D.sparse.identity(distancematrix.nRows());
		Matrix I = distanceMatrix.like(distanceMatrix.nRows(), distanceMatrix.nRows(), 0.0);
		for (int row = 0; row < distanceMatrix.nRows(); row++)
			I.setValue(row, row, 1.0);
		/*
		IntStream.range(0, distanceMatrix.nRows()).parallel()
			.forEach(row -> {
					I.setValue(row, row, 1.0);
				});
		*/

		// I.writeMatrix("I");
		// Create the ones matrix.  This is equivalent to 11'/n
		// DoubleMatrix2D one = DoubleFactory2D.dense.make(distancematrix.nRows(), distancematrix.nRows(), 1.0/distancematrix.nRows());
		Matrix one = distanceMatrix.like(distanceMatrix.nRows(), distanceMatrix.nRows(), 1.0/distanceMatrix.nRows());
		// I.writeMatrix("one");

		// Create the subtraction matrix (I-11'/n)
		// DoubleMatrix2D mat = I.assign(one, DoubleFunctions.minus);
		Matrix mat = subtractElement(I, one);
		// mat.writeMatrix("mat");

		// Create our data matrix
		//final DoubleMatrix2D A = DoubleFactory2D.sparse.make(distancematrix.nRows(), distancematrix.nRows());
		Matrix A = distanceMatrix.like(distanceMatrix.nRows(), distanceMatrix.nRows());
		/*for (int row = 0; row < distancematrix.nRows(); row++) {
			for (int col = 0; col < distancematrix.nColumns(); col++) {
				System.out.print(distancematrix.getValue(row, col)+",");
			}
		}*/

		/*
		DoubleMatrix2D data = distancematrix.getColtMatrix();

		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					double v = -Math.pow(value,2)/2.0;
					if (Math.abs(v) > EPSILON)
						A.setQuick(row, column, v);
					return value;
				}
			}
		);
		*/
		IntStream.range(0, distanceMatrix.nRows()).parallel()
			.forEach(row -> IntStream.range(0, distanceMatrix.nColumns())
				.forEach(col -> {
					double v = -Math.pow(distanceMatrix.doubleValue(row, col), 2)/2.0;
					if (Math.abs(v) > EPSILON)
						A.setValue(row, col, v);
				})
			);

		// A.writeMatrix("A");

		Matrix cMat = distanceMatrix.like(mat);
		Matrix cA = distanceMatrix.like(A);
		
		//System.out.println("Completed Gowers Matrix");

		// Finally, the Gower's matrix is mat*A*mat
		Matrix mat1 = cMat.ops().multiplyMatrix(cA);

		Matrix G = mat1.ops().multiplyMatrix(cMat);
		// System.out.println("Completed Gowers Matrix");
		// G.writeMatrix("Gowers");
		return G;
	}
}

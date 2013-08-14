package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

public abstract class APMatrix {
	protected double lambda; /*lambda value from 0 to 1 dampens messages passed to avoid numberical oscillation*/
	protected DoubleMatrix2D matrix;
	protected DoubleMatrix2D s_matrix;

	public APMatrix (DoubleMatrix2D s_matrix, double lambda) {
		this.matrix = DoubleFactory2D.sparse.make(s_matrix.rows(), s_matrix.columns());
		this.s_matrix = s_matrix;
		this.lambda = lambda;
	}

	public abstract double getEvidence (int row);

	public double get(int row, int column) { return matrix.get(row, column); }

	public void setDamped(int row, int column, double value) {
		double previousValue = matrix.get(row, column);
		matrix.set(row, column, previousValue*lambda+value*(1-lambda));
	}

	public DoubleMatrix2D getMatrix() { return matrix; }

	protected void printVector(String v, DoubleMatrix1D vec) {
		System.out.print(v+": ");
		for (int i = 0; i < s_matrix.rows(); i++)
			System.out.print(" "+vec.get(i));
		System.out.println("");
	}

}

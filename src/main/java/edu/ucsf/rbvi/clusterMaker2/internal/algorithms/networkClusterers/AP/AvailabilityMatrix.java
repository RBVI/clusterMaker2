package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class AvailabilityMatrix extends APMatrix {
	private DoubleMatrix1D evidenceVector = null;

	public AvailabilityMatrix (DoubleMatrix2D s_matrix, double lambda) {
		super(s_matrix, lambda);
	}

	public double getEvidence (int row) {
		if (evidenceVector == null) {
			updateEvidence();
		}
		return evidenceVector.get(row);
	}

	public void updateEvidence () { 
		evidenceVector = DoubleFactory1D.dense.make(s_matrix.rows());
		evidenceVector.assign(-Double.MAX_VALUE);
		s_matrix.forEachNonZero(new CalculateEvidence(evidenceVector));
		// printVector("Availability evidence: ", evidenceVector);
	}

	public void update(ResponsibilityMatrix r_matrix) {
		s_matrix.forEachNonZero(new UpdateAvailability(r_matrix));
	}

	//
	// These inner classes are used for the "forEachNonZero" calls...
	//
	class CalculateEvidence implements IntIntDoubleFunction {
		DoubleMatrix1D maxVector;

		public CalculateEvidence(DoubleMatrix1D maxMat) {
			this.maxVector = maxMat;
		}

		public double apply(int row, int col, double value) {
			if (row != col) {
				maxVector.set(row, Math.max(maxVector.get(row),get(row,col)+value));
			}

			return value;
		}
	}

	class UpdateAvailability implements IntIntDoubleFunction {
		ResponsibilityMatrix rMatrix;

		public UpdateAvailability(ResponsibilityMatrix responsibilityMatrix) {
			rMatrix = responsibilityMatrix;
		}

		public double apply(int row, int col, double value) {
			double newValue;
			if (row != col)
				newValue = Math.min(0.0, rMatrix.get(col, col) + rMatrix.getEvidence(col) - Math.max(0.0, rMatrix.get(row,col)));
			else
				newValue = rMatrix.getEvidence(col);

			setDamped(row, col, newValue);

			return value;
		}
	}
}

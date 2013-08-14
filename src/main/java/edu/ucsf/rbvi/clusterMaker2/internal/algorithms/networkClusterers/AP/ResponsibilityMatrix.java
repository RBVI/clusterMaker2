package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;


public class ResponsibilityMatrix extends APMatrix {
	private DoubleMatrix1D evidenceVector = null;

	public ResponsibilityMatrix (DoubleMatrix2D s_matrix, double lambda) {
		super(s_matrix, lambda);
	}

	public double getEvidence (int col) {
		if (evidenceVector == null) {
			updateEvidence();
		}
		return evidenceVector.get(col);
	}

	public void updateEvidence () { 
		evidenceVector = DoubleFactory1D.dense.make(s_matrix.columns());
		s_matrix.forEachNonZero(new CalculateEvidence(evidenceVector));
		// printVector("Responsibility evidence: ", evidenceVector);
	}

	public void update(AvailabilityMatrix a_matrix) {
		s_matrix.forEachNonZero(new UpdateResponsibility(a_matrix));
	}
	
	class UpdateResponsibility implements IntIntDoubleFunction {
		AvailabilityMatrix a_matrix;

		public UpdateResponsibility(AvailabilityMatrix a_matrix) {
			this.a_matrix = a_matrix;
		}

		public double apply(int row, int col, double value) {
			double newValue;
			if (row != col)
				newValue = value - a_matrix.getEvidence(row);
			else
				newValue = s_matrix.get(row, col) - a_matrix.getEvidence(row);

			// Damp
			setDamped(row, col, newValue);

			return value;
		}
	}

	class CalculateEvidence implements IntIntDoubleFunction {
		DoubleMatrix1D maxVector;

		public CalculateEvidence(DoubleMatrix1D maxMat) {
			maxVector = maxMat;
		}

		public double apply(int row, int col, double value) {
			if (row != col) {
				maxVector.set(col, maxVector.get(col) + Math.max(0.0, get(row, col)));
			}
			return value;
		}
	}

}

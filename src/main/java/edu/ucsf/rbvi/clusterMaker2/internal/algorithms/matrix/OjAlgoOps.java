package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.matrix.task.TaskException;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixOps;

public class OjAlgoOps implements MatrixOps {
	protected final OjAlgoMatrix matrix;
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	private Eigenvalue<Double> decomp = null;
	private int nThreads = -1;
	final Logger logger = Logger.getLogger(CyUserLog.NAME);

	public OjAlgoOps(OjAlgoMatrix matrix) {
		nThreads = Runtime.getRuntime().availableProcessors()-1;
		this.matrix = matrix;
	}

	public void threshold() {
		threshold(EPSILON);
	}

	public void threshold(final double thresh) {
		Thresh t = new Thresh(thresh);
		matrix.data.modifyAll(t);
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (!matrix.isSymmetrical()) {
			logger.warn("clusterMaker2 OjAlgoMatrix: attempt to invert an assymetric network");
		}

		final InverterTask<Double> tmpInverter = InverterTask.PRIMITIVE.make(matrix.data, false, false);
		final DecompositionStore<Double> tmpAlloc = tmpInverter.preallocate(matrix.data);

		try {
			MatrixStore<Double> inv = tmpInverter.invert(matrix.data, tmpAlloc);
			matrix.data = matrix.storeFactory.copy(inv);
		} catch(Exception e) { e.printStackTrace(); }
	}

	/**
	 * Normalize the matrix in place
	 * 
	 * TODO: This isn't really matrix normalization.  Do we want to
	 * do real normalization here?
	 */
	public void normalize() {
		double minValue = matrix.getMinValue();
		double span = matrix.getMaxValue() - minValue;
		for (int row = 0; row < matrix.nRows(); row++) {
			for (int col = matrix.colStart(row); col < matrix.nColumns(); col++) {
				Double d = matrix.getValue(row, col);
				if (d == null)
					continue;
				matrix.setValue(row, col, (d-minValue)/span);
				if (matrix.isSymmetrical() && col != row)
					matrix.setValue(col, row, (d-minValue)/span);
			}
		}
		matrix.updateMinMax();
	}

	/**
	 * Normalize the matrix such that the sum of all elements=1
	 */
	public void normalizeMatrix() {
		// Start by seeing if we need to adjust the matrix to be
		// positive
		double minValue = matrix.getMinValue();
		if (minValue < 0.0) {
			addScalar(-minValue);
		}

		// Get the sum of all of the cells in the matrix
		double sum = matrix.data.aggregateAll(Aggregator.SUM);
		divideScalar(sum);  // Devide all of the cells by the sum
		matrix.updateMinMax();
	}

	public void normalizeRow(int row) {
		// First see if we've got any negative numbers
		AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().minimum();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		double minValue =  tmpVisitor.getNumber();

		// FIXME
		if (minValue < 0) {
			for (int column = 0; column < matrix.nColumns(); column++) 
				matrix.setValue(row, column, matrix.getValue(row, column)-minValue);
		}

		tmpVisitor = matrix.storeFactory.aggregator().sum();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		double sum =  tmpVisitor.getNumber();

		for (int column = 0; column < matrix.nColumns(); column++) 
			matrix.setValue(row, column, matrix.getValue(row, column)/sum);
		matrix.updateMinMax();
	}

	public void normalizeColumn(int column) {
		// First see if we've got any negative numbers
		AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().minimum();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		double minValue =  tmpVisitor.getNumber();

		// FIXME
		if (minValue < 0) {
			for (int row = 0; row < matrix.nRows(); row++) 
				matrix.setValue(row, column, matrix.getValue(row, column)-minValue);
		}

		tmpVisitor = matrix.storeFactory.aggregator().sum();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		double sum =  tmpVisitor.getNumber();

		for (int row = 0; row < matrix.nRows(); row++) 
			matrix.setValue(row, column, matrix.getValue(row, column)/sum);
		matrix.updateMinMax();
	}

	public void standardizeRow(int row) {
		double mean = rowMean(row);
		double variance = rowVariance(row, mean);
		double stdev = Math.sqrt(variance);
		for (int column = 0; column < matrix.nColumns(); column++) {
			double cell = matrix.getValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
		matrix.updateMinMax();
	}

	public void standardizeColumn(int column) {
		double mean = columnMean(column);
		double variance = columnVariance(column, mean);
		double stdev = Math.sqrt(variance);
		for (int row = 0; row < matrix.nRows(); row++) {
			double cell = matrix.getValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
		matrix.updateMinMax();
	}

	// FIXME
	public void centralizeColumns() {
		for(int col=0;col<matrix.nColumns();col++){
			double mean = columnMean(col);
			for(int row=0;row<matrix.nRows();row++){
				double cell = matrix.getValue(row, col);
				if (!Double.isNaN(cell))
					matrix.setValue(row, col, cell - mean);
				else
					matrix.setValue(row, col, 0.0d);
			}
		}
		matrix.updateMinMax();
	}

	public void centralizeRows() {
		for(int row=0;row<matrix.nRows();row++){
			double mean = rowMean(row);

			for(int col=0;col<matrix.nColumns();col++){
				double cell = matrix.getValue(row, col);
				if (!Double.isNaN(cell))
					matrix.setValue(row, col, cell - mean);
				else
					matrix.setValue(row, col, 0.0d);
			}
		}
		matrix.updateMinMax();
	}

	public double columnSum(int column) {
		final AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().sum();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		return tmpVisitor.getNumber();
	}

	public double columnSum2(int column) {
		final AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().sum2();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		return tmpVisitor.getNumber();
	}

	public double rowSum(int row) {
		final AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().sum();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		return tmpVisitor.getNumber();
	}

	public double rowSum2(int row) {
		final AggregatorFunction<Double> tmpVisitor = matrix.storeFactory.aggregator().sum2();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		return tmpVisitor.getNumber();
	}

	public double columnMean(int column) {
		double sum = columnSum(column);
		return sum/matrix.nRows();
	}

	public double rowMean(int row) {
		double sum = rowSum(row);
		return sum/matrix.nColumns();
	}
	
	public double columnVariance(int column) {
		double mean = columnMean(column);
		return columnVariance(column, mean);
	}

	// FIXME
	public double columnVariance(int column, double mean) {
		double variance = 0.0;
		for(int j=0;j<matrix.nRows(); j++){
			double cell = matrix.getValue(j, column);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nRows();
	}
	
	public double rowVariance(int row) {
		double mean = rowMean(row);
		return rowVariance(row, mean);
	}

	public double rowVariance(int row, double mean) {
		double variance = 0.0;
		for(int j=0;j<matrix.nColumns(); j++){
			double cell = matrix.getValue(row, j);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nColumns();
	}

	public Matrix covariance() {
		PhysicalStore<Double> cov = matrix.storeFactory.makeZero(matrix.nColumns(), matrix.nColumns());
		double[] columnMeans = new double[matrix.nColumns()];
		for (int i=0; i < matrix.nColumns(); i++) {
			columnMeans[i] = columnMean(i);
		}

		for (int i=0; i < matrix.nColumns(); i++) {
			for (int j=0; j < i; j++) {
				double covij = covariance(i, j, columnMeans[i], columnMeans[j]);
				cov.set(i, j, covij);
				cov.set(j, i, covij);
			}
			double mean = columnMean(i);
			double variance = columnVariance(i, mean);
			cov.set(i, i, variance);
		}
		return matrix.copyDataFromMatrix(cov);
	}

	/**
	 * Some simple utility methods
	 */

	/**
	 * Add a value to all cells in the matrix
	 */
	public void addScalar(double value) {
		matrix.data.modifyAll(PrimitiveFunction.ADD.second(value));
	}

	/**
	 * Add a matrix to this matrix
	 */
	public Matrix addMatrix(Matrix addend) {
		OjAlgoMatrix ojAddend = (OjAlgoMatrix)addend;
		MatrixStore<Double> d = matrix.data.add(ojAddend.data);
		return new OjAlgoMatrix(matrix, d);
	}

	/**
	 * Subtract a value to all cells in the matrix
	 */
	public void subtractScalar(double value) {
		matrix.data.modifyAll(PrimitiveFunction.SUBTRACT.second(value));
	}

	/**
	 * Subtract a matrix from this matrix
	 */
	public Matrix subtractMatrix(Matrix subend) {
		OjAlgoMatrix ojSubend = (OjAlgoMatrix)subend;
		MatrixStore<Double> d = matrix.data.subtract(ojSubend.data);
		return new OjAlgoMatrix(matrix, d);
	}

	/**
	 * Multiply a value to all cells in the matrix
	 */
	public void multiplyScalar(double value) {
		matrix.data.modifyAll(PrimitiveFunction.MULTIPLY.second(value));
	}
	
	public Matrix multiplyMatrix(Matrix m2) {
		if (matrix instanceof OjAlgoMatrix) {
			OjAlgoMatrix m = (OjAlgoMatrix)m2;
			PhysicalStore<Double> mat = (PhysicalStore<Double>)matrix.data.multiply(m.data);
			return new OjAlgoMatrix(matrix, mat);
		}
		return null; // Throw error -- mismatched implementations?
	}

	/**
	 * Divide a value to all cells in the matrix
	 */
	public void divideScalar(double value) {
		matrix.data.modifyAll(PrimitiveFunction.DIVIDE.second(value));
	}

	/**
	 * Raise all of the cells in the matrix by a power
	 */
	public void powScalar(double value) {
		Pow p = new Pow(value);
		matrix.data.modifyAll(p);
	}

	private double covariance(int i, int j, double iMean, double jMean) {
		double result = 0;
		for (int row = 0; row < matrix.nRows(); row++) {
			double iDev = matrix.doubleValue(row, i) - iMean;
			double jDev = matrix.doubleValue(row, j) - jMean;
			result += (iDev*jDev) / matrix.nRows();
		}
		return result;
	}

	/**
	 * Calculates the Pearson correlation matrix
	 */
	public Matrix correlation() {
		PhysicalStore<Double> corr = matrix.storeFactory.makeZero(matrix.nColumns(), matrix.nColumns());
		double[] columnMeans = new double[matrix.nColumns()];
		double[] columnStdDev = new double[matrix.nColumns()];
		for (int i=0; i < matrix.nColumns(); i++) {
			columnMeans[i] = columnMean(i);
			columnStdDev[i] = stdDev(i, columnMeans[i]);
		}
		for (int i = 0; i < matrix.nColumns(); i++) {
			for (int j = 0; j < i; j++) {
				double covij = covariance(i, j, columnMeans[i], columnMeans[j]);
				if (covij != 0.0) {
					double stdDevi = columnStdDev[i];
					double stdDevj = columnStdDev[j];
					double corrij = covij/(stdDevi*stdDevj);
					corr.set(i, j, corrij);
					corr.set(j, i, corrij);
				}
			}
			corr.set(i, i, 1.0);
		}
		return matrix.copyDataFromMatrix(corr);
	}

	private double stdDev(int column, double columnMean) {
		// double mean = columnMean(column);
		double variance = columnVariance(column, columnMean);
		double stdev = Math.sqrt(variance);
		return stdev;
	}

	public double[] eigenValues(boolean nonZero){
		if (decomp == null) {
			decomp = Eigenvalue.make(matrix.data);
			decomp.decompose(matrix.data);
		}

		double[] allValues = decomp.getD().toRawCopy1D();
		if (!nonZero)
			return allValues;

		int size = 0;
		for (double d: allValues) {
			if (Math.abs(d) > EPSILON)size++;
		}
		double [] nonZ = new double[size];
		int index = 0;
		for (double d: allValues) {
			if (Math.abs(d) > EPSILON)
				nonZ[index++] = d;
		}

		return nonZ;
	}

	public double[][] eigenVectors(){
		if (decomp == null) {
			decomp = Eigenvalue.make(matrix.data);
			decomp.decompose(matrix.data);
		}

		MatrixStore<Double> eigv = decomp.getV();
		return eigv.toRawCopy2D();
	}

	public int cardinality() { return matrix.data.aggregateAll(Aggregator.CARDINALITY).intValue(); }

	public Matrix mult(Matrix b) {
		return multiplyMatrix(b);
	}

	/*
	public Matrix gowers() {
		// Create the Identity matrix
		DoubleMatrix2D I = DoubleFactory2D.sparse.identity(this.nRows());

		// Create the ones matrix.  This is equivalent to 11'/n
		DoubleMatrix2D one = DoubleFactory2D.dense.make(this.nRows(), this.nRows(), 1.0/this.nRows());

		// Create the subtraction matrix (I-11'/n)
		DoubleMatrix2D mat = I.assign(one, DoubleFunctions.minus);

		// Create our data matrix
		final DoubleMatrix2D A = DoubleFactory2D.sparse.make(this.nRows(), this.nRows());

		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					A.setQuick(row, column, -Math.pow(value,2)/2.0);
					return value;
				}
			}
		);

		OjAlgoMatrix cMat = new OjAlgoMatrix(this, mat);
		OjAlgoMatrix cA = new OjAlgoMatrix(this, A);

		// Finally, the Gower's matrix is mat*A*mat
		
		Matrix mat1 = cMat.multiplyMatrix(cA);
		return mat1.multiplyMatrix(cMat);
	}
	*/

	class Thresh implements PrimitiveFunction.Unary {
		double epsilon;
		public Thresh(double epsilon) {
			this.epsilon = epsilon;
		}
		public double invoke(double a) {
			if (a <= epsilon)
				return 0.0;
			return a;
		}
	}

	class Pow implements PrimitiveFunction.Unary {
		double power;
		public Pow(double power) {
			this.power = power;
		}
		public double invoke(double a) {
			return Math.pow(a, power);
		}
	}
}

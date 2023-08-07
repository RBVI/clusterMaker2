package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.MatrixOps;

public class OjAlgoOps implements MatrixOps {
	protected final OjAlgoMatrix matrix;
	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
	private Eigenvalue<Double> decomp = null;
	private SingularValue<Double> svdDecomp = null;
	private int nThreads = -1;
	final Logger logger = Logger.getLogger(CyUserLog.NAME);

	// This is used for Eigenvalue deomposition, which is faster with Colt
	// ColtMatrix cMat;

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
	 * Create a new matrix that is the transpose of this one
	 */
	public Matrix transpose() {
		Primitive64Store data = matrix.storeFactory.transpose(matrix.data);
		OjAlgoMatrix result = new OjAlgoMatrix(matrix, data);
		result.transposed = true;
		return result;
	}

	/**
	 * Invert the matrix in place
	 */
	public void invertMatrix() {
		if (!matrix.isSymmetrical()) {
			logger.warn("clusterMaker2 OjAlgoMatrix: attempt to invert an assymetric network");
		}

		final InverterTask<Double> tmpInverter = InverterTask.PRIMITIVE.make(matrix.data, false, false);
		final PhysicalStore<Double> tmpAlloc = tmpInverter.preallocate(matrix.data);

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
		matrix.minValue = Double.MAX_VALUE;
		matrix.maxValue = Double.MIN_VALUE;
		for (int row = 0; row < matrix.nRows(); row++) {
			for (int col = matrix.colStart(row); col < matrix.nColumns(); col++) {
				double d = matrix.doubleValue(row, col);
				if (Double.isNaN(d))
					continue;
				matrix.setValue(row, col, (d-minValue)/span);
				if (matrix.isSymmetrical() && col != row)
					matrix.setValue(col, row, (d-minValue)/span);
			}
		}
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
		double sum = sum();
		divideScalar(sum);  // Devide all of the cells by the sum
		matrix.updateMinMax();
	}

	public double normalizeRow(int row) {
		double sum = rowSum(row);
		matrix.data.modifyRow(row, 0L, PrimitiveMath.DIVIDE.second(sum));
		return sum;
	}

	public double normalizeColumn(int column) {
		double sum = columnSum(column);
		matrix.data.modifyColumn(0L, column, PrimitiveMath.DIVIDE.second(sum));
		return sum;
	}

	public void standardizeRow(int row) {
		double mean = rowMean(row);
		double variance = rowVariance(row, mean);
		double stdev = Math.sqrt(variance);
		for (int column = 0; column < matrix.nColumns(); column++) {
			double cell = matrix.doubleValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	public void standardizeColumn(int column) {
		double mean = columnMean(column);
		double variance = columnVariance(column, mean);
		double stdev = Math.sqrt(variance);
		for (int row = 0; row < matrix.nRows(); row++) {
			double cell = matrix.doubleValue(row, column);
			matrix.setValue(row, column, (cell-mean)/stdev);
		}
	}

	// FIXME
	public void centralizeColumns() {
		for(int col=0;col<matrix.nColumns();col++){
			double mean = columnMean(col);
			for(int row=0;row<matrix.nRows();row++){
				double cell = matrix.doubleValue(row, col);
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
				double cell = matrix.doubleValue(row, col);
				if (!Double.isNaN(cell))
					matrix.setValue(row, col, cell - mean);
				else
					matrix.setValue(row, col, 0.0d);
			}
		}
		matrix.updateMinMax();
	}

	public double sum() {
		// This method results in a loss of precision!  Rounding
		// error somewhere (autoboxing?)
		// return matrix.data.aggregateAll(Aggregator.SUM);
		final AggregatorFunction<Double> tmpVisitor = MySUM.get().reset();
		matrix.data.visitAll(tmpVisitor);
		return tmpVisitor.doubleValue();
		/*
		double sum = 0.0;
		for (int col = 0; col < matrix.data.countColumns(); col++)
			sum += columnSum(col);
		return sum;
		*/
	}

	public double columnSum(int column) {
		final AggregatorFunction<Double> tmpVisitor = MySUM.get().reset();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		return tmpVisitor.doubleValue();
	}

	public double columnSum2(int column) {
		final AggregatorFunction<Double> tmpVisitor = MySUM2.get().reset();
		matrix.data.visitColumn(0L, column, tmpVisitor);
		return tmpVisitor.doubleValue();
	}

	public double rowSum(int row) {
		final AggregatorFunction<Double> tmpVisitor = MySUM.get().reset();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		return tmpVisitor.doubleValue();
	}

	public double rowSum2(int row) {
		final AggregatorFunction<Double> tmpVisitor = MySUM2.get().reset();
		matrix.data.visitRow(row, 0L, tmpVisitor);
		return tmpVisitor.doubleValue();
	}

	public double columnMean(int column) {
		double sum = columnSum(column);
		return sum/(double)matrix.data.countRows();
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
			double cell = matrix.doubleValue(j, column);
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
			double cell = matrix.doubleValue(row, j);
			if (!Double.isNaN(cell))
				variance += Math.pow((cell-mean),2);
		}
		return variance/matrix.nColumns();
	}

	public Matrix covariance() {
    OjAlgoMatrix mat = new OjAlgoMatrix(matrix.nColumns(), matrix.nColumns());
    Primitive64Store cov = mat.data;
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
		matrix.data.modifyAll(PrimitiveMath.ADD.second(value));
	}

	/**
	 * Add a matrix to this matrix
	 */
	public void addElement(Matrix addend) {
		OjAlgoMatrix ojAddend = (OjAlgoMatrix)addend;
		MatrixStore<Double> d = matrix.data.add(ojAddend.data);
		matrix.data = (Primitive64Store)d;
	}

	/**
	 * Subtract a value to all cells in the matrix
	 */
	public void subtractScalar(double value) {
		matrix.data.modifyAll(PrimitiveMath.SUBTRACT.second(value));
	}

	/**
	 * Subtract a matrix from this matrix
	 */
	public void subtractElement(Matrix subend) {
		OjAlgoMatrix ojSubend = (OjAlgoMatrix)subend;
		MatrixStore<Double> d = matrix.data.subtract(ojSubend.data);
		matrix.data = (Primitive64Store)d;
	}

	/**
	 * Multiply a value to all cells in the matrix
	 */
	public void multiplyScalar(double value) {
		matrix.data.modifyAll(PrimitiveMath.MULTIPLY.second(value));
	}
	
	public Matrix multiplyMatrix(Matrix m2) {
		if (m2 instanceof OjAlgoMatrix) {
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
		matrix.data.modifyAll(PrimitiveMath.DIVIDE.second(value));
	}

	/**
	 * divide all cells in a column by a value.  This is used
	 * primarily for normalization when the current sum
	 * of the column is already known.
	 * Note: does not update matrix min/max values.  
	 * 
	 * @param column the column we're dividing
	 * @param value to divide each cell in the column by
	 */
	public void divideScalarColumn(int column, double value) {
		matrix.data.modifyColumn(0L, column, PrimitiveMath.DIVIDE.second(value));
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
		OjAlgoMatrix ojCorr = new OjAlgoMatrix(matrix.nColumns(), matrix.nColumns());
    Primitive64Store corr = ojCorr.data;
		// Primitive64Store corr = (Primitive64Store)matrix.storeFactory.makeFilled(matrix.nColumns(), matrix.nColumns());
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

	public void eigenInit(){
		decomp = null;
	}

	public double[] eigenValues(boolean nonZero){
		if (decomp == null) {
			decomp = Eigenvalue.PRIMITIVE.make(matrix.data);
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
			decomp = Eigenvalue.PRIMITIVE.make(matrix.data);
			decomp.decompose(matrix.data);
		}

		MatrixStore<Double> eigv = decomp.getV();

		return eigv.toRawCopy2D();
	}

	public void svdInit(){
		svdDecomp = null;
	}

	public Matrix svdU() {
		if (svdDecomp == null) {
			svdDecomp = SingularValue.PRIMITIVE.make(matrix.data);
			svdDecomp.decompose(matrix.data);
		}
		return wrap(svdDecomp.getU());
	}

	public Matrix svdS() {
		if (svdDecomp == null) {
			svdDecomp = SingularValue.PRIMITIVE.make(matrix.data);
			svdDecomp.decompose(matrix.data);
		}
		return wrap(svdDecomp.getD());
	}

	public Matrix svdV() {
		if (svdDecomp == null) {
			svdDecomp = SingularValue.PRIMITIVE.make(matrix.data);
			svdDecomp.decompose(matrix.data);
		}
		return wrap(svdDecomp.getV());
	}

	public int cardinality() { return matrix.data.aggregateAll(Aggregator.CARDINALITY).intValue(); }

	public Matrix mult(Matrix b) {
		return multiplyMatrix(b);
	}

	private Matrix wrap(MatrixStore<Double> mat) {
		OjAlgoMatrix result = new OjAlgoMatrix();
		result.data = matrix.storeFactory.copy(mat);
		result.nRows = (int)mat.countRows();
		result.nColumns = (int)mat.countColumns();
		return result;
	}

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

	public static final ThreadLocal<AggregatorFunction<Double>> MySUM = new ThreadLocal<AggregatorFunction<Double>>() {

		@Override
		protected AggregatorFunction<Double> initialValue() {
			return new AggregatorFunction<Double>() {

				private double sum = 0.0;

				public void invoke(final Double anArg) {
					if (anArg != null) invoke(anArg.doubleValue());
				}
				public void invoke(final double anArg) {
					if (!Double.isNaN(anArg))
						sum += anArg;
				}

				public double doubleValue() { return sum; }
				public Scalar<Double> toScalar() { 
					return PrimitiveScalar.of(this.doubleValue()); 
				}
				public AggregatorFunction<Double> reset() { sum = 0.0; return this; }
				public void merge(final Double result) {
					this.invoke(result.doubleValue());
				}
				public Double merge(final Double result1, final Double result2) {
					return result1 + result2;
				}
				/*
				public Double getNumber() {
					return Double.valueOf(this.doubleValue());
				}
				*/
				public Double get() {
					return Double.valueOf(this.doubleValue());
				}
			};
		}
	};

	public static final ThreadLocal<AggregatorFunction<Double>> MySUM2 = new ThreadLocal<AggregatorFunction<Double>>() {

		@Override
		protected AggregatorFunction<Double> initialValue() {
			return new AggregatorFunction<Double>() {

				private double sum = 0;

				public void invoke(final Double anArg) {
					if (anArg != null) invoke(anArg.doubleValue());
				}
				public void invoke(final double anArg) {
					if (!Double.isNaN(anArg))
						sum += anArg*anArg;
				}

				public double doubleValue() { return sum; }
				public Scalar<Double> toScalar() { return PrimitiveScalar.of(this.doubleValue()); }
				public AggregatorFunction<Double> reset() { sum = 0.0; return this; }
				public void merge(final Double result) {
					this.invoke(result.doubleValue());
				}
				public Double merge(final Double result1, final Double result2) {
					return result1 + result2;
				}
				public Double get() {
					return Double.valueOf(this.doubleValue());
				}
			};
		}
	};

}

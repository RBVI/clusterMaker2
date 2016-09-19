package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import java.text.DecimalFormat;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;



public class CalculationMatrix {
	static DecimalFormat mydecimalFormat = new DecimalFormat("00.###E0");
	private static ForkJoinPool pool = new ForkJoinPool();
	public static int noDigits = 4;

	public static String formatDouble(double d) {
		if ( d == 0.0 ) return "<0.0>";
		if ( d<0.0001 && d>0 || d > -0.0001 && d < 0) {
			return mydecimalFormat.format(d);
		} else {
			String formatString = "%." + noDigits + "f";
			return String.format(formatString, d);
		}
	}

	public static double [] rep(double val, int times) {
		double [] res = new double[times];
		for (int i = 0; i < res.length; i++) {
			res[i] = val;
		}
		return res;
	}
	
	public Matrix transpose(Matrix matrix) {
		 return transpose(matrix, 1000);
	}

	public Matrix transpose(Matrix matrix, int ll) {
		// return matrix.transpose();
		int cols = matrix.nColumns();
		int rows = matrix.nRows();
		Matrix transpose=new ColtMatrix(cols,rows);
		for (int i = 0; i < cols; i++){
			for (int j = 0; j < rows; j++){
				transpose.setValue(i, j, matrix.getValue(j, i));
			}
		}
		return transpose;
	}

	
	public static Matrix exp(Matrix m1) {
		Matrix matrix=new ColtMatrix(m1.nRows(),m1.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				matrix.setValue(i, j, Math.exp(m1.getValue(i, j)));
			}
		}
		return matrix;
	}

	public static double [] sqrt(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.sqrt(v1[i]);
		}
		return vector;
	}

	
	public static double mean(double [] vector) {
		double sum = 0.0;
		for (int i = 0; i < vector.length; i++) {
			sum +=vector[i];
		}
		return sum/vector.length;
	}

	public static Matrix log(Matrix m1) {
		Matrix matrix=new ColtMatrix(m1.nRows(),m1.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				matrix.setValue(i, j, Math.log(m1.getValue(i, j)));
			}
		}
		return matrix;
	}
	
	public static double [] pow(double [] m1, double power) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = Math.pow(m1[i], power);
		}
		return matrix;
	}

	public static Matrix scalarInverse(Matrix m1) {
		Matrix matrix=new ColtMatrix(m1.nRows(),m1.nColumns());
		
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				matrix.setValue(i, j, (1/m1.getValue(i, j)));
			}
		}
		return matrix;
	}
	
	public static double [] scalarInverse(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = 1/v1[i];
		}
		return vector;
	}

	
	public static Matrix rnorm(int m, int n) {
		Matrix matrix=new ColtMatrix(m, n);
		for (int i = 0; i < m; i++) {				
			for (int j = 0; j < matrix.nColumns(); j++) {
				double value = rnorm(0.0,1.0);
				matrix.setValue(i, j, value);
			}
		}
		return matrix;
	}
	
	public static double [] rnorm(int n, double [] mus, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mus[i] + (rnorm() * sigmas[i]);
		}
		return res; 
	}

	public static double [] rnorm(int n, double mu, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mu + (rnorm() * sigmas[i]);
		}
		return res; 
	}

	public static double rnorm() {
		return ThreadLocalRandom.current().nextGaussian();
	}

	public static double rnorm(double mu, double sigma) {
		return mu + (rnorm() * sigma);
	}

	
	public static boolean [][] equal(boolean [][] matrix1, boolean [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = (matrix1[i][j] == matrix2[i][j]);
			}
		}
		return equals;
	}

	public static boolean [][] biggerThan(Matrix matrix, double value) {
		boolean [][] equals = new boolean[matrix.nRows()][matrix.nColumns()];
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				equals[i][j] = Double.compare(matrix.getValue(i, j), value) == 1;
			}
		}
		return equals;
	}
	
	public static boolean [][] negate(boolean [][] booleans) {
		boolean [][] negates = new boolean[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				negates[i][j] = !booleans[i][j];
			}
		}
		return negates;
	}

	public static Matrix abs(boolean [][] booleans) {
		Matrix absolutes = new ColtMatrix(booleans.length,booleans[0].length);
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				absolutes.setValue(i, j, (booleans[i][j] ? 1 : 0));
			}
		}
		return absolutes;
	}

	
	public static Matrix mean(Matrix matrix, int axis) {
		
		Matrix result;
		double zerovalue=0;
		if( axis == 0) {
			result = new ColtMatrix(1,matrix.nColumns());
			for (int j = 0; j < matrix.nColumns(); j++) {
				double colsum = 0.0;
				for (int i = 0; i < matrix.nRows(); i++) {
					colsum += matrix.getValue(i, j);
				}
				result.setValue(0, j, (colsum/matrix.nRows()));
			}
		}   else if (axis == 1) {
			result = new ColtMatrix(matrix.nRows(),1);
			for (int i = 0; i < matrix.nRows(); i++) {
				double rowsum = 0.0;
				for (int j = 0; j < matrix.nColumns(); j++) {
					rowsum += matrix.getValue(i, j);
				}
				result.setValue(i, 0, (rowsum / matrix.nColumns()));
			}
		}   else if (axis == 2) {
			result=new ColtMatrix(1,1);
			for (int j = 0; j < matrix.nColumns(); j++) {
				for (int i = 0; i < matrix.nRows(); i++) {					
					zerovalue+=matrix.getValue(i, j);
				}
			}
			zerovalue/=(matrix.nColumns()*matrix.nRows());
			result.setValue(0, 0, zerovalue);
		}else {
			throw  new IllegalArgumentException("Axes other than 0,1,2 is unsupported");
		}
		return result;
	}

	
	public static Matrix sum(Matrix matrix, int axis) {
		
		Matrix result;
		if( axis == 0) {
			
			result = new ColtMatrix(1,matrix.nColumns());
			for (int j = 0; j < matrix.nColumns(); j++) {
				double rowsum = 0.0;
				for (int i = 0; i < matrix.nRows(); i++) {
					rowsum += matrix.getValue(i, j);
				}
				result.setValue(0, j, rowsum);
			}
		}   else if (axis == 1) {
			result=new ColtMatrix(matrix.nRows(), 1);
			for (int i = 0; i < matrix.nRows(); i++) {
				double colsum = 0.0;
				for (int j = 0; j < matrix.nColumns(); j++) {
					colsum += matrix.getValue(i, j);
				}
				result.setValue(i, 0, colsum);
			}
		}   else {
			throw  new IllegalArgumentException("Axes other than 0,1 is unsupported");
		}
		return result;
	}


	public static double sum(Matrix matrix) {
		double sum = 0.0;
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				sum+=matrix.getValue(i, j);
			}
		}
		return sum;
	}

	public static Matrix maximum(Matrix matrix, double maxval) {
		
		Matrix maxed=new ColtMatrix(matrix.nRows(),matrix.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				maxed.setValue(i, j, (matrix.getValue(i, j) > maxval ? matrix.getValue(i, j):maxval));
			}
		}
		return maxed;
	}
	

	public static void assignAllLessThan(Matrix matrix, double lessthan, double assign) {
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				if( matrix.getValue(i, j) < lessthan) {
					matrix.setValue(i, j, assign);
				}
			}
		}
	}
	public static Matrix square(Matrix matrix) {
		return scalarPow(matrix,2);
	}


	public static Matrix replaceNaN(Matrix matrix, double repl) {
		
		Matrix result=new ColtMatrix(matrix.nRows(),matrix.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				if(Double.isNaN(matrix.getValue(i, j))) {
					result.setValue(i, j, repl);
				} else {
					result.setValue(i, j, matrix.getValue(i, j));
				}
			}
		}
		return result;
	}

	
	public static Matrix scalarPow(Matrix matrix, double power) {
		Matrix result=new ColtMatrix(matrix.nRows(), matrix.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				result.setValue(i, j, Math.pow(matrix.getValue(i, j), power));
			}
		}
		return result;
	}

	public static Matrix addColumnVector(Matrix matrix, Matrix colvector) {
		
		Matrix result=new ColtMatrix(matrix.nRows(),matrix.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				result.setValue(i, j, (matrix.getValue(i, j)+colvector.getValue(i, 0)));
			}
		}
		return result;
	}

	public static Matrix addRowVector(Matrix matrix, Matrix rowvector) {
	
		Matrix result=new ColtMatrix(matrix.nRows(),matrix.nColumns());
		for (int i = 0; i < matrix.nRows(); i++) {
			for (int j = 0; j < matrix.nColumns(); j++) {
				result.setValue(i, j, (matrix.getValue(i, j)+rowvector.getValue(0, j)));
			}
		}
		return result;
	}

	public static Matrix tile(Matrix matrix, int rowtimes, int coltimes) {
		
		Matrix result=new ColtMatrix(matrix.nRows()*rowtimes,matrix.nColumns()*coltimes);
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (int j = 0; j < matrix.nRows(); j++) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix.nColumns(); l++) {
						result.setValue(resultrow, resultcol++, matrix.getValue(j, l));
					}
				}
				resultrow++;
			}
		}

		return result;
	}
	
	public static int [] range(int n) {
		int [] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = i;
		}
		return result;
	}

	public static int [] range(int a, int b) {
		if( b < a ) {
			throw new IllegalArgumentException("b has to be larger than a");
		}
		int val = a;
		int [] result = new int[b-a];
		for (int i = 0; i < (b-a); i++) {
			result[i] = val++;
		}
		return result;
	}

	
	public static int [] concatenate(int [] v1,int [] v2) {
		int [] result = new int[v1.length+v2.length];
		int index = 0;
		for (int i = 0; i < v1.length; i++, index++) {
			result[index] = v1[index];
		}
		for (int i = 0; i < v2.length; i++, index++) {
			result[index] = v2[i];
		}
		return result;
	}

	public Matrix scalarMultiply(Matrix m1,Matrix m2) {
		return parScalarMultiply(m1, m2);
	}
	
	
	public Matrix parScalarMultiply(Matrix m1,Matrix m2) {
		int ll = 600;
		Matrix result=new ColtMatrix(m1.nRows(),m1.nColumns());
		MatrixOperator process = new MatrixOperator(m1,m2,result, multiplyop, 0, m1.nRows(),ll);                
		pool.invoke(process);
		return result;
	}

	
	public Matrix parScalarMinus(Matrix m1,Matrix m2) {
		int ll = 600;
		Matrix result = new ColtMatrix(m1.nRows(),m1.nColumns());
		
		MatrixOperator process = new MatrixOperator(m1,m2,result, minusop, 0, m1.nRows(),ll);                
		pool.invoke(process);
		return result;
	}

	public interface MatrixOp {
		double compute(double op1, double op2);
	}

	MatrixOp multiplyop = new MatrixOp() {
		public double compute(double f1, double f2) {
			return f1 * f2;
		}
	};

	MatrixOp minusop = new MatrixOp() {
		public double compute(double f1, double f2) {
			return f1 - f2;
		}
	};

	class MatrixOperator extends RecursiveAction {
		final static long serialVersionUID = 1L;
		
		Matrix matrix1;
		
		Matrix matrix2;
		
		Matrix resultMatrix;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;
		MatrixOp op;

		public MatrixOperator(Matrix matrix1,Matrix matrix2,Matrix resultMatrix, 
				MatrixOp op, int startRow, int endRow, int ll) {
			this.op = op;
			this.limit = ll;
			this.matrix1 = matrix1;
			this.matrix2 = matrix2;
			this.resultMatrix = resultMatrix;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ( (endRow-startRow) <= limit ) {
					int cols = matrix1.nColumns();
					for (int i = startRow; i < endRow; i++) {
						for (int j = 0; j < cols; j++) {
							resultMatrix.setValue(i, j,op.compute(matrix1.getValue(i, j), matrix2.getValue(i, j)) );
						}
					}
				}
				else {
					int range = (endRow-startRow);
					int startRow1 = startRow;
					int endRow1 = startRow + (range / 2);
					int startRow2 = endRow1;
					int endRow2 = endRow;
					invokeAll(new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow1, endRow1, limit),
							new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow2, endRow2, limit));
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}


	public static void assignAtIndex(Matrix num, int[] range, int[] range1, double value) {
		for (int j = 0; j < range.length; j++) {
			num.setValue(range[j], range1[j], value);
		}
	}
	public static Matrix getValuesFromRow(Matrix matrix, int row, int[] indicies) {
		Matrix values=new ColtMatrix(1,indicies.length);
		for (int j = 0; j < indicies.length; j++) {
			
			values.setValue(0, j, matrix.getValue(row, indicies[j]));
		}
		return values;
	}
	
	public static void assignValuesToRow(Matrix matrix, int row, int[] indicies, double [] values) {
		if( indicies.length != values.length ) {
			throw new IllegalArgumentException("Length of indicies and values have to be equal");
		}
		for (int j = 0; j < indicies.length; j++) {
			matrix.setValue(row, indicies[j], values[j]);
		}
	}


	public static Matrix fillMatrix(int rows, int cols, double fillvalue) {
		Matrix matrix=new ColtMatrix(rows, cols);
			for (int i = 0; i < matrix.nRows(); i++){
				for (int j = 0; j < matrix.nColumns(); j++){
					matrix.setValue(i, j, fillvalue);
				}	
			}
		return matrix;
	}

	
	public static Matrix plus(Matrix m1, Matrix m2) {
		Matrix matrix = new ColtMatrix(m1.nRows(),m1.nColumns());
		
		
		for (int i = 0; i < m1.nRows(); i++){
			for (int j = 0; j < m1.nColumns(); j++){
				matrix.setValue(i, j, ( m1.getValue(i, j) + m2.getValue(i, j)));
		}
		}
			
		return matrix;
	}


	public static Matrix scalarPlus(Matrix m1, double m2) {
		Matrix matrix=new ColtMatrix(m1.nRows(),m1.nColumns());
		for (int i = 0; i < m1.nRows(); i++){
			for (int j = 0; j < m1.nColumns(); j++){
				matrix.setValue(i, j, (m1.getValue(i, j)+m2));
			}
				
		}
		return matrix;
	}

	public Matrix minus(Matrix m1, Matrix m2) {
		return parScalarMinus(m1, m2);
	}


	public static Matrix scalarDivide(Matrix numerator, double denom) {
		Matrix matrix = new ColtMatrix(numerator.nRows(),numerator.nColumns());
		for (int i = 0; i < numerator.nRows(); i++){
			for (int j = 0; j < numerator.nColumns(); j++){
				matrix.setValue(i, j, (numerator.getValue(i, j)/denom));
			}
				
		}
				
		return matrix;
	}

	public static Matrix scalarDivide(Matrix numerator, Matrix denom) {
		Matrix matrix=new ColtMatrix(numerator.nRows(),numerator.nColumns());
		for (int i = 0; i < numerator.nRows(); i++){
			for (int j = 0; j < numerator.nColumns(); j++){
				matrix.setValue(i, j, (numerator.getValue(i, j)/denom.getValue(i, j)));
			}
				
		}
			
		return matrix;
	}
	
	public static Matrix scalarMult(Matrix m1, double mul) {
		Matrix matrix=new ColtMatrix(m1.nRows(),m1.nColumns());
		for (int i = 0; i < m1.nRows(); i++){
			for (int j = 0; j < m1.nColumns(); j++){
				matrix.setValue(i, j, m1.getValue(i, j)*mul);
			}
					
		}
				
		return matrix;
	}
	
	
	public static Matrix times(Matrix m1, Matrix m2) {
		return m1.multiplyMatrix(m2);
	}

	public static Matrix diag(Matrix ds) {
		boolean isLong = ds.nRows() > ds.nColumns();
		int dim = Math.max(ds.nRows(),ds.nColumns());
		Matrix result = new ColtMatrix(dim,dim);
		for (int i = 0; i < result.nRows(); i++) {
			for (int j = 0; j < result.nColumns(); j++) {
				if(i==j) {
					if(isLong){
						result.setValue(i, j, ds.getValue(i, 0));
					}
					else{
						result.setValue(i, j, ds.getValue(0, i));
					}
						
				}
			}
		}

		return result;
	}
	

	

}

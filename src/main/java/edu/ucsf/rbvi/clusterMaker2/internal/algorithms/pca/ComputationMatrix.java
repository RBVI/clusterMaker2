/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleStatistic;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author root
 */
public class ComputationMatrix {
	private DoubleMatrix2D matrix;
	private int nRows;
	private int nColumns;
	private DenseDoubleEigenvalueDecomposition decomp = null;
	static public double MISSING_DATA = Double.NaN;

	public ComputationMatrix(double[][] values) {
		this.matrix = new DenseDoubleMatrix2D(values);
		nRows = matrix.rows();
		nColumns = matrix.columns();
	}

	public ComputationMatrix(DoubleMatrix2D matrix) {
		this.matrix = matrix;
		nRows = matrix.rows();
		nColumns = matrix.columns();
	}

	public ComputationMatrix(int rows, int columns){
		this.matrix = new SparseDoubleMatrix2D(rows, columns);
		nRows = rows;
		nColumns = columns;
	}

	public double[][] toArray(){
		return matrix.toArray();
	}

	public int nRow(){
		return nRows;
	}

	public int nColumn(){
		return nColumns;
	}

	public void setCell(int row, int column, double value){
		if (row < nRows && column < nColumns)
			matrix.setQuick(row, column, value);
	}

	public double getCell(int row, int column){
		return matrix.get(row, column);
	}

	public DoubleMatrix2D getMatrix() { return matrix; }

	public ComputationMatrix multiplyMatrix(ComputationMatrix compMat){
		DoubleMatrix2D resultMatrix = matrix.zMult(compMat.getMatrix(), null);

		if (resultMatrix == null) return null;

		return new ComputationMatrix(resultMatrix);
	}

	public ComputationMatrix centralizeRows(){
		ComputationMatrix resultCompMat = new ComputationMatrix(nRows, nColumns);
		double mean = 0;
		for(int i=0;i<nRows;i++){
			for(int j=0;j<nColumns; j++){
				double cell = this.getCell(i, j);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= nColumns;
			for(int j=0;j<nColumns;j++){
				double cell = this.getCell(i, j);
				if (!Double.isNaN(cell))
					resultCompMat.setCell(i, j, cell - mean);
				else
					resultCompMat.setCell(i, j, 0.0d);
			}
			mean = 0;
		}

		return resultCompMat;
	}

	public ComputationMatrix centralizeColumns(){
		ComputationMatrix resultCompMat = new ComputationMatrix(nRows, nColumns);
		double mean = 0;
		for(int i=0;i<nColumns;i++){
			for(int j=0;j<nRows; j++){
				double cell = this.getCell(j, i);
				if (!Double.isNaN(cell))
					mean += cell;
			}
			mean /= nRows;
			for(int j=0;j<nRows;j++){
				double cell = this.getCell(j, i);
				if (!Double.isNaN(cell))
					resultCompMat.setCell(j, i, cell - mean);
				else
					resultCompMat.setCell(i, j, 0.0d);
			}
			mean = 0;
		}

		return resultCompMat;
	}

	public ComputationMatrix covariance(){
		DoubleMatrix2D matrix2D = DoubleStatistic.covariance(matrix);
		return new ComputationMatrix(matrix2D);
	}

	public double[] eigenValues(){
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(matrix);

		double[] allValues = decomp.getRealEigenvalues().toArray();
		int size = 0;
		for (double d: allValues) {
			if (d > 0)size++;
		}
		double [] nonZero = new double[size];
		for (int i = 0; i < size; i++)
			nonZero[i] = allValues[i];

		return nonZero;
	}

	public double[][] eigenVectors(){
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(matrix);
		return decomp.getV().toArray();
	}

	public static double getMax(double[][] mat){
		int row = mat.length;
		int col = mat[0].length;
		double max = Double.NEGATIVE_INFINITY;
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				if(max < mat[i][j])
					max = mat[i][j];
			}
	   }
		return Math.ceil(max);
	}

	public static double getMin(double[][] mat){
		int row = mat.length;
		int col = mat[0].length;
		double min = Double.POSITIVE_INFINITY;
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				if(min > mat[i][j])
					min = mat[i][j];
			}
		}
		return Math.floor(min);
	}
 
	public void printMatrix(){
		int row = nRows;
		int column = nColumns;
		for(int i=0;i<row;i++){
			System.out.println("");
			for(int j=0;j<column;j++){
				System.out.print("\t" + getCell(i,j));
			}
		}
	}

	public void writeMatrix(String fileName){
		try{
			File file = new File("/tmp/" + fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter("/tmp/" + fileName, "UTF-8");

			int row = nRows;
			int column = nColumns;
			for(int i=0;i<row;i++){
				writer.write("\n");
				for(int j=0;j<column;j++){
					double cell = getCell(i,j);
					if (Double.isNaN(cell)) {
						writer.write("\tN/A");
					} else {
						BigDecimal bd = new BigDecimal(cell);
						bd = bd.round(new MathContext(3));
						writer.write("\t" + bd.doubleValue());
					}
				}
			}
			writer.close();
		}catch(IOException e){
			e.printStackTrace(System.out);
		}
	}

	public static ComputationMatrix multiplyArray(double[] first, double[] second){
		if(first.length != second.length)
			return null;

		double[][] result = new double[first.length][second.length];
		for(int i=0; i<first.length; i++){
			for(int j=0;j<second.length;j++){
				result[i][j] = first[i]*second[j];
			}
		}

		return new ComputationMatrix(result);
	}

	public static ComputationMatrix multiplyMatrixWithArray(ComputationMatrix matrix, double[] array){
		if(matrix.nColumns != array.length)
			return null;
		ComputationMatrix result = new ComputationMatrix(matrix.nRows, 1);
		double sum;
		for(int i=0;i<matrix.nRows;i++){
			sum = 0;
			for(int j=0; j<matrix.nColumns; j++){
				sum += matrix.getCell(i, j) * array[j];
			}
			result.setCell(i, 0, sum);
		}
		return result;
	}

	public static void printArray(double[] array){
		for(int i=0;i<array.length;i++)
			System.out.println(array[i]);
	}

	public static void printDoubleArray(double[][] array){
		int row = array.length;
		int col = array[0].length;
		for(int i=0;i<row;i++){
			System.out.println("");
			for(int j=0;j<col;j++){
				System.out.print("\t" + array[i][j]);
			}
		}
	}
}

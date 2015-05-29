/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
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
    private double[][] matrix;
    private int nRow;
    private int nColumn;

    public ComputationMatrix(double[][] matrix) {
        this.matrix = matrix;
        nRow = matrix.length;
        if(nRow>0)
            nColumn = matrix[0].length;
    }
    
    public ComputationMatrix(int row, int column){
        nRow = row;
        nColumn = column;
        matrix = new double[row][column];
    }
    
    public double[][] toArray(){
        return matrix;
    }
    
    public int nRow(){
        return nRow;
    }
    
    public int nColumn(){
        return nColumn;
    }
    
    public void setCell(int row, int column, double value){
        matrix[row][column] = value;
    }
    
    public double getCell(int row, int column){
        return matrix[row][column];
    }
    
    public void addToCell(int row, int column, double value){
        matrix[row][column] += value;
    }
    
    public void subtractFromCell(int row, int column, double value){
        matrix[row][column] -= value;
    }
    
    public ComputationMatrix addMatrix(ComputationMatrix compMat){
        ComputationMatrix resultCompMat = new ComputationMatrix(this.matrix);
        if(compMat.nRow == resultCompMat.nRow && compMat.nColumn == resultCompMat.nColumn){
            for(int i=0;i<resultCompMat.nRow();i++){
                for(int j=0;j<resultCompMat.nColumn;j++){
                    resultCompMat.addToCell(i, j, compMat.getCell(i, j));
                }
            }            
            return resultCompMat;
        }else{
            return null;
        }
    }
    
    public ComputationMatrix subtractMatrix(ComputationMatrix compMat){
        ComputationMatrix resultCompMat = new ComputationMatrix(this.matrix);
        if(compMat.nRow == resultCompMat.nRow && compMat.nColumn == resultCompMat.nColumn){
            for(int i=0;i<resultCompMat.nRow();i++){
                for(int j=0;j<resultCompMat.nColumn;j++){
                    resultCompMat.subtractFromCell(i, j, compMat.getCell(i, j));
                }
            }            
            return resultCompMat;
        }else{
            return null;
        }
    }
    
    public ComputationMatrix multiplyMatrix(ComputationMatrix compMat){
        if(nColumn != compMat.nRow)
            return null;
        
        ComputationMatrix resultCompMat = new ComputationMatrix(nRow, compMat.nColumn);
        double sum=0;
        
        for(int i=0; i<nRow; i++){
            for(int j=0; j<compMat.nColumn; j++){
                for(int k=0; k<nColumn; k++){
                    sum += this.getCell(i, k)*compMat.getCell(k, j);
                }
                resultCompMat.setCell(i, j, sum);
                sum = 0;
            }
        }
        
        return resultCompMat;
    }
    
    public ComputationMatrix transpose(){
        if(nRow != nColumn)
            return null;
        
        ComputationMatrix resultCompMat = new ComputationMatrix(nRow, nColumn);
        
        for(int i=0;i<nRow; i++){
            for(int j=0;j<nColumn; j++){
                resultCompMat.setCell(i, j, matrix[j][i]);
            }
        }
        
        return resultCompMat;
    }
    
    public ComputationMatrix centralizeRows(){
        ComputationMatrix resultCompMat = new ComputationMatrix(nRow, nColumn);
        double mean = 0;
        for(int i=0;i<nRow;i++){
            for(int j=0;j<nColumn; j++){
                mean += this.getCell(i, j);
            }
            mean /= nColumn;
            for(int j=0;j<nColumn;j++){
                resultCompMat.setCell(i, j, getCell(i, j) - mean);
            }
            mean = 0;
        }
        
        return resultCompMat;
    }
    
    public ComputationMatrix centralizeColumns(){
        ComputationMatrix resultCompMat = new ComputationMatrix(nRow, nColumn);
        double mean = 0;
        for(int i=0;i<nColumn;i++){
            for(int j=0;j<nRow; j++){
                mean += this.getCell(j, i);
            }
            mean /= nRow;
            for(int j=0;j<nRow;j++){
                resultCompMat.setCell(j, i, getCell(j, i) - mean);
            }
            mean = 0;
        }
        
        return resultCompMat;
    }
    
    public ComputationMatrix covariance(){
        DenseDoubleMatrix2D matrix2D = new DenseDoubleMatrix2D(matrix);
        return new ComputationMatrix(DoubleStatistic.covariance(matrix2D).toArray());
    }
    
    public double[][] diagonalMatrix(){
        DenseDoubleEigenvalueDecomposition result = new DenseDoubleEigenvalueDecomposition(new DenseDoubleMatrix2D(matrix));
        return result.getD().toArray();
    }
    
    public double[] eigenValues(){
        DenseDoubleEigenvalueDecomposition result = new DenseDoubleEigenvalueDecomposition(new DenseDoubleMatrix2D(matrix));
        return result.getRealEigenvalues().toArray();
    }
    
    public double[][] eigenVectors(){
        DenseDoubleEigenvalueDecomposition result = new DenseDoubleEigenvalueDecomposition(new DenseDoubleMatrix2D(matrix));
        return result.getV().toArray();
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
        int row = matrix.length;
        int column = matrix[0].length;
        for(int i=0;i<row;i++){
            System.out.println("");
            for(int j=0;j<column;j++){
                System.out.print("\t" + matrix[i][j]);
            }
        }
    }
    
    public void writeMatrix(String fileName){
        try{
            File file = new File("/home/vijay13/Downloads/" + fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
            PrintWriter writer = new PrintWriter("/home/vijay13/Downloads/" + fileName, "UTF-8");

            int row = matrix.length;
            int column = matrix[0].length;
            for(int i=0;i<row;i++){
                writer.write("\n");
                for(int j=0;j<column;j++){
                    BigDecimal bd = new BigDecimal(matrix[i][j]);
                    bd = bd.round(new MathContext(3));
                    writer.write("\t" + bd.doubleValue());
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
        if(matrix.nRow != matrix.nColumn || matrix.nRow != array.length)
            return null;
        ComputationMatrix result = new ComputationMatrix(matrix.nRow, 1);
        double sum;
        for(int i=0;i<matrix.nRow;i++){
            sum = 0;
            for(int j=0; j<matrix.nColumn; j++){
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

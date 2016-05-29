package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix{

	double data[][];
	int rows;
	int columns;
	
	public CalculationMatrix(int rows,int columns,double inputdata[][]){
		this.rows=rows;
		this.columns=columns;
		data=new double[rows][columns];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				this.data[row][column] = inputdata[row][column];
			}
		}
		
	}
	

	public Double getValue(int row, int column) {
		// TODO Auto-generated method stub
		return data[row][column];
	}


	public boolean isSymmetrical() {
		for( int row=0; row < data.length; row++ ){
            for( int col=0; col < row; col++ ){
            	
                if( data[row][col] != data[col][row] ){
                    return false;
                }
            }
        }
        return true;
	}

	public static double[] matrixReverse(double[] x) {

	    double[] d = new double[x.length];


	    for (int i = 0; i < x.length; i++) {
	        d[i] = x[x.length - 1 -i];
	    }
	    return d;
	}
	public static double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length; // m1 columns length
        int m2RowLength = m2.length;    // m2 rows length
        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
        int mRRowLength = m1.length;    // m result rows length
        int mRColLength = m2[0].length; // m result columns length
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }
	
	//calculate transpose of a matrix
	public  double[][] transposeMatrix(double matrix[][]){
        double[][] temp = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                temp[j][i] = matrix[i][j];
        return temp;
    }
	
	//calculate Gowern's matrix
	public double[][] getGowernsMatrix(){
		//set ones matrix with row vector
		double ones[][]=new double[rows][rows];
		for(int i=0;i<ones.length;i++){
			for(int j=0;j<ones.length;j++){
				if(j==0){
					ones[i][j]=1;	}
			}}
		
		//create unit matrix with row*row dimension
		double unitmatrix[][]=new double[rows][rows];
		for(int i=0;i<rows;i++){
			for(int j=0;j<rows;j++){
				if(i==j){
					unitmatrix[i][j]=1;
				}else{
					unitmatrix[i][j]=0;
					}}}
	
		//calculate matrixA
		double matrixA[][]=new double[rows][columns]; 
		for(int i=0;i<rows;i++){
			for(int j=0;j<columns;j++){
				matrixA[i][j]=-0.5*Math.pow(data[i][j], 2);
			}}
		
		//calculate matrixG
		double transposeOnes[][]=transposeMatrix(ones);
		
		
		double multimatrix[][]=multiplyByMatrix(ones, transposeOnes);
		double tempmatrix[][]=new double[rows][rows];
		for(int i=0;i<rows;i++){
			for(int j=0;j<rows;j++){
				tempmatrix[i][j]=unitmatrix[i][j]-(multimatrix[i][j])/rows;
			}}
		multimatrix=multiplyByMatrix(tempmatrix, matrixA);
		double matrixG[][]=multiplyByMatrix(multimatrix, tempmatrix);
	return matrixG;
	}
	
	
	public double[][] eigenAnalysis(){
		ComputationMatrix computationMatrix=new ComputationMatrix(getGowernsMatrix());
		double eigenvector[][]=computationMatrix.eigenVectors();
		double eigenvalues[]=computationMatrix.eigenValuesAll();		
		double tolerance=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
		
		int idx_size=0;//for set idx length 
		double tempeigen[]=new double[eigenvalues.length];
		for(int i=0;i<eigenvalues.length;i++){
			if(Math.abs(eigenvalues[i])>tolerance){
				tempeigen[i]=1;
				idx_size++;
			}
		}
		
		//calculate idx value from eigen values
		double idx[]=matrixReverse(tempeigen);
		int count=1;
		//double idx[]=new double[idx_size];
		for(int i=0;i<idx.length;i++){
			if(idx[i]!=0){
				idx[i]=count;
			}
			count++;
		}	
		//discard eigen values
		double reverseeigen[]=new double[eigenvalues.length];
		int j=0;
		for(int i=eigenvalues.length-1;i>=0;i--){
			reverseeigen[j]=eigenvalues[i];
			j++;
		}
		double temp_reverseeigen[]=new double[idx.length];
		for(int i=0;i<reverseeigen.length;i++){
			for(j=0;j<idx.length;j++){
		if(i+1==idx[j]){
			temp_reverseeigen[j]=reverseeigen[i];
		}
			}}		
		return eigenvector;
	}
}

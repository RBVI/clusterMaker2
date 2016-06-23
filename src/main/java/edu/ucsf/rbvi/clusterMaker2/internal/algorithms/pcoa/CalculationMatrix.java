package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix implements CyMatrix{

	double data[][];
	int rows;
	int columns;
	private int nRows;
	private int nColumns;
	int diag;//make diagnostic plots 
	int scale;//scale eigenvectors (= scores) by their eigenvalue 
	int neg;//discard (= 0), keep (= 1), or correct (= 2)  negative eigenvalues  
	double eigen_values[];
	double eigenvectors[][];
	double combine_array[][];
	double scores[][];
	private DoubleMatrix2D matrix;
	private DenseDoubleEigenvalueDecomposition decomp = null;
	
	public CalculationMatrix(int rows,int columns,double inputdata[][],int diag,int scale,int neg){
		this.matrix = new DenseDoubleMatrix2D(inputdata);
		nRows = matrix.rows();
		nColumns = matrix.columns();
		this.rows=rows;
		this.columns=columns;
		this.diag=diag;
		this.scale=scale;
		this.neg=neg;
		data=new double[rows][columns];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				this.data[row][column] = inputdata[row][column];
			}
		}
		
		if(isSymmetrical()){
	
			getGowernsMatrix();
			eigenAnalysis();
			getVarianceExplained();
			negativeEigenAnalysis();
			scaleEigenVectors();
		}
	
	}
	

	public double[][] getScores() {
		return scores;
	}


	public void setScores(double[][] scores) {
		this.scores = scores;
	}


	public int getNeg() {
		return neg;
	}


	public void setNeg(int neg) {
		this.neg = neg;
	}


	public double[][] getCombine_array() {
		return combine_array;
	}


	public void setCombine_array(double[][] combine_array) {
		this.combine_array = combine_array;
	}


	public double[] getEigen_values() {
		return eigen_values;
	}


	public void setEigen_values(double[] eigen_values) {
		this.eigen_values = eigen_values;
	}


	public double[][] getEigenvectors() {
		return eigenvectors;
	}


	public void setEigenvectors(double[][] eigenvectors) {
		this.eigenvectors = eigenvectors;
	}


	public Double getValue(int row, int column) {
		// TODO Auto-generated method stub
		return data[row][column];
	}


	//to check matrix isSymmertical
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

	//reverse matrix
	public static double[] matrixReverse(double[] x) {

	    double[] d = new double[x.length];


	    for (int i = 0; i < x.length; i++) {
	        d[i] = x[x.length - 1 -i];
	    }
	    return d;
	}
	
	//matrix multiplication
	public static double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length; 
        int m2RowLength = m2.length;    
        if(m1ColLength != m2RowLength) return null; 
        int mRRowLength = m1.length;    
        int mRColLength = m2[0].length; 
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         
            for(int j = 0; j < mRColLength; j++) {     
                for(int k = 0; k < m1ColLength; k++) { 
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
	
	
	public double[] eigenAnalysis(){
		this.matrix = new DenseDoubleMatrix2D(getGowernsMatrix());
		double eigenvector[][]=eigenVectors();
		double eigenvalues[]=eigenValues(true);	
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
		 eigen_values=new double[idx.length];
		for(int i=0;i<reverseeigen.length;i++){
			for(j=0;j<idx.length;j++){
		if(i+1==idx[j]){
			eigen_values[j]=reverseeigen[i];
		}
			}}
		
		
		//discard eigen vectors
		eigenvectors=new double[eigenvector.length][eigenvector.length];
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
				for(int k=0;k<idx.length;k++){
					if(j+1==idx[k]){
						eigenvectors[i][k]=eigenvector[i][j];
						}}}
		}
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
				if(eigenvectors[i][j]==0 && j!=eigenvectors.length-1){
					double temp=eigenvectors[i][j];
					eigenvectors[i][j]=eigenvectors[i][j+1];
					eigenvectors[i][j+1]=temp;
				}
			}}
		//calculate final length of eigen values
		int length_count=0;
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]==0 && i!=eigen_values.length-1){//shift all zeros to back
				double temp=eigen_values[i];
				eigen_values[i]=eigen_values[i+1];
				eigen_values[i+1]=temp;
			}
				if(eigen_values[i]!=0){
					length_count+=1;//count to reduced eigen val length
				}
			}
		
		
		//need n-1 axes for n objects
	if(length_count>eigen_values.length-1){
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]!=0 && length_count<i+length_count-1){
				eigen_values[i]=0;
			}
		}
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
			if(eigenvectors[i][j]!=0 && length_count<j+length_count-1){
				eigenvectors[i][j]=0;
			}
		}}
	}
		return eigen_values;
	}
	
	//get Variance explained
	public double[][] getVarianceExplained(){
		double eigen_values_sum=0;
		int length=0;//length for cum_sum and var_explain
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]!=0){
				eigen_values_sum+=eigen_values[i];
				length+=1;
			}
		}
		//calculate varianceExplained
		double var_explain[]=new double[length];
		for(int i=0;i<length;i++){
			var_explain[i]=(eigen_values[i]*100)/eigen_values_sum;
		}
		
		//calculate cumulativeSum
		double cumsum[]=new double[length];
		double sum_temp=0;
		for(int i=0;i<length;i++){
			cumsum[i]=sum_temp+var_explain[i];
			sum_temp=cumsum[i];
		}
		
		//combine two arrays
		combine_array=new double[length][2];//for all this will set a n by 2 matrix
		int j=0;
		for(int i=0;i<length;i++){
			combine_array[i][j]=var_explain[i];
			combine_array[i][j+1]=cumsum[i];
		}
		return combine_array;
	}
	
	//calculate upper triangular matrix from vector
	public double[] getUpperMatrixInVector(double symmetricmat[][]){
		
		int length=0;//calculate size of upper trianguar matrix length
		for (int j = 1; j < symmetricmat.length; j++) {
			length+=j;
		}
		double uppertrimatrix[]=new double[length];
		int p=0;
		for (int i =0; i<symmetricmat.length; i++) {
            for (int j=i ; j<symmetricmat.length ; j++) {
             if(symmetricmat[i][j]!=0){
            	 uppertrimatrix[p]=symmetricmat[i][j];
            	 p++;
             }}
            }
		return uppertrimatrix;
	}
	
	//convert column To Matrix 
	public double[][] convertColumntoMatrix(double columnmatrix[]){
		double matrix[][]=new double[eigen_values.length][eigen_values.length];
		int p=0;
		for(int i=0;i<matrix.length;i++){
			for(int j=i;j<matrix.length;j++){
				if(i==j){
					matrix[i][j]=0;
				}else{
					matrix[i][j]=columnmatrix[p];
					matrix[j][i]=columnmatrix[p];
					p++;
				}
			}	
		}
		return matrix;
	}
	
	
	// handle negative eigen values
	public double[][] negativeEigenAnalysis(){
		
		//neg=0;//for test
		double negSum=0;
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]<0){
				negSum+=i+1;
			}
		}
		
		double temp_min=0;
		double uppermatrixp[]=new double[2];//the size can be changed
		double columnmatrix[]=new double[2];//the size can be changed
		double converedmatrix[][]=new double[2][2];//the size can be changed
		if(negSum>0 && neg==2){//should include && correct value check matlab
			
			
			for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]<temp_min){
				temp_min=eigen_values[i];
			}}
			temp_min=Math.abs(temp_min);
			 uppermatrixp=getUpperMatrixInVector(data);
			 columnmatrix=new double[uppermatrixp.length];
			 for(int i=0;i<uppermatrixp.length;i++){
				 columnmatrix[i]=Math.sqrt((Math.pow(uppermatrixp[i],2)+2*temp_min));
			 }
			 converedmatrix=convertColumntoMatrix(columnmatrix);
			 CalculationMatrix calc=new CalculationMatrix(converedmatrix.length, converedmatrix.length, converedmatrix,0,0,1);
			 eigen_values=calc.getEigen_values();
			 combine_array=calc.getCombine_array();
			 scores=calc.getScores();

			 
		}else if(negSum>0 && neg<1){//discard negative eigen values
			
			int count=0;
			for(int i=0;i<eigen_values.length;i++){
				if(eigen_values[i]<0){
					eigen_values[i]=0;
				}else{
					count+=1;
				}
			}
			for(int i=0;i<eigenvectors.length;i++){
				for(int j=0;j<eigenvectors.length;j++){
					if(j+1==count){
						eigenvectors[i][j]=0;
					}}}
			
			for(int i=0;i<combine_array.length;i++){
				for(int j=0;j<2;j++){//combine array column length is 2. Because it declares using two column matrixes
					if(i+1==count){
						combine_array[i][j]=0;
					}}}
		}
		return combine_array;
	}
	
	//scale eigen vectors
	public double[][] scaleEigenVectors(){
		double temp_eigen[][]=new double[eigen_values.length][eigen_values.length];
		double multi_matrix[][];
		
		if(scale<1){//default value
			
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<eigen_values.length;j++){
					if(j==0){
						temp_eigen[i][j]=Math.pow(Math.abs(eigen_values[i]), 0.5);	
					}else{
						temp_eigen[i][j]=0;
					}
					}}
			
			temp_eigen=transposeMatrix(temp_eigen);
			multi_matrix=new double[eigenvectors.length][temp_eigen.length];
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<temp_eigen.length;j++){
					multi_matrix[i][j]=temp_eigen[0][j];
				}
			}
			scores=multiplyByMatrix(eigenvectors, multi_matrix);
				scores=eigenvectors;
			}
	return scores;
	}


	public int nRows() {
		// TODO Auto-generated method stub
		return 0;
	}


	public int nColumns() {
		// TODO Auto-generated method stub
		return 0;
	}


	public double doubleValue(int row, int column) {
		// TODO Auto-generated method stub
		return 0;
	}


	public void setValue(int row, int column, double value) {
		// TODO Auto-generated method stub
		
	}


	public void setValue(int row, int column, Double value) {
		// TODO Auto-generated method stub
		
	}


	public boolean hasValue(int row, int column) {
		// TODO Auto-generated method stub
		return false;
	}


	public String[] getColumnLabels() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getColumnLabel(int col) {
		// TODO Auto-generated method stub
		return null;
	}


	public void setColumnLabel(int col, String label) {
		// TODO Auto-generated method stub
		
	}


	public void setColumnLabels(List<String> labelList) {
		// TODO Auto-generated method stub
		
	}


	public String[] getRowLabels() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRowLabel(int row) {
		// TODO Auto-generated method stub
		return null;
	}


	public void setRowLabel(int row, String label) {
		// TODO Auto-generated method stub
		
	}


	public void setRowLabels(List<String> labelList) {
		// TODO Auto-generated method stub
		
	}


	public Matrix getDistanceMatrix(DistanceMetric metric) {
		// TODO Auto-generated method stub
		return null;
	}


	public double[][] toArray() {
		// TODO Auto-generated method stub
		return null;
	}


	public double getMaxValue() {
		// TODO Auto-generated method stub
		return 0;
	}


	public double getMinValue() {
		// TODO Auto-generated method stub
		return 0;
	}


	public boolean isTransposed() {
		// TODO Auto-generated method stub
		return false;
	}


	public void setTransposed(boolean transposed) {
		// TODO Auto-generated method stub
		
	}


	public void setSymmetrical(boolean symmetrical) {
		// TODO Auto-generated method stub
		
	}


	public void setMissingToZero() {
		// TODO Auto-generated method stub
		
	}


	public void adjustDiagonals() {
		// TODO Auto-generated method stub
		
	}


	public double[] getRank(int row) {
		// TODO Auto-generated method stub
		return null;
	}


	public void index() {
		// TODO Auto-generated method stub
		
	}


	public Matrix submatrix(int[] index) {
		// TODO Auto-generated method stub
		return null;
	}


	public Matrix submatrix(int row, int col, int rows, int cols) {
		// TODO Auto-generated method stub
		return null;
	}


	public void invertMatrix() {
		// TODO Auto-generated method stub
		
	}


	public void normalize() {
		// TODO Auto-generated method stub
		
	}


	public void normalizeMatrix() {
		// TODO Auto-generated method stub
		
	}


	public void normalizeRow(int row) {
		// TODO Auto-generated method stub
		
	}


	public void normalizeColumn(int column) {
		// TODO Auto-generated method stub
		
	}


	public void centralizeRows() {
		// TODO Auto-generated method stub
		
	}


	public void centralizeColumns() {
		// TODO Auto-generated method stub
		
	}


	public int cardinality() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Matrix multiplyMatrix(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}


	public Matrix covariance() {
		// TODO Auto-generated method stub
		return null;
	}


	public double[] eigenValues(boolean nonZero) {
		 if (decomp == null)
		decomp = new DenseDoubleEigenvalueDecomposition(matrix);

	return decomp.getRealEigenvalues().toArray();
		
	}


	public double[][] eigenVectors() {
		if (decomp == null)
			decomp = new DenseDoubleEigenvalueDecomposition(matrix);
		return decomp.getV().toArray();
		
	}


	public String printMatrixInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	public String printMatrix() {
		// TODO Auto-generated method stub
		return null;
	}


	public DoubleMatrix2D getColtMatrix() {
		// TODO Auto-generated method stub
		return null;
	}


	public CyNetwork getNetwork() {
		// TODO Auto-generated method stub
		return null;
	}


	public void setRowNodes(CyNode[] rowNodes) {
		// TODO Auto-generated method stub
		
	}


	public void setRowNodes(List<CyNode> rowNodes) {
		// TODO Auto-generated method stub
		
	}


	public void setRowNode(int row, CyNode node) {
		// TODO Auto-generated method stub
		
	}


	public CyNode getRowNode(int row) {
		// TODO Auto-generated method stub
		return null;
	}


	public List<CyNode> getRowNodes() {
		// TODO Auto-generated method stub
		return null;
	}


	public void setColumnNodes(CyNode[] columnNodes) {
		// TODO Auto-generated method stub
		
	}


	public void setColumnNodes(List<CyNode> columnNodes) {
		// TODO Auto-generated method stub
		
	}


	public void setColumnNode(int column, CyNode node) {
		// TODO Auto-generated method stub
		
	}


	public CyNode getColumnNode(int column) {
		// TODO Auto-generated method stub
		return null;
	}


	public List<CyNode> getColumnNodes() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean isAssymetricalEdge() {
		// TODO Auto-generated method stub
		return false;
	}


	public void setAssymetricalEdge(boolean assymetricalEdge) {
		// TODO Auto-generated method stub
		
	}


	public CyMatrix copy() {
		// TODO Auto-generated method stub
		return null;
	}


	public CyMatrix copy(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}
}

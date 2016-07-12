package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.function.tdouble.DoubleFunction;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
// import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.SimpleMatrix;
// import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix  {

	int diag;//make diagnostic plots 
	boolean scale;//scale eigenvectors (= scores) by their eigenvalue 
	int neg;//discard (= 0), keep (= 1), or correct (= 2)  negative eigenvalues  
	double eigen_values[];
	CyMatrix eigenVectors;
	double combine_array[][];
	double scores[][];
	CyMatrix distancematrix;
	
	public CalculationMatrix(CyMatrix matrix, int diag, boolean scale, int neg){
		this.diag = diag;
		this.scale = scale;
		this.neg = neg;
		this.distancematrix=matrix;
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
		return eigenVectors.toArray();
	}


	public boolean isSymmetricalCyMatrix() {
		return distancematrix.isSymmetrical();
	}
	
	//reverse matrix
	public static double[] matrixReverse(double[] x) {

	    double[] d = new double[x.length];

	    for (int i = 0; i < x.length; i++) {
	        d[i] = x[x.length - 1 -i];
	    }
	    return d;
	}
	
	//calculate transpose of a matrix
	public  double[][] transposeMatrix(double matrix[][]){
        double[][] temp = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                temp[j][i] = matrix[i][j];
        return temp;
    }


	public Matrix getGowersMatrix() {
		// Create the Identity matrix
		DoubleMatrix2D I = DoubleFactory2D.sparse.identity(distancematrix.nRows());

		// Create the ones matrix.  This is equivalent to 11'/n
		DoubleMatrix2D one = DoubleFactory2D.dense.make(distancematrix.nRows(), distancematrix.nRows(), 1.0/distancematrix.nRows());

		// Create the subtraction matrix (I-11'/n)
		DoubleMatrix2D mat = I.assign(one, DoubleFunctions.minus);

		// Create our data matrix
		final DoubleMatrix2D A = DoubleFactory2D.sparse.make(distancematrix.nRows(), distancematrix.nRows());

		DoubleMatrix2D data = distancematrix.getColtMatrix();

		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					A.setQuick(row, column, -Math.pow(value,2)/2.0);
					return value;
				}
			}
		);

		ColtMatrix cMat = new ColtMatrix((ColtMatrix)distancematrix, mat);
		ColtMatrix cA = new ColtMatrix((ColtMatrix)distancematrix, A);

		// Finally, the Gower's matrix is mat*A*mat
		
		Matrix mat1 = cMat.multiplyMatrix(cA);
		Matrix G = mat1.multiplyMatrix(cMat);
		return G;
	}
	
	/*
	//calculate Gowern's matrix
	public double[][] getGowernsMatrix(){
		System.out.println("Started calculating gowerns matrix ");
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
		double multimatrix[][]=multiplyByMatrix(ones, transposeMatrix(ones));
		double tempmatrix[][]=new double[rows][rows];
		for(int i=0;i<rows;i++){
			for(int j=0;j<rows;j++){
				tempmatrix[i][j]=unitmatrix[i][j]-(multimatrix[i][j])/rows;
			}}

		double matrixG[][]=multiplyByMatrix(multiplyByMatrix(tempmatrix, matrixA), tempmatrix);
		System.out.println("Finished calculating gowerns matrix ");
	return matrixG;
	}
	*/
	
	
	public double[] eigenAnalysis(){
		Matrix G = getGowersMatrix();
		double eigenvector[][]=G.eigenVectors();
		double eigenvalues[]=G.eigenValues(true);	
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
			}
		}

		CyMatrix eigenVectors = distancematrix.copy();

		for(int row=0;row<eigenVectors.nRows();row++){
			for(int col=0;col<eigenVectors.nColumns();col++){
				for(int k=0;k<idx.length;k++){
					if(col+1==idx[k]){
						eigenVectors.setValue(row, col, eigenvector[row][col]);
					}
				}
			}
		}
		for(int row=0;row<eigenVectors.nRows();row++){
			for(int col=0;col<eigenVectors.nColumns();col++){
				if(eigenVectors.getValue(row,col)==0 && col!=eigenVectors.nRows()-1){
					double temp=eigenVectors.getValue(row,col);
					eigenVectors.setValue(row, col, eigenVectors.getValue(row,col+1));
					eigenVectors.setValue(row, col+1, temp);
				}
			}
		}
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
			for(int row=0;row<eigenVectors.nRows();row++){
				for(int col=0;col<eigenVectors.nColumns();col++){
					if(eigenVectors.getValue(row, col)!=0 && length_count<col+length_count-1){
						eigenVectors.setValue(row, col, 0);
					}
				}
			}
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
	public double[] getUpperMatrixInVector(CyMatrix matrix){
		
		int length=0;//calculate size of upper trianguar matrix length
		for (int j = 1; j < matrix.nRows(); j++) {
			length+=j;
		}
		double uppertrimatrix[]=new double[length];
		int p=0;
		for (int i =0; i<matrix.nRows(); i++) {
			for (int j=i ; j<matrix.nRows() ; j++) {
		 		if(matrix.getValue(i,j)!=0){
			 		uppertrimatrix[p]=matrix.getValue(i,j);
			 		p++;
		 		}
			}
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

			// XXX: What correction is this?  Looks kind of like Cailliez, but not quite.  Why not
			// just use Lingoes correction: (D' = -0.5*D^2 - c1), which is probably much easier to compute, and we
			// won't lose track of our labels
			/*
			for(int i=0;i<eigen_values.length;i++){
				if(eigen_values[i]<temp_min){
					temp_min=eigen_values[i];
				}
			}
			temp_min=Math.abs(temp_min);
			uppermatrixp=getUpperMatrixInVector(distancematrix);
			columnmatrix=new double[uppermatrixp.length];
			for(int i=0;i<uppermatrixp.length;i++){
				columnmatrix[i]=Math.sqrt((Math.pow(uppermatrixp[i],2)+2*temp_min));
			}

			// TODO: How do we track the labels -- e.g. what nodes go with what eigenvectors??
			converedmatrix=convertColumntoMatrix(columnmatrix);
			CalculationMatrix calc=new CalculationMatrix(converedmatrix.length, converedmatrix.length, converedmatrix,0,0,1);
			eigen_values=calc.getEigen_values();
			combine_array=calc.getCombine_array();
			scores=calc.getScores();
		*/

		} else if(negSum>0 && neg<1){//discard negative eigen values
			
			int count=0;
			for(int i=0;i<eigen_values.length;i++){
				if(eigen_values[i]<0){
					eigen_values[i]=0;
				}else{
					count+=1;
				}
			}
			for(int row=0;row<eigenVectors.nRows();row++){
				for(int col=0;col<eigenVectors.nColumns();col++){
					if(col+1==count){
						eigenVectors.setValue(row, col, 0);
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
	public CyMatrix scaleEigenVectors(){
		double temp_eigen[][]=new double[eigen_values.length][eigen_values.length];
		
		if(scale){//default value
			// Use Matrix for this?
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<eigen_values.length;j++){
					if(j==0){
						temp_eigen[i][j]=Math.pow(Math.abs(eigen_values[i]), 0.5);	
					}else{
						temp_eigen[i][j]=0;
					}
				}
			}
			
			temp_eigen=transposeMatrix(temp_eigen);
			CyMatrix multi_matrix=eigenVectors.copy();
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<temp_eigen.length;j++){
					multi_matrix.setValue(i, j, temp_eigen[0][j]);
				}
			}
			Matrix scorematrix = eigenVectors.multiplyMatrix(multi_matrix);
			// Does this work or do we still mess up the correspondence between labels and values?
			return eigenVectors.copy(scorematrix);
		} else {
			return eigenVectors;
			// scores=eigenvectors;
		}
	}

}

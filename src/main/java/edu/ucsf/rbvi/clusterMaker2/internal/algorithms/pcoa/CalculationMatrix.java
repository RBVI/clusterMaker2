package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Arrays;
import java.text.DecimalFormat;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix  {

	int diag;//make diagnostic plots 
	boolean scale;//scale eigenvectors (= scores) by their eigenvalue 
	int neg;//discard (= 0), keep (= 1), or correct (= 2)  negative eigenvalues  
	double eigen_values[];
	double eigen_vectors[][];
	CyMatrix eigenVectors;
	double combine_array[][];
	double scores[][];
	CyMatrix distancematrix;


	private static double EPSILON=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens

	
	public CalculationMatrix(CyMatrix matrix, int diag, int neg){
		this.diag = diag;
		this.neg = neg;
		this.distancematrix=matrix;
	}
	

	public void setNeg(int neg) {
		this.neg = neg;
	}

	public boolean isSymmetricalCyMatrix() {
		return distancematrix.isSymmetrical();
	}


	public Matrix getGowersMatrix() {
		// Create the Identity matrix
		DoubleMatrix2D I = DoubleFactory2D.sparse.identity(distancematrix.nRows());
distancematrix.writeMatrix("distancematrix.txt");
		// Create the ones matrix.  This is equivalent to 11'/n
		DoubleMatrix2D one = DoubleFactory2D.dense.make(distancematrix.nRows(), distancematrix.nRows(), 1.0/distancematrix.nRows());

		// Create the subtraction matrix (I-11'/n)
		DoubleMatrix2D mat = I.assign(one, DoubleFunctions.minus);

		// Create our data matrix
		final DoubleMatrix2D A = DoubleFactory2D.sparse.make(distancematrix.nRows(), distancematrix.nRows());
		/*for (int row = 0; row < distancematrix.nRows(); row++) {
			for (int col = 0; col < distancematrix.nColumns(); col++) {
				System.out.print(distancematrix.getValue(row, col)+",");
			}
		}*/

		DoubleMatrix2D data = distancematrix.getColtMatrix();

		data.forEachNonZero(
			new IntIntDoubleFunction() {
				public double apply(int row, int column, double value) {
					double v = -Math.pow(value,2)/2.0;
					if (Math.abs(v) > EPSILON)
						A.setQuick(row, column, v);
					return value;
				}
			}
		);

		ColtMatrix cMat = new ColtMatrix((ColtMatrix)distancematrix, mat);
		ColtMatrix cA = new ColtMatrix((ColtMatrix)distancematrix, A);

		// Finally, the Gower's matrix is mat*A*mat

		
		
		//System.out.println("Completed Gowers Matrix");

		Matrix mat1 = cMat.ops().multiplyMatrix(cA);

		Matrix G = mat1.ops().multiplyMatrix(cMat);
		System.out.println("Completed Gowers Matrix");
		return G;
	}

		
	//do the eigen analysis for both eigen vectors and eigen values
	public double[] eigenAnalysis(){
		System.out.println("Getting Gowers Matrix");
		Matrix G = getGowersMatrix();
		System.out.println("Done Getting Gowers Matrix");
		G.writeMatrix("Gowers.txt");
		
		eigen_vectors=G.ops().eigenVectors();
		eigen_values=G.ops().eigenValues(true);	

		
		return eigen_values;
	}
	
	
	
	//get the coordinates for PCoA
	public CyMatrix[] getCoordinates(CyMatrix matrix){
		CyMatrix[] components = new CyMatrix[eigen_values.length];

		for(int j=eigen_values.length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), matrix.nRows(),1);
			for(int i=0;i<eigen_vectors.length;i++){
				result.setValue(i,j,eigen_vectors[i][j]);
			}
			// System.out.println("matrix: "+matrix.printMatrixInfo());
			// System.out.println("vector: "+result.printMatrixInfo());
			System.out.println("Matrix rows "+matrix.printMatrixInfo());
			System.out.println("Result rows "+result.printMatrixInfo());
			Matrix mat = matrix.ops().multiplyMatrix(result);
			// System.out.println("After vector multiply: "+mat.printMatrixInfo());
			components[k] = matrix.copy(mat);
			components[k].printMatrixInfo();
			components[k].writeMatrix("component_"+k+".txt");
			// System.out.println("Component matrix "+k+" has "+components[k].getRowNodes().size()+" nodes");
		}

		return components;
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
	
	public void correctEigenValues(){
		for (int i=0;i<eigen_values.length;i++) {
			eigen_values[i]=Math.abs(eigen_values[i]);
		}
	}
		
	//calculate variance explained
	public static double[] computeVariance(double[] values){
		double[] explainedVariance = new double[values.length];
		double total = 0.0;
		for (int i = 0; i < values.length; i++)
			total += values[i];

		for (int i = 0, j=values.length-1; j >= 0; j--,i++) {
			explainedVariance[i] = (values[j] / total) * 100;
		}

		return explainedVariance;
	}

	private static DecimalFormat scFormat = new DecimalFormat("0.###E0");
	private static DecimalFormat format = new DecimalFormat("0.###");
	
	String printMatrix(DoubleMatrix2D mat) {
		StringBuilder sb = new StringBuilder();
		sb.append("matrix("+mat.rows()+", "+mat.columns()+")\n");
		if (mat.getClass().getName().indexOf("Sparse") >= 0)
			sb.append(" matrix is sparse\n");
		else
			sb.append(" matrix is dense\n");
		sb.append(" cardinality is "+mat.cardinality()+"\n\t");

		for (int row = 0; row < mat.rows(); row++) {
			for (int col = 0; col < mat.columns(); col++) {
				double value = mat.getQuick(row, col);
				if (value < 0.001)
					sb.append(""+scFormat.format(value)+"\t");
				else
					sb.append(""+format.format(value)+"\t");
			} 
			sb.append("\n");
		} 
		return sb.toString();
	}
}

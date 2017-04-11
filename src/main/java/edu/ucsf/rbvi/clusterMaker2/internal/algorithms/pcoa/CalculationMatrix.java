package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Arrays;
import java.text.DecimalFormat;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix  {
	double[] eigen_values;
	double[][] eigen_vectors;

	//do the eigen analysis for both eigen vectors and eigen values
	public double[] eigenAnalysis(Matrix G){
		eigen_vectors=G.ops().eigenVectors();
		eigen_values=G.ops().eigenValues(false);	
		return eigen_values;
	}
	
	//get the coordinates for PCoA
	public CyMatrix[] getCoordinates(CyMatrix matrix){
		CyMatrix[] components = new CyMatrix[eigen_values.length];

		System.out.println("Found "+eigen_values.length+" eigenvalues");
		System.out.println("Eigenvectors["+eigen_vectors.length+"]["+eigen_vectors[0].length+"]");

		for(int j=eigen_values.length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), eigen_values.length,1);
			for(int i=0;i<eigen_values.length;i++){
				System.out.println("Setting eigen_vector["+i+"]["+j+"]");
				result.setValue(i,0,eigen_vectors[i][j]);
			}

			matrix.writeMatrix("matrix");
			result.writeMatrix("result");
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
		 		if(matrix.doubleValue(i,j)!=0){
			 		uppertrimatrix[p]=matrix.doubleValue(i,j);
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

}

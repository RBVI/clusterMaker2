package test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.RunPCoA;
import junit.framework.Assert;

public class CalculationMatrixTest {

	double inputdata[][]={{0,3,4,5},{3,0,6,3},{4,6,0,1},{5,3,1,0}};
	double outputdatagower[][]={{-3,0.6667,2.3333},{0.6667,-0.1667,-0.5},{2.3333,-0.5,-1.8333}};
	double outputdataA[][]={{-0.5,-2,-4.5},{-2,-8,-12.5},{-4.5,-12.5,-18}};
	double outputdataunit[][]={{1,1,1},{0,0,0},{0,0,0}};
	
	double symetricdata[][]={{1,2,3},{2,4,5},{3,5,8}};
	RunPCoA runpcoa=new RunPCoA(4, 4, inputdata);
	
	double gowermatrix[][]=runpcoa.getCalculationMatrix().getGowernsMatrix();
	double eigen_vals[][]=runpcoa.getCalculationMatrix().eigenAnalysis();
	double outputeigenvectors[][]={{0.3280,0.5910,0.7370},{0.5910,-0.7370,0.3280},{0.7370,0.3280,-0.5910}};
	double outputeigenvalues[][]={{1,1,1},{0,0,0},{0,0,0}};
	ComputationMatrix computationmatrix=new ComputationMatrix(gowermatrix);//get eigen vectors from Gowerns matrix
	double compval[][]=computationmatrix.eigenVectors();
	double compval_2[]=computationmatrix.eigenValuesAll();
	
	
	double idxvalues[]={1,2,3};
	
	double getvariance[][]=runpcoa.getCalculationMatrix().getVarianceExplained();
	
	double negsum[][]=runpcoa.getCalculationMatrix().negativeEigenAnalysis();
	//double normmatrix[][]=runpcoa.getCalculationMatrix().convertColumntoMatrix(negsum);
	double uppermatri[]=runpcoa.getCalculationMatrix().getUpperMatrixInVector(inputdata);
	//double scaleEigenvectors[]=runpcoa.getCalculationMatrix().scaleEigenVectors();
	@Test
	public void test() {
		System.out.println("Eigen Vectors");
		for(int i=0;i<compval.length;i++){
			for(int j=0;j<compval.length;j++){
				System.out.print(compval[i][j]+" ");
			}
			System.out.println("");
		}
		
		
		System.out.println("Eigen Analysis");
		for(int j=0;j<eigen_vals.length;j++){
			//System.out.print(eigen_vals[j]+" ");
			for(int i=0;i<eigen_vals.length;i++){
				System.out.print(eigen_vals[j][i]+" ");	
			}
			System.out.println("");
		}
		
		
		System.out.println("Variance Explained");
		for(int i=0;i<getvariance.length;i++){
			for(int j=0;j<2;j++){
				System.out.print(getvariance[i][j]+" ");	
			}
			System.out.println("");	
		}
	}
}


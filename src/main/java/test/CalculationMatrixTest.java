package test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.CalculationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.PCoAContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.RunPCoA;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import junit.framework.Assert;

public class CalculationMatrixTest {

	public PCoAContext context = null;
	double inputdata[][]={{0,3,4,5},{3,0,6,3},{4,6,0,1},{5,3,1,0}};
	double single_line_data[]={2,4,57,76};
	

	//CalculationMatrix calcmatrix=new CalculationMatrix(4, 4, inputdata, 0, 0, 0);
	@Test
	public void isSymmetricaltest() {
	//	assertTrue(calcmatrix.isSymmetrical());
	}
	@Test
	public void matrixReversetest() {
		double expect[]={76,57,4,2};
		//assertArrayEquals(expect,(calcmatrix.matrixReverse(single_line_data)),0);
	}
	
	@Test
	public void matrixMultiplytest() {
		double value_1[][]={
				{12,3,1},
				{4,5,2}		};
		double value_2[][]={
				{4,2},
				{6,5},
				{1,1}		};
		double expect[][]={
				{67,40},
				{48,35}};
		//assertArrayEquals(null,expect,(calcmatrix.multiplyByMatrix(value_1,value_2)));
	}
	
	@Test
	public void transposeMatrixtest() {
		double actual[][]={
				{12,3,1},
				{4,5,2}		};
		double expect[][]={
				{12,4},
				{3,5},
				{1,2}
		};
		//assertArrayEquals(null,expect,(calcmatrix.transposeMatrix(actual)));
	}
	
	@Test
	public void gowersMatrixtest() {
		double expect[][]={
				{6.5,2.5,-1.125,-7.875},
				{2.5,7.5,-10.625,0.625},
				{-1.125,-10.625,7.25,4.5},
				{-7.875,0.625,4.5,2.75}
		};
		//assertArrayEquals(null,expect,(calcmatrix.getGowernsMatrix()));
	}
	
	
	@Test
	public void eigenAnalysistest() {
		double expect[]={
				19.942260245679883,10.940200595315455,-6.882460840995355 ,0.0
		};
		//double result[]=calcmatrix.eigenAnalysis();
		//for(int i=0;i<result.length;i++){
		//	System.out.print(result[i]+" ");
	//	}
		//assertArrayEquals(expect,(result),0);
	}
	
	@Test
	public void getVarianceExplainedtest() {
		double expect[][]={
				{83.09275102366625,83.09275102366625 },
				{45.58416914714777,128.67692017081401 },
				{-28.676920170814007,100.0 }
		};
		//double result[][]=calcmatrix.getVarianceExplained();
		System.out.println("");
		System.out.print("Variance explained");
		/*for(int i=0;i<result.length;i++){
			for(int j=0;j<2;j++){
				System.out.print(result[i][j]+" ");
			}
			System.out.println("");
		}
		assertArrayEquals(null,expect,(result));*/
	}
	
	@Test
	public void getUpperMatrixInVector() {
		double expect[]={
				3.0 ,4.0 ,5.0 ,6.0 ,3.0 ,1.0 	
		};
		//assertArrayEquals(expect,(calcmatrix.getUpperMatrixInVector(inputdata)),0);
	}
	
	@Test
	public void convertColumntoMatrixtest() {
		double input[]={
				3.0 ,4.0 ,5.0 ,6.0 ,3.0 ,1.0	
		};
		double expect[][]={{0,3,4,5},{3,0,6,3},{4,6,0,1},{5,3,1,0}};
	//	assertArrayEquals(null,expect,(calcmatrix.convertColumntoMatrix(input)));
	}
	
	@Test
	public void negativeEigenAnalysistest() {
		double expect[][]={
				{83.09275102366625,83.09275102366625}, 
				{45.58416914714777,128.67692017081401}, 
				{0.0,0.0} 		
		};
	//	assertArrayEquals(null,expect,(calcmatrix.negativeEigenAnalysis()));
	}
	
	@Test
	public void scaleEigenVectorstest() {
		double expect[][]={
				{0.39521395928926467,-0.49999999999999994,-0.34615296149811603,0.0}, 
				{-0.4748314299343729,-0.5000000000000001,-0.6066736114483725,0.0}, 
				{-0.5147954337536527,-0.4999999999999998,0.6469800144154148,0.0}, 
				{0.5944129043987613,-0.5000000000000001,0.30584655853107445,0.0} 	
		};
	//	assertArrayEquals(null,expect,(calcmatrix.scaleEigenVectors()));
	}
	@Test
	public void test() {
		eigenAnalysistest();
		getVarianceExplainedtest();
		//gowersMatrixtest();
		/*double arr[][]=calcmatrix.getVarianceExplained();
		for(int i=0;i<arr.length;i++){
			
			for(int j=0;j<2;j++){
				System.out.print(arr[i][j]+" ");
			}
			System.out.println("");
		}*/
		/*isSymmetricaltest();
		matrixReversetest();
		matrixMultiplytest();
		transposeMatrixtest();
		gowersMatrixtest();
		eigenAnalysistest();
		getVarianceExplainedtest();
		getUpperMatrixInVector();
		convertColumntoMatrixtest();*/
		//negativeEigenAnalysistest();
		///scaleEigenVectorstest();
		
	}
	
}


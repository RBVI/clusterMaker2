package test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.RunPCoA;
import junit.framework.Assert;

public class CalculationMatrixTest {

	double inputdata[][]={{1,2,3},{2,4,5},{3,5,6}};
	double outputdatagower[][]={{-3,0.6667,2.3333},{0.6667,-0.1667,-0.5},{2.3333,-0.5,-1.8333}};
	double outputdataA[][]={{-0.5,-2,-4.5},{-2,-8,-12.5},{-4.5,-12.5,-18}};
	double outputdataunit[][]={{1,1,1},{0,0,0},{0,0,0}};
	
	
	
	double symetricdata[][]={{1,2,3},{2,4,5},{3,5,8}};
	RunPCoA runpcoa=new RunPCoA(3, 3, inputdata);
	
	double gowermatrix[][]=runpcoa.getCalculationMatrix().getGowernsMatrix();
	
	double outputeigenvectors[][]={{0.3280,0.5910,0.7370},{0.5910,-0.7370,0.3280},{0.7370,0.3280,-0.5910}};
	double outputeigenvalues[][]={{1,1,1},{0,0,0},{0,0,0}};
	ComputationMatrix computationmatrix=new ComputationMatrix(inputdata);
	double compval[][]=computationmatrix.eigenVectors();
	
	double idxvalues[]={1,2,3};
	@Test
	public void test() {
		
		//Assert.assertEquals(runpcoa.getCalculationMatrix().isSymmetrical(),true );//symmetric testing
		//assertArrayEquals(inputdata, outputdata);//transpose testing
		//assertArrayEquals(outputdatagower, gowermatrix);//goerns matrix testing
		assertArrayEquals(compval, outputeigenvectors);//eigenvectors testing
		
	}

}


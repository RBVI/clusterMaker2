package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tsne;

import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tsne.CalculationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrixTest {
Matrix result=new ColtMatrix(10,10);
Matrix matrixdata=new ColtMatrix(10,10);
Matrix tempmatrix;
double temparray[][];
CalculationMatrix calcmatrix;
MatrixTest matrixTest;
int row=4,col=4;
double data[][]=new double[row][col];
public void setInitiate(){
	double min = 100;
	double max = 103;
	double randomValue;
	Random r=new Random();
	for(int i=0;i<data.length;i++){
		for(int j=0;j<data[0].length;j++){
			randomValue= min + (max - min) * r.nextDouble();
			//System.out.print(randomValue+" ");
			data[i][j]=randomValue;
			}
	//	System.out.println("");
		}
matrixdata.initialize(row, col, data);	
calcmatrix=new CalculationMatrix();
matrixTest=new MatrixTest();
}

@Test
public void transposeTest() {
	setInitiate();
	tempmatrix=calcmatrix.transpose(matrixdata);
	
	temparray=matrixTest.transpose(data);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void expTest() {
	setInitiate();
	tempmatrix=calcmatrix.exp(matrixdata);
	
	temparray=matrixTest.exp(data);
	double arratmp[][]=tempmatrix.toArray();
	
	/*System.out.println("Normal array");
	for(int i=0;i<arratmp.length;i++){
		for(int j=0;j<arratmp[0].length;j++){
			System.out.print(arratmp[i][j]+" ");
			}
		System.out.println("");
		}
	
	System.out.println("Convereted array");
	for(int i=0;i<temparray.length;i++){
		for(int j=0;j<temparray[0].length;j++){
			System.out.print(temparray[i][j]+" ");
			}
		System.out.println("");
		}*/
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void logTest() {
	setInitiate();
	tempmatrix=calcmatrix.log(matrixdata);
	
	temparray=matrixTest.log(data);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarInverseTest() {
	setInitiate();
	tempmatrix=calcmatrix.scalarInverse(matrixdata);
	
	temparray=matrixTest.scalarInverse(data);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}
@Test
public void test() {
	setInitiate();
	assertArrayEquals(null, data, matrixdata.toArray());
	transposeTest();
	expTest();
	logTest();
	scalarInverseTest();
}

}

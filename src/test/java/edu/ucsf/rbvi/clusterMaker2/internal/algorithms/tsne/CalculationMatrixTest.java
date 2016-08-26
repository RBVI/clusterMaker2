package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tsne;

import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netlib.util.booleanW;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrixTest {
Matrix result=new ColtMatrix(10,10);
Matrix matrixdata=new ColtMatrix(10,10);
Matrix coldata=new ColtMatrix(10,10);
Matrix tempmatrix;
double temparray[][];
CalculationMatrix calcmatrix;
MatrixTest matrixTest;
int row=10,col=10;
int axis=1;
int rowtimes=9,coltimes=10;
double maxval=101.2345454,replace=105.35783;
double data[][]=new double[row][col];
double data_col[][]=new double[row][col];
int indices[]={2,1,0};
double fillvalue=12.345;
boolean boolarr[][]={{true,false,false,true},{true,true,false,false},{false,true,true,false},{false,false,false,true}};
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

public void setInitiateSecond(){
	double min = 50;
	double max = 103;
	double randomValue;
	Random r=new Random();
	for(int i=0;i<data_col.length;i++){
		for(int j=0;j<data_col[0].length;j++){
			randomValue= min + (max - min) * r.nextDouble();
			//System.out.print(randomValue+" ");
			data_col[i][j]=randomValue;
			}
	//	System.out.println("");
		}
	coldata.initialize(row, col, data_col);	

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
public void absTest() {
	setInitiate();
	tempmatrix=calcmatrix.abs(boolarr);
	
	temparray=matrixTest.abs(boolarr);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void meanTest() {
	setInitiate();
	tempmatrix=calcmatrix.mean(matrixdata,axis);
	
	temparray=matrixTest.mean(data,axis);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void sumTest() {
	setInitiate();
	tempmatrix=calcmatrix.sum(matrixdata,axis);
	
	temparray=matrixTest.sum(data,axis);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void sumdoubleTest() {
	setInitiate();
	double valmat=calcmatrix.sum(matrixdata);
	
	double valarr=matrixTest.sum(data);
	//double arratmp[][]=tempmatrix.toArray();
	
	boolean cond=valarr==valmat;
	assertTrue(cond);
}

@Test
public void maximumTest() {
	setInitiate();
	tempmatrix=calcmatrix.maximum(matrixdata,maxval);
	
	temparray=matrixTest.maximum(data,maxval);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void squareTest() {
	setInitiate();
	tempmatrix=calcmatrix.square(matrixdata);
	
	temparray=matrixTest.square(data);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void replaceNaNTest() {
	setInitiate();
	tempmatrix=calcmatrix.replaceNaN(matrixdata,replace);
	
	temparray=matrixTest.replaceNaN(data,replace);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarPowTest() {
	setInitiate();
	tempmatrix=calcmatrix.scalarPow(matrixdata,replace);
	
	temparray=matrixTest.scalarPow(data,replace);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void addColumnVectorTest() {
	setInitiate();
	setInitiateSecond();
	tempmatrix=calcmatrix.addColumnVector(matrixdata,coldata);
	
	temparray=matrixTest.addColumnVector(data,data_col);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void addRowVectorTest() {
	setInitiate();
	setInitiateSecond();
	tempmatrix=calcmatrix.addRowVector(matrixdata,coldata);
	
	temparray=matrixTest.addRowVector(data,data_col);
	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void tileTest() {
	setInitiate();
	tempmatrix=calcmatrix.tile(matrixdata,rowtimes,coltimes);
	
	temparray=matrixTest.tile(data,rowtimes,coltimes);
	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarMultiplyTest() {
	setInitiate();
	setInitiateSecond();
	tempmatrix=calcmatrix.scalarMultiply(matrixdata,coldata);
	
	temparray=matrixTest.scalarMultiply(data,data_col);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void fillMatrixTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.fillMatrix(row, col, fillvalue);
	
	temparray=matrixTest.fillMatrix(row,col,fillvalue);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}


@Test
public void plusTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.plus(matrixdata,coldata);
	
	temparray=matrixTest.plus(data,data_col);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarplusTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.scalarPlus(matrixdata,fillvalue);
	
	temparray=matrixTest.scalarPlus(data,fillvalue);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void minusTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.minus(matrixdata,coldata);
	
	temparray=matrixTest.minus(data,data_col);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarDivideTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.scalarDivide(matrixdata, fillvalue);
	
	temparray=matrixTest.scalarDivide(data,fillvalue);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarDivideMatTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.scalarDivide(matrixdata, coldata);
	
	temparray=matrixTest.scalarDivide(data,data_col);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void scalarMultTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.scalarMult(matrixdata, fillvalue);
	
	temparray=matrixTest.scalarMult(data,fillvalue);

	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void timesTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.times(matrixdata, coldata);
	
	temparray=matrixTest.times(data,data_col);

	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}

@Test
public void diagTest() {
	setInitiate();
	//setInitiateSecond();
	tempmatrix=calcmatrix.diag(matrixdata);
	
	temparray=matrixTest.diag(data);

	double arratmp[][]=tempmatrix.toArray();
	
	
	assertArrayEquals(null, temparray, arratmp);
}
/*@Test
public void getValuesFromRowTest() {
	setInitiate();
	tempmatrix=calcmatrix.getValuesFromRow(matrixdata, row, indices);
	
	temparray=matrixTest.getValuesFromRow(data,row,indices);

	double arratmp[][]=tempmatrix.toArray();
	
	assertArrayEquals(null, temparray, arratmp);
}*/


/*@Test
public void rnormTest() {
	setInitiate();
	tempmatrix=calcmatrix.rnorm(row,col);
	
	temparray=matrixTest.rnorm(row,col);
	double arratmp[][]=tempmatrix.toArray();
	System.out.println("Normal array");
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
		}
	
	assertArrayEquals(null, temparray, arratmp);
}*/
@Test
public void test() {
	setInitiate();
	assertArrayEquals(null, data, matrixdata.toArray());
	transposeTest();
	expTest();
	logTest();
	scalarInverseTest();
	absTest();
	meanTest();
	sumTest();
	sumdoubleTest();
	maximumTest();
	squareTest();
	replaceNaNTest();
	scalarPowTest();
	addColumnVectorTest();
	addRowVectorTest();
	tileTest();
	scalarMultiplyTest();
	fillMatrixTest();
	minusTest();
	scalarplusTest();
	minusTest();
	scalarDivideTest();
	scalarDivideMatTest();
	scalarMultTest();
	timesTest();
	diagTest();
	//getValuesFromRowTest();
	//rnormTest();
}

}

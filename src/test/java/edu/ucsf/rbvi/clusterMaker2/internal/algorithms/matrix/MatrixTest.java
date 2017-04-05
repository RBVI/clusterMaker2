package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class MatrixTest {

	double DELTA = 0.000001;
	int rows = 1000;
	int columns = 1000;
	Matrix coltMatrix;
	Matrix coltMatrix2;
	Matrix simpleMatrix;
	Matrix simpleMatrix2;
	Matrix ojAlgoMatrix;
	Matrix ojAlgoMatrix2;
	long time;

	@Test
	public void scalarMultTest() {
		initialize();
		double scalarValue = 1.6;
		// First, simpleMatrix
		timeStart();
		simpleMatrix.ops().multiplyScalar(scalarValue);
		timeEnd("multiply simple matrix by scalar");

		timeStart();
		coltMatrix.ops().multiplyScalar(scalarValue);
		timeEnd("multiply colt matrix by scalar");

		assertArrayEquals(simpleMatrix.toArray(), coltMatrix.toArray(), DELTA);

		timeStart();
		ojAlgoMatrix.ops().multiplyScalar(scalarValue);
		timeEnd("multiply ojAlgo matrix by scalar");
		assertArrayEquals(simpleMatrix.toArray(), ojAlgoMatrix.toArray(), DELTA);
	}

	@Test
	public void timesTest() {
		initialize();
		// First, simpleMatrix
		timeStart();
		Matrix resultSimple = simpleMatrix.ops().multiplyMatrix(simpleMatrix2);
		timeEnd("multiply simple matrix");
		System.out.println("resultSimple: "+resultSimple.printMatrixInfo());

		timeStart();
		Matrix resultColt = coltMatrix.ops().multiplyMatrix(coltMatrix2);
		timeEnd("multiply colt matrix");
		System.out.println("resultColt: "+resultColt.printMatrixInfo());

		assertArrayEquals(resultSimple.toArray(), resultColt.toArray(), DELTA);

		timeStart();
		Matrix resultAlgo = ojAlgoMatrix.ops().multiplyMatrix(ojAlgoMatrix2);
		timeEnd("multiply ojAlgo matrix");
		System.out.println("resultOjAlgo: "+ resultAlgo.printMatrixInfo());
		assertArrayEquals(resultSimple.toArray(), resultAlgo.toArray(), DELTA);

	}

	@Test
	public void centralizeColumnsTest() {
		initialize();

		timeStart();
		simpleMatrix.ops().centralizeColumns();
		timeEnd("centralizeColumns simple matrix");

		timeStart();
		coltMatrix.ops().centralizeColumns();
		timeEnd("centralizeColumns colt matrix");

		assertArrayEquals(simpleMatrix.toArray(), coltMatrix.toArray(), DELTA);

		timeStart();
		ojAlgoMatrix.ops().centralizeColumns();
		timeEnd("centralizeColumns ojAlgo matrix");

		assertArrayEquals(simpleMatrix.toArray(), ojAlgoMatrix.toArray(), DELTA);
	}

	@Test
	public void normTest() {
		initialize();

		timeStart();
		simpleMatrix.ops().normalizeMatrix();
		timeEnd("normalize simple matrix");

		timeStart();
		System.out.println("Sum by rows: "+sumByRows(simpleMatrix));
		timeEnd("Sum by rows");
		timeStart();
		System.out.println("Sum by columns: "+sumByColumns(simpleMatrix));
		timeEnd("Sum by columns");

		timeStart();
		coltMatrix.ops().normalizeMatrix();
		timeEnd("normalize colt matrix");

		timeStart();
		System.out.println("Sum by rows: "+sumByRows(coltMatrix));
		timeEnd("Sum by rows");
		timeStart();
		System.out.println("Sum by columns: "+sumByColumns(coltMatrix));
		timeEnd("Sum by columns");

		timeStart();
		ojAlgoMatrix.ops().normalizeMatrix();
		timeEnd("normalize ojAlgo matrix");

		timeStart();
		System.out.println("Sum by rows: "+sumByRows(ojAlgoMatrix));
		timeEnd("Sum by rows");
		timeStart();
		System.out.println("Sum by columns: "+sumByColumns(ojAlgoMatrix));
		timeEnd("Sum by columns");
	}

	@Test
	public void covTest() {
		initialize();

		timeStart();
		Matrix simpleCov = simpleMatrix.ops().covariance();
		timeEnd("simple matrix covariance");

		timeStart();
		Matrix coltCov = coltMatrix.ops().covariance();
		timeEnd("colt matrix covariance");

		assertArrayEquals(simpleCov.toArray(), coltCov.toArray(), DELTA);

		timeStart();
		Matrix ojAlgoCov = ojAlgoMatrix.ops().covariance();
		timeEnd("ojAlgo matrix covariance");

		assertArrayEquals(simpleCov.toArray(), ojAlgoCov.toArray(), DELTA);
	}

	@Test
	public void corrTest() {
		initialize();

		timeStart();
		Matrix simpleCorr = simpleMatrix.ops().correlation();
		timeEnd("simple matrix correlation");

		timeStart();
		Matrix coltCorr = coltMatrix.ops().correlation();
		timeEnd("colt matrix correlation");

		assertArrayEquals(simpleCorr.toArray(), coltCorr.toArray(), DELTA);

		timeStart();
		Matrix ojAlgoCorr = ojAlgoMatrix.ops().correlation();
		timeEnd("ojAlgo matrix correlation");

		assertArrayEquals(simpleCorr.toArray(), ojAlgoCorr.toArray(), DELTA);
	}

	@Test
	public void eigenTest() {
		initialize();

		timeStart();
		double[][] simpleVectors = simpleMatrix.ops().eigenVectors();
		double[]  simpleValues = simpleMatrix.ops().eigenValues(false);
		timeEnd("simple matrix eigen");

		timeStart();
		double[][] coltVectors = coltMatrix.ops().eigenVectors();
		double[]  coltValues = coltMatrix.ops().eigenValues(false);
		timeEnd("colt matrix eigen");

		assertArrayEquals(simpleVectors, coltVectors, DELTA);

		timeStart();
		double[][] ojAlgoVectors = ojAlgoMatrix.ops().eigenVectors();
		double[]  ojAlgoValues = ojAlgoMatrix.ops().eigenValues(false);
		timeEnd("ojAlgo matrix eigen");

		assertArrayEquals(simpleVectors, ojAlgoVectors, DELTA);
	}

	public void	assertArrayEquals(double[][] temparray, double[][] arratmp, double DELTA) {
		for(int row=0;row<arratmp.length;row++){
			for(int col=0;col<arratmp[0].length;col++){
				assertEquals(temparray[row][col],arratmp[row][col], DELTA);
			}
		}
	}

	private void initialize() {
		// Arbirary bounding
		double min = 90;
		double max = 103;

		simpleMatrix = new SimpleMatrix(rows, columns);
		double randomValue;
		Random r=new Random();
		for(int row=0;row<rows;row++){
			for(int col=0;col<columns;col++){
				randomValue= min + (max - min) * r.nextDouble();
				simpleMatrix.setValue(row, col, randomValue);
			}
		}
		coltMatrix = new ColtMatrix((SimpleMatrix)simpleMatrix);
		ojAlgoMatrix = new OjAlgoMatrix((SimpleMatrix)simpleMatrix);

		simpleMatrix2 = new SimpleMatrix(rows, columns);
		for(int row=0;row<rows;row++){
			for(int col=0;col<columns;col++){
				randomValue= min + (max - min) * r.nextDouble();
				simpleMatrix2.setValue(row, col, randomValue);
			}
		}
		coltMatrix2 = new ColtMatrix((SimpleMatrix)simpleMatrix2);
		ojAlgoMatrix2 = new OjAlgoMatrix((SimpleMatrix)simpleMatrix2);
	}

	private double sumByRows(Matrix mat) {
		double sum = 0.0;
		for (int row = 0; row < mat.nRows(); row++)
			sum += mat.ops().rowSum(row);
		return sum;
	}

	private double sumByColumns(Matrix mat) {
		double sum = 0.0;
		for (int col = 0; col < mat.nColumns(); col++)
			sum += mat.ops().columnSum(col);
		return sum;
	}

	private void timeStart() {
		time = System.currentTimeMillis();
	}

	private void timeEnd(String test) {
		long dur = System.currentTimeMillis() - time;
		System.out.println("Time for "+test+": "+((double)dur)/1000+"s");
	}

}

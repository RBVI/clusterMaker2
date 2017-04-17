package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;
/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 * 
 * Adapted by 2014, Leif Jonsson, added pca method.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

/**
 * <p>
 * The following is a simple example of how to perform basic principal component analysis in EJML.
 * </p>
 *
 * <p>
 * Principal Component Analysis (PCA) is typically used to develop a linear model for a set of data
 * (e.g. face images) which can then be used to test for membership.  PCA works by converting the
 * set of data to a new basis that is a subspace of the original set.  The subspace is selected
 * to maximize information.
 * </p>
 * <p>
 * PCA is typically derived as an eigenvalue problem.  However in this implementation {@link org.ejml.interfaces.decomposition.SingularValueDecomposition SVD}
 * is used instead because it will produce a more numerically stable solution.  Computation using EVD requires explicitly
 * computing the variance of each sample set. The variance is computed by squaring the residual, which can
 * cause loss of precision.
 * </p>
 *
 * <p>
 * Usage:<br>
 * 1) call setup()<br>
 * 2) For each sample (e.g. an image ) call addSample()<br>
 * 3) After all the samples have been added call computeBasis()<br>
 * 4) Call  sampleToEigenSpace() , eigenToSampleSpace() , errorMembership() , response()
 * </p>
 *
 * @author Peter Abeles
 */
public class PrincipalComponentAnalysis {

    // principal component subspace is stored in the rows
    private Matrix V_t;

    // how many principal components are used
    private int numComponents;

    // where the data is stored
    private CyMatrix A = null;
    private int sampleIndex;

    // mean values of each element across all the samples
    Matrix mean;

    public PrincipalComponentAnalysis() {
    }

    /**
     * Must be called before any other functions. Declares and sets up internal data structures.
     *
     * @param numSamples Number of samples that will be processed.
     * @param sampleSize Number of elements in each sample.
     */
    public void setup( CyMatrix data ) {
        mean = data.like(data.nColumns(), 1);
				A = data.copy();
        sampleIndex = 0;
        numComponents = -1;
    }

    /**
     * Computes a basis (the principal components) from the most dominant eigenvectors.
     *
     * @param numComponents Number of vectors it will use to describe the data.  Typically much
     * smaller than the number of elements in the input vector.
     */
    public void computeBasis( int numComponents ) {
        if( numComponents > A.nColumns() )
            throw new IllegalArgumentException("More components requested that the data's length.");

        this.numComponents = numComponents;

        // compute the mean of all the samples
				for (int j = 0; j < mean.nRows(); j++) {
					mean.setValue(j, 0, CommonOps.columnMean(A, j));
				}

        // subtract the mean from the original data
        for( int i = 0; i < A.nRows(); i++ ) {
            for( int j = 0; j < A.nColumns(); j++ ) {
                A.setValue(i,j,A.doubleValue(i,j)-mean.doubleValue(j,0));
            }
        }

        // Compute SVD
        V_t = CommonOps.transpose(A.ops().svdV());
        Matrix W = A.ops().svdS();

        // Singular values are in an arbitrary order initially
        // SingularOps.descendingOrder(null,false,W,V_t,true);

        // strip off unneeded components and find the basis
        // V_t.reshape(numComponents,mean.length,true);
				V_t = reshape(V_t, numComponents, mean.nRows());
    }

    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    public double[] sampleToEigenSpace( Matrix sampleData, int row ) {
        if( sampleData.nColumns() != A.nColumns() )
            throw new IllegalArgumentException("Unexpected sample length");

        Matrix s = A.like(A.nColumns(), 1);
				for (int col = 0; col < A.nColumns(); col++)
					s.setValue(col, 0, sampleData.doubleValue(row, col));

        CommonOps.subtractElement(s, mean);
				// s.writeMatrix("s-"+row);

        Matrix r = CommonOps.multiplyMatrix(V_t.copy(),s);
				// V_t.writeMatrix("V_t"+row);
				// r.writeMatrix("r-"+row);

        return r.getColumn(0);
    }

	public CyMatrix pca(CyMatrix matrix, int no_dims) {
		double [][] trafoed = new double[matrix.nRows()][no_dims];
		setup(matrix);
		computeBasis(no_dims);
		CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), matrix.nRows(), no_dims);
		result.setRowNodes(matrix.getRowNodes());
		result.setRowLabels(Arrays.asList(matrix.getRowLabels()));
		for (int i = 0; i < matrix.nRows(); i++) {
			trafoed[i] = sampleToEigenSpace(matrix, i);
			for (int j = 0; j < trafoed[i].length; j++) {
				result.setValue(i, j, trafoed[i][j] *= -1);
				// trafoed[i][j] *= -1;
			}
		}
		// result.writeMatrix("pca");
		return result;
	}

	private Matrix reshape(Matrix src, int numRows, int numCols) {
		Matrix result  = src.like(numRows, numCols);
		long index = 0;
		long size = src.nRows()*src.nColumns();
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				double value = 0.0;
				if (index < size) {
					int srcRow = (int)(index/src.nColumns());
					int srcCol = (int)(index%src.nColumns());
					value = src.doubleValue(srcRow, srcCol);
					index++;
				}
				result.setValue(row, col, value);
			}
		}

		return result;
	}
}

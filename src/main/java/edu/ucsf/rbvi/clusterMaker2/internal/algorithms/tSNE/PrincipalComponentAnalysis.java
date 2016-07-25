package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.SingularOps;


public class PrincipalComponentAnalysis {

    // principal component subspace is stored in the rows
    private DenseMatrix64F V_t;

    // how many principal components are used
    private int numComponents;

    // where the data is stored
    private DenseMatrix64F A = new DenseMatrix64F(1,1);
    private int sampleIndex;

    // mean values of each element across all the samples
    double mean[];

    public PrincipalComponentAnalysis() {
    }

    
    public void setup( int numSamples , int sampleSize ) {
        mean = new double[ sampleSize ];
        A.reshape(numSamples,sampleSize,false);
        sampleIndex = 0;
        numComponents = -1;
    }

    public void addSample( double[] sampleData ) {
        if( A.getNumCols() != sampleData.length )
            throw new IllegalArgumentException("Unexpected sample size");
        if( sampleIndex >= A.getNumRows() )
            throw new IllegalArgumentException("Too many samples");

        for( int i = 0; i < sampleData.length; i++ ) {
            A.set(sampleIndex,i,sampleData[i]);
        }
        sampleIndex++;
    }

    
    public void computeBasis( int numComponents ) {
        if( numComponents > A.getNumCols() )
            throw new IllegalArgumentException("More components requested that the data's length.");
        if( sampleIndex != A.getNumRows() )
            throw new IllegalArgumentException("Not all the data has been added");
        if( numComponents > sampleIndex )
            throw new IllegalArgumentException("More data needed to compute the desired number of components");

        this.numComponents = numComponents;

        // compute the mean of all the samples
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                mean[j] += A.get(i,j);
            }
        }
        for( int j = 0; j < mean.length; j++ ) {
            mean[j] /= A.getNumRows();
        }

        // subtract the mean from the original data
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                A.set(i,j,A.get(i,j)-mean[j]);
            }
        }

        // Compute SVD and save time by not computing U
        SingularValueDecomposition<DenseMatrix64F> svd =
                DecompositionFactory.svd(A.numRows, A.numCols, false, true, false);
        if( !svd.decompose(A) )
            throw new RuntimeException("SVD failed");

        V_t = svd.getV(null,true);
        DenseMatrix64F W = svd.getW(null);

        // Singular values are in an arbitrary order initially
        SingularOps.descendingOrder(null,false,W,V_t,true);

        // strip off unneeded components and find the basis
        V_t.reshape(numComponents,mean.length,true);
    }

   
    public double[] getBasisVector( int which ) {
        if( which < 0 || which >= numComponents )
            throw new IllegalArgumentException("Invalid component");

        DenseMatrix64F v = new DenseMatrix64F(1,A.numCols);
        CommonOps.extract(V_t,which,which+1,0,A.numCols,v,0,0);

        return v.data;
    }

    
    public double[] sampleToEigenSpace( double[] sampleData ) {
        if( sampleData.length != A.getNumCols() )
            throw new IllegalArgumentException("Unexpected sample length");
        DenseMatrix64F mean = DenseMatrix64F.wrap(A.getNumCols(),1,this.mean);

        DenseMatrix64F s = new DenseMatrix64F(A.getNumCols(),1,true,sampleData);
        DenseMatrix64F r = new DenseMatrix64F(numComponents,1);

        CommonOps.subtract(s, mean, s);

        CommonOps.mult(V_t,s,r);

        return r.data;
    }

    
    public double[] eigenToSampleSpace( double[] eigenData ) {
        if( eigenData.length != numComponents )
            throw new IllegalArgumentException("Unexpected sample length");

        DenseMatrix64F s = new DenseMatrix64F(A.getNumCols(),1);
        DenseMatrix64F r = DenseMatrix64F.wrap(numComponents,1,eigenData);
        
        CommonOps.multTransA(V_t,r,s);

        DenseMatrix64F mean = DenseMatrix64F.wrap(A.getNumCols(),1,this.mean);
        CommonOps.add(s,mean,s);

        return s.data;
    }


   
    public double errorMembership( double[] sampleA ) {
        double[] eig = sampleToEigenSpace(sampleA);
        double[] reproj = eigenToSampleSpace(eig);


        double total = 0;
        for( int i = 0; i < reproj.length; i++ ) {
            double d = sampleA[i] - reproj[i];
            total += d*d;
        }

        return Math.sqrt(total);
    }

    
    public double response( double[] sample ) {
        if( sample.length != A.numCols )
            throw new IllegalArgumentException("Expected input vector to be in sample space");

        DenseMatrix64F dots = new DenseMatrix64F(numComponents,1);
        DenseMatrix64F s = DenseMatrix64F.wrap(A.numCols,1,sample);

        CommonOps.mult(V_t,s,dots);

        return NormOps.normF(dots);
    }
    
    public double [][] pca(double [][]matrix, int no_dims) {
		double [][] trafoed = new double[matrix.length][matrix[0].length];
		setup(matrix.length, matrix[0].length);
		for (int i = 0; i < matrix.length; i++) {
			addSample(matrix[i]);
		}
		computeBasis(no_dims);
		for (int i = 0; i < matrix.length; i++) {
			trafoed[i] = sampleToEigenSpace(matrix[i]);
			for (int j = 0; j < trafoed[i].length; j++) {
				trafoed[i][j] *= -1;
			}
		}
		return trafoed;
    }
}

/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


// clusterMaker imports
import cern.colt.matrix.tdouble.algo.DoubleStatistic;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import cern.colt.list.tint.IntArrayList;
import cern.colt.list.tdouble.DoubleArrayList;


public class KCluster {




	// Random seeds for uniform()
	static int seed1 = 0;
	static int seed2 = 0;
	static boolean debug = false;


       

        //perform Kmeans clustering on columns of matrix, the size of whose row is K
	public static int kmeans(int nClusters, int nIterations, DoubleMatrix2D matrix, 
	                          int[] clusterID) {

		int nelements = matrix.rows();

		// System.out.println("NClusters " + nClusters + " NElements " + nelements);

		int ifound = 1;

		int[] tclusterid = new int[nelements];

		int[] saved = new int[nelements];

		int[] mapping = new int[nClusters];
		int[] counts = new int[nClusters];

		double error = Double.MAX_VALUE;

		// This matrix will store the KxK centroid data.matrix
		DoubleMatrix2D cData = DoubleFactory2D.sparse.make(nClusters, nClusters);

		// Outer initialization
		if (nIterations <= 1) {
			for (int i=0; i < clusterID.length; i++) {
				tclusterid[i] = clusterID[i];
			}
			nIterations = 1;
		} else {
			for (int i = 0; i < nelements; i++) 
				clusterID[i] = 0;
		}

		int iteration = 0;
		boolean firstIteration = true;

		

		do {
			double total = Double.MAX_VALUE;
			int counter = 0;
			int period = 10;
		       
		       

			// Randomly assign elements to clusters
			if (nIterations != 0) randomAssign(nClusters, nelements, tclusterid);
		      
			//boolean firstIteration = true;

			// Initialize
			for (int i = 0; i < nClusters; i++) counts[i] = 0;
			for (int i = 0; i < nelements; i++) counts[tclusterid[i]]++;

			while (true) {

			    

				double previous = total;
				total = 0.0;
				if (counter % period == 0) // Save the current cluster assignments
				{
					for (int i = 0; i < nelements; i++)
						saved[i] = tclusterid[i];
					if (period < Integer.MAX_VALUE / 2) 
						period *= 2;
				}
				counter++;

				//assign centroids as to maximize orthogonality on first iteration of kmeans
				if(firstIteration){

				    selectCentroidsOrthogonally(nClusters, matrix, cData);
				    firstIteration = false;
				}

				// Find the centeroids based on cluster means on all other iterations
				else
				    getClusterMeans(nClusters, matrix, cData, tclusterid);

			       

				for (int i = 0; i < nelements; i++) {
					// Calculate the distances
					double distance;
					int k = tclusterid[i];
					if (counts[k]==1) continue;

					// Get the distance
					
					distance = getDistance(i,k,matrix,cData);

				    
 
					for (int j = 0; j < nClusters; j++) { 
						double tdistance;
						if (j==k) continue;
						
						tdistance = getDistance(i,j,matrix,cData);
						if (tdistance < distance) 
						{ 
							distance = tdistance;
							
	  
							counts[tclusterid[i]]--;
							tclusterid[i] = j;
							counts[j]++;
						}
					}
					total += distance;
				}
			     
				if (total>=previous) break;
				/* total>=previous is FALSE on some machines even if total and previous
				 * are bitwise identical. */
				int i;
				for (i = 0; i < nelements; i++)
				    if (saved[i]!=tclusterid[i]) break;
				if (i==nelements)
				    break; /* Identical solution found; break out of this loop */
			}

			if (nIterations<=1)
			{ error = total;
				break;
			}

			for (int i = 0; i < nClusters; i++) mapping[i] = -1;

			int element = 0;
			for (element = 0; element < nelements; element++)
			{ 
				int j = tclusterid[element];
				int k = clusterID[element];
				if (mapping[k] == -1) 
					mapping[k] = j;
				else if (mapping[k] != j)
      	{ 
					if (total < error)
					    { 
						ifound = 1;
						error = total;
					       
						for (int i = 0; i < nelements; i++) clusterID[i] = tclusterid[i];
					    }
					break;
      	}
			}

			if (element==nelements) ifound++; /* break statement not encountered */
		} while (++iteration < nIterations);

	       
		return ifound;
	}


            
        //Assign centroids to rows at random
        private static void selectCentroidsRandom(int nClusters, DoubleMatrix2D data, DoubleMatrix2D cdata){

	    System.out.println("Assigning centroids randomly");

            //number of centroids allready set	    
	    int centroid_count = 0;

	    //centroid assigned element == 1 if centroid assigned, zero otherwise
	    int[] centroid_assigned = new int[data.rows()];
	    for(int i = 0; i < nClusters; i++){centroid_assigned[i] = 0;}

	    //randomly select first centroid
	    Random generator = new Random();
	    int centroid_id = generator.nextInt(data.rows());

	    for(int i = 0; i < nClusters; i++){

		System.out.println("Centroid selection iteration " + i + " Centroid id " + centroid_id);

		//update centroid assinged array 
		centroid_assigned[centroid_id] = 1;

		DoubleMatrix1D centroid = data.viewRow(centroid_id);

		//copy newly selected centroid from data matrix
		for(int j = 0; j < centroid.size(); j++)
		    cdata.set(centroid_count,j,centroid.get(j));

		centroid_count++;

		if(centroid_count == nClusters)
		    break;

		while(centroid_assigned[centroid_id] == 1){
		    centroid_id = generator.nextInt(data.rows());
		}

	    }
	}
        
        //Assign centroids to rows with maximal orthogonality
        private static void selectCentroidsOrthogonally(int nClusters, DoubleMatrix2D data, DoubleMatrix2D cdata){

	    System.out.println("Assigning centroids orthogonolly");

            //number of centroids allready set	    
	    int centroid_count = 0;

	    //centroid assigned element == 1 if centroid assigned, zero otherwise
	    int[] centroid_assigned = new int[data.rows()];
	    for(int i = 0; i < nClusters; i++){centroid_assigned[i] = 0;}

	    //array of cosine sums to centroids allreay chosen
	    double[] cosines = new double[data.rows()];
	    for(int i = 0; i < data.rows(); i++){cosines[i] = 0;}

	    //randomly select first centroid
	    Random generator = new Random();
	    int centroid_id = generator.nextInt(data.rows());

	    for(int i = 0; i < nClusters; i++){

		System.out.println("Centroid selection iteration " + i + " Centroid id " + centroid_id);

		//update centroid assinged array 
		centroid_assigned[centroid_id] = 1;

		DoubleMatrix1D centroid = data.viewRow(centroid_id);

		//copy newly selected centroid from data matrix
		for(int j = 0; j < centroid.size(); j++)
		    cdata.set(centroid_count,j,centroid.get(j));

		centroid_count++;

		if(centroid_count == nClusters)
		    break;

		double min_cosine = 10000;
		int new_centroid_id = -1;

		//loop through rows of data matrix, seach for next centroid, which will minimize the cosine angle (dot product) with all current centroids
		for(int j = 0; j < data.rows(); j++){

		    //ignore centroids that allready have been set
		    if(centroid_assigned[j] == 1)
			continue;

		    cosines[j] += centroid.zDotProduct(data.viewRow(j));

		    //if new cosine sum value < min cosine value, update min_cosine and new_centroid_id to reflect that
		    if(min_cosine > cosines[j]){
			
			min_cosine = cosines[j];
			new_centroid_id = j;
		    }
		}
		

		centroid_id = new_centroid_id;

	    }
	}
					

    

	private static void getClusterMeans(int nClusters, DoubleMatrix2D data, DoubleMatrix2D cdata, int[] clusterid) {

		double[][]cmask = new double[nClusters][cdata.rows()];

		for (int i = 0; i < nClusters; i++) {
			for (int j = 0; j < cdata.rows(); j++) {
			        cdata.set(j, i, 0);
				cmask[i][j] = 0.0;
			}
		}

		for (int k = 0; k < data.rows(); k++) {

		        //centroid id
			int i = clusterid[k];

		       
			

			for (int j = 0; j < data.columns(); j++) {


				if (data.get(k,j) != 0) {
					double cValue = 0.0;
					double dataValue = data.get(k,j);
					if (cdata.get(i,j) != 0) {
						cValue = cdata.get(i,j);
					}
					cdata.set(i,j, cValue+dataValue);
					cmask[i][j] = cmask[i][j] + 1.0;
				}
			}
		}
		for (int i = 0; i < nClusters; i++) {
			for (int j = 0; j < cdata.rows(); j++) {
				if (cmask[i][j] > 0.0) {
				    double cData = cdata.get(i,j) / cmask[i][j];
					cdata.set(i,j,cData);
				}
			}
		}
	}


    //get Euclidian Distance between two rows of matrix. Use colt Euclidian Distance function
    private static double getDistance(int row1_id, int row2_id, DoubleMatrix2D matrix, DoubleMatrix2D cdata){

       
	DoubleMatrix1D row1 = matrix.viewRow(row1_id);
	DoubleMatrix1D row2 = cdata.viewRow(row2_id);

	return DoubleStatistic.EUCLID.apply(row1,row2);
    }


	private static void randomAssign (int nClusters, int nElements, int[] clusterID) {
		int n = nElements - nClusters;
		int k = 0;
		int i = 0;
		for (i = 0; i < nClusters-1; i++) {
			double p = 1.0/(nClusters-1);
			int j = binomial(n, p);
			n -= j;
			j += k+1; // Assign at least one element to cluster i
			for (;k<j; k++) clusterID[k] = i;
		}
		// Assign the remaining elements to the last cluster
		for (; k < nElements; k++) clusterID[k] = i;

		// Create a random permutation of the cluster assignments
		for (i = 0; i < nElements; i++) {
			int j = (int) (i + (nElements-i)*uniform());
			k = clusterID[j];
			clusterID[j] = clusterID[i];
			clusterID[i] = k;
		}
	}

	// Debug version of "randomAssign" that isn't random
	private static void debugAssign (int nClusters, int nElements, int[] clusterID) {
		for (int element = 0; element < nElements; element++) {
			clusterID[element] = element%nClusters;
		}
	}

	/**
	 * This routine generates a random number between 0 and n inclusive, following
	 * the binomial distribution with probability p and n trials. The routine is
	 * based on the BTPE algorithm, described in:
	 * 
	 * Voratas Kachitvichyanukul and Bruce W. Schmeiser:
	 * Binomial Random Variate Generation
	 * Communications of the ACM, Volume 31, Number 2, February 1988, pages 216-222.
	 * 
	 * @param p The probability of a single event.  This should be less than or equal to 0.5.
	 * @param n The number of trials
	 * @return An integer drawn from a binomial distribution with parameters (p, n).
	 */

	private static int binomial (int n, double p) {
		double q = 1 - p;
		if (n*p < 30.0) /* Algorithm BINV */
		{ 
			double s = p/q;
			double a = (n+1)*s;
			double r = Math.exp(n*Math.log(q)); /* pow() causes a crash on AIX */
			int x = 0;
			double u = uniform();
			while(true)
			{ 
				if (u < r) return x;
				u-=r;
				x++;
				r *= (a/x)-s;
			}
		}
		else /* Algorithm BTPE */
		{ /* Step 0 */
			double fm = n*p + p;
			int m = (int) fm;
			double p1 = Math.floor(2.195*Math.sqrt(n*p*q) -4.6*q) + 0.5;
			double xm = m + 0.5;
			double xl = xm - p1;
			double xr = xm + p1;
			double c = 0.134 + 20.5/(15.3+m);
			double a = (fm-xl)/(fm-xl*p);
			double b = (xr-fm)/(xr*q);
			double lambdal = a*(1.0+0.5*a);
			double lambdar = b*(1.0+0.5*b);
			double p2 = p1*(1+2*c);
			double p3 = p2 + c/lambdal;
			double p4 = p3 + c/lambdar;
			while (true)
			{ /* Step 1 */
				int y;
				int k;
				double u = uniform();
				double v = uniform();
				u *= p4;
				if (u <= p1) return (int)(xm-p1*v+u);
				/* Step 2 */
				if (u > p2)
				{ /* Step 3 */
					if (u > p3)
					{ /* Step 4 */
						y = (int)(xr-Math.log(v)/lambdar);
						if (y > n) continue;
						/* Go to step 5 */
						v = v*(u-p3)*lambdar;
					}
					else
					{
						y = (int)(xl+Math.log(v)/lambdal);
						if (y < 0) continue;
						/* Go to step 5 */
						v = v*(u-p2)*lambdal;
					}
				}
				else
				{
					double x = xl + (u-p1)/c;
					v = v*c + 1.0 - Math.abs(m-x+0.5)/p1;
					if (v > 1) continue;
					/* Go to step 5 */
					y = (int)x;
				}
				/* Step 5 */
				/* Step 5.0 */
				k = Math.abs(y-m);
				if (k > 20 && k < 0.5*n*p*q-1.0)
				{ /* Step 5.2 */
					double rho = (k/(n*p*q))*((k*(k/3.0 + 0.625) + 0.1666666666666)/(n*p*q)+0.5);
					double t = -k*k/(2*n*p*q);
					double A = Math.log(v);
					if (A < t-rho) return y;
					else if (A > t+rho) continue;
					else
					{ /* Step 5.3 */
						double x1 = y+1;
						double f1 = m+1;
						double z = n+1-m;
						double w = n-y+1;
						double x2 = x1*x1;
						double f2 = f1*f1;
						double z2 = z*z;
						double w2 = w*w;
						if (A > xm * Math.log(f1/x1) + (n-m+0.5)*Math.log(z/w)
						      + (y-m)*Math.log(w*p/(x1*q))
						      + (13860.-(462.-(132.-(99.-140./f2)/f2)/f2)/f2)/f1/166320.
						      + (13860.-(462.-(132.-(99.-140./z2)/z2)/z2)/z2)/z/166320.
						      + (13860.-(462.-(132.-(99.-140./x2)/x2)/x2)/x2)/x1/166320.
						      + (13860.-(462.-(132.-(99.-140./w2)/w2)/w2)/w2)/w/166320.)
							continue;
						return y;
					}
				}
				else
				{ /* Step 5.1 */
					int i;
					double s = p/q;
					double aa = s*(n+1);
					double f = 1.0;
					for (i = m; i < y; f *= (aa/(++i)-s));
					for (i = y; i < m; f /= (aa/(++i)-s));
					if (v > f) continue;
					return y;
				}
			}
		}
	}

	/**
	 * This routine returns a uniform random number between 0.0 and 1.0. Both 0.0
	 * and 1.0 are excluded. This random number generator is described in:
	 *
	 * Pierre l'Ecuyer
	 * Efficient and Portable Combined Random Number Generators
	 * Communications of the ACM, Volume 31, Number 6, June 1988, pages 742-749,774.
	 *
	 * The first time this routine is called, it initializes the random number
	 * generator using the current time. First, the current epoch time in seconds is
	 * used as a seed for the random number generator in the C library. The first two
	 * random numbers generated by this generator are used to initialize the random
	 * number generator implemented in this routine.
	 *
	 * NOTE: how different is this from Java's Math.random() or Random.nextDouble()?
	 *
	 * @return A double-precison number between 0.0 and 1.0.
	 */
	private static double uniform() {
		int z;
		int m1 = 2147483563;
		int m2 = 2147483399;
		double scale = 1.0/m1;

		if (seed1==0 || seed2==0) /* initialize */
		{ 
			Date date = new Date();
			int initseed = (int) date.getTime();
			Random r = new Random(initseed);
			seed1 = r.nextInt();
			seed2 = r.nextInt();
		}

		do
		{ 
			int k;
			k = seed1/53668;
			seed1 = 40014*(seed1-k*53668)-k*12211;
			if (seed1 < 0) seed1+=m1;
			k = seed2/52774;
			seed2 = 40692*(seed2-k*52774)-k*3791;
			if(seed2 < 0) seed2+=m2;
			z = seed1-seed2;
			if(z < 1) z+=(m1-1);
		} while (z==m1); /* To avoid returning 1.0 */

		return z*scale;
	}
}

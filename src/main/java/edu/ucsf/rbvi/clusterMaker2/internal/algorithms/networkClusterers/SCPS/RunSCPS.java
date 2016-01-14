/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *	1. Redistributions of source code must retain the above copyright
 *	  notice, this list of conditions, and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above
 *	  copyright notice, this list of conditions, and the following
 *	  disclaimer in the documentation and/or other materials provided
 *	  with the distribution.
 *	3. Redistributions must acknowledge that this software was
 *	  originally developed by the UCSF Computer Graphics Laboratory
 *	  under support by the NIH National Center for Research Resources,
 *	  grant P41-RR01081.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleSingularValueDecomposition;

public class RunSCPS {

	private List<CyNode> nodes;
	private boolean canceled = false;
	public final static String GROUP_ATTRIBUTE = "__SCPSGroups";
	protected int clusterCount = 0;
	private boolean createMetaNodes = false;
	private CyMatrix distanceMatrix = null;
	private DoubleMatrix2D matrix = null;
	private boolean debug = false;

	private double epsilon;
	private int kvalue;
	private int rnumber;
	private DoubleMatrix2D LMat;
	private int numComponents;

	private  HashMap<Integer, NodeCluster> clusterMap;

	private HashMap<Integer,Integer> new2oldMap;
	private HashMap<Integer,Integer> old2newMap;

	private TaskMonitor monitor;


	public RunSCPS(CyMatrix dMat, double epsilon, int kvalue, int rnumber, TaskMonitor monitor )
	{
		this.distanceMatrix = dMat;
		this.epsilon = epsilon;
		this.kvalue = kvalue;
		this.rnumber = rnumber; 

		this.monitor = monitor;
		this.clusterMap = new HashMap<Integer,NodeCluster>();
		this.clusterCount = 0;
		nodes = distanceMatrix.getRowNodes();
		matrix = distanceMatrix.getColtMatrix();

		//maps indices of filtered nodes in new uMatrix to the the original in the complete, unfiltered matrix, and vice-versa
		this.old2newMap = new HashMap<Integer,Integer>();
		this.new2oldMap = new HashMap<Integer,Integer>();
	}


	public void cancel () { canceled = true; }

	public List<NodeCluster> run(CyNetwork network, TaskMonitor monitor)
	{
		int k;

		monitor.showMessage(TaskMonitor.Level.INFO,"Formatting Matrix Data");
		DoubleMatrix2D sMat = getSMat(this.distanceMatrix);
		DoubleMatrix2D LMat = getLMat(sMat);

		monitor.showMessage(TaskMonitor.Level.INFO,"Calculating Eigenvalues");
		DenseDoubleEigenvalueDecomposition decomp = new DenseDoubleEigenvalueDecomposition(LMat);
		DoubleMatrix2D eigenVect = decomp.getV();
		DoubleMatrix1D eigenVal = decomp.getRealEigenvalues();


		monitor.showMessage(TaskMonitor.Level.INFO,"Calculating K value");

		if(this.kvalue > -1)
			k = this.kvalue;
		else
			k = getK(eigenVal,.3);

		System.out.println("K is " + k);

		if(numComponents > k){
			doComponentClustering();
			return new ArrayList<NodeCluster>(this.clusterMap.values());
		}

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating uMatrix for kMeans");
		DoubleMatrix2D uMat = getUMat(eigenVect,k);
		monitor.showMessage(TaskMonitor.Level.INFO,"Running kmeans clustering");
		doKMeansClustering(uMat,sMat);

		//clusterMap calculated in getSMat and doKMeansClustering steps. Simply return the results
		return new ArrayList<NodeCluster>(this.clusterMap.values());
	}

	//map old2new and new2old key-value pair
	public void setMap(int old_index, int new_index){
		new2oldMap.put(new Integer(new_index), new Integer(old_index));
		old2newMap.put(new Integer(old_index), new Integer(new_index));
	}

	//return value of old_index in new2oldMap. Mapping should invariably exist. 
	public int getMap_old(int new_index){
		return (new2oldMap.get(new Integer(new_index))).intValue();
	}

	//return value of new_index in old2newMap. If no such mapping exists, return -1.
	public int getMap_new(int old_index){
		try{ 
			return (old2newMap.get(new Integer(old_index))).intValue(); 
		} catch(Exception e){ return -1;}
	}

	//Get Connected Components, cluster all components <= |5|, and connect the remaining components with random lowscoring edges
	public DoubleMatrix2D getSMat(CyMatrix distanceMatrix){

		//Matrix prior to filtration modification
		DoubleMatrix2D unfiltered_mat = distanceMatrix.getColtMatrix();

		//Size of newly created Umat after filtering of small components
		int sMat_rows = 0;

		HashMap<Integer, List<CyNode>> filtered_cmap = new HashMap<Integer, List<CyNode>>();

		//Connected Componets
		Map<Integer, List<CyNode>> cMap = MatrixUtils.findConnectedComponents(distanceMatrix);

		IntArrayList rowList = new IntArrayList();
		IntArrayList columnList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();

		//Iterate through connected components
		int component_size_sum = 0;

		 for (List<CyNode> component: cMap.values()) {
			numComponents += 1;

			//Size <= 5. Automatically create cluster and increment clusterCount. 
			if(component.size() <= 5){
				NodeCluster iCluster = new NodeCluster(component);
				iCluster.setClusterNumber(this.clusterCount);
				//iCluster.add(component,this.clusterCount);
				this.clusterMap.put(new Integer(clusterCount),iCluster);
				this.clusterCount++;
			} else{
				//iterate through components and assign them index mappings in new uMatrix
				component_size_sum += component.size();

				System.out.println("Normal Component size " + component.size() + " Total Sum " + component_size_sum);

				for(int i = 0; i < component.size(); i++){

					CyNode n = component.get(i);
					int node_id = this.nodes.indexOf(n);

					//set mapping of new matrix index to old index
					setMap(node_id, sMat_rows);
					sMat_rows++;
				}
			}
		}

		DoubleMatrix2D sMat = DoubleFactory2D.sparse.make(sMat_rows, sMat_rows);

		//set diagnols of sMat to one
		for(int i = 0; i < sMat_rows; i++)
			sMat.set(i,i,1);

		//iterate through nonzero edges. If both nodes in new index map, transfer the edge to new matrix
		unfiltered_mat.getNonZeros(rowList,columnList,valueList);

		for(int i = 0; i<rowList.size(); i++){
			int row_id = rowList.get(i);
			int column_id = columnList.get(i);

			int new_row_id = getMap_new(row_id);
			int new_column_id = getMap_new(column_id);
			double value = valueList.get(i);

			//Set symmetrically the values in new matrix
			if(new_row_id > -1 && new_column_id > -1)
			{
				sMat.set(new_row_id,new_column_id,value);
				sMat.set(new_column_id,new_row_id,value);
			}
		}
		return sMat;
	}

 	//Calculate negative square root of matrix using singular value decomposition
	 public DoubleMatrix2D getNegSqrRoot(DoubleMatrix2D A){

		 //A = USV, where S is Diagnol Matrix
		 DenseDoubleSingularValueDecomposition decomp = new DenseDoubleSingularValueDecomposition(A, true, true);
		 DoubleMatrix2D U = decomp.getU();
		 DoubleMatrix2D S = decomp.getS();
		 DoubleMatrix2D V = decomp.getV();

		 //S^1/2 = Square root of every value in diangol matrix
		 for(int i = 0; i < S.rows(); i++)
		 	S.set(i,i,Math.pow(S.get(i,i),.5));

		 //A^1/2 = VS^1/2U
		 DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
		 DoubleMatrix2D sqrtA = alg.mult(alg.mult(V,S),U);

		 //return A^-1/2
		 return alg.inverse(sqrtA);
	 }

	 //Return Negative Sqrt of a Diagnol Matrix
	 public DoubleMatrix2D getNegSqrRootDMat(DoubleMatrix2D dMat){

		 for(int i = 0; i < dMat.rows(); i++)
		 dMat.set(i,i,Math.pow(dMat.get(i,i),-.5));

		 return dMat;
	 }


	// L = D^-1/2 * S * D^-1/2
	public DoubleMatrix2D getLMat(DoubleMatrix2D sMat){
		DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
		DoubleMatrix2D dMat = getDMat(sMat);
		DoubleMatrix2D transDMat = getNegSqrRootDMat(dMat);
	
		return alg.mult(transDMat,alg.mult(sMat,transDMat));
	}


	//D is Diagonal Matrix formed of vertex degree Dii = Sum Columns j over row Si
	public DoubleMatrix2D getDMat(DoubleMatrix2D sMat){
		DoubleMatrix2D dMat = sMat.like();

		for(int i = 0; i < sMat.rows(); i++){
			//set the Diagnal (i,i) to sum of columns over row i
			dMat.set(i,i, sMat.viewRow(i).zSum());
		}

		return dMat;	
	}

	//Get K using eigenvetors of S Matrix
	public int getK(DoubleMatrix1D eigenVal, double minLambda){
		double prevLamb;
		double nextLamb;
		int k;

		//set K to smallest integer such that LambdaK/LambdaK+1 > epsilon
		prevLamb = round(eigenVal.get((int)eigenVal.size()-1));

		for(k = 1; k < eigenVal.size(); k++){
			nextLamb = round(eigenVal.get((int)eigenVal.size()-k-1));

			System.out.println("k " + k + " PrevLamb " + prevLamb + " nextLamb " + nextLamb +	" prevLamb/nextLamb " + prevLamb/nextLamb);

			if(nextLamb < minLambda)
				break;

			if(prevLamb/nextLamb > this.epsilon)
				break;

			prevLamb = nextLamb;
		}

		return k;
	}

	//U constructed from top K Eigenvectors of L. After construction, each row of U is normalized to unit length.
	public DoubleMatrix2D getUMat(DoubleMatrix2D eigenVect, int k){
		DoubleMatrix2D uMat;
		IntArrayList indexList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();

		//construct matrix U from first K eigenvectors (ordered in ascending value by eigenvalue in eigenVect so start with the k-to-last column)
	 	uMat = eigenVect.viewPart(0,eigenVect.columns()-k,eigenVect.rows(),k);

		//Normalize each row of matrix U to have unit length
		for(int i = 0; i < uMat.columns(); i++){

			DoubleMatrix1D row = uMat.viewRow(i);
			double rowLength = Math.pow(row.zDotProduct(row),.5);
			row.getNonZeros(indexList,valueList);

			//normalize each Nozero value in row
			for(int j = 0; j < indexList.size(); j ++){
				int index = indexList.get(j);
				double value = valueList.get(j)/rowLength;

				uMat.set(i,index,value);
			}

		}
		return uMat;
	}

	public void doKMeansClustering(DoubleMatrix2D uMat,DoubleMatrix2D sMat){
		int k = uMat.columns();
		int[] clusterArray = new int[uMat.rows()];

		//do kmeans clustering
		KCluster.kmeans(k,rnumber,uMat,clusterArray);

		//redistribute cluster results
		clusterArray = redistributeMaxCluster(clusterArray,sMat,k);

		//Loop through clustering results, getting the clusters by order

		for(int cluster_id = 0; cluster_id < k; cluster_id++){

			NodeCluster iCluster = new NodeCluster();
			List<CyNode> node_list = new ArrayList<CyNode>();

			for(int j = 0; j < clusterArray.length; j++){

				//node j in uMatrix belongs to cluster #cluster_id
				if(clusterArray[j] == cluster_id)
					node_list.add(this.nodes.get(getMap_old(j)));
			}

			iCluster = new NodeCluster(node_list);
			iCluster.setClusterNumber(this.clusterCount);

			this.clusterMap.put(new Integer(clusterCount),iCluster);

			System.out.println("Clustercount " + this.clusterCount + " cluster_id " + cluster_id + " node_list_length " + node_list.size());

			this.clusterCount++;
		}
	}

	//Takes largest cluster obtained by Kmeans and redisributes some of its elements across the other clusters via Kurucz Algorithm
	public int[]redistributeMaxCluster(int[] clusters, DoubleMatrix2D sMat, int k){

		int maxClusterID = -1;
		int maxClusterSize = -1;
		int maxClusterConnection = -1;
		double maxClusterConnectionSize = -1;

		IntArrayList indexList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();

		//Array of cluster sizes
		int[] clusterSizeArray = new int[k];

		//array of redistributed clusters
		int[] redistribClusters = new int[clusters.length];

		//array summing edge connections from node in largest cluster to all other clusters
		double[] clusterConnectionCount = new double[k];

		for(int i = 0; i < clusterSizeArray.length; i++)
			clusterSizeArray[i] = 0;


		//compute size of each cluster
		for(int i = 0; i < clusters.length; i++){
			int clusterID = clusters[i];
			clusterSizeArray[clusterID] += 1;
		}

		//find max cluster size and max cluster id
		for(int i = 0; i < clusterSizeArray.length; i++){
			int clusterSize = clusterSizeArray[i];

			if(clusterSize > maxClusterSize){
				maxClusterSize = clusterSize;
				maxClusterID = i;
			}
		}

		//run loop until no changes observed in cluster transfers
		while(true) {
			int transfer_count = 0;

			//loop through SMat redistribute elements in largest cluster based on edge weight connectivity
			for(int i = 0; i < clusters.length; i++){
				//node belongs to one of smaller clusters. Merely add existing cluster value to redistributed cluster array
				if(clusters[i] != maxClusterID){
					redistribClusters[i] = clusters[i];
					continue;
				}

				//index corresponds to element in main cluster. Count the cluster connections from node
				for(int j = 0; j < k; j++)
					clusterConnectionCount[j] = 0;

				maxClusterConnection = -1;
				maxClusterConnectionSize = -1;

				DoubleMatrix1D row = sMat.viewRow(i);
				row.getNonZeros(indexList, valueList);

				//loop through existing edges for node and record how many times the connection bridges each cluster
				for(int j = 0; j < indexList.size(); j++){
					int connectingNode = indexList.get(j);
					int connectingNodeCluster = clusters[connectingNode];
					clusterConnectionCount[connectingNodeCluster] += valueList.get(j);
				}

				//loop through cluster connection counts and find cluster with greatest number of avg edge connections
				for(int j = 0; j<k; j++){
					double avgConnectionSize = clusterConnectionCount[j] / (double)(clusterSizeArray[j] + 1);

					if(maxClusterConnectionSize < avgConnectionSize){
						maxClusterConnectionSize = avgConnectionSize;
						maxClusterConnection = j;
					}
				}

				//update redistributed cluster array to reflect maxClusterConnection
				redistribClusters[i] = maxClusterConnection;

				if(clusters[i] != redistribClusters[i]){
					transfer_count++;
					System.out.println("Node " + i + " moved from " + clusters[i] + " to " + redistribClusters[i]);
				}
			}

			//transfer has occured, update clusters to equal redistrib clusters
			if(transfer_count > 0){
				for(int i = 0; i < clusters.length; i++)
					if(clusters[i] != redistribClusters[i]){
						int clusterID = redistribClusters[i];
						clusterSizeArray[maxClusterID]--;
						clusterSizeArray[clusterID]++;
						clusters[i] = redistribClusters[i];
				}
				System.out.println("Transfer Count " + transfer_count + " MaxClusterSize " + clusterSizeArray[maxClusterID]);
			} else
		 	//No transfer occured. Break out of loop
				break;
		}

		return redistribClusters;
	}

	//Store all components length greater then 5 in clusters, if number components is greater then K
	public void doComponentClustering(){
		//Connected Componets
		Map<Integer, List<CyNode>> cMap = MatrixUtils.findConnectedComponents(distanceMatrix);

		//Iterate through connected components
		int component_size_sum = 0;
		for (List<CyNode> component: cMap.values()) {
			if(component.size() > 5){
				NodeCluster iCluster = new NodeCluster(component);
				iCluster.setClusterNumber(this.clusterCount);
				this.clusterMap.put(new Integer(clusterCount),iCluster);
				this.clusterCount++;
			}
		}
	}

	//round double to two decimal points
	public double round(double d){
		int precision = 100;
		return Math.floor(d*precision + .5)/precision;
	}

	/**
	 * Normalize normalizes a cell in the matrix
	 */
	private class Normalize implements IntIntDoubleFunction {
		private double maxValue;
		private double minValue;
		private double span;
		private double factor;
		private double minWeight = Double.MAX_VALUE;

		public Normalize(double minValue, double maxValue, double factor) {
			this.maxValue = maxValue;
			this.minValue = minValue;
			this.factor = factor;
			span = maxValue - minValue;
		}

		public double apply(int row, int column, double value) {
			return ((value-minWeight)/span)*factor;
		}
	}
}
  

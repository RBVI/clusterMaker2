package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch.ChengChurchContext;

public class RunBiMine {

	protected CyNetwork network;
	protected String[] weightAttributes;
	//protected DistanceMetric metric;
	protected BET<Integer> bet;
	protected Matrix matrix;
	protected Matrix matrix_preproc;
	protected Matrix matrix_preproc_t;
	protected Matrix biclusterMatrix;
	protected Double arr[][];
	protected Double geneRho[][];
	protected Double conditionRho[][];
	protected int[] clusters;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	BiMineContext context;	
	double delta;
	double alpha;
	
	protected Map<Integer,List<Long>> clusterNodes;
	protected Map<Integer,List<String>> clusterAttrs;
	
	public Matrix getMatrix() { return matrix; }
	public Matrix getBiclusterMatrix() { return biclusterMatrix; }
	public int[] getClustersArray() {return clusters;}
	
	public RunBiMine(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, BiMineContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		//this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.delta = context.delta.getValue();
		this.alpha = context.alpha;

	}
	
	public Integer[] cluster(boolean transpose) {
		// Create the matrix
		matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		
		// Create a weight vector of all ones (we don't use individual weighting, yet)
		matrix.setUniformWeights();
				
		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		matrix_preproc = new Matrix(network,nelements,nattrs);		
		matrix_preproc_t = new Matrix(network,nattrs,nelements);
		//arr = preProcess();
		bet = Init_BET();
		System.out.println("Matrix after preprocessing:");
		for(int i = 0;i<nelements;i++){
			for (int j = 0; j < nattrs; j++){
				System.out.print(matrix_preproc.getValue(i, j) +"\t");
			}		
			System.out.println("");
		}
		//calculateRhos();
		
		List<BETNode<Integer>> biclusters = BET_tree();
		
		int totalRows = 0;
		for(BETNode bicluster: biclusters)totalRows+= bicluster.getGenes().size();
		
		clusters = new int[totalRows];
		CyNode rowNodes[] = new CyNode[totalRows];
		biclusterMatrix = new Matrix(network,totalRows,nattrs);
		int i = 0;
		
		clusterNodes = new HashMap<Integer,List<Long>>();
		clusterAttrs = new HashMap<Integer,List<String>>();
		
		for(int k = 0; k < biclusters.size(); k++){
			List<Integer> geneList = biclusters.get(k).getGenes();
			List<Integer> conditionList = biclusters.get(k).getConditions();
			
			List<Long> nodes = new ArrayList<Long>();
			for(Integer node:geneList){				
				biclusterMatrix.setRowLabel(i, matrix.getRowLabel(node));
				rowNodes[i] = matrix.getRowNode(node);
				
				for(int j = 0; j< nattrs; j++){
					biclusterMatrix.setValue(i, j,matrix.getValue(node, j));					
				}
				clusters[i] = k;				
				i++;
				
				nodes.add(matrix.getRowNode(node).getSUID());
			}
			clusterNodes.put(k, nodes);
			
			List<String> attrs = new ArrayList<String>();
			for(Integer cond:conditionList){
				attrs.add(matrix.getColLabel(cond));
			}
			clusterAttrs.put(k, attrs);
		}
		
		for(int j = 0; j<nattrs;j++){
			biclusterMatrix.setColLabel(j, matrix.getColLabel(j));			
		}
		
		biclusterMatrix.setRowNodes(rowNodes);
		
		Integer[] rowOrder;
		rowOrder = biclusterMatrix.indexSort(clusters, clusters.length);
		return rowOrder;
	}
		
	private BET<Integer> Init_BET() {
		BETNode<Integer> root = new BETNode<Integer>();
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		for(int i = 0; i < nelements; i++){			
			ArrayList<Integer> condList = new ArrayList<Integer>();
			ArrayList<Integer> geneList = new ArrayList<Integer>();
			geneList.add(i);
			double avg_i = getRowAvg(i);
			
			for(int j = 0 ; j < nattrs; j++){
				Double value = matrix.getValue(i, j);
				if(value!=null && avg_i != 0.0){				
					if( (Math.abs(value-avg_i)/avg_i) > delta ){
						condList.add(j);			
						matrix_preproc.setValue(i, j, value);
						matrix_preproc_t.setValue(j, i, value);
					}
					else{
						matrix_preproc.setValue(i, j, null);
						matrix_preproc_t.setValue(j, i, null);
					}
				}
			}
			BETNode<Integer> child = new BETNode<Integer>(geneList,condList);
			root.addChild(child);
		}
		
		BET<Integer> newBet = new BET<Integer>(root);
		return newBet;
	}
	
	private List<BETNode<Integer>> BET_tree(){
		BETNode<Integer> node = bet.getRoot();
		List<BETNode<Integer>> level = node.getChildren();
		List<BETNode<Integer>> leaves = new ArrayList<BETNode<Integer>>();
		int levelnum = 1;
		while(level.size() > 0){
			if(levelnum>2)break;
			System.out.println("Level : "+levelnum);
			int levelSize = level.size();	
			List<BETNode<Integer>> nextLevel = new ArrayList<BETNode<Integer>>();
			for(int i = 0; i < levelSize; i++){				
				BETNode<Integer> node_i = level.get(i);
				
				for(int j = i+1; j < levelSize; j++){
					BETNode<Integer> uncle_j = level.get(j);
					List<Integer> childGenes = union(node_i.getGenes(),uncle_j.getGenes());
					List<Integer> childConditions = intersection(node_i.getConditions(),uncle_j.getConditions());
					Collections.sort(childGenes);
					Collections.sort(childConditions);
					BETNode<Integer> child_j = new BETNode<Integer>(childGenes,childConditions);
					
					double asr = getASR(child_j);
					System.out.println("i: "+i+", j: "+j+", ASR: "+asr);					
					System.out.println("Genes:");
					for(Integer gene: childGenes)System.out.print(gene+", ");
					System.out.println("\nConditions:");
					for(Integer cond: childConditions)System.out.print(cond+", ");					
					System.out.println("");
					
					if(asr >= alpha){
						//Collections.sort(child_j.getGenes());
						//Collections.sort(child_j.getConditions());
						node_i.addChild(child_j);					
					}
					else{
					}				
				}
				if(node_i.getChildren().size() > 0){
					nextLevel.addAll(node_i.getChildren());
				}
				else{
					leaves.add(node_i);
				}
			}
			level = nextLevel;
			levelnum++;
			System.out.println("NumLeaves: "+leaves.size());
		}
		return getBiClusters(leaves);
	}
	
	private List<BETNode<Integer>> getBiClusters(List<BETNode<Integer>> leaves) {
		Collections.reverse(leaves);
		List<BETNode<Integer>> biclusters = new ArrayList<BETNode<Integer>>();
		biclusters.add(leaves.get(0));
		for(int i = 1; i < leaves.size(); i++){
			BETNode<Integer> leaf = leaves.get(i);
			boolean isSubset = false;
			for(BETNode bicluster: biclusters){
				if(bicluster.getGenes().containsAll(leaf.getGenes()) && 
						bicluster.getConditions().containsAll(leaf.getConditions())){
					isSubset = true;
					break;
				}
			}
			if(!isSubset)biclusters.add(leaf);
		}
		return biclusters;
	}

	private double getASR(BETNode<Integer> node) {
		List<Integer> genes = node.getGenes();
		List<Integer> conditions = node.getConditions();
		
		Matrix data = new Matrix(network,genes.size(),conditions.size());
		Matrix data_t = new Matrix(network,conditions.size(),genes.size());
		for(int i = 0; i < genes.size();i++){
			for(int j = 0; j < conditions.size();j++){
				data.setValue(i, j, matrix.getValue(genes.get(i), conditions.get(j)));
				data_t.setValue(j, i, matrix.getValue(genes.get(i), conditions.get(j)));
			}
		}
		
		double asr = 0.0;
		double asr_g = 0.0;
		double asr_c = 0.0;
		
		for(int i = 0; i < genes.size(); i++){			
			for(int j = i+1; j < genes.size(); j++){
				//asr_g += geneRho[genes.get(i)][genes.get(j)];
				asr_g += getSpearmansRho(data,i,j);
			}
		}
		asr_g /= genes.size()*(genes.size()-1);
		/*
		for(int i = 0; i < conditions.size(); i++){			
			for(int j = i+1; j < conditions.size(); j++){
				//asr_c += conditionRho[conditions.get(i)][conditions.get(j)];
				asr_c += getSpearmansRho(data_t,i,j);
			}
		}
		asr_c /= conditions.size()*(conditions.size()-1);
		*/
		//asr = 2*Math.max(asr_g, asr_c);
		asr = 2*asr_g;
		return asr;
	}
	
	private void calculateRhos() {
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		DistanceMetric spearman = DistanceMetric.SPEARMANS_RANK;
		geneRho = new Double[nelements][nelements];
		conditionRho = new Double[nattrs][nattrs];
		
		for(int i=0; i < nelements; i++){
			for(int j = i+1;j < nelements;j++){
				System.out.println("i:"+i+",j:"+j);
				//geneRho[i][j] = spearman.getMetric(matrix_preproc, matrix_preproc, matrix_preproc.getWeights(), i, j);
				geneRho[i][j] = getSpearmansRho(matrix_preproc,i,j);
			}			
		}
		
		for(int i=0; i< nattrs; i++){
			for(int j = i+1; j < nattrs;j++){
				//conditionRho[i][j] = spearman.getMetric(matrix_preproc_t, matrix_preproc_t, matrix_preproc_t.getWeights(), i, j);
				conditionRho[i][j] = getSpearmansRho(matrix_preproc_t,i,j);
			}			
		}
	}
	private Double getSpearmansRho(Matrix data, int i, int j) {
		Double rho = 0.0;
		double[] rank1 = data.getRank(i);
		double[] rank2 = data.getRank(j);
		
		int n = rank1.length;
		
		System.out.print("\nData1:\t");
		for(int k = 0;k<n;k++)System.out.print(data.getValue(i, k)+"\t");
		System.out.print("\nRank1:\t");
		for(int k = 0;k<n;k++)System.out.print(rank1[k]+"\t");
		System.out.print("\nData2:\t");
		for(int k = 0;k<n;k++)System.out.print(data.getValue(j, k)+"\t");
		System.out.print("\nRank2:\t");
		for(int k = 0;k<n;k++)System.out.print(rank2[k]+"\t");
		System.out.println("");
		
		/*
		int n1 = rank1.length;
		int n2 = rank2.length;
		int n = Math.max(n1, n2);
		
		double[] temp = new double[n];
		int index = 0;
		if(n1<n2){
			while(index<n){
				if(index<n1)temp[index] = rank1[index];
				else temp[index] = index;
				index++;
			}		
			rank1 = temp;
		}
		else if(n1>n2){
			while(index<n){
				if(index<n2)temp[index] = rank2[index];
				else temp[index] = index;
				index++;
			}		
			rank2 = temp;
		}
		*/
		//double[] d2 = new double[rank1.length];
		double sum_d2 = 0.0;
		for (int k = 0; i < n; i++) {
			sum_d2 += Math.pow((rank1[k])-(rank2[k]),2);
		}
		
		rho = 1- (6*sum_d2/(n*(n*n-1)));
		return rho;
	}
	public List<Integer> union(List<Integer> a, List<Integer> b){
		List<Integer> unionList = new ArrayList<Integer>(a);
		unionList.removeAll(b);
		unionList.addAll(b);
		return unionList;
	}
	
	public List<Integer> intersection(List<Integer> a, List<Integer> b){
		List<Integer> intersectionList = new ArrayList<Integer>(a);
		intersectionList.retainAll(b);		
		return intersectionList;
	}

	public Double[][] preProcess(){
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		Double matrix_proc[][] = new Double[nelements][nattrs];
		
		for(int i = 0; i < nelements; i++){
			
			double avg_i = getRowAvg(i);
			for(int j = 0 ; j < nattrs; j++){
				Double value = matrix.getValue(i, j);
				if(value==null || avg_i == 0.0)matrix_proc[i][j] = null;
				else{
					if( (Math.abs(value-avg_i)/avg_i) > delta )matrix_proc[i][j] = value;
					else matrix_proc[i][j] = null;					
				}
			}
		}
		
		return matrix_proc;
	}
	
	public double getRowAvg(int row){
		int nattrs = matrix.nColumns();
		double avg = 0.0;
		int count = 0;
		for(int j = 0; j < nattrs; j++){
			Double value = matrix.getValue(row, j);
			if(value!=null){
				avg += value;
				count++;
			}
		}
		
		if(count==0)return avg;
		else return avg/count;
	}
	
	public Map<Integer, List<Long>> getClusterNodes(){
		return clusterNodes;
	}
	
	public Map<Integer, List<String>> getClusterAttrs(){
		return clusterAttrs;
	}
}


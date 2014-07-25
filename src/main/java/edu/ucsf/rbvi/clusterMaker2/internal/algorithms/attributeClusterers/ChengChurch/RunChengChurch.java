package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

public class RunChengChurch {

	protected CyNetwork network;
	protected String[] weightAttributes;
	//protected DistanceMetric metric;
	protected Matrix matrix;
	protected double arr[][];
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	ChengChurchContext context;
	protected int nClusters;
	double delta;
	double alpha;
	double MatrixMax;
	double MatrixMin;
	protected Map<Integer,List<Integer>> clusterRows;
	protected Map<Integer,List<Integer>> clusterCols;
	protected Map<Integer,List<Long>> clusterNodes;
	protected Map<Integer,List<String>> clusterAttrs;
	
	List<Integer> unvisited;
	double distanceMatrix[][];	
	
	public RunChengChurch(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, ChengChurchContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		//this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.nClusters = context.nClusters;	
		this.delta = context.delta;
		this.alpha = context.alpha.getValue();
	}

	public Matrix getMatrix() { return matrix; }
	public int getNClusters() {return nClusters;}
	
	public int[] cluster(boolean transpose) {
		// Create the matrix
		matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		
		// Create a weight vector of all ones (we don't use individual weighting, yet)
		matrix.setUniformWeights();
				
		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		System.out.println("nelements = "+nelements+", nattrs = "+nattrs);
		arr = new double[nelements][nattrs];
		for(int i= 0 ;i < nelements; i++){
			for(int j = 0; j < nattrs; j++){
				arr[i][j] = matrix.getValue(i, j);
			}
		}
		
		int ifound = 1;
		int currentC = -1;
		int[] clusters = new int[nelements];
		clusterRows = new HashMap<Integer,List<Integer>>();
		clusterCols = new HashMap<Integer,List<Integer>>();
		
		clusterNodes = new HashMap<Integer,List<Long>>();
		clusterAttrs = new HashMap<Integer,List<String>>();
		
		//Initialising all clusters to -1
		for(int i = 0; i < nelements; i++) clusters[i] = -1;
		
		setMatrixMinMax();
		
		//The Cheng and Church algorithm
		for(int iter = 0; iter < nClusters; iter++){
			List<Integer> rows = new ArrayList<Integer>();
			List<Integer> cols = new ArrayList<Integer>();
			
			//Initialize rows and cols to all rows and columns
			for(int i = 0; i < nelements; i++){
				rows.add(i);
			}
			for(int j = 0; j < nattrs; j++){
				cols.add(j);
			}
			
			boolean changed = multipleNodeDeletion(rows,cols);
			
			if(changed == false){
				singleNodeDeletion(rows,cols);
			}
			
			nodeAddition(rows,cols);
			
			List<Long> nodes = new ArrayList<Long>();
			for (int i = 0; i < rows.size(); i++){
				nodes.add(matrix.getRowNode(rows.get(i)).getSUID());				
			}
			clusterNodes.put(iter, nodes);
			
			List<String> attrs = new ArrayList<String>();
			for (int j = 0; j < cols.size(); j++){
				attrs.add(matrix.getColLabel(cols.get(j)));				
			}
			clusterAttrs.put(iter, attrs);
			
			clusterRows.put(iter, rows);
			clusterCols.put(iter, cols);
			maskMatrix(rows,cols);			
		}
		
		return clusters;
	}
	
	public void setMatrixMinMax(){
		MatrixMax = Double.MIN_VALUE;
		MatrixMin = Double.MAX_VALUE;
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		for(int i = 0; i < nelements; i++){
			for(int j = 0; j < nattrs; j++){
				//double value = matrix.getValue(i, j);
				double value = arr[i][j];
				if(value > MatrixMax) MatrixMax = value;
				if(value < MatrixMin) MatrixMin = value;
			}
		}		
	}
	
	//For masking the array values corresponding to found bicluster with random values
	public void maskMatrix(List<Integer> rows, List<Integer> cols){
		
		int nRows= rows.size();
		int nCols = cols.size();
		Random generator = new Random();
		double range = MatrixMax - MatrixMin;
		for(int i = 0; i< nRows; i++){
			for(int j = 0; j< nCols; j++){
				double maskVal = generator.nextDouble()*range + MatrixMin;
				//matrix.setValue(rows.get(i), cols.get(j), maskVal);
				arr[rows.get(i)][cols.get(j)] = maskVal;				
			}
		}
	}
	
	//For computing the MSR of a bicluster
	public double calcMSR(List<Integer> rows, List<Integer> cols){
		double msr = 0;
		int rowSize = rows.size();
		int colSize = cols.size();
		
		Map<Integer,Double> rowSums = getRowSums(rows,cols);
		Map<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		for(Integer i: rows){
			for(Integer j: cols){
				
				double aiJ = rowSums.get(i)/colSize;
				double aIj = colSums.get(j)/rowSize;				
				double residue = arr[i][j] - aiJ - aIj + aIJ;
				
				msr += Math.pow(residue, 2);				
			}			
		}
		msr = msr/(rowSize*colSize);
		
		return msr;
	}
	
	public Map<Integer,Double> getRowSums(List<Integer> rows, List<Integer> cols){
		Map<Integer,Double> rowSums = new HashMap<Integer,Double>();
		
		for(Integer i : rows){
			double rowSum = 0;
			for(Integer j : cols){
				rowSum += arr[i][j];
			}
			rowSums.put(i, rowSum);			
		}
		return rowSums;
	}
	
	public Map<Integer,Double> getColSums(List<Integer> rows, List<Integer> cols){
		Map<Integer,Double> colSums = new HashMap<Integer,Double>();
		
		for(Integer j : cols){
			double colSum = 0;
			for(Integer i : rows){
				colSum += arr[i][j];
			}
			colSums.put(j, colSum);			
		}
		return colSums;
	}
	
	public Map<Integer,Double> calcRowMSR(List<Integer> rows, List<Integer> cols){
		
		int rowSize = rows.size();
		int colSize = cols.size();
		Map<Integer,Double> rowMSRs = new HashMap<Integer,Double>();
		
		Map<Integer,Double> rowSums = getRowSums(rows,cols);
		Map<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		for(Integer i: rows){
			double rowMsr = 0.0;
			for(Integer j: cols){
				
				double aiJ = rowSums.get(i)/colSize;
				double aIj = colSums.get(j)/rowSize;				
				double residue = arr[i][j] - aiJ - aIj + aIJ;
				
				rowMsr += Math.pow(residue, 2);				
			}	
			rowMsr = rowMsr/colSize;
			rowMSRs.put(i, rowMsr);
		}
		
		return rowMSRs;
	}
	
	public Map<Integer,Double> calcColMSR(List<Integer> rows, List<Integer> cols){
		
		int rowSize = rows.size();
		int colSize = cols.size();
		Map<Integer,Double> colMSRs = new HashMap<Integer,Double>();
		
		Map<Integer,Double> rowSums = getRowSums(rows,cols);
		Map<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		for(Integer j: cols){
			double colMsr = 0.0;
			for(Integer i: rows){
				
				double aiJ = rowSums.get(i)/colSize;
				double aIj = colSums.get(j)/rowSize;				
				double residue = arr[i][j] - aiJ - aIj + aIJ;
				
				colMsr += Math.pow(residue, 2);				
			}	
			colMsr = colMsr/rowSize;
			colMSRs.put(j, colMsr);
		}
		
		return colMSRs;
	}
	
	public Map<Integer,Double> calcOtherRowMSR(List<Integer> rows, List<Integer> cols, boolean inverted){
		List<Integer> otherRows = new ArrayList<Integer>();
		List<Integer> otherCols = new ArrayList<Integer>();
		
		int nRows = matrix.nRows();
		int nCols = matrix.nColumns();
		
		for(int i = 0; i< nRows; i++ ){
			if( !(rows.contains(i)) ){
				otherRows.add(i);
			}
		}
		
		for(int j = 0; j< nCols; j++ ){
			if( !(cols.contains(j)) ){
				otherCols.add(j);
			}
		}
		
		int rowSize = rows.size();
		int colSize = cols.size();
		int otherRowSize = otherRows.size();
		int otherColSize = otherCols.size();
		
		Map<Integer,Double> rowMSRs = new HashMap<Integer,Double>();
		
		Map<Integer,Double> rowSums = getRowSums(rows,cols);
		Map<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		Map<Integer,Double> otherRowSums = getRowSums(otherRows,cols);
		
		for(Integer i: otherRows){
			double rowMsr = 0.0;
			for(Integer j: cols){
				
				double aiJ = otherRowSums.get(i)/colSize;
				double aIj = colSums.get(j)/rowSize;
				double residue = 0.0;
				
				if(!inverted){
					residue = arr[i][j] - aiJ - aIj + aIJ;
				}
				else{
					residue = -arr[i][j] + aiJ - aIj + aIJ;
				}
				
				rowMsr += Math.pow(residue, 2);				
			}	
			rowMsr = rowMsr/colSize;
			rowMSRs.put(i, rowMsr);
		}
		
		return rowMSRs;
	}
	
	public Map<Integer,Double> calcOtherColMSR(List<Integer> rows, List<Integer> cols){
		
		List<Integer> otherRows = new ArrayList<Integer>();
		List<Integer> otherCols = new ArrayList<Integer>();
		
		int nRows = matrix.nRows();
		int nCols = matrix.nColumns();
		
		for(int i = 0; i< nRows; i++ ){
			if( !(rows.contains(i)) ){
				otherRows.add(i);
			}
		}
		
		for(int j = 0; j< nCols; j++ ){
			if( !(cols.contains(j)) ){
				otherCols.add(j);
			}
		}
		
		int rowSize = rows.size();
		int colSize = cols.size();
		int otherRowSize = otherRows.size();
		int otherColSize = otherCols.size();
		
		Map<Integer,Double> colMSRs = new HashMap<Integer,Double>();
		
		Map<Integer,Double> rowSums = getRowSums(rows,cols);
		Map<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		Map<Integer,Double> otherColSums = getColSums(rows,otherCols);
		
		for(Integer j: otherCols){
			double colMsr = 0.0;
			for(Integer i: rows){
				
				double aiJ = rowSums.get(i)/colSize;
				double aIj = otherColSums.get(j)/rowSize;				
				double residue = arr[i][j] - aiJ - aIj + aIJ;
				
				colMsr += Math.pow(residue, 2);				
			}	
			colMsr = colMsr/rowSize;
			colMSRs.put(j, colMsr);
		}
		
		return colMSRs;
		
	}
	
	public boolean multipleNodeDeletion(List<Integer> rows, List<Integer> cols){
		double msr = calcMSR(rows,cols);
		boolean changed = false;
		
		while(msr > delta){
			changed = false;
			Map<Integer,Double> rowMSRs = calcRowMSR(rows,cols);			
			double cutoff = alpha*msr;
			
			List<Integer> remRows = new ArrayList<Integer>();
			
			for(Integer i : rows){				
				if(rowMSRs.get(i) > cutoff){
					//rows.remove(rows.indexOf(i));
					remRows.add(i);
					changed = true;
				}
			}
			
			for(Integer i : remRows){
				rows.remove(rows.indexOf(i));
			}
			
			List<Integer> remCols = new ArrayList<Integer>();
			Map<Integer,Double> colMSRs = calcColMSR(rows,cols);
			for(Integer j : cols){				
				if(colMSRs.get(j) > cutoff){
					//cols.remove(cols.indexOf(j));
					remCols.add(j);
					changed = true;
				}
			}
			
			for(Integer j : remCols){
				cols.remove(cols.indexOf(j));
			}
			
			if(changed == false) break;
			msr = calcMSR(rows,cols);
					
		}
		return changed;
	}
	
	public void singleNodeDeletion(List<Integer> rows, List<Integer> cols){
		double msr = calcMSR(rows,cols);
		
		while(msr > delta){
			Map<Integer,Double> rowMSRs = calcRowMSR(rows,cols);
			Map<Integer,Double> colMSRs = calcColMSR(rows,cols);
			
			int maxRow = getMax(rowMSRs);
			int maxCol = getMax(colMSRs);
			
			if(rowMSRs.get(maxRow) > colMSRs.get(maxCol) ){
				rows.remove(maxRow);
			}
			else{
				cols.remove(maxCol);
			}
			msr = calcMSR(rows,cols);
		}		
	}
	
	public void nodeAddition(List<Integer> rows, List<Integer> cols){
		
		while (true){
			int rowSize = rows.size();
			int colSize = cols.size();
			
			double msr = calcMSR(rows,cols);
			
			Map<Integer,Double> otherColMSRs = calcOtherColMSR(rows,cols);
			for (Integer j : otherColMSRs.keySet()){
				if(otherColMSRs.get(j) <= msr){
					cols.add(j);
				}
			}
			
			msr = calcMSR(rows,cols);
			Map<Integer,Double> otherRowMSRs = calcOtherRowMSR(rows,cols,false);
			Map<Integer,Double> otherRowMSRs_inverted = calcOtherRowMSR(rows,cols,true);
			
			for (Integer i : otherRowMSRs.keySet()){
				if(otherRowMSRs.get(i) <= msr){
					rows.add(i);
				}
			}
			
			for (Integer i : otherRowMSRs_inverted.keySet()){
				if( rows.contains(i) ) continue;
				
				if(otherRowMSRs_inverted.get(i) <= msr){
					rows.add(i);
				}
			}
			
			//end iteration if nothing is added to either rows or columns
			if(rowSize == rows.size() && colSize == cols.size()) break;
		}
		
	}
	
	public int getMax(Map<Integer,Double> map){
		Integer maxkey = null;
		Map.Entry<Integer,Double> maxEntry = null;

		for (Map.Entry<Integer,Double> entry : map.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		    {
		        maxEntry = entry;
		    }
		}
		maxkey = maxEntry.getKey();
		return maxkey;
	}
	
	public Map<Integer, List<Integer>> getClusterRows(){
		return clusterRows;
	}
	
	public Map<Integer, List<Integer>> getClusterCols(){
		return clusterCols;
	}
	
	public Map<Integer, List<Long>> getClusterNodes(){
		return clusterNodes;
	}
	
	public Map<Integer, List<String>> getClusterAttrs(){
		return clusterAttrs;
	}
}

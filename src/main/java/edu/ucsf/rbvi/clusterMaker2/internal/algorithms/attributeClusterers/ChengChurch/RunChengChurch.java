package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

public class RunChengChurch {

	protected CyNetwork network;
	protected String[] weightAttributes;
	//protected DistanceMetric metric;
	protected Matrix matrix;
	protected double Arr[][];
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	ChengChurchContext context;
	protected int nClusters;
	double delta;
	double alpha;
	double MatrixMax;
	double MatrixMin;
	
	ArrayList<Integer> unvisited;
	double distanceMatrix[][];	
	
	public RunChengChurch(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, ChengChurchContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		//this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.nClusters = 0;	
		this.delta = context.delta;
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
		
		Arr = new double[nelements][nattrs];
		for(int i= 0 ;i < nelements; i++){
			for(int j = 0; j < nattrs; j++){
				Arr[i][j] = matrix.getValue(i, j);
			}
		}
		
		int ifound = 1;
		int currentC = -1;
		int[] clusters = new int[nelements];
		//Initialising all clusters to -1
		for(int i = 0; i < nelements; i++) clusters[i] = -1;
		
		setMatrixMinMax();
		
		//The Cheng and Church algorithm
		for(int iter = 0; iter < nClusters; iter++){
			ArrayList<Integer> rows = new ArrayList<Integer>();
			ArrayList<Integer> cols = new ArrayList<Integer>();
			
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
			for (int i = 0; i < rows.size(); i++){
				if(clusters[rows.get(i)] == -1){
					clusters[rows.get(i)] = iter;
				}
			}
			
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
				double value = Arr[i][j];
				if(value > MatrixMax) MatrixMax = value;
				if(value < MatrixMin) MatrixMin = value;
			}
		}		
	}
	
	//For masking the array values corresponding to found bicluster with random values
	public void maskMatrix(ArrayList<Integer> rows, ArrayList<Integer> cols){
		
		int nRows= rows.size();
		int nCols = cols.size();
		Random generator = new Random();
		double range = MatrixMax - MatrixMin;
		for(int i = 0; i< nRows; i++){
			for(int j = 0; j< nCols; j++){
				double maskVal = generator.nextDouble()*range + MatrixMin;
				//matrix.setValue(rows.get(i), cols.get(j), maskVal);
				Arr[rows.get(i)][cols.get(j)] = maskVal;				
			}
		}
	}
	
	//For computing the MSR of a bicluster
	public double calcMSR(ArrayList<Integer> rows, ArrayList<Integer> cols){
		double msr = 0;
		int rowSize = rows.size();
		int colSize = cols.size();
		
		HashMap<Integer,Double> rowSums = getRowSums(rows,cols);
		HashMap<Integer,Double> colSums = getColSums(rows,cols);
		
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
				double residue = Arr[i][j] - aiJ - aIj + aIJ;
				
				msr += Math.pow(residue, 2);				
			}			
		}
		msr = msr/(rowSize*colSize);
		
		return msr;
	}
	
	public HashMap<Integer,Double> getRowSums(ArrayList<Integer> rows, ArrayList<Integer> cols){
		HashMap<Integer,Double> rowSums = new HashMap<Integer,Double>();
		
		for(Integer i : rows){
			double rowSum = 0;
			for(Integer j : cols){
				rowSum += Arr[i][j];
			}
			rowSums.put(i, rowSum);			
		}
		return rowSums;
	}
	
	public HashMap<Integer,Double> getColSums(ArrayList<Integer> rows, ArrayList<Integer> cols){
		HashMap<Integer,Double> colSums = new HashMap<Integer,Double>();
		
		for(Integer j : cols){
			double colSum = 0;
			for(Integer i : rows){
				colSum += Arr[i][j];
			}
			colSums.put(j, colSum);			
		}
		return colSums;
	}
	
	public HashMap<Integer,Double> calcRowMSR(ArrayList<Integer> rows, ArrayList<Integer> cols){
		
		int rowSize = rows.size();
		int colSize = cols.size();
		HashMap<Integer,Double> rowMSRs = new HashMap<Integer,Double>();
		
		HashMap<Integer,Double> rowSums = getRowSums(rows,cols);
		HashMap<Integer,Double> colSums = getColSums(rows,cols);
		
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
				double residue = Arr[i][j] - aiJ - aIj + aIJ;
				
				rowMsr += Math.pow(residue, 2);				
			}	
			rowMsr = rowMsr/colSize;
			rowMSRs.put(i, rowMsr);
		}
		
		return rowMSRs;
	}
	
	public HashMap<Integer,Double> calcColMSR(ArrayList<Integer> rows, ArrayList<Integer> cols){
		
		int rowSize = rows.size();
		int colSize = cols.size();
		HashMap<Integer,Double> colMSRs = new HashMap<Integer,Double>();
		
		HashMap<Integer,Double> rowSums = getRowSums(rows,cols);
		HashMap<Integer,Double> colSums = getColSums(rows,cols);
		
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
				double residue = Arr[i][j] - aiJ - aIj + aIJ;
				
				colMsr += Math.pow(residue, 2);				
			}	
			colMsr = colMsr/rowSize;
			colMSRs.put(j, colMsr);
		}
		
		return colMSRs;
	}
	
	public HashMap<Integer,Double> calcOtherRowMSR(ArrayList<Integer> rows, ArrayList<Integer> cols, boolean inverted){
		ArrayList<Integer> otherRows = new ArrayList<Integer>();
		ArrayList<Integer> otherCols = new ArrayList<Integer>();
		
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
		
		HashMap<Integer,Double> rowMSRs = new HashMap<Integer,Double>();
		
		HashMap<Integer,Double> rowSums = getRowSums(rows,cols);
		HashMap<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		HashMap<Integer,Double> otherRowSums = getRowSums(otherRows,cols);
		
		for(Integer i: otherRows){
			double rowMsr = 0.0;
			for(Integer j: cols){
				
				double aiJ = otherRowSums.get(i)/colSize;
				double aIj = colSums.get(j)/rowSize;
				double residue = 0.0;
				
				if(!inverted){
					residue = Arr[i][j] - aiJ - aIj + aIJ;
				}
				else{
					residue = -Arr[i][j] + aiJ - aIj + aIJ;
				}
				
				rowMsr += Math.pow(residue, 2);				
			}	
			rowMsr = rowMsr/colSize;
			rowMSRs.put(i, rowMsr);
		}
		
		return rowMSRs;
	}
	
	public HashMap<Integer,Double> calcOtherColMSR(ArrayList<Integer> rows, ArrayList<Integer> cols){
		
		ArrayList<Integer> otherRows = new ArrayList<Integer>();
		ArrayList<Integer> otherCols = new ArrayList<Integer>();
		
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
		
		HashMap<Integer,Double> colMSRs = new HashMap<Integer,Double>();
		
		HashMap<Integer,Double> rowSums = getRowSums(rows,cols);
		HashMap<Integer,Double> colSums = getColSums(rows,cols);
		
		//normalized sum of all elements of sub matrix
		double aIJ = 0;
		for(Integer i: rowSums.keySet()){
			aIJ += rowSums.get(i);
		}
		
		aIJ = aIJ/(rowSize*colSize);
		
		HashMap<Integer,Double> otherColSums = getColSums(rows,otherCols);
		
		for(Integer j: otherCols){
			double colMsr = 0.0;
			for(Integer i: rows){
				
				double aiJ = rowSums.get(i)/colSize;
				double aIj = otherColSums.get(j)/rowSize;				
				double residue = Arr[i][j] - aiJ - aIj + aIJ;
				
				colMsr += Math.pow(residue, 2);				
			}	
			colMsr = colMsr/rowSize;
			colMSRs.put(j, colMsr);
		}
		
		return colMSRs;
		
	}
	
	public boolean multipleNodeDeletion(ArrayList<Integer> rows, ArrayList<Integer> cols){
		double msr = calcMSR(rows,cols);
		boolean changed = false;
		
		while(msr > delta){
			changed = false;
			HashMap<Integer,Double> rowMSRs = calcRowMSR(rows,cols);			
			double cutoff = alpha*msr;
			
			for(Integer i : rows){				
				if(rowMSRs.get(i) > cutoff){
					rows.remove(rows.indexOf(i));
					changed = true;
				}
			}
			
			HashMap<Integer,Double> colMSRs = calcColMSR(rows,cols);
			for(Integer j : cols){				
				if(colMSRs.get(j) > cutoff){
					cols.remove(cols.indexOf(j));
					changed = true;
				}
			}
			
			if(changed == false) break;
			msr = calcMSR(rows,cols);
					
		}
		return changed;
	}
	
	public void singleNodeDeletion(ArrayList<Integer> rows, ArrayList<Integer> cols){
		double msr = calcMSR(rows,cols);
		
		while(msr > delta){
			HashMap<Integer,Double> rowMSRs = calcRowMSR(rows,cols);
			HashMap<Integer,Double> colMSRs = calcColMSR(rows,cols);
			
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
	
	public void nodeAddition(ArrayList<Integer> rows, ArrayList<Integer> cols){
		
		while (true){
			int rowSize = rows.size();
			int colSize = cols.size();
			
			double msr = calcMSR(rows,cols);
			
			HashMap<Integer,Double> otherColMSRs = calcOtherColMSR(rows,cols);
			for (Integer j : otherColMSRs.keySet()){
				if(otherColMSRs.get(j) <= msr){
					cols.add(j);
				}
			}
			
			msr = calcMSR(rows,cols);
			HashMap<Integer,Double> otherRowMSRs = calcOtherRowMSR(rows,cols,false);
			HashMap<Integer,Double> otherRowMSRs_inverted = calcOtherRowMSR(rows,cols,true);
			
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
	
	public int getMax(HashMap<Integer,Double> map){
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
	
}

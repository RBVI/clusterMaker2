package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

public class RunChengChurch {

	protected CyNetwork network;
	protected String[] weightAttributes;
	protected DistanceMetric metric;
	protected Matrix matrix;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	ChengChurchContext context;
	protected int nClusters;
	double delta;
	double alpha;
	
	ArrayList<Integer> unvisited;
	double distanceMatrix[][];	
	
	public RunChengChurch(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
            TaskMonitor monitor, ChengChurchContext context, double delta) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.nClusters = 0;	
		this.delta = delta;
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
		int ifound = 1;
		int currentC = -1;
		int[] clusters = new int[nelements];
		
		return clusters;
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
				double residue = matrix.getValue(i, j) - aiJ - aIj + aIJ;
				
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
				rowSum += matrix.getValue(i, j);
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
				colSum += matrix.getValue(i, j);
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
				double residue = matrix.getValue(i, j) - aiJ - aIj + aIJ;
				
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
				double residue = matrix.getValue(i, j) - aiJ - aIj + aIJ;
				
				colMsr += Math.pow(residue, 2);				
			}	
			colMsr = colMsr/colSize;
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
					
		}
		return changed;
	}
	
}

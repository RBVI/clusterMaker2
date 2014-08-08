package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch.ChengChurchContext;

public class RunBiMine {

	protected CyNetwork network;
	protected String[] weightAttributes;
	//protected DistanceMetric metric;
	protected Matrix matrix;
	protected Double arr[][];
	protected int[] clusters;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	BiMineContext context;	
	double delta;	
	
	public RunBiMine(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, BiMineContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		//this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.delta = context.delta.getValue();
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
		
		arr = preProcess();
		
		Integer[] rowOrder = null;
		return rowOrder;
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
}













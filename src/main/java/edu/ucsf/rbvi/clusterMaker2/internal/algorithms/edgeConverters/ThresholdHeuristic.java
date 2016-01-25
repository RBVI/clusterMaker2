package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

public class ThresholdHeuristic{

	private CyMatrix matrix;

	//total minimum and maximum weights encoutered in edgeWeight array
	private int totalMin = 100000000;
	private int totalMax = -100000000;

	//this multipled against all edgeweights to expand the number of integer bins if edge weight range less then 100
	private double binFactor;

	public ThresholdHeuristic(CyMatrix matrix){
		this.matrix = matrix;
		setBinFactor();
	}

	//calculates the binFactor. Multiplies edgeweights by binFactor edge weight range less then 100
	private void setBinFactor(){
		double minWeight = matrix.getMinValue();
		double maxWeight = matrix.getMaxValue();
		double weightRange;

		weightRange = maxWeight - minWeight;

		//keep edges as they are
		if(weightRange >= 100) {
			binFactor =  1.0;
		} else {
			//avoid dividing by zero
			if(weightRange == 0)
				weightRange = .00001;

			binFactor = 100.0/weightRange;
		}
	}

	//run threshold heuristic, returning -1000 if no threshold is found
	public double run() {
		int[] numConnectedNodes  = getNumConnectedNodes();
		int[] seArray = getSEarray();

		if(seArray.length == 0)
		    return -1000;

		return selectThreshold(numConnectedNodes,seArray);
	}

	//return the number of non-singleton nodes at each edge weight (rounded to the nearest integer)
	private int[] getNumConnectedNodes(){
		//maps id of each node to its maximum edge weight connection
		HashMap <Integer,Integer>  maxNodeConnection = new HashMap<Integer, Integer>();

		//each index of array maps number of non-singleton nodes remaining at 
		//index (taking account a shift such that lowest index is zero)
		int[] numConnectedNodes = null;

		for (int row = 0; row < matrix.nRows(); row++) {
			for (int col = row; col < matrix.nColumns(); col++) {
				if (!matrix.hasValue(row, col))
					continue;

				int edgeWeight = (int)(matrix.getValue(row, col)*binFactor);

				//update totalMin and totalMax, if neccesary
				if(totalMin > edgeWeight)
					totalMin = edgeWeight;

				if(totalMax < edgeWeight)
					totalMax = edgeWeight;

		    //update the maximum EdgeWeight associated with each source and target nodes
				if(maxNodeConnection.get(row) != null){
					int maxWeight = maxNodeConnection.get(row).intValue();

					//update max EdgeWeight for node if neccesary
					if(maxWeight < edgeWeight)
						maxNodeConnection.put(row,new Integer(edgeWeight));
				} else {
					maxNodeConnection.put(row,new Integer(edgeWeight));
				}

				if(maxNodeConnection.get(col) != null){
					int maxWeight = maxNodeConnection.get(col).intValue();

					//update max EdgeWeight for node if neccesary
					if(maxWeight < edgeWeight)
						maxNodeConnection.put(col,new Integer(edgeWeight));
				} else {
					maxNodeConnection.put(col,new Integer(edgeWeight));
				}
			}
		}

		//initialize array
		numConnectedNodes = initDistributionArray();

		//Iterate through maxNodeConnection values, updating the numConnectedNodes array
		for (Integer value: maxNodeConnection.values()) {
	    int index = shiftIndex(value);

	    //increment numConnected Nodes at index
	    numConnectedNodes[index] += 1;
		}

		return numConnectedNodes;
	}

	//initialize numConnectedNodes array or SEarray
	private int[] initDistributionArray(){
		int[] distribArray = new int[shiftIndex(totalMax + 1)];

		for(int i = 0; i < distribArray.length; i++)
			distribArray[i] = 0;

		return distribArray;
	}

	//takes edge weight threshold value and adjusts it thus that the min threshold corresponds to Zero index in array
	private int shiftIndex(int value){return (value - totalMin);}

	//intializes array where each index represents the number of edges remaining after a shifted threshold is applied
	private int[] getSEarray(){

		int[] distribArray = initDistributionArray();
		int[] seArray = initDistributionArray();

		//loop through edge weights, and create the network distribution
		for(int row = 0; row < matrix.nRows(); row++){
			for(int col = row; col < matrix.nColumns(); col++){
				if (matrix.hasValue(row, col)) {
	  	  	int edgeWeight = (int)(matrix.getValue(row, col)*binFactor);
	    		int index = shiftIndex(edgeWeight);
	    		distribArray[index] += 1;
				}
			}
		}

		//Create SE array from edgeweight distribution
		for(int i = 0; i < distribArray.length; i++)
			for(int j = 0; j < distribArray.length; j++)
				seArray[i] += distribArray[j];

		return seArray;
	}

	//use change in NSV distribution to calculate threshold
	private double selectThreshold(int[] numConnectedNodes, int[] seArray){

		double oldNSV = (double)numConnectedNodes[0]/(double)seArray[0]; 
		double newNSV;
		double deltaNSV;

		for(int i = 1; i < seArray.length; i ++){
			newNSV = (double)numConnectedNodes[i]/(double)seArray[i];
			deltaNSV = newNSV - oldNSV;

	    //dNSV/dTH is positive, shift index by minimum edge weight and return as threshold. 
			//Dive by binFactor to adjust for original scale
			if(deltaNSV > 0)
				return ((double)(i + totalMin))/binFactor;
		}

		//If no threshold found, return -1000
		return -1000;

	}
}


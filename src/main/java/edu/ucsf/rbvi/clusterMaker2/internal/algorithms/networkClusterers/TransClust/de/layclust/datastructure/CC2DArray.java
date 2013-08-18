/*
 * Created on 27. September 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;

import java.util.Arrays;


/**
 * This class is a realisation of the ICCEdges interface. It creates a 2
 * dimensional array where the costs for the edges are saved. The cost
 * for edge (i,j) is saved in cell [i][j].
 * 
 * @author Sita Lange
 */
public class CC2DArray implements ICCEdges {
	
	/* 2D float array for edge costs, both (i,j) and (j,i) are saved */
	private float[][] edgeCostArray;
	
	private float maxFromNormalisation, minFromNormalisation;
	private double l, r, minFromNormalisationWithThreshold, maxFromNormalisationWithThreshold;

	
	public CC2DArray (int size) {
		initCCEdges(size);
	}

	public CC2DArray clone(){
		CC2DArray clone = new CC2DArray(this.edgeCostArray.length);
		clone.l = this.l;
		clone.r = this.r;
		clone.minFromNormalisation = this.minFromNormalisation;
		clone.maxFromNormalisation = this.maxFromNormalisation;
		clone.minFromNormalisationWithThreshold = this.minFromNormalisationWithThreshold;
		clone.maxFromNormalisationWithThreshold = this.maxFromNormalisationWithThreshold;
		clone.edgeCostArray = this.edgeCostArray.clone();
		return clone;
		
	}
	
	/**
	 * Here the data structure for the symmetric array is initialised, but costs
	 * still need to be added.
	 * 
	 * @param size
	 *            The number of nodes in the component.
	 */
	public void initCCEdges(int size) {
		edgeCostArray = new float[size][size];		
	}

	/**
	 * Sets the cost for deleting the edge between node i and node j.
	 * 
	 * @param node_i
	 *            The value of the first edge node.
	 * @param node_j
	 *            The value of the seciond edge node.
	 * @param cost
	 *            The cost of adding or deleting the edge (i,j).
	 */
	public void setEdgeCost(int node_i, int node_j, float cost) {
		edgeCostArray[node_i][node_j] = cost;
		edgeCostArray[node_j][node_i] = cost;
	}
	
	/**
	 * Gets the cost for adding or deleting the edge (i,j).
	 * 
	 * @param node_i
	 *            The value of the first edge node.
	 * @param node_j
	 *            The value of the seciond edge node.
	 * @return cost The cost of adding or deleting the edge (i,j).
	 */
	public float getEdgeCost(int node_i, int node_j) {
//		if(node_i>node_j){
			return edgeCostArray[node_i][node_j];
//		}else {
//			return edgeCostArray[node_j][node_i];
//		}
//		try{
//			return edgeCostArray[node_i][node_j];
//		}catch (NullPointerException e){
//			return edgeCostArray[node_j][node_i];
//		}
		
	}
	/**
	 * Normalises the values between 0 and 1.
	 */
	public void normalise() {
		//find max and min:
		if(edgeCostArray.length < 2) return;
		float min = edgeCostArray[0][1];
		float max = edgeCostArray[0][1];
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = i+1; j < edgeCostArray[i].length; j++) {
				if(Math.abs(edgeCostArray[i][j]) > max) max = Math.abs(edgeCostArray[i][j]);
				if(edgeCostArray[i][j] < min) min = edgeCostArray[i][j];
			}
		}
		maxFromNormalisation = max;
		minFromNormalisation = min;

		if(max==0) return;
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = i+1; j < edgeCostArray[i].length; j++) {
				if(edgeCostArray[i][j]>0){
					edgeCostArray[i][j] = edgeCostArray[j][i]=  (edgeCostArray[i][j])/max;
				}else{
					edgeCostArray[i][j] = edgeCostArray[j][i]=  (edgeCostArray[i][j])/max;
				}
			}
		}
	}

	public void normaliseWithThreshold(double alpha) {
		//find max and min:
		if(edgeCostArray.length < 2) return;
		float min = edgeCostArray[0][1];
		float max = edgeCostArray[0][1];
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = 0; j < edgeCostArray[i].length; j++) {
				if(i ==j) continue;
				if(edgeCostArray[i][j] > max) max = edgeCostArray[i][j];
				if(edgeCostArray[i][j] < min) min = edgeCostArray[i][j];
			}
		}
		maxFromNormalisationWithThreshold = max;
		minFromNormalisationWithThreshold = min;
		//range of the normalised values: l & r
		if(Math.abs(min) < Math.abs(max)) {
			l = -alpha * Math.abs(min) / max;
			r = 1;
		} else {
			l = -1;
			r = alpha * max / Math.abs(min);
		}
//		float range = max - min;
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = 0; j < edgeCostArray[i].length; j++) {
				
				if(edgeCostArray[i][j] > 0) {
					edgeCostArray[i][j] = (float) r * edgeCostArray[i][j] / max;
				} else {
					edgeCostArray[i][j] = (float) l * edgeCostArray[i][j] / min;
				}
			}
		}
	}
	
	/**
	 * Undo the normalisation done by normalise()
	 */
	public void denormalise() {
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = i+1; j < edgeCostArray[i].length; j++) {
				if(edgeCostArray[i][j]>0){
					edgeCostArray[i][j] = edgeCostArray[j][i]= (float) (edgeCostArray[i][j])*maxFromNormalisation;
				}else{
					edgeCostArray[i][j] = edgeCostArray[j][i]= (float) (edgeCostArray[i][j])*maxFromNormalisation;
				}
			}
		}
	}

	/**
	 * Undo the normalisation done by normaliseWithThreshold
	 */
	public void denormaliseWithThreshold() {
		for(int i = 0; i < edgeCostArray.length; i++) {
			for (int j = 0; j < edgeCostArray[i].length; j++) {
				if(edgeCostArray[i][j] > 0) {
					edgeCostArray[i][j] = (float) (edgeCostArray[i][j] / r * maxFromNormalisationWithThreshold);
				} else {
					edgeCostArray[i][j] = (float) (edgeCostArray[i][j] / l * minFromNormalisationWithThreshold);
					
				}
			}
		}
	}
	
	
}

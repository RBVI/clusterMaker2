/*
 * Created on 27. September 2007
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;


/**
 * This class is a realisation of the ICCEdges interface. It creates a 1
 * dimensional array where the costs for the edges are saved. The cost
 * for edge (i,j) is saved in cell [i*k + j].
 *
 * @author Tobias Wittkop
 */
public class CC1DArray implements ICCEdges {

	/* 1D float array for edge costs, both (i,j) and (j,i) are saved */
	private float[] edgeCostArray;
	private int[] pos;
	private float maxFromNormalisation, minFromNormalisation;
	private double l, r, minFromNormalisationWithThreshold, maxFromNormalisationWithThreshold;
        private int size;
        private int dum;


	public CC1DArray (int size) {
            this.size = size;
            initCCEdges(size);
	}

	public CC1DArray clone(){
		CC1DArray clone = new CC1DArray(this.edgeCostArray.length);
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
		edgeCostArray = new float[(size*(size-1))/2];
                pos = new int[size];
                int count = 0;
                for (int i= 0; i < size; i++) {
                    pos[i] = count;
                    for (int j= i+1; j < size; j++) {
                        count++;
                    }
            }
	}

	/**
	 * Sets the cost for deleting the edge between node i and node j.
	 *
	 * @param node_i
	 *            The value of the first edge node.
	 * @param node_j
	 *            The value of the second edge node.
	 * @param cost
	 *            The cost of adding or deleting the edge (i,j).
	 */
	public void setEdgeCost(int node_i, int node_j, float cost) {
            if(node_i>node_j) {
		edgeCostArray[pos[node_j]+(node_i-node_j)-1] = cost;
            }else{
		edgeCostArray[pos[node_i]+(node_j-node_i)-1] = cost;
            }
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
                dum = node_j-node_i;
		if(dum>0){
			return edgeCostArray[pos[node_i]+dum-1];
		}else {
			return edgeCostArray[pos[node_j]-dum-1];
		}
	}
	/**
	 * Normalises the values between 0 and 1.
	 */
	public void normalise() {
//            cc2d.normalise();
		//find max and min:
		if(edgeCostArray.length < 2) return;
		float min = edgeCostArray[0];
		float max = edgeCostArray[0];
		for(int i = 0; i < size; i++) {
			for (int j = i+1; j < size; j++) {
				if(Math.abs(edgeCostArray[pos[i]+(j-i)-1]) > max) max = Math.abs(edgeCostArray[pos[i]+(j-i)-1]);
				if(edgeCostArray[pos[i]+(j-i)-1] < min) min = edgeCostArray[pos[i]+(j-i)-1];
			}
		}
		maxFromNormalisation = max;
		minFromNormalisation = min;

		if(max==0) return;
		for(int i = 0; i < size; i++) {
			for (int j = i+1; j < size; j++) {
				if(edgeCostArray[pos[i]+(j-i)-1]>0){
					edgeCostArray[pos[i]+(j-i)-1] =   (edgeCostArray[pos[i]+(j-i)-1])/max;
				}else{
					edgeCostArray[pos[i]+(j-i)-1] =  (edgeCostArray[pos[i]+(j-i)-1])/max;
				}
			}
		}
	}

	public void normaliseWithThreshold(double alpha) {
//            cc2d.normaliseWithThreshold(alpha);
		//find max and min:
		if(edgeCostArray.length < 2) return;
		float min = edgeCostArray[1];
		float max = edgeCostArray[1];
		for(int i = 0; i < size ;i++) {
			for (int j = 0; j < size; j++) {
				if(i ==j) continue;
				if(edgeCostArray[pos[i]+(j-i)-1] > max) max = edgeCostArray[pos[i]+(j-i)-1];
				if(edgeCostArray[pos[i]+(j-i)-1] < min) min = edgeCostArray[pos[i]+(j-i)-1];
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
		for(int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {

				if(edgeCostArray[pos[i]+(j-i)-1] > 0) {
					edgeCostArray[pos[i]+(j-i)-1] = (float) r * edgeCostArray[pos[i]+(j-i)-1] / max;
				} else {
					edgeCostArray[pos[i]+(j-i)-1] = (float) l * edgeCostArray[pos[i]+(j-i)-1] / min;
				}
			}
		}
	}

	/**
	 * Undo the normalisation done by normalise()
	 */
	public void denormalise() {
//            cc2d.denormalise();
		for(int i = 0; i < size; i++) {
			for (int j = i+1; j < size; j++) {
				if(edgeCostArray[pos[i]+(j-i)-1]>0){
					edgeCostArray[pos[i]+(j-i)-1] = (float) (edgeCostArray[pos[i]+(j-i)-1])*maxFromNormalisation;
				}else{
					edgeCostArray[pos[i]+(j-i)-1] = (float) (edgeCostArray[pos[i]+(j-i)-1])*maxFromNormalisation;
				}
			}
		}
	}

	/**
	 * Undo the normalisation done by normaliseWithThreshold
	 */
	public void denormaliseWithThreshold() {
//            cc2d.denormaliseWithThreshold();
		for(int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(edgeCostArray[pos[i]+(j-i)-1] > 0) {
					edgeCostArray[pos[i]+(j-i)-1] = (float) (edgeCostArray[pos[i]+(j-i)-1] / r * maxFromNormalisationWithThreshold);
				} else {
					edgeCostArray[pos[i]+(j-i)-1] = (float) (edgeCostArray[pos[i]+(j-i)-1] / l * minFromNormalisationWithThreshold);

				}
			}
		}
	}


}

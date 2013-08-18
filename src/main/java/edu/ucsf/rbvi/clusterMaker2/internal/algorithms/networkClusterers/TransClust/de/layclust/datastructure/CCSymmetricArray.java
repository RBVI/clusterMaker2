/*
 * Created on 25. September 2007
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;

/**
 * This class is a realisation of the ICCEdges interface. It creates a symmetric
 * 2 dimensional matrix in the form of array of arrays for the weights of the
 * edges of a component. The position of the first array equals to node i and
 * contains an array of floats. The position j in this second array stores the
 * cost of deleting or adding the edge (i,j). The created matrix is symmetric so
 * to save space. This doesn't have to be considered when getting or setting a
 * value, because the respective methods can deal with this.
 * 
 * @author Sita Lange
 */
public class CCSymmetricArray implements ICCEdges {

	/*
	 * This is an array of float[], which saves the cost for an edge. The
	 * position in the first array corresponds to node i and the position in the
	 * second float[] corresponds to node j. Note that the matrix created is
	 * symmetric, so i>=j.
	 */
	private Object[] symmetricEdgeArray;
	
	private double maxFromNormalisation, minFromNormalisation;
	private double minFromNormalisatioWithThreshold;
//	private double maxFromNormalisationWithThreshold;

	public CCSymmetricArray(int size) {
		initCCEdges(size);
	}

	public CCSymmetricArray clone(){
		return null;//TODO
	}
	
	/**
	 * Here the data structure for the symmetric array is initialised, but costs
	 * still need to be added.
	 * 
	 * @param size
	 *            The number of nodes in the component.
	 */
	public void initCCEdges(int size) {

		/*
		 * the array of float arrays created here is symmetric, which means node
		 * i is greater or equal to node j.
		 */
		symmetricEdgeArray = new Object[size];
		for (int i = 0; i < size; i++) {
			symmetricEdgeArray[i] = new float[i + 1];
		}
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
//		try{
//		float[] edges4i = (float[]) symmetricEdgeArray[node_i];
//		edges4i[node_j] = cost;
//		} catch (NullPointerException e){
//			float[] edges4j = (float[]) symmetricEdgeArray[node_j];
//			edges4j[node_i] = cost;			
//		}
		

		/* if i>j stay in that order, otherwise swap the values for symmetry */
		if (node_i > node_j) {
			float[] edges4i = (float[]) symmetricEdgeArray[node_i];
			edges4i[node_j] = cost;
		} else if (node_i < node_j){
			float[] edges4j = (float[]) symmetricEdgeArray[node_j];
			edges4j[node_i] = cost;
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

		float cost;
		
//		try{
//			cost = ((float[]) symmetricEdgeArray[node_i])[node_j];
//		}catch(NullPointerException e){
//			cost = ((float[]) symmetricEdgeArray[node_j])[node_i];
//		}
		
		
		/* if i>=j stay in that order, otherwise swap the values for symmetry */
		if (node_i >= node_j) {
			cost = ((float[]) symmetricEdgeArray[node_i])[node_j];
		} else {
			cost = ((float[]) symmetricEdgeArray[node_j])[node_i];
		}
		return cost;
	}
	
	/**
	 * Normalises the values between 0 and 1.
	 */
	public void normalise() {
		//find min / max:
		if(symmetricEdgeArray.length < 2) return;
		float min = ((float[])symmetricEdgeArray[0])[0];
		float max = min;
		for (int i = 0; i < symmetricEdgeArray.length; i++) {
			float[] arr = (float[]) symmetricEdgeArray[i];
			for (int j = 0; j < arr.length; j++) {
				if(arr[j] > max) max = arr[j];
				if(arr[j] < min) min = arr[j];
			}
		}
		minFromNormalisation = min;
		maxFromNormalisation = max;
		float range = max - min;
		for (int i = 0; i < symmetricEdgeArray.length; i++) {
			float[] arr = (float[]) symmetricEdgeArray[i];
			for (int j = 0; j < arr.length; j++) {
				arr[j] = (arr[j] - min) / range;
			}
		}
	}

	/**
	 * Normalises the values between -1 and 1.
	 */
	public void normaliseWithThreshold(double alpha) {
		//min & max:
		if(symmetricEdgeArray.length < 2) return;
		float min = ((float[])symmetricEdgeArray[0])[0];
		float max = min;
		for (int i = 0; i < symmetricEdgeArray.length; i++) {
			float[] arr = (float[]) symmetricEdgeArray[i];
			for (int j = 0; j < arr.length; j++) {
				if(arr[j] > max) max = arr[j];
				if(arr[j] < min) min = arr[j];
			}
		}
		//range of the normalised values: l & r
		double l, r;
		if(Math.abs(min) < Math.abs(max)) {
			l = -alpha * Math.abs(min) / max;
			r = 1;
		} else {
			l = -1;
			r = alpha * max / Math.abs(min);
		}
		//normalise between l & r:
		for (int i = 0; i < symmetricEdgeArray.length; i++) {
			float[] arr = (float[]) symmetricEdgeArray[i];
			for (int j = 0; j < arr.length; j++) {
				if(arr[j] > 0) {
					arr[j] = (float) r * arr[j] / max;
				} else {
					arr[j] = (float) l * arr[j] / min;
				}
			}
		}
	}
	/**
	 * Undo the normalisation done by normalise()
	 */
	public void denormalise() {
		double range = maxFromNormalisation - minFromNormalisatioWithThreshold;
		for (int i = 0; i < symmetricEdgeArray.length; i++) {
			float[] arr = (float[]) symmetricEdgeArray[i];
			for (int j = 0; j < arr.length; j++) {
				arr[j] = (float) (arr[j] * range - minFromNormalisation);
			}
		}
	}

	/**
	 * Undo the normalisation done by normaliseWithThreshold
	 */
	public void denormaliseWithThreshold() {
		// TODO Auto-generated method stub
		
	}
	
}

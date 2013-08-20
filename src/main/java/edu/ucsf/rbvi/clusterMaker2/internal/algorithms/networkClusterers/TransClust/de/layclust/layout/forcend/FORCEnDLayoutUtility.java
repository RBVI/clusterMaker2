/*
 * Created on 6. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * This class is a collection of static methods that are used for the layouting
 * process of FORCEnD.
 * 
 * @author Sita Lange
 * CeBiTec, Universitaet Bielefeld
 * 
 */
public class FORCEnDLayoutUtility {

	/**
	 * Calculates the temperature (cooling factor) for the given iteration.
	 * 
	 * @param iteration
	 *            The current iteration number.
	 * @param node_no
	 *            The number of nodes in the current ConnectedComponent.
	 * @param param
	 *            The parameters object for this layouting instance.
	 * @return The cooling temperature factor for the given iteration.
	 */
	public static double calculateTemperature(int iteration, int node_no,
			FORCEnDParameters param) {
		int k = 2;
		double temp = Math.pow((1.0 / (iteration + 1)), k)
				* param.getTemperature() * node_no;
//		double dummy = param.getTemperature()*param.getTemperature();
//		double temp = Math.sqrt(dummy-(iteration*(dummy/param.getIterations())));
		return temp;
	}

	/**
	 * Sets all the values in the displacement array to zero.
	 * 
	 * @param allDisplacements
	 *            The displacement array.
	 * @param node_no
	 *            The number of nodes for the current ConnectedComponent.
	 * @param dim
	 *            The current dimensions the layouting is run in.
	 */
	private static void setDisplacementsToZero(double[][] allDisplacements,
			int node_no, int dim) {
		for (int i = 0; i < node_no; i++) {
			Arrays.fill(allDisplacements[i], 0);
		}
	}

	/**
	 * Calculates the displacement vector for all nodes and saves it in a 2D
	 * double array.
	 * 
	 * @param allDisplacements
	 *            The displacement values for all nodes.
	 * @param cc
	 *            The current ConnectedComponent object.
	 * @param dim
	 *            The current dimensions the layouting is run in.
	 * @param param
	 *            The parameters object for FORCEnD.
	 * @param temperature 
	 */
	public static void calculateDisplacementVectors(
			double[][] allDisplacements, ConnectedComponent cc, int dim,
			FORCEnDParameters param, double temperature) {
		setDisplacementsToZero(allDisplacements, cc.getNodeNumber(), dim);
		double attraction = param.getAttractionFactor()/cc.getNodeNumber();
		double repulsion = param.getRepulsionFactor()/cc.getNodeNumber();
		int[] list = new int[cc.getNodeNumber()];
		if(TaskConfig.useThreads){
//			if(false){
			ExecutorService es = java.util.concurrent.Executors.newFixedThreadPool(TaskConfig.maxNoThreads);
			for (int i = 0; i < TaskConfig.maxNoThreads; i++) {
				DisplacementCalculator s = new DisplacementCalculator(attraction,repulsion,allDisplacements,cc,dim,list,(int) Math.rint((double) cc.getNodeNumber()/TaskConfig.maxNoThreads)*i,Math.min((int) Math.rint((double) cc.getNodeNumber()/TaskConfig.maxNoThreads)*(i+1)-1,cc.getNodeNumber()-1));
				
				es.execute(s);
			}
//			for (int i = 0; i < cc.getNodeNumber(); i++) {
//				int start = i;
//				int end = i;
//				int estimatedOperation = cc.getNodeNumber()-i;
//				while(estimatedOperation<((cc.getNodeNumber()*(cc.getNodeNumber()-1))/2)/TaskConfig.maxNoThreads){
//					if(i==cc.getNodeNumber()) break;
//					end++;
//					i++;
//					estimatedOperation+=cc.getNodeNumber()-i;
//				}
//				DisplacementCalculator s = new DisplacementCalculator(attraction,repulsion,allDisplacements,cc,dim,list,start,end);
//				es.execute(s);
//			}
			
			es.shutdown(); 
			try {
				es.awaitTermination(5, java.util.concurrent.TimeUnit.HOURS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}else{
			double distance,force,displacement;
			for (int i = 0; i < cc.getNodeNumber(); i++) {
				/*
				 * only need to calculate the forces for j<i, because force(i,j) =
				 * force(j,i)
				 * if it should at some stage not be the case, then change
				 * this!
				 */
				
				for (int j = i+1; j < cc.getNodeNumber(); j++) {
					
					distance = calculateEuclideanDistance(cc.getCCPositions(), dim, i,j);
					if(distance<FORCEnDLayoutConfig.MIN_DISTANCE) continue;
					/*
					 * calculate attraction or repulsion force 
					 * 
					 * attraction:
					 * 				log(d(i,j)+1) x cost(i,j) x attraction factor
					 * 				--------------------------------------------- 
					 * 	  		   	          number of nodes 
					 * 
					 * repulsion: 
					 * 				cost(i,j) x repulsion factor
					 *             -------------------------------
					 *             log(d(i,j)+1) x number of nodes 
					 * 
					 */
					
					if (cc.getCCEdges().getEdgeCost(i, j)>0) {
						force =  (Math.log(distance + 1) * cc.getCCEdges().getEdgeCost(i, j) * attraction)/distance;
						for (int d = 0; d < dim; d++) {
							displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
							allDisplacements[i][d] += displacement;
							allDisplacements[j][d] -= displacement;
						}
					}else{
						force = ((cc.getCCEdges().getEdgeCost(i, j) * repulsion)/Math.log(distance + 1))/distance;
						for (int d = 0; d < dim; d++) {
							displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
							allDisplacements[i][d] += displacement;
							allDisplacements[j][d] -= displacement;
						}
					}
				}
			}	
		}
		
//		
	}
	
	public class DisplacementTask implements Runnable {
		
		private static final long serialVersionUID = 1L;
		private double[][] allDisplacements;
		private ConnectedComponent cc;
		private int dim;
		private int[] list;
		private double attraction;
		private double repulsion;
		private int start;
		private int end;
		
		public DisplacementTask(double attraction, double repulsion,
				double[][] allDisplacements, ConnectedComponent cc, int dim,
				int[] list,int start, int end) {
			super();
			this.allDisplacements = allDisplacements;
			this.cc = cc;
			this.dim = dim;
			this.attraction = attraction;
			this.repulsion = repulsion;
			this.list = list;
			this.start = start;
			this.end = end;
		}

		public void run(){
			for (int i = start; i <= end; i++) {
				for (int j = i+1; j < cc.getNodeNumber(); j++) {
					
//					double distance = calculateEuclideanDistance(cc.getCCPositions(), dim, i,j);
					double distance=0;
					double dummy = 0;
					for (int d = 0; d < dim; d++) {	
						dummy = (cc.getCCPositions()[i][d] - cc.getCCPositions()[j][d]);
						distance += (dummy*dummy);
					}
					distance = Math.sqrt(distance);
					if(distance<FORCEnDLayoutConfig.MIN_DISTANCE) continue;
					/*
					 * calculate attraction or repulsion force 
					 * 
					 * attraction:
					 * 				log(d(i,j)+1) x cost(i,j) x attraction factor
					 * 				--------------------------------------------- 
					 * 	  		   	          number of nodes 
					 * 
					 * repulsion: 
					 * 				cost(i,j) x repulsion factor
					 *             -------------------------------
					 *             log(d(i,j)+1) x number of nodes 
					 * 
					 */
					
					if (cc.getCCEdges().getEdgeCost(i, j)>0) {
						double force =  (Math.log(distance + 1) * cc.getCCEdges().getEdgeCost(i, j) * attraction)/distance;
						for (int d = 0; d < dim; d++) {
							double displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
							synchronized (allDisplacements) {
								allDisplacements[i][d] += displacement;
								allDisplacements[j][d] -= displacement;
							}
							
						}
					}else{
						double force = ((cc.getCCEdges().getEdgeCost(i, j) * repulsion)/Math.log(distance + 1))/distance;
						for (int d = 0; d < dim; d++) {
							double displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
							synchronized (allDisplacements) {
								allDisplacements[i][d] += displacement;
								allDisplacements[j][d] -= displacement;
							}
						}
					}
				}
			}
		}
		
	}
	/**
	 * Calculates the euclidean distance between two nodes.
	 * 
	 * @param node_pos
	 *            The positions array with all node positions.
	 * @param dim
	 *            The dimension that FORCEnD is run in.
	 * @param node_i
	 *            Node i.
	 * @param node_j
	 *            Node j.
	 * @return The euclidean distance between node i and node j.
	 */
	private static double calculateEuclideanDistance(double[][] node_pos,
			int dim, int node_i, int node_j) {
		double distance = 0;
		double dummy = 0;
		for (int d = 0; d < dim; d++) {	
			dummy = (node_pos[node_i][d] - node_pos[node_j][d]);
			distance += (dummy*dummy);
		}
		distance = Math.sqrt(distance);
		return distance;
	}

	/**
	 * Calculates the new node positions. This means the cooling temperature
	 * factor is taken into account, the displacement has to exeed a fixed
	 * minimal length and then the calculated displacement is added to the
	 * previous node position. The method also checks if the new position would
	 * exceed the int boundary (min and max value), and if so it is bounded by
	 * this value.
	 * 
	 * @param allDisplacements
	 *            The double[][] with all calculated force vectors.
	 * @param node_pos
	 *            The previous node positions.
	 * @param node_no
	 *            The number of nodes.
	 * @param dim
	 *            The dimension for this layouting instance.
	 * @param temp
	 *            The cooling temperature factor.
	 */
	public static void moveAllNodesByDisplacement(double[][] allDisplacements,
			double[][] node_pos, int node_no, int dim, double temp) {

		for (int i = 0; i < node_no; i++) {

			/*
			 * the norm of the resulting force vector represents the
			 * displacement distance
			 */
			double norm = calculateNorm(allDisplacements, i, dim);
			
			/*
			 * only carry out next steps if the norm is greater than the defined
			 * minimal movement
			 */
//			if (norm > FORCEnDLayoutConfig.MIN_MOVEMENT) {

				for (int d = 0; d < dim; d++) {

					if (norm > temp) {
						allDisplacements[i][d] = (allDisplacements[i][d] / norm)
								* temp;
					}

					/* the new position for node i in dimension plane d */
					double newPos = node_pos[i][d] + allDisplacements[i][d];

					/*
					 * the boundaries of the layouting space are the maximal and
					 * the minimal values of an int
					 */
					if (newPos > Integer.MAX_VALUE) {
						node_pos[i][d] = Integer.MAX_VALUE;
					} else if (newPos < Integer.MIN_VALUE) {
						node_pos[i][d] = Integer.MIN_VALUE;
					} else {
						node_pos[i][d] = newPos;
					}
				}
//			}
		}
	}

	/**
	 * Calculates the norm for a given node in a positions array.
	 * 
	 * @param positions
	 *            All force vectors in one 2D double array.
	 * @param node
	 *            The position in the array (first dim) for which the norm
	 *            should be calculated.
	 * @param dim
	 *            The dimension of the force vector (second dim).
	 * @return The norm of the force vector for the given position.
	 */
	public static double calculateNorm(double[][] positions, int node, int dim) {
		double norm = 0;
		for (int d = 0; d < dim; d++) {
			double pos_i = positions[node][d];
			norm += pos_i * pos_i;
		}
		norm = Math.sqrt(norm);
		return norm;
	}
	
	
}

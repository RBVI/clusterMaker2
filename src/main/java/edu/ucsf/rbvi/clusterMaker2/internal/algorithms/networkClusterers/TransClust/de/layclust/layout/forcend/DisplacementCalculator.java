package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;

public class DisplacementCalculator implements Runnable {


//	java.util.concurrent.Callable<String>
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][] allDisplacements;
	private ConnectedComponent cc;
	private int dim;
	private int[] list;
	private double attraction;
	private double repulsion;
	private int start;
	private int end;
	
	public DisplacementCalculator(double attraction, double repulsion,
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
		double distance=0,dummy,displacement,force;
		for (int i = start; i <= end; i++) {
			for (int j = 0; j < cc.getNodeNumber(); j++) {
//			for (int j = i+1; j < cc.getNodeNumber(); j++) {
				
//				double distance = calculateEuclideanDistance(cc.getCCPositions(), dim, i,j);
				for (int d = 0; d < dim; d++) {	
					dummy = (cc.getCCPositions()[i][d] - cc.getCCPositions()[j][d]);
					distance += (dummy*dummy);
				}
				distance = Math.sqrt(distance);
//				distance = 5;
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
//					force =  (5 * cc.getCCEdges().getEdgeCost(i, j) * attraction)/distance;
					for (int d = 0; d < dim; d++) {
						displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
//						synchronized (allDisplacements[i]) {
							allDisplacements[i][d] += displacement;
//						}
//						synchronized(allDisplacements[j]){
//							allDisplacements[j][d] -= displacement;
//						}
					}
				}else{
					force = ((cc.getCCEdges().getEdgeCost(i, j) * repulsion)/Math.log(distance + 1))/distance;
//					force = ((cc.getCCEdges().getEdgeCost(i, j) * repulsion)/5)/distance;
					for (int d = 0; d < dim; d++) {
						displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
//						synchronized (allDisplacements[i]) {
							allDisplacements[i][d] += displacement;
//						}
//						synchronized(allDisplacements[j]){
//							allDisplacements[j][d] -= displacement;
//						}
					}
				}
			}
//			System.out.println();
		}
	}
	

//	@Override
//	protected void compute() {
////		if(list.length*(cc.getNodeNumber()-list[0])<cc.getNodeNumber()*1+1){
//		if (list.length<=1) {
//			for (int k = 0; k < list.length; k++) {
//				int i = list[k];
//			
//				
//				for (int j = i+1; j < cc.getNodeNumber(); j++) {
//					
////					double distance = calculateEuclideanDistance(cc.getCCPositions(), dim, i,j);
//					double distance=0;
//					double dummy = 0;
//					for (int d = 0; d < dim; d++) {	
//						dummy = (cc.getCCPositions()[i][d] - cc.getCCPositions()[j][d]);
//						distance += (dummy*dummy);
//					}
//					distance = Math.sqrt(distance);
//					if(distance<FORCEnDLayoutConfig.MIN_DISTANCE) continue;
//					/*
//					 * calculate attraction or repulsion force 
//					 * 
//					 * attraction:
//					 * 				log(d(i,j)+1) x cost(i,j) x attraction factor
//					 * 				--------------------------------------------- 
//					 * 	  		   	          number of nodes 
//					 * 
//					 * repulsion: 
//					 * 				cost(i,j) x repulsion factor
//					 *             -------------------------------
//					 *             log(d(i,j)+1) x number of nodes 
//					 * 
//					 */
//					
//					if (cc.getCCEdges().getEdgeCost(i, j)>0) {
//						double force =  (Math.log(distance + 1) * cc.getCCEdges().getEdgeCost(i, j) * attraction)/distance;
//						for (int d = 0; d < dim; d++) {
//							double displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
////							synchronized (allDisplacements) {
//								allDisplacements[i][d] += displacement;
//								allDisplacements[j][d] -= displacement;
////							}
//							
//						}
//					}else{
//						double force = ((cc.getCCEdges().getEdgeCost(i, j) * repulsion)/Math.log(distance + 1))/distance;
//						for (int d = 0; d < dim; d++) {
//							double displacement = (cc.getCCPositions()[j][d] - cc.getCCPositions()[i][d])*force;
////							synchronized (allDisplacements) {
//								allDisplacements[i][d] += displacement;
//								allDisplacements[j][d] -= displacement;
////							}
//						}
//					}
//				}
//			}
//		} else {
//			int midpoint = list.length / 2;
//			int[] l1 = Arrays.copyOfRange(list, 0, midpoint);
//			int[] l2 = Arrays.copyOfRange(list, midpoint, list.length);
////			System.out.println(l1.length + "\t" + l2.length + "\t" + this.getPool().getActiveThreadCount() + "\t" + this.getPool().getMaximumPoolSize());
////			System.out.println(this.getPool().getActiveThreadCount() + "\t" + this.getPool().getQueuedTaskCount() + "\t" + this.getPool().getPoolSize() + "\t" + this.getPool().getRunningThreadCount() + "\t" + this.getPool().getParallelism() + "\t" + this.getPool().getMaximumPoolSize() + "\t" + this.getPool().getAsyncMode());
//			DisplacementCalculator s1 = new DisplacementCalculator(attraction,repulsion,allDisplacements,cc,dim,l1);
//			DisplacementCalculator s2 = new DisplacementCalculator(attraction,repulsion,allDisplacements,cc,dim,l2);
//			invokeAll(s1,s2);
//		}
//	}
	
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

}

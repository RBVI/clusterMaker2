/* 
 * Created on 22. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * Creates an initial layout for the objects in an n-dimensional space.
 * The number of objects are divided by the number of planes in the
 * n-dimensional space (e.g. n=3, planes=3). This subset of objects
 * is then arranged in a circle using a equal distances between each, 
 * which corresponds to the number of objects.
 * 
 * @author Sita Lange
 *
 */
public class LayoutInitCirclesInPlanes implements ILayoutInitialiser {
	
	private ConnectedComponent cc = null;
	private double radius = 0;
	private int dim = 0;
	private int node_no = 0;
	public double[][] node_pos = null;

	/**
	 * Initialises the class with a {@link ConnectedComponent}
	 * and the dimension. Uses a fixed radius of length
	 * 1. Also initialises the positions array for the objects.
	 * 
	 * No random positions - a fixed number of initial objects in the same
	 * dimension will always create the same arrangement.
	 * 
	 * @param cc The ConnectedComponent object.
	 */
	public void initLayoutInitialiser(ConnectedComponent cc) {
		this.cc = cc;
		this.radius = 1;//TODO changed radius from 1 to 1000
		this.dim = TaskConfig.dimension;
		this.node_no = cc.getNodeNumber();
		this.node_pos = new double[node_no][dim];
	}

	/**
	 * Runs the initial layouting for the objects.
	 * Lays the objects equally in all planes of the n-dimensional space.
	 * 
	 */
	public void run() { 
//		if(cc.getNumberOfClusters()>0){
		if(false){
			int planes = getNumberOfPlanes();
			int currentPlane = 1;
			int nodes_per_plane = cc.getNumberOfClusters()/planes;
			int startNode = 0;
			
			
			for (int d1=0;d1<dim;d1++){ // first dimension for plane
				for(int d2 =0;d2<d1;d2++){ // second dimension for plane
					if(currentPlane==planes){
						nodes_per_plane = cc.getNumberOfClusters() - (nodes_per_plane * (planes-1)); //rest nodes
					}			
					createCircleForPlaneForClusters(d1, d2, startNode, nodes_per_plane);
					startNode += nodes_per_plane;
					++currentPlane;
				}
			}
			cc.setCCPositions(this.node_pos);
		}else{
			
			if(dim==1){
				for (int i = 0; i < cc.getNodeNumber(); i++) {
					this.node_pos[i][0] = i;
				}
			}else{
				int planes = getNumberOfPlanes();
				int currentPlane = 1;
				int nodes_per_plane = node_no/planes;
				int startNode = 0;
				
				for (int d1=0;d1<dim;d1++){ // first dimension for plane
					for(int d2 =0;d2<d1;d2++){ // second dimension for plane
						if(currentPlane==planes){
							nodes_per_plane = node_no - (nodes_per_plane * (planes-1)); //rest nodes
						}			
						createCircleForPlane(d1, d2, startNode, nodes_per_plane);
						startNode += nodes_per_plane;
						++currentPlane;
					}
				}
			}
			
			
			
			
			cc.setCCPositions(this.node_pos);
		}
	}
	
	/**
	 * Lays the given objects in a circle in the given plane with
	 * equal distances according to the number of objects.
	 * 
	 * @param d1 First dimension of the plane.
	 * @param d2 Second dimension of the plane.
	 * @param startNode Number of first node to arrange.
	 * @param nodes_per_plane Number of nodes that need to be arranged.
	 */
	private void createCircleForPlane(int d1, int d2, int startNode,int nodes_per_plane){
		double angle_step = 2*Math.PI/nodes_per_plane;
		double phi = 0; //angle from positive x-axis
		for(int i=startNode;i<=startNode+nodes_per_plane-1;i++){
			double x = getXValue(phi);
			double y = getYValue(phi);
			for(int d=0;d<dim;d++){
				if(d==d1){
					this.node_pos[i][d] = x;
				} else if(d==d2){
					this.node_pos[i][d] = y;
				}
			}
			phi += angle_step;
		}
	}
	
	/**
	 * Lays the given objects in a circle in the given plane with
	 * equal distances according to the number of objects.
	 * 
	 * @param d1 First dimension of the plane.
	 * @param d2 Second dimension of the plane.
	 * @param startNode Number of first node to arrange.
	 * @param nodes_per_plane Number of nodes that need to be arranged.
	 */
	private void createCircleForPlaneForClusters(int d1, int d2, int startNode,int nodes_per_plane){
		double angle_step = 2*Math.PI/nodes_per_plane;
		double phi = 0; //angle from positive x-axis
		for(int i=startNode;i<=startNode+nodes_per_plane-1;i++){
			double x = getXValue(phi);
			double y = getYValue(phi);
			for(int d=0;d<dim;d++){
				for (int j = 0; j < cc.getClusters().length; j++) {
					if(cc.getClusters()[j]==i){
						if(d==d1){
							this.node_pos[j][d] = x+(j*0.0001);
						} else if(d==d2){
							this.node_pos[j][d] = y+(j*0.0001);
						}
					}
				}
				
			}
			phi += angle_step;
		}
	}
	
	private double getXValue(double phi){
		/* x = r * cos (phi)  where r=radius and phi=angle from positive x axis */
		return radius * Math.cos(phi);
	}
	
	private double getYValue(double phi){
		/* y = r * sind (phi)  where r = radius and phi=angle from positve x axis */
		return radius * Math.sin(phi);
	}
	
	/**
	 * Calculates the number of planes that exist in the current n-dimensional space.
	 * planes(dim) = (dim-1) + (dim-2) + ... + 1 (can ignore 0).
	 * @return The number of planes in this dimension.
	 */
	private int getNumberOfPlanes(){
		int planes = 0;
		for(int i=1;i<dim;i++){
			planes +=i;
		}
		return planes;
	}
	
}

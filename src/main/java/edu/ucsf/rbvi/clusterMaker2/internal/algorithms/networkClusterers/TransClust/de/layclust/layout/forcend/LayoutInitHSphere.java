/*
 * Created on 2. October 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * This initialises the positions of the nodes from the ConnectedComponent
 * that is given to it in the constructor. The positions are uniformly distributed 
 * on the surface of a hypersphere, which has as many dimensions as given in 
 * the input of this program.
 * 
 * Use example:  ILayoutInitialiser li = new LayoutInitHSphere(connectedComponent);
 * 						  li.run();
 * 
 * @author Sita Lange
 */
public class LayoutInitHSphere implements ILayoutInitialiser {
	
	private int dim = 0;
	private int node_no = 0;
	private double[][] node_pos;
	private ConnectedComponent cc;
	
	// TODO organise constructor and call of this object so that it is 
	// not so prone to exceptions!!
	
	/**
	 * This constructor creates a new LayoutInitHSphere object without initialising
	 * it with a {@link ConnectedComponent}. This need to still be done and then 
	 *  run() needs to be called to fill the positions array.
	 */
	public LayoutInitHSphere(){}
	
	/**
	 * This constructor creates a new LayoutInitHSphere object and initialialises it
	 * with the given {@link ConnectedComponent} object. This instance then needs
	 * to call run() to fill the positions array.
	 * @param cc The ConnectedComponent object for which the positions need to initialised.
	 */
	public LayoutInitHSphere(ConnectedComponent cc){
		initLayoutInitialiser(cc);
	}
	
	/**
	 * This method initialises the node positions array. This is a 2-dimensional double array
	 * where each row i is the position of node i, where j is the position on the respective axis.
	 * The length of the row is equal to the dimension the program is run in. 
	 * @param cc The ConnectedComponent object for which the positions need to initialised.
	 */
	public void initLayoutInitialiser(ConnectedComponent cc) {
		this.node_no = cc.getNodeNumber();
		this.cc = cc;
		this.dim = TaskConfig.dimension;
		node_pos = new double[node_no][this.dim];
	}

	/**
	 * This method fills the positions array and sets it in the ConnectedComponent object.
	 * The positions are uniformly distributed on the surface of a hypersphere with as
	 * many dimensions as in the entire program.
	 */
	public void run() {
// for(int n=0;n<node_no;n++){
// double[] pos = getPosOnSphere();
// System.out.print("position "+n+": ");
// for (int i=0;i<dim;i++)
// System.out.print(pos[i]+"\t");
// System.out.print("\n");
// node_pos[n] = pos;
// }
		
		
		for(int n=0;n<node_no;n++){
			double r = 0.0;
			
			for (int d=0;d<dim;d++){				
		            /* generate N variables from uniform distribution */
//		            node_pos[n][d] = Math.random();
					node_pos[n][d] = uniform(-5.0, 5.0);
//		            System.out.println("random: "+node_pos[n][d]);		
		            
		            /* compute Euclidean norm of vector x[] */
		            r = r + node_pos[n][d]*node_pos[n][d];
			}
			r = Math.sqrt(r);
			for (int d2=0;d2<dim;d2++){
				/* scale vector with respect to r */
				node_pos[n][d2] = node_pos[n][d2]/r;			 
			}
		}	
		cc.setCCPositions(node_pos);		
	}
	
	
	/**
	 * Returns a real number uniformly between a and b.
	 * @return Uniformly distributed number between a and b.
	 */
    public static double uniform(double a, double b) {
        return a + Math.random() * (b-a);
    } 
	
//	private double[] getPosOnSphere(){
//		
//        double[] pos = new double[dim];
//        double r = 0.0;
//
//        /* generate N variables from uniform distribution */
//        for (int i = 0; i < dim; i++){
//            pos[i] = Math.random();
//            System.out.println("random: "+pos[i]);
//        }
//        
//
//        /* compute Euclidean norm of vector x[] */
//        for (int i = 0; i < dim; i++)
//            r = r + pos[i]*pos[i];
//            r = Math.sqrt(r);
//        
//
//        /* scale vector with respect to r */
//        for (int i = 0; i < dim; i++)
//        	pos[i] = pos[i]/r;
//// System.out.println(pos[i] / r);
//   
//		return pos;
//	}
}

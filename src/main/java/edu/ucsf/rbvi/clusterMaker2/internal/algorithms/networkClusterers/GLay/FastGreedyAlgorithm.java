/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * *A backup copy of the fastGreedy algorithm
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay;

/**
 *
 * @author sugang
 * This is an implementation of the Fast Greedy Girvan-Newman algorithm 
 * (see www.pnas.org/cgi/doi/10.1073/pnas.122653799, PNAS (99) 12. 2002)
 */

import java.util.HashMap;

import org.cytoscape.work.TaskMonitor;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class FastGreedyAlgorithm implements GAlgorithm {
    private double modularity;
    private int[] membership;
    private int clusterNumber;
    private double progress;
		private boolean halt = false;

    public FastGreedyAlgorithm(){
        super();
        reset();
    }

    public void reset(){
        this.modularity = 0;
        this.membership = new int[0];
        this.clusterNumber = 0;
        this.progress = 0;
    }


    public double getModularity(){
        return this.modularity;
    }

    public int[] getMembership(){
        /*This membership correpsonds to the index in network.getNodeIndicesArray()*/
        return this.membership;
    }

    public int getClusterNumber(){
        return this.clusterNumber;
    }

    public double getProgress(){
        return this.progress;
    }

		public void cancel() {
			this.halt = true;
		}
    
    /*Still use the sparse matrix implementation, but with improved structure*/
    public void execute(GSimpleGraphData g, TaskMonitor monitor) {

        /*Initialize*/
        
        double[] ai = new double[g.nodeCount];
        double qInitial = 0;

        membership = new int[g.nodeCount];
        //int counter = 0;
        for(int i=0; i<g.nodeCount; i++){
             
             //graphIndex[counter] = index;
             membership[i] = i;
             ai[i] = g.degree[i]/(2.0*g.edgeCount);
             qInitial -= ai[i]*ai[i];
             //nodeDegree[i] = network.getDegree(index);
             //counter++;
        }

        //for(int i=0; i<nodeIndexes.length-1; i++){
        //    for(int j=i+1; j<nodeIndexes.length; j++){
        //        //If there are more than one edge connecting two nodes, reduce the degree
        //        if(network.getEdgeCount(network.getNode(graphIndex[i]), network.getNode(graphIndex[j]), false)>1){
        //
        //        }
        //    }
        //}

        DoubleMatrix2D deltaQMx = DoubleFactory2D.sparse.make(g.nodeCount, g.nodeCount);
        
        //if(this.mode == 0){
            MaxFun func = new MaxFun();
        //}
        double maxDeltaQ = 0;
        double q = 0;
        int maxI=0;
        int maxJ=0;
        double deltaQ = 0;

        int itojDirectedEdgecount = 0;
        int jtoiDirectedEdgecount = 0;
        int ijUndirectedEdgeCount = 0;
        int ijTotalEdgeCount = 0;
        
        for(int i=0; i<g.nodeCount; i++){
            for(int j=0; j<g.nodeCount; j++){

                if(i == j){
                    continue;
                }
                if (g.hasEdge(i, j) && i < j) {
                    deltaQ = (1.0d/(2*g.edgeCount)-(g.degree[i]*g.degree[j])/(4.0*Math.pow(g.edgeCount, 2.0))) * 2;                    
                    deltaQMx.setQuick(i, j, deltaQ);
                    if(maxDeltaQ <= deltaQ){
                        maxDeltaQ = deltaQ;
                        maxI = i;
                        maxJ = j;
                    }
                } else if(g.hasEdge(i, j) && i > j) {
                    deltaQMx.setQuick(i, j, deltaQMx.getQuick(j, i));
                }

            }
        }
                
        //make sure i,j are ordered
        if(maxI > maxJ){
            //swap
            int temp = maxI;
            maxI = maxJ;
            maxJ = temp;
        }


        q = qInitial;
        int counter = 0;
        this.progress = (double)counter/(double)g.nodeCount;
				if (monitor != null)
					monitor.setProgress(this.progress);

				if (halt)
					return;

        //System.out.println("qInitial:" + qInitial);
        while(maxDeltaQ > 0){
            counter++;
            //this.progress = (int)(100.0*counter/nodeCount);
            q += maxDeltaQ;

            //System.out.println(q);
        
        for(int k=0; k<g.nodeCount; k++){
                if (halt)
                    return;
                if(k==maxJ || k==maxI){
                    continue;
                }
                else{
                    //that k is connected to both I and J
                    //we have an issue here, how to detect whether k is connected to i or j?
                    
                    if(deltaQMx.getQuick(maxI, k)!=0 && deltaQMx.getQuick(maxJ, k)!=0){
                        deltaQMx.setQuick(maxJ, k, deltaQMx.getQuick(maxJ, k)+deltaQMx.getQuick(maxI, k));
                        
                        
                        
                        //System.out.println("added");
                    }
                    else if(deltaQMx.getQuick(maxI, k)==0 && deltaQMx.getQuick(maxJ, k)!=0){
                        deltaQMx.setQuick(maxJ, k, deltaQMx.getQuick(maxJ, k)-2*ai[maxI]*ai[k]);
                    }
                    else if(deltaQMx.getQuick(maxJ, k)==0 && deltaQMx.getQuick(maxI, k)!=0 ){
                        deltaQMx.setQuick(maxJ, k, deltaQMx.getQuick(maxI, k)-2*ai[maxJ]*ai[k]);
                    }
                    else{
                        //both are zero
                        //nothing is done
                        
                        //it seems that nothing is wrong.
                        //but the result is not quite correct
                        //both are zero, no need to update
                        //System.out.println("This is wrong!");
                        //System.out.println("Do nothing.");
                    }
                }
            }//end update jth row
        
            int membershipI = membership[maxI];
            int membershipJ = membership[maxJ];
            for(int k=0;k<g.nodeCount; k++){
                deltaQMx.setQuick(k, maxJ, deltaQMx.getQuick(maxJ, k));
                deltaQMx.setQuick(maxI, k, 0.0);
                deltaQMx.setQuick(k, maxI, 0.0);
                if(membership[k] == membershipI){
                    membership[k] = membershipJ;
                }
            }
            
            ai[maxJ] = ai[maxI] + ai[maxJ];
            ai[maxI] = 0;
            
            deltaQMx.trimToSize();
            
            maxDeltaQ = 0;
            maxI = 0;
            maxJ = 0;
            func.reset();
            
            deltaQMx.forEachNonZero(func);
            maxDeltaQ = func.max;
            maxI = func.row;
            maxJ = func.column;
            
            if(maxI > maxJ){
                //swap
                int temp = maxI;
                maxI = maxJ;
                maxJ = temp;
            }
            
            //System.out.println("maxDeltaQ" + maxDeltaQ);
        }//end of while loop
        
        
        //these all can be done in linear time    
        HashMap<Integer, Integer> membershipMapping = new HashMap<Integer, Integer>();
        int index=0;
        for(int i=0; i<membership.length; i++){
            if(membershipMapping.containsKey(new Integer(membership[i]))){
            
            }
            else{
                membershipMapping.put(new Integer(membership[i]), new Integer(index));
                index++;
            }
        }

        //System.out.println("NumOfClusters:" + membershipMapping.keySet().size());
        this.clusterNumber = membershipMapping.keySet().size();
        
        for(int i=0; i<membership.length; i++){
            membership[i] = membershipMapping.get(new Integer(membership[i])).intValue();
        }
        
        //assignMembership(network, graphIndex, membership, membershipMapping);
        
        //System.out.println(q);
        this.modularity = q;
        this.progress = 100.0;

				if (monitor != null)
					monitor.setProgress(this.progress);
        //This notifies that the current thread is over
        //this.progress = 100;
        //System.out.println("The result modularity is:" + q);

        //for(int i=0; i<membership.length; i++){
        //    System.out.println(membership[i]);
        //}
    }
 

    //The max function for general Fast-Greedy Algorithm
    private class MaxFun implements IntIntDoubleFunction{
        public double max;
        public int row;
        public int column;

        public MaxFun(){
            max = -Double.MAX_VALUE;
            row = 0;
            column = 0;
        }

        public void reset(){
            max = -Double.MAX_VALUE;
            row = 0;
            column = 0;
        }

        public double apply(int row, int col, double value){
        /*There can be some fold of multiplicity of problem, the reason is at a certain merge,
         *When ther are more than one best merge, the choice this merge may impact the subsequent
         *Merges. So the q-values from different implementation of the same algorithm can be
         *slightly different
         */
        
            if(max < value){
                max = value;
                this.row = row;
                this.column = col;
            }
            return value;
        }
    }
}

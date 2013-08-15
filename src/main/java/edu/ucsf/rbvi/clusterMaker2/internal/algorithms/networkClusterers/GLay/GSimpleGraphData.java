/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay;

import java.util.ArrayList;
import java.util.List;


import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
/**
 *
 * @author Gang Su
 */
public class GSimpleGraphData {

    /*No encapisulation, just make the coding simpler*/
    public int nodeCount;
    public int edgeCount;
    public CyNetwork network;
    public CyNode[] graphIndices;
    public int[] degree;
    public DoubleMatrix2D edgeMatrix;
		private boolean selectedOnly;
		private boolean undirectedEdges;
		private List<CyNode> nodeList;
		private List<CyEdge> connectingEdges;

		@SuppressWarnings("unchecked")
    public GSimpleGraphData(CyNetwork network, boolean selectedOnly, boolean undirectedEdges){
        this.network = network;
				this.selectedOnly = selectedOnly;
				this.undirectedEdges = undirectedEdges;
				if (!selectedOnly) {
					this.nodeList = (List<CyNode>)network.getNodeList();
				} else {
					this.nodeList = new ArrayList<CyNode>(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true));
				}

				this.nodeCount = nodeList.size();
				this.connectingEdges = ModelUtils.getConnectingEdges(network, nodeList);
				this.edgeCount = this.connectingEdges.size();
        this.graphIndices = new CyNode[this.nodeCount];
        this.degree = new int[this.nodeCount];
        this.edgeMatrix = DoubleFactory2D.sparse.make(nodeCount, nodeCount);
        this.simplify();
    }
    
    private void simplify(){

        //Assign index and degree
        for(int i=0; i<nodeList.size(); i++){
            this.graphIndices[i] = nodeList.get(i);
            this.degree[i] = network.getNeighborList(this.graphIndices[i], CyEdge.Type.ANY).size();
        }

        //Assign edge
        int ijEdge = 0; //Edge from i to j
        int jiEdge = 0; //Edge from j to i
        int ijUEdge = 0; //Undirectional edges
        int totalEdge = 0;
        for(int i=0; i<graphIndices.length-1; i++){
            for(int j=i+1; j<graphIndices.length; j++){
                //Count the number of edges
                if (undirectedEdges) {
                	jiEdge = network.getConnectingEdgeList(graphIndices[j], graphIndices[i], CyEdge.Type.ANY).size(); //Count un-directional
                	ijEdge = network.getConnectingEdgeList(graphIndices[i], graphIndices[j], CyEdge.Type.ANY).size(); //Count un-directional
								} else {
                	ijEdge = network.getConnectingEdgeList(graphIndices[i], graphIndices[j], CyEdge.Type.DIRECTED).size(); //Doesn't count un-directional
                	jiEdge = network.getConnectingEdgeList(graphIndices[j], graphIndices[i], CyEdge.Type.ANY).size(); //Count un-directional
								}
                //ijUEdge = network.getEdgeCount(graphIndices[i], graphIndices[j], true);
                totalEdge = ijEdge + jiEdge;
                this.edgeMatrix.setQuick(i, j, totalEdge); //conversion from int to double is ok.

                //fix degree and edge count.
                if(totalEdge > 1){
                    edgeCount = edgeCount-totalEdge+1;
                    degree[i] = degree[i]-totalEdge+1;
                    degree[j] = degree[j]-totalEdge+1;
                }
            }
        }

        /*
         * If simplification is succesful, the toal degree should be twice of edge count
         * */
        //this.simplificationCheck();
        

    }

    public boolean hasEdge(int i, int j){
        /*Note i and j must 0< i,j < nodeCount - 1*/
        if(i==j)return false;
        if(i > j){
            int temp = i;
            i = j;
            j = temp;
        }

        if(edgeMatrix.getQuick(i, j) != 0){
            return true;
        }
        return false;
    }

    public void simplificationCheck(){
        System.out.println("---------------");
        System.out.println("Simplify check:");
        System.out.println("NodeCount:" + this.nodeCount);
        System.out.println("EdgeCount: O:" + network.getEdgeCount());
        System.out.println("EdgeCount: S:" + this.edgeCount);
        int totalDegree = 0;
        int totalDegreeBefore = 0;
        for(int i=0; i< this.graphIndices.length;i++){
            totalDegree += degree[i];
            totalDegreeBefore += network.getNeighborList(graphIndices[i], CyEdge.Type.ANY).size();
        }
        System.out.println("TotalDegree O:" + totalDegreeBefore);
        System.out.println("TotalDegree S:" + totalDegree);
    }

}

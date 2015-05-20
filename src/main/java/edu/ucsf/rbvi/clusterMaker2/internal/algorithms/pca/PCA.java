/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster.NAME;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster.SHORTNAME;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import java.util.List;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 *
 * @author root
 */
public class PCA extends AbstractTask{
        ClusterManager clusterManager;
        public static String SHORTNAME = "pca";
	public static String NAME = "Principal Component Analysis";
        private List<String>attrList;
        
        @Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
        
        @ContainsTunables
        public PCAContext context = null;
        
        public PCA(PCAContext context, ClusterManager clusterManager){
            this.context = context;
            if (network == null)
                    network = clusterManager.getNetwork();
            context.setNetwork(network);
        }
        
        public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}
        
        public void run(TaskMonitor monitor){
            monitor.setStatusMessage("Running Principal Component Analysis");
            List<String> dataAttributes = context.getNodeAttributeList();
            
            if (dataAttributes == null || dataAttributes.isEmpty() ) {
                monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no attribute list selected");
                return;
            }
            
            if (context.selectedOnly &&
			network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) == 0) {
                monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no nodes selected from network");
                return;
            }
            
            String[] attrArray = new String[dataAttributes.size()];
            int att = 0;
            for (String attribute: dataAttributes) {
                    attrArray[att++] = "node."+attribute;
            }
            
            double[][] distanceMatrix;
            if(context.inputValue.getSelectedValue().equals("Distance Matric")){
                Matrix matrix = new Matrix(network, attrArray, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
                
            }else if(context.inputValue.getSelectedValue().equals("Edge Value")){
                DistanceMatrix disMatrix = context.edgeAttributeHandler.getMatrix();
                distanceMatrix = disMatrix.getDistanceMatrix().toArray();
            }else{
                return;
            }
            
            int nRow = distanceMatrix.length;
            int nColumn = distanceMatrix[0].length;
                       
            for(int i=0;i<nRow;i++){
                for(int j=0;j<nColumn;j++){
                    System.out.print(distanceMatrix[i][j] + "\t");
                }
                System.out.println("\n");
            }
            
            System.out.println("" + nRow + " " + nColumn);
        }
}

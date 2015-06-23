/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

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
            
            RunPCA runPCA = new RunPCA(network, context, monitor, attrArray);
            if(context.inputValue.getSelectedValue().equals("Distance Matric") && 
                    context.pcaType.getSelectedValue().equals("PCA of input weight between nodes")){
                    runPCA.runOnNodeToNodeDistanceMatric();
            }else if(context.inputValue.getSelectedValue().equals("Distance Matric") && 
                    context.pcaType.getSelectedValue().equals("PCA of nodes and attributes") ){
                    runPCA.runOnNodeToAttributeMatric();
            }else if(context.inputValue.getSelectedValue().equals("Edge Value")){
                runPCA.runOnEdgeValues();
            }
        }
}

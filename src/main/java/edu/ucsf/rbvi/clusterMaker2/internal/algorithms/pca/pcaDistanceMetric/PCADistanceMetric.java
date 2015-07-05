/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.pcaDistanceMetric;

import java.util.List;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 *
 * @author root
 */
public class PCADistanceMetric extends AbstractTask{
        CyServiceRegistrar bc;
        private final CyApplicationManager appManager;
        public static String SHORTNAME = "pca";
	public static String NAME = "PCA of Network (Based on Distance Metric)";
        private final CyNetworkView networkView;
        
        @Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
        
        @ContainsTunables
        public PCADistanceMetricContext context = null;
        
        public PCADistanceMetric(PCADistanceMetricContext context, CyServiceRegistrar bc){
            this.context = context;
            this.appManager = bc.getService(CyApplicationManager.class);
            this.networkView = appManager.getCurrentNetworkView();
            if (network == null)
                    network = appManager.getCurrentNetwork();
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
            
            RunPCADistanceMetric runPCA = new RunPCADistanceMetric(network, networkView, context, monitor, attrArray);
            runPCA.computePCA();
        }
}

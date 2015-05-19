/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster.NAME;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster.SHORTNAME;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
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
            for(int i=0;i<5;i++){
                System.out.println("Running pca");
            }
        }
}

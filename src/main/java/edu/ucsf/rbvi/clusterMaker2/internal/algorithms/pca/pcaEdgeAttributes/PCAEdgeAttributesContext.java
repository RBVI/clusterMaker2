/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.pcaEdgeAttributes;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

/**
 *
 * @author root
 */
public class PCAEdgeAttributesContext {
        CyNetwork network;
        
        @Tunable(description = "Only use edges between selected nodes for PCA", groups={"Data Input"}, gravity=7.0)
	public boolean selectedOnly = false;
        
        @Tunable(description="Ignore nodes with no data", groups={"Data Input"}, gravity=8.0)
	public boolean ignoreMissing = true;
        
        @ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
        
        @Tunable(description = "Create Result Panel with Principal Component selection option", groups={"Result Options"}, gravity=83.0)
	public boolean pcaResultPanel = true;
        
        @Tunable(description = "Create PCA scatter plot with node selection option", groups={"Result Options"}, gravity=84.0)
	public boolean pcaPlot = false;
                
        public PCAEdgeAttributesContext(){
            
        }
        
        public void setNetwork(CyNetwork network){
            if (this.network != null && this.network.equals(network))
			return;
            
            this.network = network;
            if (edgeAttributeHandler == null)
			edgeAttributeHandler = new EdgeAttributeHandler(network);
		else
			edgeAttributeHandler.setNetwork(network);
          }
        
        public CyNetwork getNetwork() { return network; }
        
        public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }
        
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;

/**
 *
 * @author root
 */
public class PCAContext {
        CyNetwork network;
        
        @Tunable(description="Distance Metric", gravity=1.0)
	public ListSingleSelection<DistanceMetric> metric = 
		new ListSingleSelection<DistanceMetric>(BaseMatrix.distanceTypes);
        
        @Tunable(description="Input Value", gravity=2.0)
        public ListSingleSelection<String>inputValue = new ListSingleSelection<String>("Distance Matric", "Edge Value");

        @ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
        
        @ContainsTunables
        public AttributeList attributeList = null;
        
        @Tunable(description="PCA Type", gravity=80.0)
        public ListSingleSelection<String>pcaType = new ListSingleSelection<String>("PCA of input weight between nodes", "PCA of nodes and attributes");

        @Tunable(description = "Only use selected nodes for PCA", groups={"PCA Parameters"}, gravity=81.0)
	public boolean onlySelectedNodes = false;
        
        @Tunable(description = "Create Result Panel with Principal Component selection option", groups={"Result Options"}, gravity=82.0)
	public boolean pcaResultPanel = true;
        
        @Tunable(description = "Create PCA scatter plot with node selection option", groups={"Result Options"}, gravity=83.0)
	public boolean pcaPlot = false;
        
        public PCAContext(){
            
        }
        
        public void setNetwork(CyNetwork network){
            if (this.network != null && this.network.equals(network))
			return;
            
            this.network = network;
            if (edgeAttributeHandler == null)
			edgeAttributeHandler = new EdgeAttributeHandler(network);
		else
			edgeAttributeHandler.setNetwork(network);
            
            if (attributeList == null)
                    attributeList = new AttributeList(network);
            else
                    attributeList.setNetwork(network);
            
        }
        
        public CyNetwork getNetwork() { return network; }
        
        public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }
        
}

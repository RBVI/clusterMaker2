/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author root
 */
public class RunPCA {
    protected CyNetwork network;
    protected PCAContext context;
    protected TaskMonitor monitor;
    protected String[] weightAttributes;
    protected boolean ignoreMissing;
    protected boolean selectedOnly;
    protected double[][] distanceMatrix;
    
    public RunPCA(CyNetwork network, PCAContext context, TaskMonitor monitor, String[] weightAttributes){
        this.network = network;
        this.context = context;
        this.monitor = monitor;
        this.weightAttributes = weightAttributes;
    }
    
    public void runOnDistanceMatric(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
    }
    
    public void runOnEdgeValues(){        
                DistanceMatrix disMatrix = context.edgeAttributeHandler.getMatrix();
                distanceMatrix = disMatrix.getDistanceMatrix().toArray();
    }
}

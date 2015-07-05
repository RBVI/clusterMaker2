/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.pcaEdgeAttributes;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ResultPanelPCA;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.RunPCA;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ScatterPlotPCA;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author root
 */
public class RunPCAEdgeAttributes implements RunPCA{
    private final CyNetwork network;
    private final CyNetworkView networkView;
    private final PCAEdgeAttributesContext context;
    private final TaskMonitor monitor;
    private double[][] distanceMatrix;
    
    public RunPCAEdgeAttributes(CyNetwork network, CyNetworkView networkView, PCAEdgeAttributesContext context, TaskMonitor monitor){
        this.network = network;
        this.networkView = networkView;
        this.context = context;
        this.monitor = monitor;
    }
    
    public void computePCA(){
        DistanceMatrix matrix = context.edgeAttributeHandler.getMatrix();
        distanceMatrix = matrix.getDistanceMatrix().toArray();
        ComputationMatrix mat = new ComputationMatrix(distanceMatrix);

        ComputationMatrix[] components = this.computePCs(mat);
        
        if(context.pcaResultPanel)
            ResultPanelPCA.createAndShowGui(components, matrix.getNodes(), network, networkView, mat.computeVariance());
        
        if(context.pcaPlot)
            ScatterPlotPCA.createAndShowGui(components, mat.computeVariance());
    }
    
    public ComputationMatrix[] computePCs(ComputationMatrix matrix){

        ComputationMatrix mat = matrix.centralizeColumns();

        ComputationMatrix C = mat.covariance();

        double[] values = C.eigenValues();
        double[][] vectors = C.eigenVectors();

        ComputationMatrix[] components = new ComputationMatrix[values.length];
        double sum=0;
        for(int j=values.length-1, k=0;j>=0;j--,k++){
            sum += values[j];
            double[] w = new double[vectors.length];
            for(int i=0;i<vectors.length;i++){
                w[i] = vectors[i][j];
            }

            components[k] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));

            System.out.println("PC: " + k);
            components[k].printMatrix();
        }
        return components;
    }
    
    public ComputationMatrix[] computePCsSorted(ComputationMatrix matrix){
        
        ComputationMatrix mat = matrix.centralizeColumns();

        ComputationMatrix C = mat.covariance();

        double[] values = C.eigenValues();
        double[][] vectors = C.eigenVectors();

        double max = Double.MAX_VALUE;

        ComputationMatrix[] components = new ComputationMatrix[values.length];
        for(int j=0;j<values.length;j++){
            double value = values[0];
            int pos = 0;
            for(int i=0; i<values.length; i++){
                if(values[i] >= value && values[i] < max){
                    value = values[i];
                    pos = i;
                }
            }
            double[] w = new double[vectors.length];
            for(int i=0;i<vectors.length;i++){
                w[i] = vectors[i][pos];
            }

            components[j] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));
            max = value;
        }

        return components;
    }
    
}

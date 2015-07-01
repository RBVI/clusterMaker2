/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author root
 */
public class RunPCA {
    protected CyNetwork network;
    protected CyNetworkView networkView;
    protected PCAContext context;
    protected TaskMonitor monitor;
    protected String[] weightAttributes;
    protected boolean ignoreMissing;
    protected boolean selectedOnly;
    protected double[][] distanceMatrix;
    
    private static final int PCA_NODE_NODE = 1;
    private static final int PCA_NODE_ATTRIBUTE = 2;
    
    public RunPCA(CyNetwork network, CyNetworkView networkView, PCAContext context, TaskMonitor monitor, String[] weightAttributes){
        this.network = network;
        this.networkView = networkView;
        this.context = context;
        this.monitor = monitor;
        this.weightAttributes = weightAttributes;
    }
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are not sorted in their order from maximum to minimum
    public ComputationMatrix[] runOnNodeToNodeDistanceMatricSorted(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                
                ComputationMatrix[] components = this.computePCsSorted(mat, PCA_NODE_NODE);
                
                if(context.pcaPlot)
                    ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));
                
                return components;
    }
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are sorted in increasing order
    public ComputationMatrix[] runOnNodeToNodeDistanceMatric(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                
                ComputationMatrix[] components = this.computePCs(mat, PCA_NODE_NODE);
                
                if(context.pcaPlot)
                    ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));

                return components;
    }
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are not sorted in their order from maximum to minimum
    public void runOnNodeToAttributeMatricSorted(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                double[][] matrixArray = matrix.toArray();
                ComputationMatrix mat = new ComputationMatrix(matrixArray);
                
                ComputationMatrix[] components = this.computePCsSorted(mat, PCA_NODE_ATTRIBUTE);
                
                if(context.pcaPlot)
                    ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));
                
    }
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are sorted in increasing order
    public void runOnNodeToAttributeMatric(){
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                double[][] matrixArray = matrix.toArray();
                
                ComputationMatrix mat = new ComputationMatrix(matrixArray);
                
                ComputationMatrix[] components = this.computePCs(mat, PCA_NODE_ATTRIBUTE);
                
                if(context.pcaResultPanel)
                    ResultPanelPCA.createAndShowGui(components, matrix.getNodes(), network, networkView, computeVariance(mat));
                
                if(context.pcaPlot)
                    ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));
                
    }

    public void runOnEdgeValues(){
                DistanceMatrix disMatrix = context.edgeAttributeHandler.getMatrix();
                distanceMatrix = disMatrix.getDistanceMatrix().toArray();
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                mat.writeMatrix("output.txt");
    }

    public ComputationMatrix[] computePCs(ComputationMatrix matrix, int type){
                matrix.writeMatrix("output.txt");
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
                    if(type == PCA_NODE_NODE)
                        components[k] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));
                    else if(type == PCA_NODE_ATTRIBUTE)
                        components[k] = ComputationMatrix.multiplyMatrixWithArray(mat, w);
                    
                    System.out.println("PC: " + k);
                    components[k].printMatrix();
                }
                return components;
    }
    
    public ComputationMatrix[] computePCsSorted(ComputationMatrix matrix, int type){
                matrix.writeMatrix("output.txt");
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
                    if(type == PCA_NODE_NODE)
                        components[j] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));
                    else if(type == PCA_NODE_ATTRIBUTE)
                        components[j] = ComputationMatrix.multiplyMatrixWithArray(mat, w);
                    max = value;
                }

                return components;
    }
    
    public double[] computeVariance(ComputationMatrix matrix){
                ComputationMatrix mat = matrix.centralizeColumns();

                ComputationMatrix C = mat.covariance();

                double[] values = C.eigenValues();
                double[] variances = new double[values.length];
                
                double sum = 0;
                for(int i=0;i<values.length;i++)
                    sum += values[i];
                
                for(int i=0,j=values.length-1; j>=0; j--,i++){
                    variances[i] = (double) Math.round((values[j]*100/sum) * 100) / 100;
                }
                return variances;
    }
}

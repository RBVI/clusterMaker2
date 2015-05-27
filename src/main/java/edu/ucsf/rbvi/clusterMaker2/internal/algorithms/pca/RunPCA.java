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
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are not sorted in their order from maximum to minimum
    public ComputationMatrix[] runOnNodeToNodeDistanceMatricSorted(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                mat.writeMatrix("output.txt");
                mat = mat.centralizeColumns();
                
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
                    components[j].printMatrix();
                }

                return components;
    }
    
    // this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
    // are sorted in decreasing order
    public ComputationMatrix[] runOnNodeToNodeDistanceMatric(){        
                Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
                matrix.setUniformWeights();
                distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                mat.writeMatrix("output.txt");
                mat = mat.centralizeColumns();

                ComputationMatrix C = mat.covariance();

                double[] values = C.eigenValues();
                double[][] vectors = C.eigenVectors();

                ComputationMatrix[] components = new ComputationMatrix[values.length];
                for(int j=0;j<values.length;j++){
                    double[] w = new double[vectors.length];
                    for(int i=0;i<vectors.length;i++){
                        w[i] = vectors[i][j];
                    }
                    components[j] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));
                    components[j].printMatrix();
                }

                return components;
    }

    public void runOnEdgeValues(){        
                DistanceMatrix disMatrix = context.edgeAttributeHandler.getMatrix();
                distanceMatrix = disMatrix.getDistanceMatrix().toArray();
                ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
                mat.writeMatrix("output.txt");
    }
}

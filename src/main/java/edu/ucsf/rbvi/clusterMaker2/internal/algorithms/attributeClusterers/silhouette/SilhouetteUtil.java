/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;


import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;


/**
 *
 * @author lucasyao
 *
 * This is the utility class for Silhouette calculation and related functions
 */
public class SilhouetteUtil {
    
    /**
     * This method calculates the silhouette for a given data matrix using a metric as it's string.  The current
     * cluster is provided.
     *
     * @param matrix the data matrix
     * @param metric the distrance metric we're using
     * @param labels the labels for each of clusters
     * @return the resulting silhouette
     */
    public static SilhouetteResult SilhouetteCalculator(CyMatrix matrix, DistanceMetric metric, int[] labels)
    {
        double[][] distanceMatrix = matrix.getDistanceMatrix(metric).toArray();
        return SilhouetteCalculator(distanceMatrix, labels);
    }
    
    /**
     * This method calculates the silhouette for a given matrix and the current cluster labels.
     * @para distancematrix is 2-D double arrays for the pair-wise distances
     * @para labels the labels for each of clusters
     * @return the resulting silhouette
     */
    public static SilhouetteResult SilhouetteCalculator(double[][] distancematrix, int[] labels)
    {
 
        SilhouetteResult silresult = new SilhouetteResult();
        HashMap<Integer, Integer> classlabels = new HashMap<Integer, Integer>();
        int samplenum = labels.length;
    
        // Get the size of each cluster
        for(int i=0; i<samplenum; i++)
        {
            Integer currentlabel = labels[i];
            if(classlabels.containsKey(currentlabel))
            {
                int count = classlabels.get(currentlabel).intValue()+1;
                classlabels.put(currentlabel, Integer.valueOf(count));
            } else {
                classlabels.put(currentlabel, 1);
            }
        }

        // The number of classes (clusters) that we have
        int classnum = classlabels.size();

        // OK, now calculate the silhouete
        for(int i=0;i<samplenum;i++)
        {
            double silhouettevalue=0;
            double a=0;
            double b=0;
            Integer classlabel = labels[i];
            // System.out.println("Row "+i+" is in cluster "+classlabel);
        
            //initializing
            HashMap<Integer, Double> bvalues = new HashMap<Integer, Double>();
        
            //calculate distance by different classes
            for(int j=0;j<samplenum;j++)
            {
                // System.out.println("Distance from "+i+" to "+j+" is "+distancematrix[i][j]);
                if (i == j) continue;
                Integer currentclasslabel = labels[j];
            		// System.out.println("Row "+j+" is in cluster "+currentclasslabel);
                double distancevalue = 0.0;
                if(bvalues.containsKey(currentclasslabel))
                    distancevalue = bvalues.get(currentclasslabel).doubleValue();
                distancevalue = distancevalue + distancematrix[i][j];
                // System.out.println("Cumulative distance from "+i+" to cluster "+currentclasslabel+" is "+distancevalue);
                bvalues.put(currentclasslabel, Double.valueOf(distancevalue));
            }

            //calculate a b and silhouette
            double mindis = Double.MAX_VALUE;
            Integer minlabel = null;
            for(Integer kLabel: bvalues.keySet())
            {
                int count = classlabels.get(kLabel).intValue();
                double value = bvalues.get(kLabel).doubleValue();
                if (kLabel.equals(classlabel))
                    a = value/count;
                else if (value/count < mindis) {
                    mindis = value/count;
                    minlabel = kLabel;
                }
            }
            b = mindis;

            if(a>b) {
                silhouettevalue = (b-a)/a;
            } else  {
                silhouettevalue = (b-a)/b;
            }
						// System.out.println("silhouetteValue for "+i+" = "+silhouettevalue+", a = "+a+", b = "+b);
            
            silresult.addSilhouettevalue(silhouettevalue, minlabel);
            
        }
        return silresult;
    }

    /**
     * This method prints out the silhouette profile for a given result.  In typical silhouette display,
     * the values are organized by cluster with the silhouette values ranked largest to smallest.
     *
     * @param result the result that we're displaying
     * @param labels the clustering
     */
    public static void printSilhouette(SilhouetteResult result, int[] labels) {
        // Divide the indices into clusters
        TreeMap<Integer, SortedSet<Double>> clusters = new TreeMap<Integer, SortedSet<Double>>();
        for (int row = 0; row < labels.length; row++) {
            if (!clusters.containsKey(labels[row]))
                clusters.put(labels[row], new TreeSet<Double>());
            clusters.get(labels[row]).add(result.getSilhouettevalue(row));
        }
        // For each cluster, output the profile
        for (Integer cluster: clusters.keySet()) {
            System.out.println("Cluster #"+cluster);
            for (Double sil: clusters.get(cluster)) {
            	System.out.println("Silhouette "+sil);
            }
				}

    }
    
}

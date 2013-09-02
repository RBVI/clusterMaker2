/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;
import java.util.ArrayList;

/**
 *
 * @author lucasyao
 *
 * This is the data structure for result of silhouette.
 * members: 1. total number of samples 2. silhouette values of each sample 3. the neighboring class of each sample (closest cluster)
 */
public class SilhouetteResult {
    
    int samplenumber;
    ArrayList<Double> silhouetteValues;
    ArrayList<Integer> neighborLabels;
    
    /**
     * constructor of the SilhouetteResult
     */
    public SilhouetteResult()
    {
        samplenumber = 0;
        silhouetteValues = new ArrayList<Double>();
        neighborLabels = new ArrayList<Integer>();
    }

    /**
     * add a value in the silhouette list
		 * @param value is the silhouette value
		 * @param the nearest cluster of the sample
     */
    public void addSilhouettevalue(double value, Integer label)
    {
        samplenumber++;
        silhouetteValues.add(value);
        neighborLabels.add(label);
    }

    /**
     * delete a silhouette at a given index
		 * @param index the position of the sample you want to delete (0~ size-1)
     */
    public void deleteSilhouettevalue(int index)
    {
        samplenumber--;
        silhouetteValues.remove(index);
        neighborLabels.remove(index);
    }

    /**
     * get the value for the silhouette at a given index
		 * @param index the position of the sample you want to delete (0~ size-1)
		 * return the value for the silhouette at
     */
    public double getSilhouettevalue(int index)
    {
        
        return silhouetteValues.get(index).doubleValue();
    }

    /**
     * This function is to get the neighbor cluster of current sample (the given index)
		 * @param index the position of the sample you want to delete (0~ size-1)
		 * return the neighbor cluster of current sample
     */
    public Integer getSilhouetteneighborlabel(int index)
    {
        return neighborLabels.get(index);
    }

    /**
     * Return the average silhouette for this clustering.
     *
     * @return the average silhouette
     */
    public double getAverageSilhouette()
    {
				// System.out.println("Have "+silhouetteValues.size()+" values");
        double avgS = 0;
        for (Double v: silhouetteValues) {
            avgS = avgS+v.doubleValue();
        }
				// System.out.println("avgS = "+avgS+" average = "+avgS/(double)silhouetteValues.size());
        return avgS/(double)silhouetteValues.size();
    }
    
}

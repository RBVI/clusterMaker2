/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay;

/**
 *
 * @author Gang Su
 */
public interface GAlgorithm {

    public abstract double getModularity();
    public int[] getMembership();

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct;

/**
 * store benchmarking data and running time for a given autosome run
 * @author Aaron
 */


import java.util.*;

public class autosomeRun {

    
    private bm ensemble;
    private ArrayList single = new ArrayList();
    
    
    public void setEnsemble(double Fmeasure, double Prec, double Rec, double NMI,String settings, String file){
        ensemble  = new bm(Fmeasure,Prec,Rec,NMI,settings,file);
    }
    
    public void addSingle(double Fmeasure, double Prec, double Rec, double NMI,String settings, String file){
        bm singleton = new bm(Fmeasure,Prec,Rec,NMI,settings,file);
        single.add(singleton);
    }
    
    public bm getEnsemble() {return ensemble;}
    public ArrayList getAllSingles() {return single;}
    
    
}

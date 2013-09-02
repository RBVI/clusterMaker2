package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct;

/*
***********************************************************************************************
SOFTWARE USE AGREEMENT

Conditions of Use:
AutoSOME is freely available to the academic/non-profit community for non-commercial research
purposes.

All downloads are subject to the following terms: Software and source code Copyright (C) 2009
Aaron M. Newman. Permission to use this software and its documentation is hereby granted to
all academic and not-for-profit institutions for non-profit/non-commercial applications
without fee. The right to use this software for profit, by private companies or other organizations, or in
conjunction with for profit activities, are NOT granted except by prior arrangement and written
 consent of the copyright holder.

For these purposes, downloads of the software constitutes "use" and downloads of this software
 by for profit organizations and/or distribution to for profit institutions is explicitly
prohibited without the prior consent of the copyright holder.

The software is provided "AS-IS" and without warranty of any kind, express, implied or
otherwise. In no event shall the copyright holder be liable for any damages of any kind
arising out of or in connection with the use or performance of this software. This code was
written using Java and may be subject to certain additional restrictions as a result.
***********************************************************************************************
 */





import java.util.*;
import java.io.Serializable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;
/**
 * store all cluster properties for a given cluster run
 * @author Aaron
 */
public class clusterRun implements Serializable{
    
    public Point[] nodes; //nodes from mapping
    public double[][] edges; //edges of clusters
    public List<Integer>[] ids; //data ids
    public String[] labelsSorted;
    public double[][] membership;
    public double[] memTotal; //sum of fractional membership for each cluster
    public float[][] DEC; //density equalized cartogram
    public double thresh; //p-value threshold for pruning tree
    public cluster[] c; //all clusters for current run
    public boolean[] usedEdges; //which edges were used for current clustering?
    private int size = 0; //number of data points
    private String inputFile = new String(); //directory of input file
    public String[][] fcn_nodes; //stores nodes pf fuzzy cluster network
    public String[][] fcn_edges; //stores edges of fuzzy cluster network
    public int[] columnClusters; //store column clusters

    
    //////////////benchmark metrics//////////
    public double Fmeasure = 0;
    public double Precision = 0;
    public double Recall = 0;
    public double NMI = 0;
    public double adjRand = 0;
    
    public clusterRun(Point[] nodes, double[][] edges,List<Integer>[] ids, float[][] DEC, double thresh,  int size){

        this.nodes = nodes;
        this.edges = edges;
    
        this.ids = ids;
        this.thresh = thresh;
        this.DEC = DEC;
        usedEdges = new boolean[edges.length];
        for(int i = 0; i < usedEdges.length; i++) usedEdges[i] = false;
        this.size = size;
    }
    
    public clusterRun(cluster[] c) {this.c = c;  usedEdges = new boolean[edges.length];}
    
    public clusterRun(Point[] nodes, List<Integer>[] ids, int size){
        this.nodes = nodes;
        this.ids = ids;
        this.size = size;
    }

    public clusterRun() {};
    
    public void setInputFile(String input) {inputFile = input;}
    public String getInputFile() {return inputFile;}
            
    public void makeMembership(Settings s){
        int dataCount = 0;
        for(int i = 0; i < c.length; i++) dataCount += c[i].ids.size();
        membership = new double[dataCount][c.length];
        labelsSorted = new String[dataCount];
        for(int i = 0; i < c.length; i++){
            for(int j = 0; j < c[i].ids.size(); j++){
                int id = c[i].ids.get(j).intValue();
                labelsSorted[id] = s.input[id].toString();
               // System.out.println(s.input[id].getIdentity()+" "+labelsSorted[id]+" "+i);
                membership[id][i] = 1;
            }
        }
    }
    
    public void updateFuzzy(double d){
        for(int i = 0; i < membership.length; i++){
            for(int j = 0; j < membership[i].length; j++){
                membership[i][j] *= d;
            }
        }
    }
    
    public void sumMembership(){
        memTotal = new double[membership[0].length];
        for(int i = 0; i < memTotal.length; i++){
            for(int j = 0; j < membership.length; j++){
                if(membership[j][i] > 0){
                    //System.out.println(Math.round(membership[j][i])+" "+membership[j][i]);
                    memTotal[i]+=(int)membership[j][i];
                }
            }
        }
    }
    
    public void cleanFuzzy(){
        
        for(int i = 0; i < membership.length; i++){
            double max = 0;
            int pos = 0;
            for(int j = 0; j < membership[i].length; j++){
                if(membership[i][j] >= max) {max = membership[i][j]; pos = j;}                
            }
            for(int k = 0; k < membership[i].length; k++) membership[i][k] = 0;
            membership[i][pos] = max;
        }
    }
    
    public void printFuzzy(){
        for(int i = 0; i < membership.length; i++){
            String[] tokens = labelsSorted[i].split(",");
            System.out.print(tokens[0]+"\t");
            for(int j = 0; j < membership[i].length; j++){
                System.out.print(membership[i][j]+"\t");
            }
            System.out.println();
        }
    }
    
    public void setMetrics(double F, double P, double R, double NMI, double adjRand){
        Fmeasure = F;
        Precision = P;
        Recall = R;
        this.NMI = NMI;
        this.adjRand = adjRand;
    }
    
    public void edgeSort(){
        sortEdges[] se = new sortEdges[edges.length];
        for(int i = 0; i < se.length; i++) se[i] = new sortEdges(edges[i]);
        Arrays.sort(se);
        for(int i = 0; i < se.length; i++) edges[i] = se[i].edge;
    }
    
    public int getSize() {return size;}
    
    private class sortEdges implements Comparable{
         double dist;
         double[] edge;
         
         public sortEdges(double[] edge){
             this.edge = edge;
             dist = edge[2];
         }
         
         public int compareTo(Object o){
           double dist2 = ((sortEdges)o).dist;
           return (dist < dist2 ? -1 : (dist == dist2 ? 0 : 1));
       }
    }
}

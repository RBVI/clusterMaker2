package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.som;


/*
 *Node.java
 *
 * Created on February 19, 2007, 6:03 PM
 *
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

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.dataItem;

/**
 * SOM node datastructure
 * @author Aaron
 */
public class Node{
    
    private float[] weights; //node weights
    public float[] pos = new float[2];
    private ArrayList dataItems = new ArrayList(); //dataItems that map to this node
    private double error; //error associated with this node
    private double[][] directionalError = new double[5][5];
    boolean vis = true;
    boolean hiLight = false;
    
    //initialize nodes
    public Node(float[] f){
        weights = new float[f.length];
        for(int j = 0; j < weights.length; j++) weights[j] = f[j];
    }
    
    //randomly initialize nodes within range of min and max values of data column
    public Node(int length, float[] max, float[] min){
        weights = new float[length];
        Random r = new Random();
        for(int i = 0; i < weights.length; i++){
            weights[i] = min[i]+(r.nextFloat()*(max[i]-min[i]));
        }
    }
    
    public int getSize() {return weights.length;}
    public float getWeight(int i) {return weights[i];}
    public void setWeight(float value, int index){weights[index] = value;}
    
    //return euclidean distance between current node and input node
    public double getEuclideanDist(Node input){
        float dist = 0;
        for(int i = 0; i < this.getSize(); i++) 
            dist += Math.pow(this.getWeight(i) - input.getWeight(i), 2);
        return ((double)dist);
    }

    //return cosine distance
    public double getCosineDist(Node input){
        double cosine=0;
        float distA = 0;
        for(int i = 0; i < this.getSize(); i++)
            distA += Math.pow(this.getWeight(i), 2);
        float distB = 0;
        for(int i = 0; i < input.getSize(); i++)
            distB += Math.pow(input.getWeight(i), 2);
        float dot = 0;
        for(int i = 0; i < input.getSize(); i++)
            dot += (this.getWeight(i)*input.getWeight(i));
        cosine = Math.cos((double)(dot/(distA*distB)));
        return cosine;
    }
    
    //return pearson correlation distance between current node and input node
    public double getPearsonDist(Node input){
        float dist = 0;
        float distSqr1 = 0;
        float distSqr2 = 0;
        float meanInput = 0;
        float meanNode = 0;
        for(int i = 0; i < this.getSize(); i++){
            meanInput += input.getWeight(i);
            meanNode += this.getWeight(i);
        }
        meanInput /= this.getSize();
        meanNode /= this.getSize();
        
        for(int i = 0; i < this.getSize(); i++){ 
            //System.out.println(this.getWeight(i)+" "+center.getWeight(i)+" "+input.getWeight(i)+" "+inputCenter[i]);
            dist += (this.getWeight(i) - meanNode)*
                        (input.getWeight(i) - meanInput);
            distSqr1 += Math.pow(this.getWeight(i) - meanNode, 2);
            distSqr2 += Math.pow(input.getWeight(i) - meanInput, 2);
        }
        //System.out.println("**"+((dist/Math.sqrt(distSqr1*distSqr2))));
        return ((double)(1-(dist/Math.sqrt(distSqr1*distSqr2))));
    }
    
     //return uncentered correlation distance between current node and input node
    public double getUnCenteredDist(Node input){
        float dist = 0;
        float distSqr1 = 0;
        float distSqr2 = 0;
        for(int i = 0; i < this.getSize(); i++){ 
            //System.out.println(this.getWeight(i)+" "+center.getWeight(i)+" "+input.getWeight(i)+" "+inputCenter[i]);
            dist += (this.getWeight(i))*
                        (input.getWeight(i));
            distSqr1 += Math.pow(this.getWeight(i), 2);
            distSqr2 += Math.pow(input.getWeight(i), 2);
        }
        //System.out.println("**"+((dist/Math.sqrt(distSqr1*distSqr2))));
        return ((double)(1-(dist/Math.sqrt(distSqr1*distSqr2))));
    }
    
    
    public void addDataItem(int d) {dataItems.add(d);}
    public ArrayList getDataItems() {return dataItems;}  
    
    
    public void setError(double e){error = e;}
    public double getError() {return error;}
    
    public void setDirError(double e, int i, int j) {directionalError[i][j] = e;}
    public double getDirError(int i, int j)  {return directionalError[i][j];}
    public void normalizeDirError(double norm){
        for(int i = 0; i < directionalError.length; i++){
            for(int j = 0; j < directionalError[i].length; j++){
                directionalError[i][j] /= norm;
            }
        }
    }
    
        public void setVis(boolean vis){this.vis = vis;}
        public boolean getVis() {return vis;}
        
        public void setHi(boolean hiLight){this.hiLight = hiLight;}
        public boolean getHi() {return hiLight;}
}

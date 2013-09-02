/*
 * tranform input into distance matrix by comparing all columns with euclidean distance
 *
 * ***********************************************************************************************
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

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.*;
/**
 *
 * @author Aaron
 */
public class makeDistMatrix {

    float[] distSqr;
    
    public dataItem[] getDistMatrix(Settings s){
        
        float[][] dist = new float[s.input[0].getValues().length][s.input[0].getValues().length];

        if(s.EWEIGHT!=null){
            for(int i = 0; i < s.input[0].getValues().length; i++){
                if(s.EWEIGHT[i]==1) continue;
                for(int j = 0; j < s.input.length; j++){
                    s.input[j].getValues()[i] *= s.EWEIGHT[i];
                }
            }
        }

        if(s.dmDist==2) {
            s.setCenter();
        }
       // if(s.dmDist>1) setDistSqr(s);
        
        for(int i = 0; i < dist.length; i++){
            for(int j = i; j < dist.length; j++){
                //if(i==j) {dist[i][j] = 0; continue;}
                dist[i][j] = dist[j][i] = (s.dmDist == 1) ? getEucDist(s.input,i,j)
                                        : (s.dmDist == 2) ? getPearsonDist(s.input, i, j, s)
                                        : getUncenteredDist(s.input,i,j);
            }
        }
        
        s.input = new dataItem[dist.length];
        
        for(int i = 0; i < s.input.length; i++){
            String label = String.valueOf(i+1);
            if(s.columnHeaders!=null) if(s.columnHeaders[i+s.startData] != null) label = s.columnHeaders[i+s.startData].replace(" ","_");
            s.input[i] = new dataItem(dist[i], label);
        }
        
        return s.input;
    }
    
    //retrieve euclidean distance between data points i and j
    private float getEucDist(dataItem[] d, int i, int j){
        float f = 0;
        
        //euclidean distance
        for(int k = 0; k < d.length; k++){
            if(d[k].getValues()[i] == -999999999f || d[k].getValues()[j] == -999999999f) continue;
            f += Math.pow(d[k].getValues()[i] - d[k].getValues()[j],2);
        }
        
        return (float)Math.sqrt(f);
    }
    
    //get pearson correlation between data points i and j
    private float getPearsonDist(dataItem[] d, int i, int j, Settings s){
        float dist = 0;
        float distSqr1 = 0;
        float distSqr2 = 0;
        
        for(int k = 0; k < d.length; k++){
            if(d[k].getValues()[i] == -999999999f || d[k].getValues()[j] == -999999999f) continue;
            dist += (d[k].getValues()[i] - s.center[i])*(d[k].getValues()[j] - s.center[j]);
            distSqr1 += Math.pow(d[k].getValues()[i] - s.center[i],2);
            distSqr2 += Math.pow(d[k].getValues()[j] - s.center[j],2);
        }


        float p = dist / (float)Math.sqrt(distSqr1 * distSqr2);

        if(Float.isNaN(p)) p = 0;

        return p;
    }
    
    //get pearson correlation between data points i and j
    private float getUncenteredDist(dataItem[] d, int i, int j){
        float dist = 0;
        float distSqr1 = 0;
        float distSqr2 = 0;
        
        for(int k = 0; k < d.length; k++){
            if(d[k].getValues()[i] == -999999999f || d[k].getValues()[j] == -999999999f) continue;
            dist += (d[k].getValues()[i])*(d[k].getValues()[j]);
            distSqr1 += Math.pow(d[k].getValues()[i],2);
            distSqr2 += Math.pow(d[k].getValues()[j],2);
        }
        
        float p = dist / (float)Math.sqrt(distSqr1 * distSqr2);

        if(Float.isNaN(p)) p = 0;

        return p;
    }
    
    //store mean of every row of input and return
    private float[] getMean(dataItem[] input){
        float[] mean = new float[input.length];
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[i].getValues().length; j++){
                mean[i] += input[i].getValues()[j];
            }
            mean[i] /= input[i].getValues().length;
        }
        return mean;
    }

    private void setDistSqr(Settings s){
        distSqr = new float[s.input[0].getValues().length];
        for(int i = 0; i < s.input.length; i++){
            for(int j = 0; j < s.input[i].getValues().length; j++){
                if(s.input[i].getValues()[j] == -999999999f) continue;
                if(s.dmDist==2){
                    distSqr[j] += Math.pow(s.input[i].getValues()[j] - s.center[j],2);
                }else{
                    distSqr[j] += Math.pow(s.input[i].getValues()[j],2);
                }
            }
        }

    }
}

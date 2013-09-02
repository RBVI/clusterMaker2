package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.sammonmapping;

/*
 * sammonMapping.java
 * 
 * Created on Feb 7, 2008, 3:10:50 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.*;
import java.text.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.*;


/**
 *
 * @author a_newman
 */
public class sammonMapping {
    
    private Point[] y;
    private float[][] Y;
    private int dim = 3;
    private float conv = 0;
    float c = 0;
    float MF = .3f;
    private javax.swing.JProgressBar jpb;
    private int iters = 100;


    public sammonMapping() {};
    
    public sammonMapping(javax.swing.JProgressBar jpb, int iters, int dim){
        this.jpb = jpb;
        jpb.setValue(0);
        jpb.setStringPainted(true);
        jpb.setString("");
        this.dim = dim;
        this.iters = iters;
    }

    public float[][] run(dataItem[] input){
        DecimalFormat Format = new DecimalFormat("#.###");
        float[][] L = init(input);
        float error = getError(L);
   
      //  System.out.println(">Sammon Mapping\n\tIterations = "+iters+"\n\tInitial Error = "+error);
        //update(L);
       // while(error > conv){
        for(int i = 0; i < iters; i++){ 
            
            update(L);
            
            error = getError(L);
            
            jpb.setValue((int)(100 * ((float)i/iters)));
            jpb.setString(Format.format(error));
            //if(Float.isNaN(error)) System.out.println(error);
        } 
              
       // System.out.println("\tFinal Error = "+error);
       /* for(int j = 0; j < y.length; j++){
            System.out.print(TRs[j]+"\t");
            for(int k = 0; k < dim; k++) System.out.print(y[j].coors[k]+"\t");
            System.out.print("\n");
        }*/
       // System.out.println("Pearson correlation = "+(new trDistMatrix().corr(L, getDist()))+"\n\n");
        
        float[][] MDS = new float[y.length*4][3];
        for(int i = 0, p = 0; i < MDS.length; i+=4){
            for(int k = 0; k < 4; k++)
                for(int j = 0; j < y[p].getPoint().length; j++){
                    MDS[i+k][j] = (float)y[p].getPoint()[j];
                // System.out.print(MDS[i][j]+" ");
                }// System.out.println();
                p++;
        }
        
        return MDS;
    }
    
    private float[][] init(dataItem[] input){
        
        float[][] L = makeDistMatrix(input);
        
        y = new Point[L.length];
        float max = getMax(L);
        Random r = new Random();
        for(int i = 0; i < y.length; i++) {
            float[] coors = new float[dim];
            for(int j = 0; j < coors.length; j++)
                coors[j] = r.nextInt((int)max);
            y[i] = new Point(coors);
        }
        c = getSum(L);
        
        return L;
    }
    
    
    private float[][] makeDistMatrix(dataItem[] input){
        float[][] L = new float[input.length][input.length];
        
        for(int i = 0; i < L.length; i++){
            for(int j = i; j < L.length; j++){
                if(i==j) continue;
                L[i][j] = L[j][i] = getDist(input[i].getValues(), input[j].getValues());
            }
        }
        
        
        return L;
    }
    
    private float getDist(float[] a, float[] b){
        float dist = 0;
        
        for(int i = 0; i < a.length; i++){
            dist += Math.pow(a[i]-b[i], 2);
        }
        
        return (float)Math.sqrt(dist);
    }
    
    
    private float getMax(float[][] L){
        float max = 0;
        
        for(int i = 0; i < L.length; i++){
            for(int j = i; j < L[i].length; j++){
                if(L[i][j] > max) max = L[i][j];
            }
        }
        
        return max;
    }
    
    private float getError(float[][] L){
        
        float error = 0;
        
        Y = getDist();
        
        float errorPart = 0;
        for(int i = 0; i < L.length - 1; i++){
            for(int j = i + 1; j < L.length; j++){
                //System.out.println(errorPart+" "+L[i][j]+" "+Y[i][j]+" "+i+" "+j);
                if(L[i][j] == 0) L[i][j] = L[j][i] = 1;
                errorPart += (Math.pow(L[i][j] - Y[i][j], 2) / L[i][j]);
            }                   
        }

        error = (1/c) * errorPart;
        
        return error;
    }
    
    
    private float[][] getDist(){
        
        float[][] dist = new float[y.length][y.length];
        
        for(int i = 0; i < y.length - 1; i++){
            for(int j = i + 1; j < y.length; j++){
                dist[i][j] = dist[j][i] = euclidean(y[i], y[j]);
               // if(Float.isNaN(dist[i][j])) System.out.println(dist[i][j]+" "+y[i].getPoint()[0]+" "+y[i].getPoint()[1]+" "+y[j].getPoint()[0]+" "+y[j].getPoint()[1]);
            }
        }
        
        return dist;        
    }
    
    private float euclidean(Point pt1, Point pt2){
        float dist = 0;
        for(int i = 0; i < pt1.getPoint().length; i++){
            dist += Math.pow(pt1.getPoint()[i] - pt2.getPoint()[i] , 2);
        }
        return (float)Math.sqrt(dist);
    }
    
    private float getSum(float[][] dist){
        float sum = 0;
        for(int i = 0; i < dist.length - 1; i++){
            for(int j = i + 1; j < dist.length; j++){
                sum += dist[i][j];
            }                   
        }
        return sum;
    }
    
    
    private void update(float[][] L){
        
        for(int p = 0; p < y.length; p++){
            for(int q = 0; q < y[p].getPoint().length; q++){
                
                float partDer1 = getDer1(p, q, L);
                float partDer2 = getDer2(p, q, L);
                //System.out.println(partDer1+" "+partDer2);
                if(partDer1 == 0 || Float.isNaN(partDer1) || Float.isInfinite(partDer1)) partDer1 = 1e-6f;
                if(partDer2 == 0 || Float.isNaN(partDer2) || Float.isInfinite(partDer2)) partDer2 = 1e-6f;
                float change = partDer1 / partDer2;
              //  if(Float.isInfinite(change)) change = 0;
                
                y[p].getPoint()[q] -= (MF * change); 
                if(Double.isNaN(y[p].getPoint()[q])) System.out.println("&&&"+y[p].getPoint()[q]+" "+change+" "+partDer1+" "+partDer2);
            }
        }
        
    }
    
    
    private float getDer1(int p, int q, float[][] L){
        
        float der1 = 0;
        
        float sum = 0;
        for(int j = 0; j < L.length; j++){
            if(j == p) continue;
            float x = (L[p][j] - Y[p][j]) / (L[p][j] * Y[p][j]);
            //System.out.println("x "+x+" "+L[p][j]+" "+Y[p][j]);
            sum += x * (y[p].getPoint()[q] - y[j].getPoint()[q]);
        }

        der1 = (-2 / c) * sum;
        
        //System.out.println(der1+" "+sum+" "+c);
        
        return der1;
    }
    
    
    private float getDer2(int p, int q, float[][] L){
        
        float der2 = 0;
        
        float sum = 0;
        for(int j = 0; j < L.length; j++){
            if(j == p) continue;
            float x = (float)(Math.pow(y[p].getPoint()[q] - y[j].getPoint()[q], 2) / Y[p][j]) * 
                       (1 + ((L[p][j] - Y[p][j])/Y[p][j]));
            float z = (L[p][j] - Y[p][j]) - x;
            x = (1 / (L[p][j] * Y[p][j])) * z;
            sum += x;
        }
        
        der2 = (-2 / c) * sum;
        
        return der2;
    }
    

    
}

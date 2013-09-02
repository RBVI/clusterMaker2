package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct;

/*
 * clusterValidity.java
 * 
 * Created on Apr 22, 2008, 12:54:08 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */




import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;
/**
 *
 * @author a_newman
 */
public class clusterValidity {
    
    ArrayList[] orig;
    ArrayList[] clusters;
    List<Integer>[] clustData;
    Settings s;
    
    public clusterValidity(ArrayList[] orig, ArrayList[] clusters,List<Integer>[] clustData, Settings s){
        this.orig = orig;
        this.clusters  = clusters;
        this.clustData = clustData;
        this.s = s;
    }
    
    public clusterValidity(ArrayList[] orig, cluster[] c, Settings s){
        this.orig = orig;
        clusters = new ArrayList[c.length];
        clustData = new ArrayList[c.length];
        this.s = s;
        for(int i = 0; i < c.length; i++){
            clusters[i] = new ArrayList();
            clustData[i] = new ArrayList<Integer>();
            clustData[i] = c[i].ids;
            for(int j = 0; j < c[i].ids.size(); j++){
                double[] d = new double[1];
                String[] tokens = s.input[Integer.valueOf(c[i].ids.get(j).toString())].getIdentity().split(",");
                d[0] = Integer.valueOf(tokens[0]);
                clusters[i].add(d);
            }
        }
    }
    
    public double[] Fmeasure(){
        double F = 0;
        int n = 0;
        
        int[][] hits = new int[clusters.length][orig.length];
        
        for(int i = 0; i < clusters.length; i++){
            for(int j = 0; j < clusters[i].size(); j++){
                n++;
                double[] id = (double[]) clusters[i].get(j);
                hits[i][(int)id[0]-1]++;
            }
        }
        
        double avePrec = 0;
        double aveRec = 0;
        //System.out.println("n "+n);
        for(int i = 0; i < orig.length; i++){
            double Fj = 0;
            double Fp = 0;
            double Fr = 0;
            for(int j = 0; j < clusters.length; j++){
                if(clusters[j].size() == 0) continue;
                double prec = (double)hits[j][i]/(double)clusters[j].size();
                double rec = (double)hits[j][i]/(double)orig[i].size();
                double temp = (2 * prec * rec) / (prec + rec);
                //System.out.println(i+" "+j+" "+prec+" "+hits[j][i]+" "+((double)hits[j][i]/n)+" "+clusters[j].size());
                if(temp > Fj) {
                    Fj = temp;
                    Fp = prec;
                    Fr = rec;
                }

            }

            F += ((double)orig[i].size() / n) * Fj;
            avePrec += ((double)orig[i].size() / n) * Fp;
            aveRec += ((double)orig[i].size() / n) * Fr;
        }
        //System.out.println("F:\t"+F+"\tPrec:"+avePrec+"\tRecall:"+aveRec);
        double[] all = new double[]{F,avePrec,aveRec};
        return all;
    }
    
    //normalized mutual information /taken from 'Automatic Cluster Detection In Kohonen's SOM' IEEE 2008
    public double NMI(){
        double NMI = 0;
        
        int n = 0;
        
        int[][] hits = new int[clusters.length][orig.length];
        
        for(int i = 0; i < clusters.length; i++){
            for(int j = 0; j < clusters[i].size(); j++){
                n++;
                double[] id = (double[]) clusters[i].get(j);
                hits[i][(int)id[0]-1]++;
            }
        }
        double numerator = 0;
        double denominator1 = 0, denominator2 = 0;
        for(int k = 0; k < clusters.length; k++){
            if(clusters[k].size() == 0) continue;
            for(int g = 0; g < orig.length; g++){
                double nlh = hits[k][g];          
                double nstarh = orig[g].size();
                double nlstar = clusters[k].size();
                //System.out.println(k+" "+g+" "+nlh+" "+n+" "+nstarh+" "+nlstar);
                double add = (nlh*(Math.log((nlh*n)/(nstarh*nlstar))/Math.log(2)));
                if(Double.isNaN(add)) add = 0;
                numerator += add;
            }
        }
        for(int g = 0; g < orig.length; g++){
                double nstarh = orig[g].size();
                denominator1 += (nstarh*(Math.log(nstarh/n)/Math.log(2)));
        }
        for(int k = 0; k < clusters.length; k++){
             if(clusters[k].size() == 0) continue;
             double nlstar = clusters[k].size();
             denominator2 += (nlstar*(Math.log(nlstar/n)/Math.log(2)));
        }
       // System.out.println(numerator+"  *"+denominator1+" "+denominator2);
        NMI = numerator / (Math.sqrt(denominator1*denominator2));
        
       // System.out.println("NMI:\t"+NMI);
        
        return NMI;
    }
    
    
     public double adjRand(){
        double adjRand = 0;
        int n = 0;
        
        int[][] hits = new int[clusters.length][orig.length];
        
        for(int i = 0; i < clusters.length; i++){
            for(int j = 0; j < clusters[i].size(); j++){
                n++;
                double[] id = (double[]) clusters[i].get(j);
                hits[i][(int)id[0]-1]++;
            }
        }
        int nijF = 0;
        int niF = 0;
        int njF = 0;

        double nF = Math.pow(factorial(n) / 2,-1);
        
        for(int i = 0; i < clusters.length; i++){
            niF += factorial(clusters[i].size()) / 2 ;
        }
        for(int j = 0; j < orig.length; j++){
            njF += factorial(orig[j].size()) / 2;
        }
        for(int i = 0; i < clusters.length; i++){
            for(int j = 0; j < orig.length; j++){
                nijF += factorial(hits[i][j]) / 2;
            }
        }
        
        adjRand = (nijF-(niF*njF*nF))/((.5*(niF+njF))-(niF*njF*nF));
        
        return adjRand;
     }
    
   
    public int factorial( int n )
    {
        int f = n;
        f *= f-1;
        return f;
    }
    
    public double DunnIndex(){
        double Dunn = Double.MAX_VALUE;
        
        double[][] centroid = new double[clusters.length][3];
        
        for(int i = 0; i < clusters.length; i++){
            double maxR = 0;
            for(int j = 0; j < clusters[i].size()-1; j++){
                for(int k = j+1; k < clusters[i].size(); k++){
                    double[] coor1 = (double[])clusters[i].get(j);
                    double[] coor2 = (double[])clusters[i].get(k);
                    double dist = Euc(coor1, coor2, true);
                    if(dist > maxR) maxR = dist;
                }
            }
            double x = 0, y = 0, z = 0;
            for(int w = 0; w < clusters[i].size(); w++){
                double[] coor = (double[])clusters[i].get(w);
                x += coor[1];
                y += coor[2];
                z += coor[3];
            }
            centroid[i][0] = x/(double)clusters[i].size();
            centroid[i][1] = y/(double)clusters[i].size();
            centroid[i][2] = z/(double)clusters[i].size();
        }
        
        for(int i = 0; i < clusters.length - 1; i++){
            for(int j = i+1; j < clusters.length; j++){                
                double temp = Euc(centroid[i], centroid[j], false);
                if(temp < Dunn) Dunn = temp;
            }
        }
        
        return Dunn;
    }
    
    
    
    private double Euc(double[] coor1, double[] coor2, boolean One){
        double dist = 0;
        
        for(int i = ((One) ? 1 : 0); i < coor1.length; i++){
            dist += Math.pow(coor1[i] - coor2[i], 2);
        }
        
        return Math.sqrt(dist);
    }
    
    public double ICV(){
        double icv = 0;
        
        for(int i = 0; i < clustData.length; i++){
          if(clustData[i].size() == 0) continue;

          StringTokenizer st = new StringTokenizer(clustData[i].get(0).toString(),",");
          double[] cen = new double[st.countTokens()-1];
          st.nextToken();
          int itor = 0;
          while(st.hasMoreTokens()) cen[itor++] = Double.valueOf(st.nextToken());
          for(int j = 1; j < clustData[i].size(); j++){
             // System.out.println(clustData[i].get(j).toString());
              st = new StringTokenizer(clustData[i].get(j).toString(),",");
              st.nextToken();
              itor = 0;
              while(st.hasMoreTokens()) cen[itor++] += Double.valueOf(st.nextToken());
          }
          for(int k = 0; k < cen.length; k++) {
              cen[k] /= (double)clustData[i].size();
             // System.out.print(cen[k]+" ");
          }
          
          for(int j = 0; j < clustData[i].size(); j++){
             // System.out.println(clustData[i].get(j).toString());
              st = new StringTokenizer(clustData[i].get(j).toString(),",");
              st.nextToken();
              itor = 0;
              while(st.hasMoreTokens()) icv += Math.pow(cen[itor++] - Double.valueOf(st.nextToken()), 2);
          }
          
        }
        
        return Math.sqrt(icv);
    }
    
}

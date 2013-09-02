package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.clustering;

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
import java.io.*;

import org.cytoscape.work.TaskMonitor;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.*;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;
/**
 *
 * @author Aaron
 */
public class Ensemble {
    
    private clusterRun[] cr;
    private boolean general;  
    private int clustNum = 0;
    private boolean equalizeNum = true;
    private boolean hierarchical = true;
    private Settings s;
    private boolean[] merged;
    private int[] reLabel;
    private int ave = 0; //average cluster number across all runs
    
    private double[][] consensusMatrix;
    private boolean[][] added;
    private boolean printConsMatrix = false;
    private TaskMonitor monitor;
   

    
    public Ensemble(List<clusterRun> clusterRuns, boolean general, int clustNum, boolean equalizeNum, Settings s, TaskMonitor monitor){
        this.general = general;
        this.monitor=monitor;

        monitor.setStatusMessage("Computing cluster number");
        if(!s.writeTemp){
            cr = new clusterRun[clusterRuns.size()];
            for(int i = 0; i < cr.length; i++) cr[i] = (clusterRun) clusterRuns.get(i);
        }else cr = new clusterRun[1];
        this.clustNum = clustNum;
        this.equalizeNum = equalizeNum;
        this.s = s;
        printConsMatrix = s.printConsMatrix;
    }
    
    public clusterRun run(){

      //  if(!s.batch) System.out.println("...ensemble merging\n\n          |100%");
        monitor.setStatusMessage("Performing ensemble averaging");
        if(equalizeNum) {                
              getAverage();
              if(!s.writeTemp) equalizeClusterNumber(ave);
            //ave = s.known_clusters;           
            //if(hierarchical) runAgg(ave);

        }        
        return combineRuns();
    }
    
    //find average cluster number
    private void getAverage(){

        int min = Integer.MAX_VALUE;



        try{

            int runs = 0;
                   
            File[] f = new File[1];
            
            if(s.writeTemp) {
                f = new File(s.outputDirectory+s.getFolderDivider()+s.getName()+"_temp").listFiles();
                runs = f.length;

            }else runs = cr.length;
            
       
            for(int i = 0; i < runs; i++){
                
                clusterRun cRun = new clusterRun();
                
                if(s.writeTemp){
                    FileInputStream fis = new FileInputStream(f[i]);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    cRun = (clusterRun) ois.readObject();
                }else cRun = cr[i];
             
                if(min > cRun.nodes.length){
                    min = cRun.nodes.length;
                }
            
                getClusters gc = new getClusters(cRun,s);
                gc.findClusters(general);
                //  gc.getClusterValidity();
                cRun.c = gc.getClust();

                if(s.writeTemp && i==0) cr[0]=cRun;
               // System.out.println(cRun.c.length);
                ave += cRun.c.length;           
            
            }
            ave /= runs;
           
            if(clustNum > 0) ave = clustNum; //combine ensemble runs

            if(ave > min) ave = min;
        
        }catch(Exception err){System.err.println(err);};

    }
    
    //using MST approach, equalize cluster number in all cluster runs using average cluster number
    private void equalizeClusterNumber(int ave){
     
            int runs = cr.length;
        
            for(int i = 0; i < runs; i++){
                
            
                clusterRun cRun = cr[i];
                
                cRun = cr[i];
            
                cRun = DoEqualize(cRun);
                
            }

    }
    
    private clusterRun DoEqualize(clusterRun cRun){
        
                if(s.writeTemp){
                    getClusters gc = new getClusters(cRun,s);
                    gc.findClusters(general);
                    //gc.printClusters(j+"");
                    cRun.c = gc.getClust();
                }
        
                boolean[] validEdges = new boolean[cRun.edges.length];

                for(int k = 0; k < validEdges.length; k++) validEdges[k] = true;
                cRun.edgeSort();
                int index = 0;
                for(; index < cRun.edges.length; index++){
                    if(cRun.edges[index][2] > cRun.thresh * cRun.edges[cRun.edges.length-1][2]) break;
                }
                index += (cRun.c.length - ave);
                //System.out.println(index+" "+cr[j].c.length+" "+ave);
                for(; index < validEdges.length; index++) validEdges[index] = false;
                
                getClusters gc = new getClusters(cRun,validEdges,s);
                gc.findClusters(general);
                //gc.printClusters(j+"");
                cRun.c = gc.getClust();
                Arrays.sort(cRun.c);
                return cRun;
    }
    
    //create fuzzy clustering of all cluster runs
    private clusterRun combineRuns(){
       
        if(s.distMatrix) {
            System.out.println("initialize");
            if(s.writeTemp) cr[0] = DoEqualize(cr[0]);
            consensusMatrix = new double[s.input.length][s.input.length];
            added = new boolean[consensusMatrix.length][consensusMatrix.length];
            for(int w = 0; w < added.length; w++)
                  for(int y = 0; y < added.length; y++)
                        added[w][y] = false;
            for(int k = 0; k < cr[0].c.length; k++){          
                for(int i = 0; i < cr[0].c[k].ids.size(); i++){   
                    for(int j = 0; j < cr[0].c[k].ids.size(); j++){
                        int a = Integer.valueOf(cr[0].c[k].ids.get(i).toString());
                        int b = Integer.valueOf(cr[0].c[k].ids.get(j).toString());
                        if(added[a][b]) continue;
                        consensusMatrix[a][b]++;
                        added[a][b] = true;
                    }  
                }               
            }
            for(int w = 0; w < added.length; w++)
                  for(int y = 0; y < added.length; y++)
                        added[w][y] = false;
        }
       // long t = System.currentTimeMillis();
        int progressCount = 0;
              
        int runCount = 0;
        
        clusterRun mega = new clusterRun();
        
        try{
        
            int runs = 0;
            
            File[] f = new File[1];
 
            if(s.writeTemp) {
                f = new File(s.outputDirectory+s.getFolderDivider()+s.getName()+"_temp").listFiles();
                runs = f.length;
            }else runs = cr.length;
    
            for(int i = 0; i < runs; i++){
                         
                clusterRun cRun = new clusterRun();

                if(s.writeTemp){
                    FileInputStream fis = new FileInputStream(f[i]);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    cRun = (clusterRun) ois.readObject();
                    cRun = DoEqualize(cRun);                     
                }else cRun = cr[i];

                
                if(i==0){
                     mega = cRun;
                     mega.makeMembership(s); //create fuzzy cluster membership matrix
                     continue;        
                }
                
                merged = new boolean[cRun.c.length];
                for(int y = 0; y < merged.length; y++) merged[y] = false;
                reLabel = new int[cRun.c.length];
                mega.sumMembership();
                
                for(int k = 0; k < cRun.c.length; k++){
                    double commonPerc = 0;
                    int pos = 0;
                    for(int q = 0; q < mega.c.length; q++){
                        if(merged[q]) continue;
                        double comm = getCommonPerc(cRun.c[k], mega, q);
                        if(comm >= commonPerc) {commonPerc = comm; pos = q;}
                    }
                    
                    reLabel[k] = pos;
                    merged[pos] = true;  
                                     
                }
              
                mega = updateFuzzyClusters(mega, reLabel, i, cRun);
                monitor.setProgress(((double)(i+1)/s.ensemble_runs));
                if(!s.batch) if(Math.floor((runCount++) %((double)(runs)/10)) == 0) {
                   // System.out.print("*");
                    progressCount++;
                }
                
                if(s.distMatrix){
                    for(int w = 0; w < added.length; w++)
                        for(int y = 0; y < added.length; y++)
                            added[w][y] = false;
                    }         
            }
         }catch(IOException err){System.err.println(err);
         }catch(ClassNotFoundException err2){System.err.println(err2);};
            int currVal = 0;//jpb.jProgressBar1.getValue();
            
            if(progressCount < 10 && !s.batch){
                for(; progressCount < 10; progressCount++){
                   // System.out.print("*");
                }
            }

          //  if(!s.batch) System.out.println("\n");
            mega = fuzzyCluster(mega);                   

            getClusters gc = new getClusters(mega,s);
        
            mega.c = gc.getClust();
            
           // System.out.println(mega.c.length+"\t"+(System.currentTimeMillis()-t));
          // if(printConsMatrix){
            if(s.distMatrix)  printConsensusMatrix(mega);
          // }
            
            return mega;

    }
    
 
    
    //return percent of common cluster membership between cluster p and cluster q in combined cluster mega
    private double getCommonPerc(cluster p, clusterRun mega, int q){
        double common = 0;
        double countClustQ = mega.memTotal[q];

        for(int i = 0; i < p.ids.size(); i++){
   
              double weight = mega.membership[Integer.valueOf(p.ids.get(i).toString())][q];
              if(weight > 0) common+=(1+weight);   

        }
        
        if(s.distMatrix){
            for(int i = 0; i < p.ids.size(); i++){   
                for(int j = 0; j < p.ids.size(); j++){
                    int a = p.ids.get(i).intValue();
                    int b = p.ids.get(j).intValue();
                    if(added[a][b]) continue;
                    consensusMatrix[a][b]++;
                    added[a][b] = true;
                }  
            }
        }

        return common/(p.ids.size()+countClustQ);
    }
    
    //update combined cluster 'mega' with current cluster j
    private clusterRun updateFuzzyClusters(clusterRun mega, int[] reLabel, int j, clusterRun cRun){
        
        mega.updateFuzzy((double)j/(j+1));
   
        for(int i = 0; i < cRun.c.length; i++){
            int index = reLabel[i];
            for(int k = 0; k < cRun.c[i].ids.size(); k++){
                int id = cRun.c[i].ids.get(k).intValue();
                mega.membership[id][index] += (double)1/(j+1);
            }
        }

        return mega;
    }
    
   
    //create final clustering: resolve fuzzy cluster
    private clusterRun fuzzyCluster(clusterRun mega){
//        cytoscapeOut(mega);
       // mega = removeOutliers(mega);
        mega.cleanFuzzy();
       // mega.printFuzzy();
        for(int i = 0; i < mega.c.length; i++){
            List<Integer> ids = new ArrayList<Integer>();
            List<Double> probs = new ArrayList<Double>();
            for(int j = 0; j < mega.membership.length; j++){
                if(mega.membership[j][i] > 0) {
                    //System.out.println(100*mega.membership[j][i]);
                    if(s.confidence) if((int)(100*mega.membership[j][i]) <= s.conf_Thresh) {
                        s.discarded.add(j+","+mega.labelsSorted[j]);
                        continue;
                    }
                    ids.add(j);
                    probs.add(mega.membership[j][i]);
                }
            }

            List<String> labels = new ArrayList<String>();
            List<Integer> confidence = new ArrayList<Integer>();
            for(int q = 0; q < ids.size(); q++){
                //labels.add(mega.labelsSorted[Integer.valueOf(ids.get(q).toString())]);
                confidence.add(((int)(100*probs.get(q).doubleValue())));
            }
            mega.c[i] = new cluster(new ArrayList<double[]>(), labels, ids);
            mega.c[i].setConf(confidence); 
        }
            
        return mega;
    }
    
    
    private void printConsensusMatrix(clusterRun mega){
        
            clusterRun resolve = new clusterRun();
            resolve.membership = new double[mega.membership.length][mega.membership[0].length];
            for(int i = 0; i < resolve.membership.length; i++)
                for(int j = 0; j < resolve.membership[i].length; j++){
                    resolve.membership[i][j] = mega.membership[i][j];
                }
            resolve.cleanFuzzy();

            sortBySize[] sbs = new sortBySize[resolve.membership[0].length];
            for(int j = 0; j < resolve.membership[0].length; j++){
                int size = 0;
                for(int i = 0; i < resolve.membership.length; i++){
                    if(resolve.membership[i][j]>0) size++;
                }
                sbs[j] = new sortBySize(size,j);
            }

            Arrays.sort(sbs);

            Map<Integer,Integer> sorted = new HashMap<Integer,Integer>();
            for(int k = 0; k < sbs.length; k++) {
                sorted.put(sbs[k].id,k+1);
            }

            List<String[]> fcn_v = new ArrayList<String[]>();
            List<String[]> fcn_e = new ArrayList<String[]>();
        
         //try{
           /* DataOutputStream outEdges = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("C:\\Edges.txt")));
            DataOutputStream outNodes = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(s.outputDirectory+s.getFolderDivider()+"AutoSOME_"+s.getName()+"_E"+s.ensemble_runs+"_Pval"+s.mst_pval+"_Nodes.txt")));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(s.outputDirectory+s.getFolderDivider()+"AutoSOME_"+s.getName()+"_E"+s.ensemble_runs+"_Pval"+s.mst_pval+"_Matrix.txt")));
           out.writeBytes("Name\t");
           for(int i = 0; i < consensusMatrix.length; i++){
                if(i < consensusMatrix.length-1) out.writeBytes(new StringTokenizer(mega.labelsSorted[i],",").nextToken()+"\t");
                else out.writeBytes(new StringTokenizer(mega.labelsSorted[i],",").nextToken());
            }*/
            for(int i = 0; i < consensusMatrix.length; i++){
                String labelI = new StringTokenizer(mega.labelsSorted[i],",").nextToken();
                //out.writeBytes("\n"+labelI+"\t");
                for(int j = i+1; j < consensusMatrix.length; j++){
                    String labelJ = new StringTokenizer(mega.labelsSorted[j],",").nextToken();
                    //outEdges.writeBytes(i+"\t"+j+"\t"+consensusMatrix[i][j]+"\t"+cr.length+"\n");
                    consensusMatrix[i][j] /= cr.length;
                    consensusMatrix[i][j] -= .5;
                    /*if(j < consensusMatrix.length-1) out.writeBytes(consensusMatrix[i][j]+"\t");
                    else out.writeBytes(String.valueOf(consensusMatrix[i][j]));*/
                    if(i!=j){
                       // outEdges.writeBytes(labelI+"_"+i+"\t"+labelJ+"_"+j+"\t"+consensusMatrix[i][j]+"\n");
                        String[] edge = new String[]{labelI+"_"+i,labelJ+"_"+j,String.valueOf(consensusMatrix[i][j])};
                        //if(Double.valueOf(edge[2])>0) System.out.println(edge[2]);
                        fcn_e.add(edge);
                    }
                }
              }
            //out.close();
            //outEdges.close();
            mega.fcn_edges = new String[fcn_e.size()][3];
            for(int p = 0; p < mega.fcn_edges.length; p++) mega.fcn_edges[p] = (String[]) fcn_e.get(p);
            
            for(int i = 0; i < resolve.membership[0].length; i++)
                for(int j = 0; j < resolve.membership.length; j++){
                    if(resolve.membership[j][i] > 0){
                           String label = new StringTokenizer(mega.labelsSorted[j],",").nextToken();
                        //   outNodes.writeBytes(label+"_"+j+"\t"+sorted.get(i).toString()+"\t"+label+"\n");
                           String[] vertex = new String[]{label+"_"+j,String.valueOf(i),label};
                           fcn_v.add(vertex);
                    }
                }
          //  outNodes.close();
            mega.fcn_nodes= new String[fcn_v.size()][3];
            for(int p = 0; p < mega.fcn_nodes.length; p++) mega.fcn_nodes[p] = (String[]) fcn_v.get(p);
            
        // }catch(IOException err){};
    }
    
    
    private clusterRun removeOutliers(clusterRun mega){
            clusterRun resolve = new clusterRun();
            resolve.membership = new double[mega.membership.length][mega.membership[0].length];
            for(int i = 0; i < resolve.membership.length; i++)
                for(int j = 0; j < resolve.membership[i].length; j++){
                    resolve.membership[i][j] = mega.membership[i][j];
                }
            resolve.cleanFuzzy();
            for(int i = 0; i < resolve.membership[0].length; i++){
                int count = 0;
                int lastIndex = 0;
                for(int j = 0; j < resolve.membership.length; j++){
                    if(resolve.membership[j][i] > 0 && mega.membership[j][i] <= .25) {
                        lastIndex = j;
                        double highest = 0;
                        int newIndex = 0;
                        resolve.membership[lastIndex][i] = 0;
                        for(int k = 0; k < resolve.membership[0].length; k++){
                        if(resolve.membership[lastIndex][k] > highest && k!=i){
                            highest = resolve.membership[lastIndex][k];
                            newIndex = k;
                        }
                        }
                        mega.membership[lastIndex][newIndex] = 1;  
                    }
                }
              /*  if(count == 1){
                    double highest = 0;
                    int newIndex = 0;
                    resolve.membership[lastIndex][i] = 0;
                    for(int k = 0; k < resolve.membership[0].length; k++){
                        if(resolve.membership[lastIndex][k] > highest && k!=i){
                            highest = resolve.membership[lastIndex][k];
                            newIndex = k;
                        }
                    }
                    mega.membership[lastIndex][newIndex] = 1;                    
                }*/
            }
            return mega;
    }
    
    private void cytoscapeOut(clusterRun mega){
        try{
            clusterRun resolve = new clusterRun();
            resolve.membership = new double[mega.membership.length][mega.membership[0].length];
            for(int i = 0; i < resolve.membership.length; i++)
                for(int j = 0; j < resolve.membership[i].length; j++){
                    resolve.membership[i][j] = mega.membership[i][j];
                }
            resolve.cleanFuzzy();
            
            DataOutputStream fuzzyClust = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(s.outputDirectory+"\\AutoSOME_"+s.getName()+"_Fuzzy_Clusters_Edges_Cytoscape.txt"))); 
            DataOutputStream fuzzyClustNodes = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(s.outputDirectory+"\\AutoSOME_"+s.getName()+"_Fuzzy_Clusters_Nodes_Cytoscape.txt"))); 
            
            int maxSize = 0;
            
            for(int i = 0; i < mega.membership[0].length; i++){
                double sumI = 0;
                    for(int p = 0; p < resolve.membership.length-1; p++){
                        if(resolve.membership[p][i]>0) sumI++;
                    }
                
                    if(sumI > maxSize) maxSize = (int)sumI;
                
                    if(sumI < 2) continue;
                for(int j = i+1; j < mega.membership[0].length; j++){
                    if(i==j) continue;
                    double sumJ = 0;
                    for(int p = 0; p < resolve.membership.length; p++){
                        if(resolve.membership[p][j]>0) sumJ++;
                    }
                   
                    if(sumJ < 2) continue;
                    double sumFracJ = 0;
                    double sumFracI = 0;
                    int countJ = 0;
                    int countI = 0;
                    for(int k = 0; k < mega.membership.length; k++){
                        if(resolve.membership[k][i]>0 && mega.membership[k][j]>0){
                            countJ++;
                            sumFracJ += mega.membership[k][j];
                        }
                        if(resolve.membership[k][j]>0 && mega.membership[k][i]>0){
                            countI++;
                            sumFracI += mega.membership[k][i];
                        }
                    }
                    double sumFrac = ((sumFracI/sumJ)+(sumFracJ/sumI))/2;
                    if(Double.isNaN(sumFrac)) sumFrac = 0;

                    fuzzyClust.writeBytes(i+"\t"+j+"\t"+sumFrac+"\n");
                }
            }
            
            for(int j = 0; j < mega.membership[0].length; j++){
                double sum = 0;
                for(int p = 0; p < resolve.membership.length; p++){
                         if(resolve.membership[p][j]>0) sum ++;
                }
                if(sum==0) continue;
                int single = 0;
                if(sum==1) single = 1;
                if(single==1) continue;
                fuzzyClustNodes.writeBytes(j+"\t"+(sum/maxSize)+"\t"+sum+"\n");
            }
            fuzzyClust.close();
            fuzzyClustNodes.close();
        }catch(IOException err){};
    }
    
  
  private class sortBySize implements Comparable{

      public int size;
      public int id;
      public sortBySize(int size,  int id){
          this.size = size;
          this.id = id;
      }
      public int compareTo(Object o){
           double dist2 = ((sortBySize)o).size;
           return (size > dist2 ? -1 : (size == dist2 ? 0 : 1));
      }
  }
  
    
}

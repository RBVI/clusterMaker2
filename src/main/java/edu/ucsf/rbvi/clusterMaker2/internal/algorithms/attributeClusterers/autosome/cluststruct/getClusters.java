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



import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.Point;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;
/**
 *
 * @author Aaron
 */
public class getClusters {

     private List<Integer> currClust = new ArrayList<Integer>();
     private double[][] edges;
     private Point[] nodes;
     List<Integer>[] ids;
     private double edgesThresh;
     private double maxDist = 0;
     private cluster[] clusters;
     private List<Integer>[] origClusters;
     private boolean noThresh = false;
     private boolean[] validEdge;
     private boolean[] usedEdges;
     private clusterRun cr;
     private Settings s;
      
     public getClusters(clusterRun cr, Settings s){
        this.cr = cr;
        
        this.edges = cr.edges; //all MST edges in node network
        this.nodes = cr.nodes; //all nodes
        this.ids = cr.ids;
        this.clusters = cr.c;
        edgesThresh = cr.thresh; //p-value cutoff
        this.usedEdges = cr.usedEdges;
        this.s = s;
     }
     
     public getClusters(clusterRun cr, boolean[] validEdge, Settings s){
        this.validEdge = validEdge;
        noThresh = true;
        this.edges = cr.edges; //all MST edges in node network
        this.nodes = cr.nodes; //all nodes
        this.ids = cr.ids;
        edgesThresh = 0;
        usedEdges = cr.usedEdges;
        this.s = s;
     }

     
     public void findClusters(boolean general){

        boolean[] used = new boolean[nodes.length]; //used edges for DFS
        for(int i = 0;i < used.length; i++) used[i] = false; 
        List<List<double[]>> clust = new ArrayList<List<double[]>>(); 
        List<List<String>> clustLabels = new ArrayList<List<String>>();
        List<List<Integer>> clustIDs = new ArrayList<List<Integer>>();
        List<List<Integer>> allNodes = new ArrayList<List<Integer>>();
        
        for(int j = 0; j < edges.length; j++){
            if(edges[j][2] > maxDist) maxDist = edges[j][2];
        }

        int max = 0;

            //iterate across all rescaled SOM nodes
            for(int i = 0; i < nodes.length; i++){
                
                //if node i has not been accessed, find all nodes connected to node i
                if(!used[i]) connected(i);
                //if node i has already been added to cluster, continue
                else continue;
                
            
                
                //populate cluster arrays
                List<double[]> allIndices = new ArrayList<double[]>();
                List<String> allLabels = new ArrayList<String>();
                List<Integer> allIDs = new ArrayList<Integer>();
                List<Integer> indices = new ArrayList<Integer>();
                
                for (int index: currClust) {
                    if(!general){ //if TR clusters
                          double[] info = new double[nodes[index].getPoint().length];
                          for(int k = 0; k < info.length; k++)
                            info[k] = nodes[index].getPoint()[k];
                            allIndices.add(info);
                    }else{ //If general numerical clusters
                        for(int h = 0; h < ids[index].size(); h++){
                                double[] info = new double[nodes[index].getPoint().length+1];  
                               /* String lab = new StringTokenizer(labels[index].get(h).toString(),",").nextToken();
                                try{
                                    int ID = Integer.parseInt(lab);
                                    info[0] = ID;
                                }catch(NumberFormatException err) {info[0]=1;}   */
                               // indices.add(index);
                                info[0] = index;
                                for(int k = 1; k < info.length; k++)
                                    info[k] = nodes[index].getPoint()[k-1];

                                int id = ids[index].get(h).intValue();
                                
                                String[] tokens = s.input[id].getDesc().split(",");
                                
                                if(s.benchmark){
                                
                                    int label = Integer.valueOf(tokens[0]);
                                
                                    if(label > max) max = (int)label; //if clusters are known (benchmarking), how many are there?
                      
                                }
                                
                                allIndices.add(info);
                                //allLabels.add(labels[index].get(h).toString());
                                allIDs.add(ids[index].get(h));
                        }
                    }
                    used[index] = true; //node 'index' is now used                    
                }                    
                allNodes.add(indices);
                clust.add(allIndices);  //add node indices to current cluster  
                clustLabels.add(allLabels); //add labels to current cluster
                clustIDs.add(allIDs);
                
                currClust.clear();
            }

            clusters = new cluster[clust.size()];
            
             for(int i = 0; i < clusters.length; i++){
                clusters[i] = new cluster(clust.get(i), clustLabels.get(i),clustIDs.get(i), allNodes.get(i));
            
                if(s.benchmark){
            
                origClusters = (List<Integer>[])Array.newInstance(List.class,max); //if benchmarking, store original clusters

          
                if(general){
                    
                    for(int j = 0; j < clusters[i].labels.size(); j++){
                        String[] tokens = clusters[i].labels.get(j).toString().split(",");
                        int label = Integer.valueOf(tokens[0]);
                        if(origClusters[label-1] == null) origClusters[label-1] = new ArrayList<Integer>();
                        origClusters[label-1].add(label);
                    }
                }
            }
            
            }
            

            currClust.clear();

      
  }
     
     private void connected(int i){
       currClust.add(i);
       DFS(i, -1);
   }
    //depth first search (find all nodes for a given cluster)
   private boolean DFS(int node, int last){
       boolean valid = false;
       for(int i = 0; i < edges.length ; i++){

           if((int) edges[i][0] == node && (int)edges[i][1] == last
                   || (int) edges[i][1] == node && (int)edges[i][0] == last) continue;
           if((int) edges[i][0] == node || (int) edges[i][1] == node){
               if((int) edges[i][0] == node){
                   if(edges[i][2] <= edgesThresh * maxDist || (noThresh && validEdge[i])) {
                       currClust.add((int)edges[i][1]);
                       usedEdges[i] = true;
                       DFS((int)edges[i][1], node);
                   }
               }
               if((int) edges[i][1] == node){
                   if(edges[i][2] <= edgesThresh * maxDist || (noThresh && validEdge[i])) {
                       currClust.add((int)edges[i][0]);
                       usedEdges[i] = true;
                       DFS((int)edges[i][0], node);
                   }
               }
           }
       }
       
       return valid;
   }
   

     
     
      
  public cluster[] getClust() {return clusters;}
   
     
     
}

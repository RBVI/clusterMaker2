package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.som;


import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.Point;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.dataItem;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;

/*
 * SOM.java
 *
 * Created on February 19, 2007, 5:49 PM
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

/**
 * Self-Organizing Map with circular or rectangular topology
 * @author Aaron
 */




public class SOM implements Runnable{
    

    private int iterations = 1000;
    private double learnRate = 0.9;
    private double learningRate = 0;
    private double lambda = 0;
    private double decay = 0;
    private int halfWidth = 0;
    private int maxGridSize = 100;
    private int minGridSize = 30;
    private int gridSize = 30;
    private boolean autoGrid = true;
    private boolean circle = true; //SOM topology; false = square
    private double theta = 1.5; //dampen error surface
    private dataItem[] input; //store input data
    private Node[] trainingData; //input data stored as feature vectors
    private Node[][] map; //SOM
    private Settings s; //user parameters
    private Node center; //mean of each attribute
    private int nodeNum = 0; //number of nodes to be trained
    
    public SOM (Settings s) {
        this.input = s.input;
        this.s = s;
        iterations = s.som_iters;
        maxGridSize = s.som_maxGrid;
        minGridSize = s.som_minGrid;
        gridSize = s.som_gridSize;
        autoGrid = (gridSize > 0) ? false : true;
        circle = s.som_circle;
        theta = s.som_theta;      
    } //instantiate with input data
    
    
    public void run(){

        init();

        doTraining();
        
        doMapping();

        calcError();

    }
    
 //////////////////SOM/////////////////////////////////////////////////////////////////   
 
    
    private void init(){
        trainingData = new Node[input.length];
        if(s.Pearson) center = new Node(input[0].getValues());        
        
        ///store max and min values of each column from input
        float[] max = new float[input[0].getValues().length];
        float[] min = new float[input[0].getValues().length];
        for(int j = 0; j < max.length; j++) {
            max[j] = -Float.MAX_VALUE;
            min[j] = Float.MAX_VALUE;
        }
        for(int i = 0; i < trainingData.length; i++){
            //store data point i
            trainingData[i] = new Node(input[i].getValues());
            //update max, min values
            for(int k = 0; k < trainingData[i].getSize(); k++){
                if(trainingData[i].getWeight(k) > max[k]) max[k] = trainingData[i].getWeight(k);
                if(trainingData[i].getWeight(k) < min[k]) min[k] = trainingData[i].getWeight(k);
            }            
        }

        //randomly initialize SOM
        if(gridSize == 0) gridSize = (int)Math.min(maxGridSize, Math.max(minGridSize, Math.sqrt(trainingData.length*2)));        

        map = new Node[gridSize][gridSize];
        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){
                map[i][j] = new Node(trainingData[0].getSize(),max,min);
                map[i][j].pos[0] = i;
                map[i][j].pos[1] = j;
                //set center weights
                if(s.Pearson) for(int q = 0; q < center.getSize(); q++) center.setWeight(map[i][j].getWeight(q), q);
            }
        }

        halfWidth = gridSize;
        lambda = iterations / Math.log(halfWidth);
        
        
        nodeNum = (int)((s.som_circle) ? Math.PI*Math.pow(map.length/2,2) : Math.pow(map.length,2));
    }

    
    
    public void doTraining(){
 
        Random r = new Random();
        int progress = 0;
       // System.out.println(">Running SOM\n>training:\n                    |100%");
        for(int m = 0; m < 2; m++){
            for(int i = 0; i < iterations; i++){

                int sample = r.nextInt(trainingData.length);

                decay = Math.exp(-i / lambda);     

                int[] minCoord = findBMU(trainingData[sample]);

                double radius = calcRadius();
        
                learningRate = learnRate * decay;

                updateWeights(trainingData[sample], minCoord, radius, i);
               // if(progress++ %((double)(iterations*2)/20) == 0) System.out.print("*");

            }
            if(m == 0) {learnRate = 0.1; halfWidth = gridSize/4;     
            for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){

                map[i][j].pos[0] = i;
                map[i][j].pos[1] = j;
            }
            }
          }
        }
    }
    
    
    private int[] findBMU(Node input){
        
        int[] minCoord = new int[2];
        double minDist = Double.MAX_VALUE;
        
        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){
                if(checkCircle(i, j)) continue;
                double dist = (!s.Pearson && !s.unCentered) ? map[i][j].getEuclideanDist(input)
                               : (s.unCentered) ? map[i][j].getUnCenteredDist(input)
                               : map[i][j].getPearsonDist(input);
                if(dist < minDist) {
                    minDist = dist;
                    minCoord[0] = i;
                    minCoord[1] = j;
                }
            }
        }
        
        return minCoord;
    }

    
    private boolean checkCircle(int i, int j){
        if(!circle) return false;
        if(Math.sqrt(Math.pow(i-map.length/2,2)+Math.pow(j-map[0].length/2,2)) > map.length/2)
            return true;
        //if(i < map.length/10 || i > map.length*.9) return true;
        else return false;
    }
    
   
    private double calcRadius(){
        return (halfWidth * decay);
    }
    
    
    private void updateWeights(Node input, int[] minCoord, double radius, int iteration){
        
          for(int i = 0; i < map.length; i++){
              for(int j = 0; j < map[0].length; j++){
                    if((Math.abs(i-minCoord[0])) > radius || (Math.abs(j-minCoord[1])) > radius) continue;
                    if(checkCircle(i,j)) continue;
                    double dist = Math.sqrt(Math.pow(i-minCoord[0],2)+Math.pow(j-minCoord[1],2));
                    if(dist > radius) continue;
                    for(int q = 0; q < map[i][j].getSize(); q++){
                        float weight = map[i][j].getWeight(q);                        
                        map[i][j].setWeight((float)(weight+(Math.exp(-Math.pow(dist, 2) / (2 * Math.pow(radius, 2))) * learningRate * (input.getWeight(q) - weight))),q);
                         //update mean weights
                        if(s.Pearson) center.setWeight((((center.getWeight(q)*nodeNum)-weight)+map[i][j].getWeight(q))/nodeNum,q);
                    }
                   // System.out.println(map[i][j].pos[0]+" "+map[minCoord[0]][minCoord[1]].pos[0]+" "+(map[minCoord[0]][minCoord[1]].pos[0] - map[i][j].pos[0])+" "+(float)(map[i][j].pos[0]+(Math.exp(-Math.pow(dist, 2) / (2 * Math.pow(radius, 2))) * learningRate * (map[minCoord[0]][minCoord[1]].pos[0] - map[i][j].pos[0]))));
                    map[i][j].pos[0] = (float)(map[i][j].pos[0]+(Math.exp(-Math.pow(dist, 2) / (2 * Math.pow(radius, 2))) * .9 * decay * (map[minCoord[0]][minCoord[1]].pos[0] - map[i][j].pos[0])));                        
                    map[i][j].pos[1] = (float)(map[i][j].pos[1]+(Math.exp(-Math.pow(dist, 2) / (2 * Math.pow(radius, 2))) * .9 * decay * (map[minCoord[0]][minCoord[1]].pos[1] - map[i][j].pos[1])));
                  /*  map[i][j].pos[0] = map[i][j].pos[0]+(float)((.9/(1+iteration))
                            *Math.exp(-.01*(1+(iteration/iterations))*Math.sqrt(Math.pow(map[minCoord[0]][minCoord[1]].pos[0] - map[i][j].pos[0],2)))
                            *Math.exp(-(1+(iteration/iterations))*Math.pow(Math.sqrt(map[i][j].getEuclideanDist(input))-Math.sqrt(map[minCoord[0]][minCoord[1]].getEuclideanDist(input)),2))
                            *(map[minCoord[0]][minCoord[1]].pos[1] - map[i][j].pos[0]));
                    map[i][j].pos[1] = map[i][j].pos[1]+(float)((.9/(1+iteration))
                            *Math.exp(-.01*(1+(iteration/iterations))*Math.sqrt(Math.pow(map[minCoord[0]][minCoord[1]].pos[1] - map[i][j].pos[1],2)))
                            *Math.exp(-(1+(iteration/iterations))*Math.pow(Math.sqrt(map[i][j].getEuclideanDist(input))-Math.sqrt(map[minCoord[0]][minCoord[1]].getEuclideanDist(input)),2))
                            *(map[minCoord[0]][minCoord[1]].pos[1] - map[i][j].pos[1]));*/
                    //System.out.println(map[i][j].pos[0]);
              }
          }
    }
                

    
 
    public void doMapping(){
        for(int i = 0; i < trainingData.length; i++){
            int[] coordinates = findBMU(trainingData[i]);
            map[coordinates[0]][coordinates[1]].addDataItem(i);
            input[i].setPoint(new Point(coordinates));
        }
       /* for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){
                if(checkCircle(i,j)) continue;
                if(map[i][j].getDataItems().size()>0){
                    for(int k = 0; k < map[i][j].getDataItems().size(); k++){
                        System.out.print("\n"+map[i][j].getDataItems().get(k).toString()+" ");
                        for(int h = 0; h < map[i][j].getSize(); h++){
                            System.out.print(map[i][j].getWeight(h)+" ");
                        }
                    }
                }
            }
        }*/
    }
    
    
  
 // compute error surface of map
    public void calcError(){

        double maxDist = 0; //maximum 'sum of neighbor errors' in map : use for normalization
        double maxDirError = 0; //maximum directional error in map : use for normalization 
        ArrayList allDists = new ArrayList();
        
           for(int i = 0; i < map.length; i++)
              for(int j = 0; j < map[i].length; j++){
                
               if(checkCircle(i,j)) continue;

                double dist = 0; //accumulates total error node                
                
                int xIndex = 0, yIndex = 0; //directional error indices
                
                int counter = 0; //count neighboring nodes
                
                //sum up distances of current node compared to immediate neighbors
                
                    for(int k = ((i>0) ? i-1 : i); k < ((i<map.length-1) ? i+2 : i+1); k++){
                        for(int w = ((j>0) ? j-1 : j); w < ((j<map[i].length-1) ? j+2 : j+1); w++){
                       
                            if(k==i && w==j) continue;
                            
                            if(checkCircle(k,w)) continue;   
                            
                            //which node are we comparing to?
                            if(w < j) xIndex = 0;
                            else if(w == j) xIndex = 1;
                            else xIndex = 2;
                            if(k < i) yIndex = 0;
                            else if(k == i) yIndex = 1;
                            else yIndex = 2;                        
                            if(w-1 == j && k == i) xIndex = 2;
                        
                            //set diretional error between node pairs i,j and k,w for indices xIndex, yIndex
                            map[i][j].setDirError(map[i][j].getEuclideanDist(map[k][w]), xIndex, yIndex);
                            //store error accumulated so far
                            //dist += Math.sqrt(Math.pow(map[i][j].pos[0]-map[k][w].pos[0],2)+Math.pow(map[i][j].pos[1]-map[k][w].pos[1],2));//;map[i][j].getDirError(xIndex, yIndex);
                            dist += map[i][j].getDirError(xIndex, yIndex);
                            //store greatest directional error so far
                            if(map[i][j].getDirError(xIndex, yIndex) > maxDirError) 
                                    maxDirError = map[i][j].getDirError(xIndex, yIndex);
                            //how many neighboring nodes have been visited?
                            counter++;
                        }                    
                    }
                
                if(counter == 0) continue;
                //correct distance for nodes not completely surrounded
                //if(counter<8) dist = dist * 8/counter;
                dist /= counter;
                //System.out.println(i+"\t"+j+"\t"+dist);
                for(int a = 0; a < 3; a++)
                    for(int b = 0; b < 3; b++)
                        if(map[i][j].getDirError(a,b) == 0) map[i][j].setDirError(dist,a,b);

               map[i][j].setError(dist); //set distance 
               if(maxDist < dist) maxDist = dist;
               allDists.add(dist);
            }
            
  
        //normalize error surface using maximum distance        
           for(int i = 0; i < map.length; i++){
              for(int j = 0; j < map[i].length; j++){
                  double normDist = (map[i][j].getError() / (maxDist/theta)); //theta is dampening factor  
                  map[i][j].setError(Math.min(1,normDist)); //set 'sum of neighboring nodes' error
                  //if(map[i][j].getDataItems().size() == 0) map[i][j].setError(10);
                  //if(map[i][j].getDataItems().size() > 0) System.out.println(map[i][j].pos[0]+"\t"+map[i][j].pos[1]);
                  map[i][j].normalizeDirError(maxDirError); //normalize directional error
                  //System.out.println(i+"\t"+j+"\t"+Math.pow(normDist,3));
              }    //System.out.println(); 
           }
    }  





   
     public Object[] getDEInfo(double XYerrExp){
         int res = 3;
         List<int[]> ids = new ArrayList<int[]>();
         List<float[][]>[] polygons = new ArrayList[(map.length * map[0].length * res * res)];
         float[] census = new float[polygons.length];
         int count = 0;
        

            for(int i = (res*map.length-1); i >=0; i--){
                for(int j = 0; j < (res*map[0].length); j++){


                    float[][] poly = new float[4][3];
                    poly[0][0] = j;
                    poly[0][1] = i;
                    poly[0][2] = 0;
                    poly[1][0] = j+1;
                    poly[1][1] = i;
                    poly[1][2] = 0;
                    poly[2][0] = j+1;
                    poly[2][1] = (i-1);
                    poly[2][2] = 0;
                    poly[3][0] = j;
                    poly[3][1] = (i-1);
                    poly[3][2] = 0;
                    
                    polygons[count] = new ArrayList<float[][]>();
                    polygons[count++].add(poly);

                }
            }
        

        

            count = 0;

            for(int i = 0; i < map.length; i++){                
                for(int yItor = 0; yItor < res; yItor++){
                    for(int j = 0; j < map[0].length; j++){             
                        for(int k = 0; k < res; k++){
    
                            double err = map[i][j].getError();

                            census[count++] = (float)(255*Math.pow(err, XYerrExp));
                            //System.out.println(i+"\t"+j+"\t"+255*Math.pow(err, XYerrExp));
                    
                    }
                }
            }
        }
           
       
        int itor = 0;

       

        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[0].length; j++){
                if(map[i][j].getDataItems().size()>0){
                    int[] rgb = new int[]{200,200,200};                
                    for(int v = 0; v < map[i][j].getDataItems().size(); v++){
                        int node =  (i*res*res*map.length)+(j*res);                 
                        int lowestX = 1, lowestY = 1;
                        double lowErr = Double.MAX_VALUE;
                        for(int k = 0; k < 3; k++)
                            for(int q = 0; q < 3; q++){
                                if(map[i][j].getDirError(k, q) < lowErr){
                                    lowErr = map[i][j].getDirError(k, q);
                                    lowestX = q;
                                    lowestY = k;
                                }
                            }
                            node += lowestX;
                            node += (lowestY*(map.length*res));
                            ids.add(new int[]{node,Integer.valueOf(map[i][j].getDataItems().get(v).toString())});
                    }
                }
                itor++;
            }
        }       

        Object[] info = new Object[5];
        info[0] = polygons;
        info[1] = census;
        info[2] = ids;
        return info;
     }



     public int getGridSize() {return gridSize;}


     public Node[][] getMap() {return map;}

}

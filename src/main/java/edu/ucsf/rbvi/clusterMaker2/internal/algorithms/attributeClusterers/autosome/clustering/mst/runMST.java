package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.clustering.mst;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */





import java.util.*;
import java.io.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.clustering.mst.*;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.*;

/**
 *
 * @author Aaron
 */
public class runMST {
    
    private Point[] coors;
    private int[] id;
    
    public static void main(String[] args){
        runMST pt = new runMST();
        pt.getInput(args[0]);
        pt.cluster();
    }
    
    private void getInput(String file){
        ArrayList a = new ArrayList();
        try{
            BufferedReader bf = new BufferedReader(new FileReader(file));
            String line = new String();
            while((line = bf.readLine()) != null){
                if(line.length() != 0){
                    a.add(line);
                }
            }
        }catch(IOException err){};
     
      /*  int counter = 0;
        for(int i = 0; i < 60; i++){
            for(int j = 0; j < 100; j++){
                a.add(counter+"\t"+i+"\t"+j);
                        counter++;
            }
        }*/
        coors = new Point[a.size()];
        id = new int[coors.length];
        
        for(int i = 0; i < coors.length; i++){
            StringTokenizer st = new StringTokenizer(a.get(i).toString(),"\t");
            id[i] = Integer.valueOf(st.nextToken());
            double[] data = new double[3];
            data[0] = Double.valueOf(st.nextToken());
            data[1] = Double.valueOf(st.nextToken());
            data[2] = 0;//Double.valueOf(st.nextToken());
            coors[i] = new Point(data);
        }
    }
    
    private void cluster(){
            StringBuffer sb = new StringBuffer();
            sb.append(coors.length+"\n");
            float[][] rescaledPol = new float[coors.length*4][3];
            int itor = 0;
            for(int i = 0; i < coors.length; i++){
                sb.append(id[i]+" "+i+" 100 100 100 "+i+" 0\n");
                for(int j = 0; j < 4; j++) {
                    //System.out.println(coors[i].coors[0]+" "+coors[i].coors[1]+" "+coors[i].coors[2]);
                    rescaledPol[itor][0] = (float)coors[i].getPoint()[0];
                    rescaledPol[itor][1] = (float)coors[i].getPoint()[1];
                    rescaledPol[itor++][2] = 0;//(float)coors[i].coors[2];
                }
            }

            String decTRs = sb.toString();

          // (new MSTCluster(false)).run(new javax.swing.JProgressBar(), rescaledPol, decTRs,.1,10, true, new launch.Settings());
    }
    
}

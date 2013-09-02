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



import java.io.Serializable;
import java.util.*;
/**
 *
 * @author Aaron
 */
  public class cluster implements Comparable, Serializable{
      int size;
      public List<double[]> indices = new ArrayList<double[]>(); //node coordinates
      public List<String> labels = new ArrayList<String>(); //data labels
      public List<Integer> ids = new ArrayList<Integer>(); //label ids
      public List<Integer> nodeIndices = new ArrayList<Integer>(); //original node indices
      public List<Integer> confidence = new ArrayList<Integer>(); //cluster membership confidence
      public List order = new ArrayList(); //store data item order
      public int parentID = -1; //primary cluster identifier
      public int childID = -1; //secondary cluster identifier
      public String finalID = new String(); //string version of final cluster identifier
      
      public cluster(){};
      public cluster(List<double[]> indices, List<String> labels, List<Integer> ids, List<Integer> nodeIndices){
          this.indices = indices;
          this.labels = labels;
          this.ids = ids;
          this.nodeIndices = nodeIndices;
          size = ids.size();
      }
      
      public cluster(List<double[]> indices, List<String> labels, List<Integer> ids){
          this.indices = indices;
          this.labels = labels;
          this.ids = ids;
          size = ids.size();
      }
      
      public void setConf(List<Integer> confidence) {this.confidence = confidence;}
      
      public void setIndices(List<double[]> a) {indices = a;}
      public List<double[]> getIndices() {return indices;}
      public void addData(double[] o) {indices.add(o);}
      public void setSize() {size = ids.size();}
            
      public int compareTo(Object o){
           double size2 = ((cluster)o).size;
           return (size > size2 ? -1 : (size == size2 ? 0 : 1));
       }

  }

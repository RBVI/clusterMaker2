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

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct;


import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.*;
/**
 *
 * @author Aaron
 */
public class sortCluster {

    public cluster sortConf(cluster c, Settings s){
      sortByConf[] sbc = new sortByConf[c.ids.size()];
      for(int i = 0; i < sbc.length; i++) {
          int id = Integer.valueOf(c.ids.get(i).toString());
          sbc[i] = new sortByConf(s.input[id].getIdentity(), id, Integer.valueOf(c.confidence.get(i).toString()));
      }
      Arrays.sort(sbc);
      ArrayList ids = new ArrayList();
      ArrayList conf = new ArrayList();
      for(int i = 0; i < sbc.length; i++){
          ids.add(sbc[i].id);
          conf.add(sbc[i].conf);
      }
      c.ids = ids;
      c.confidence = conf;
      return c;
  }    
   
   private class sortByConf implements Comparable{
      public String identifier;
      public int conf;
      public int id;
      public sortByConf(String identifier,  int id, int conf){
          this.identifier = identifier;
          this.conf = conf;
          this.id = id;
      }
      public int compareTo(Object o){
           double dist2 = ((sortByConf)o).conf;
           return (conf > dist2 ? -1 : (conf == dist2 ? 0 : 1));
      }
  }
   
   
  public cluster sortVar(cluster c, Settings s){
      sortByVar[] sbc = new sortByVar[c.ids.size()];
      for(int i = 0; i < sbc.length; i++) {
          int id = Integer.valueOf(c.ids.get(i).toString());
          sbc[i] = new sortByVar(s.input[id].getValues(), id, Integer.valueOf(c.confidence.get(i).toString()), i);
      }
      Arrays.sort(sbc);
      ArrayList ids = new ArrayList();
      ArrayList conf = new ArrayList();
      ArrayList order = new ArrayList();
      for(int i = 0; i < sbc.length; i++){
          ids.add(sbc[i].id);
          conf.add(sbc[i].conf);
          order.add(sbc[i].order);
      }
      c.ids = ids;
      c.confidence = conf;
      c.order=order;
      return c;
  }    
   
   private class sortByVar implements Comparable{
      public float[] f;
      public int conf;
      public int id;
      public int order;
      public sortByVar(float[] f,  int id, int conf, int order){
          this.f = f;
          this.conf = conf;
          this.id = id;
          this.order=order;
      }
      
      private float getVar(float[] f){
          float var = 0;
          float avg = 0;
          for(int i = 0; i < f.length; i++) avg+=f[i];
          avg/=f.length;
          for(int i = 0; i < f.length; i++){
              var+=Math.pow(f[i]-avg,2);
          }
          var/=(f.length-1);
          
          return var;
      }
      
      public int compareTo(Object o){
           float[] dist2 = ((sortByVar)o).f;
           return (getVar(f) > getVar(dist2) ? -1 : (getVar(f) == getVar(dist2) ? 0 : 1));
      }
  }



public cluster sortConf2(cluster c, Settings s){
      sortByConf2[] sbc = new sortByConf2[c.ids.size()];
      for(int i = 0; i < sbc.length; i++) {
          int id = Integer.valueOf(c.ids.get(i).toString());
          int order = Integer.valueOf(c.order.get(i).toString());
          sbc[i] = new sortByConf2(s.input[id].getIdentity(), id, Integer.valueOf(c.confidence.get(i).toString()), order);
      }
      Arrays.sort(sbc);
      ArrayList ids = new ArrayList();
      ArrayList conf = new ArrayList();
      for(int i = 0; i < sbc.length; i++){
          ids.add(sbc[i].id);
          conf.add(sbc[i].conf);
      }
      c.ids = ids;
      c.confidence = conf;
      return c;
  }    
   
   private class sortByConf2 implements Comparable{
      public String identifier;
      public int order;
      public int conf;
      public int id;
      public sortByConf2(String identifier,  int id, int conf, int order){
          this.identifier = identifier;
          this.conf = conf;
          this.id = id;
          this.order=order;
      }
      public int compareTo(Object o){
           double dist2 = ((sortByConf2)o).order;
           return (order < dist2 ? -1 : (order == dist2 ? 0 : 1));
      }
  }



}

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

import java.io.Serializable;
/**
 * structure for data item : store all data values, data identifier string
 * 
 * @author Aaron
 */
public class dataItem implements Serializable{
    private float[] values; //store current data values (normalized for clustering or for display)
    private float[] originalValues; //store original data values
    private float[] normValues; //temporarily store values originally normalized for clustering
    private byte[] identifier; //data item label
    private Point pnt; //point in euclidean space after mapping is done
    private int pointIndex; //maps to point array
    private int extraID; //another id for data item (in addition to identifier)
    private byte[] desc; //set description for data item
    private short confidence = -1; //set confidence of cluster membership
    private short clusterID = 0;

    
    public dataItem(float[] v, String i){
        values = v;
        identifier = i.getBytes();
        originalValues = new float[v.length];
        for(int j = 0; j < v.length; j++) originalValues[j] = v[j];
    }
    
    public float[] getValues() {return values;}
    public void setValue(int i, float f) {values[i] = f;}
    public void setIdentity(String identity) {identifier = identity.getBytes();}
    public String getIdentity() {return new String(identifier);}
    public void setPoint(Point pnt) {this.pnt = pnt;}
    public Point getPoint() {return pnt;}
    public void setPointIndex(int index) {this.pointIndex = index;}
    public int getPointIndex() {return pointIndex;}
    public void setOriginalValue(int i, float f) {originalValues[i] = f;}
    public float[] getOriginalValues() {return originalValues;}
    public float[] getNormValues() {return normValues;}
    public void setnormValue(int i, float f) {normValues[i] = f;}
    public void initNormValue(int size) {normValues = new float[size];}
    public void setExtraID(int id) {extraID = id;}
    public int getExtraID() {return extraID;}
    public void setDesc(String desc) {this.desc = desc.getBytes();}
    public String getDesc(){return (desc==null) ? "" : new String(desc);}
    public void setConf(int conf) {this.confidence = (short)conf;}
    public int getConf() {return confidence;}
    public void setClustID(int id) {this.clusterID = (short)id;}
    public int getClustID() {return clusterID;}
    
    //prints out entire data point (with normalized input) excluding description as comma delimited string
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(new String(identifier)+",");
        for(int i = 0; i < values.length; i++) sb.append(values[i]+",");
        return sb.substring(0,sb.length()-1);
    }
    
    //prints out entire data point (with original input) including description as comma delimited string
    public String toDescString(){
        StringBuffer sb = new StringBuffer();
        sb.append(/*((desc==null) ? "" : desc+"\t")+*/new String(identifier)+"\t");
        for(int i = 0; i < originalValues.length; i++) sb.append(originalValues[i]+"\t");
        return sb.substring(0,sb.length()-1);
    }

    //prints out entire data point (with normalized input) including description as comma delimited string
    public String toDescNormString(){
        StringBuffer sb = new StringBuffer();
        sb.append(new String(identifier)+"\t");
        for(int i = 0; i < originalValues.length; i++) sb.append(values[i]+"\t");
        return sb.substring(0,sb.length()-1);
    }
}

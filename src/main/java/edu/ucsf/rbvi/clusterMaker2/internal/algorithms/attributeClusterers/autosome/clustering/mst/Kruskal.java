package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.clustering.mst;

/*
 * Kruskal.java
 * 
 * Created on Mar 4, 2008, 5:06:00 PM
 * 

 */



/**
 *
 */
/********************************************/
/* Kruskal.java                             */
/* Copyright (C) 1997, 1998, 2000  K. Ikeda */
/********************************************/


import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;



class kNode {

	int	set;
	int	first;
	int	next;
	int	w;
	int	h;
	int	name;
}
class Edge {
	short	rndd_plus;	/* initial vertex of this edge */
	short	rndd_minus;	/* terminal vertex of this edge */
	float	len;		/* length */
	int	select;
	int	name;
}

public class Kruskal{
	int	n,m;
	int	u, usel, step;
	kNode[]	v = new kNode[100];
	Edge[]	e = new Edge[200];
	int[]	idx = new int[200];


	int findkNode(int name) {
		for (int i=0; i<n; i++)
			if (v[i].name == name)
				return i;
		return -1;
	}

	void input_graph(float[] edges, short[][] nodes){
                int kNodeNum = edges.length;
		v = new kNode[kNodeNum];
                e = new Edge[edges.length];
                idx = new int[e.length];
               // System.out.println(kNodeNum);
                
		n = kNodeNum;
		m = edges.length;

		for (int i = 0; i<n; i++) {
			kNode kNode = new kNode();
			kNode.name = i;
			v[i] = kNode;
		}
		for (int i = 0; i<m; i++) {
			Edge edge = new Edge();
                        float dist = edges[i];
			edge.name = i;
                        edge.rndd_plus = nodes[i][0];
                        edge.rndd_minus = nodes[i][1];
			edge.len = dist;
			e[i] = edge;
		}
		for (int i=0; i<m; i++)
			e[i].select = -1;

		step1();
		//Krsub p = (Krsub)getAppletContext().getApplet("krsub");
		//if (p!=null)
		//	p.set(1,n,m,num,den,v,e,idx);
		step = 2;
                
                while(u < m){
                   // System.out.println(u);
                
			if (step == 3) {
				step3();
				step = 2;
			} else {
				step2();
				step = 3;
			}
		
                }
                
	}

	void swap(int i, int j) {
		int	k = idx[i];

		idx[i] = idx[j];
		idx[j] = k;
	}

	int partition(int left, int right) {
		float	pivot = e[idx[((left+right)/2)]].len;

		while (left<=right) {
			while (e[idx[left]].len < pivot)
				left++;
			while (e[idx[right]].len > pivot)
				right--;
			if (left <= right)
				swap(left++,right--);
		}
		return left;
	}

	void qsort(int left, int right) {
		int	i;

		if (left >= right)
			return;
		i = partition(left,right);
		qsort(left,i-1);
		qsort(i,right);
	}

	void step1() {		/* initialize */
		for (int i=0; i<m; i++)
			idx[i] = i;
		for (int i=0; i<m; i++)
			e[i].select = -1;
		qsort(0,m-1);
		for (int i=0; i<m; i++)
			e[i].select = -1;
		for (int i=0; i<n; i++) {
			v[i].set = i;
			v[i].first = i;
			v[i].next = -1;
		}
		usel = u = 0;
	}

	void step2() {		/* select the shortest edge */
		e[idx[u]].select = 1; /* pick up the edge */
	}

	void step3() {		/* check the loop */
		int	vl = e[idx[u]].rndd_plus;
		int	vr = e[idx[u]].rndd_minus;
		int	i,j,k;

		if (v[vl].set == v[vr].set) {
			e[idx[u++]].select = -2; /* de-select the edge */
			return;
		}
		usel ++;
		e[idx[u++]].select = 2; /* select the edge */
		for (i = vl; v[i].next>=0; i = v[i].next)
			;
		v[i].next = v[vr].first;
		j = v[vl].first;
		k = v[vl].set;
		for (i = v[vr].first; i>=0; i = v[i].next) {
			v[i].first = j;
			v[i].set = k;
		}
	}











}



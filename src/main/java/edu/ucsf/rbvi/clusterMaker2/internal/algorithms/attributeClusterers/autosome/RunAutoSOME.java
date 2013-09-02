/**
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * File last modified 06-25-13 by Aaron M. Newman, Ph.D.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.work.TaskMonitor;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.*;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.*;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.NoneConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class RunAutoSOME {


	private List<CyNode> nodes;
	private List<CyEdge> edges;
	private boolean canceled = false;
	public final static String GROUP_ATTRIBUTE = "__AutoSOMEGroups";
	protected int clusterCount = 0;
	private boolean createMetaNodes = false;
	private DoubleMatrix2D matrix = null;
	private boolean debug = false;
	private List<String> dataAttributes;
	private boolean ignoreMissing = false;
	private boolean selectedOnly = false;
	private Run autRun;
	private Settings settings;
	private Map<String,CyNode> storeNodes;
	private Map<String, String> storeClust;
	private clusterRun cr;
	public ArrayList<String>attrList = new ArrayList<String>();
	public ArrayList<String>attrOrderList = new ArrayList<String>();
	public ArrayList<String>nodeOrderList = new ArrayList<String>();
	private TaskMonitor monitor;
	private ClusterManager clusterManager;
	private CyNetwork network;

	public RunAutoSOME(ClusterManager clusterManager, List<String> dataAttributes, 
			           CyNetwork network, Settings settings, TaskMonitor monitor)
	{
		this.dataAttributes=dataAttributes;
		this.settings=settings;
		this.monitor = monitor;
		this.clusterManager = clusterManager;
		this.network = network;

		// logger.info("InflationParameter = "+inflationParameter);
		// logger.info("Iterations = "+num_iterations);
		// logger.info("Clustering Threshold = "+clusteringThresh);
	}

	public void setIgnoreMissing(boolean val) { ignoreMissing = val; }
	public void setSelectedOnly(boolean val) { selectedOnly = val; }


	public void cancel () { canceled = true; autRun.kill();}

	public void setDebug(boolean debug) { this.debug = debug; }

	public List<NodeCluster> run(TaskMonitor monitor) {
		long startTime = System.currentTimeMillis();

		debugln("Initial matrix:");
		//printMatrix(matrix);

		// Normalize
		//normalize(matrix, clusteringThresh, false);

		debugln("Normalized matrix:");
		//printMatrix(matrix);

		// logger.info("Calculating clusters");

		if (dataAttributes == null || dataAttributes.size() == 0) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no attribute list selected");
			return null;
		}

		if (selectedOnly &&
			network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) == 0) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no nodes selected from network");
			return null;
		}

		Settings s = new Settings();

		//get parameter settings
		s.ensemble_runs = settings.ensemble_runs;
		s.mst_pval = settings.mst_pval;
		s.threads = settings.threads;
		s.logNorm=settings.logNorm;
		s.unitVar=settings.unitVar;
		s.medCenter=settings.medCenter;
		s.medCenterCol=settings.medCenterCol;
		s.sumSqrRows=settings.sumSqrRows;
		s.sumSqrCol=settings.sumSqrCol;
		s.som_iters=settings.som_iters;
		s.de_resolution=settings.de_resolution;
		s.distMatrix=settings.distMatrix;
		s.dmDist=settings.dmDist;
		s.FCNrows=settings.FCNrows;
		s.som_maxGrid=20;

		s.htmlOut=false;
		s.textOut=false;

        // Create the matrix
		Matrix matrix = new Matrix(network, dataAttributes.toArray(new String[0]), 
				                   false, ignoreMissing, selectedOnly);

		/*  Not sure what this is about?
		List<EdgeWeightConverter>converters = new ArrayList<EdgeWeightConverter>();
		converters.add(new NoneConverter());
		converters.add(new DistanceConverter1());
		converters.add(new DistanceConverter2());
		converters.add(new LogConverter());
		converters.add(new NegLogConverter());
		converters.add(new SCPSConverter());
		*/
		EdgeWeightConverter converter = new NoneConverter();
		// What's going on here?  DistanceMatrix will only take a single edge attribute,
		// but dataAttributes is a list of node attributes??
        DistanceMatrix dm = new DistanceMatrix(network, dataAttributes.get(0), selectedOnly, converter);
        nodes = dm.getNodes();

        //edges = dm.getEdges();

        s.input = new dataItem[matrix.nRows()];
        //matrix.printMatrix();

        Map<String, Integer> key = new HashMap<String, Integer>();
        for(int i = 0; i < nodes.size(); i++){
        	String id = ModelUtils.getNodeName(network, nodes.get(i));
        	if(!key.containsKey(id)) key.put(id,i);
        }

        for(int k = 0, itor=0; k < matrix.nRows(); k++){
        	float[] f = new float[matrix.nColumns()];
        	//System.out.println(matrix.getRowLabels()[k]+" "+nodes.get(k).getIdentifier());
        	if(k==0) {
        		s.columnHeaders=new String[f.length+1];
        		s.columnHeaders[0] = "NAME";
        	}
        	for(int l = 0; l < f.length; l++) {
        		if(k==0) {
        			s.columnHeaders[l+1] = matrix.getColLabel(l);
        			s.columnHeaders[l+1] = s.columnHeaders[l+1].replace("\"","");
        			s.columnHeaders[l+1] = s.columnHeaders[l+1].replace(",","");

        			//System.out.println(s.columnHeaders[l+1]);
        		}
        		//System.out.println(matrix.getValue(k,l).floatValue());
        		if(matrix.getValue(k,l)!=null) {
        			f[l] = matrix.getValue(k,l).floatValue();
        		} else {
        			f[l] = -99999999;
        			s.fillMissing=true;
        		}

        	}
        	s.input[itor++] = new dataItem(f, matrix.getRowLabel(k));
        }

        if(s.FCNrows && s.distMatrix) s = transpose(s);

        if(s.input == null){
        	monitor.showMessage(TaskMonitor.Level.ERROR, "Insufficient data for clustering (1 or less rows or columns)");
        	return null;
        } else if(s.input.length<2){
        	monitor.showMessage(TaskMonitor.Level.ERROR, "Insufficient data for clustering (1 or less rows or columns)");
        	return null;
        } else if(s.input[0].getValues()==null){
        	monitor.showMessage(TaskMonitor.Level.ERROR, "Insufficient data for clustering (1 or less rows or columns)");
        	return null;
        } else if(s.input[0].getValues().length<2){
        	monitor.showMessage(TaskMonitor.Level.ERROR, "Insufficient data for clustering (1 or less rows or columns)");
        	return null;
        }

        autRun = new Run();
        cr = autRun.runAutoSOMEBasic(s, monitor);

        if(cr==null) {
        	monitor.setStatusMessage("Clustering failed!");
        	return null;
        }

        monitor.setStatusMessage("Assigning nodes to clusters");
        clusterCount = cr.c.length;
        Map<NodeCluster, NodeCluster> cMap = (!s.distMatrix) ? getNodeClusters(cr, key, matrix, s) : getNodeClustersFCN(cr, matrix, s);

        if (canceled) {
        	monitor.setStatusMessage("canceled");
        	return null;
        }


        //Update node attributes in network to include clusters. Create cygroups from clustered nodes
        monitor.setStatusMessage("Created "+clusterCount+" clusters");
        // debugln("Created "+clusterCount+" clusters:");
        //
        if (clusterCount == 0) {
        	monitor.showMessage(TaskMonitor.Level.WARN, "Created 0 clusters!!!!");
        	return null;
        }


        Set<NodeCluster>clusters = cMap.keySet();
        return new ArrayList<NodeCluster>(clusters);
	}


	private Map<NodeCluster,NodeCluster> getNodeClusters(clusterRun cr, Map<String, Integer> key, Matrix matrix, Settings s){
		Map<NodeCluster,NodeCluster> cMap = new HashMap<NodeCluster,NodeCluster>();
		attrList = new ArrayList<String>();
		attrOrderList = new ArrayList<String>();
		nodeOrderList = new ArrayList<String>();


		for(int i = 0; i < matrix.nColumns(); i++) attrOrderList.add(matrix.getColLabel(i));

		for(int i = 0; i < clusterCount; i++){
			if(cr.c[i].ids.isEmpty()) continue;
			NodeCluster nc = new NodeCluster();
			nc.setClusterNumber(i);
			for(int j = 0; j < cr.c[i].ids.size(); j++){
				int dataID = cr.c[i].ids.get(j).intValue();
				int nodeDataID = key.get(matrix.getRowLabels()[dataID]).intValue();
				CyNode cn = nodes.get(nodeDataID);                        
				nc.add(cn);
				attrList.add(ModelUtils.getNodeName(network, cn)+"\t"+i);
				nodeOrderList.add(ModelUtils.getNodeName(network, cn));
			}
			cMap.put(nc,nc);
		}

		return cMap;
	}



	private Map<NodeCluster,NodeCluster> getNodeClustersFCN(clusterRun cr, Matrix matrix, Settings s){
		attrList = new ArrayList<String>();
		attrOrderList = new ArrayList<String>();
		nodeOrderList = new ArrayList<String>();
		HashMap<NodeCluster,NodeCluster> cMap = new HashMap<NodeCluster,NodeCluster>();

		storeNodes = new HashMap<String, CyNode>();
		storeClust = new HashMap<String, String>();
		int currClust=-1;
		NodeCluster nc = new NodeCluster();

		Map<String, CyNode> storeOrigNodes = new HashMap<String, CyNode>();
		for(int i = 0; i < nodes.size(); i++){
			CyNode cn = (CyNode) nodes.get(i);
			storeOrigNodes.put(ModelUtils.getNodeName(network, cn), cn);
		}

		if(!s.FCNrows) for(int i = 1; i < s.columnHeaders.length; i++) attrOrderList.add(s.columnHeaders[i]);
		else{
			for(int i = 0; i < matrix.nColumns(); i++) attrOrderList.add(matrix.getColLabel(i));
		}

		for(int i = 0; i < cr.fcn_nodes.length; i++){

			String[] fcn = cr.fcn_nodes[i];

			if(currClust != Integer.valueOf(fcn[1])){
				if(nc.size()>0)  cMap.put(nc,nc);
				nc = new NodeCluster();
				currClust = Integer.valueOf(fcn[1]);
				nc.setClusterNumber(currClust);
				//System.out.println(currClust+"\t"+nc.getClusterNumber());
			}

			String temp = fcn[0];

			//System.out.println(temp);

			String[] tokens = temp.split("_");
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < tokens.length-1; j++) sb.append(tokens[j]+"_");
			temp = sb.substring(0,sb.length()-1);

			// Should this be in the current network, or a different network?
			// CyNode cn = Cytoscape.getCyNode(temp, true);
			CyNode cn = network.addNode();
			network.getRow(cn).set(CyNetwork.NAME, temp);
			network.getRow(cn).set(CyRootNetwork.SHARED_NAME, temp);


			nodeOrderList.add(temp);
			attrList.add(temp+"\t"+currClust);
			//System.out.println("*\t"+cn.getIdentifier()+"\t"+currClust);

			if(s.FCNrows){
				CyNode orig = (CyNode) storeOrigNodes.get(fcn[2]);

				CyTable nodeAttrs = network.getDefaultNodeTable();
				Set<String> atts = CyTableUtil.getColumnNames(nodeAttrs);
				for (String attribute: atts) {
					Class type = nodeAttrs.getColumn(attribute).getType();
					Object att = nodeAttrs.getRow(orig).getRaw(attribute);
					if(att==null) continue;
					nodeAttrs.getRow(cn).set(attribute, att);
				}
			}

			storeNodes.put(fcn[0],cn);
			storeClust.put(fcn[0],fcn[1]);
			nc.add(cn);

			/*
			CyAttributes netAttr = Cytoscape.getNetworkAttributes();
			String netID = Cytoscape.getCurrentNetwork().getIdentifier();
			netAttr.setListAttribute(netID, ClusterMaker.CLUSTER_NODE_ATTRIBUTE, attrList);
			netAttr.setListAttribute(netID, ClusterMaker.ARRAY_ORDER_ATTRIBUTE, attrOrderList);
			netAttr.setListAttribute(netID, ClusterMaker.NODE_ORDER_ATTRIBUTE, nodeOrderList);
			*/

		}
		if(nc.size()>0)  cMap.put(nc,nc);



		return cMap;
	}




	public List<CyEdge> getEdges(int MAXEDGES) {

		edges = new ArrayList<CyEdge>();
		sortEdges[] se = new sortEdges[cr.fcn_edges.length];

		Map<String, Integer> hmEdges = new HashMap<String, Integer>();

		for(int i = 0; i < cr.fcn_edges.length; i++){
			se[i] = new sortEdges(cr.fcn_edges[i][0], cr.fcn_edges[i][1], Double.valueOf(cr.fcn_edges[i][2]));

		}

		Arrays.sort(se);

		for(int i = 0; i < se.length; i++){
			cr.fcn_edges[i] = new String[]{se[i].n1,se[i].n2,String.valueOf(se[i].weight)};
			if(storeClust.get(cr.fcn_edges[i][0]).toString().equals(storeClust.get(cr.fcn_edges[i][1]).toString()))
				hmEdges.put(cr.fcn_edges[i][0], i);
		}

		Map<Integer, Integer> edgeIDs = new HashMap<Integer, Integer>();
		List<String[]> allEdges = new ArrayList<String[]>();
		for (String edge: hmEdges.keySet()) {
			int edgeID = hmEdges.get(edge).intValue();
			allEdges.add(cr.fcn_edges[edgeID]);
			if(!edgeIDs.containsKey(edgeID)) edgeIDs.put(edgeID,edgeID);
		}//System.out.println(allEdges.size());

		int itor = 0;
		while(allEdges.size()<Math.min(cr.fcn_edges.length,MAXEDGES)){
			if(edgeIDs.containsKey(itor)) {itor++; continue;}
			allEdges.add(cr.fcn_edges[itor++]);
		}

		for(int j = 0; j < allEdges.size(); j++){
			String[] fce = (String[]) allEdges.get(j);
			CyNode c1 = (CyNode) storeNodes.get(fce[0]);
			CyNode c2 = (CyNode) storeNodes.get(fce[1]);
			CyEdge edge = network.addEdge(c1, c2, false);
			network.getDefaultEdgeTable().getRow(edge).set(CyNetwork.NAME,fce[2]);
			edges.add(edge);
		}

		return edges;
	}

	private Settings transpose(Settings s){


		float[][] t = new float[s.input[0].getValues().length][s.input.length];

		for(int i = 0; i < s.input.length; i++){
			for(int j = 0; j < s.input[i].getValues().length; j++){
				t[j][i] = s.input[i].getValues()[j];
			}
		}

		dataItem[] d = new dataItem[t.length];

		for(int i = 0; i < d.length; i++){
			String label = new String();
			if(s.columnHeaders!=null) if(s.columnHeaders[i+s.startData] != null) label = s.columnHeaders[i+s.startData].replace(" ","_");
			d[i] = new dataItem(t[i], label);
		}

		String[] cols = new String[s.input.length+1];
		cols[0] = "NAME";

		for(int i = 0; i < s.input.length; i++){
			cols[i+1] = s.input[i].getIdentity();
		}

		s.input = d;
		s.columnHeaders=cols;

		return s;

	}







	private void debugln(String message) {
		if (debug) System.out.println(message);
	}

	private void debugln() {
		if (debug) System.out.println();
	}

	private void debug(String message) {
		if (debug) System.out.print(message);
	}


	class sortEdges implements Comparable{
		String n1, n2;
		double weight;
		public sortEdges(String n1, String n2, double weight){
			this.n1=n1;
			this.n2=n2;
			this.weight=weight;
		}
		public int compareTo(Object o){
			double dist2 = ((sortEdges)o).weight;
			return (weight > dist2 ? -1 : (weight == dist2 ? 0 : 1));
		}
	}



}


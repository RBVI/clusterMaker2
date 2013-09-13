/* vim: set ts=2: */
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
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;


// Cytoscape imports


public class AutoSOMEContext  {
	CyNetwork network;

	@ContainsTunables
	public AttributeList attributeList = null;
	
	@Tunable(description="Only use selected nodes for cluster", groups={"Data Input"}, gravity=60)
	public boolean selectedOnly = false;
	
	@Tunable(description="Ignore nodes with no data", groups={"Data Input"}, gravity=61)
	public boolean ignoreMissing = true;
	
	@Tunable(description="Running Mode", groups={"AutoSOME Basic Tuning"}, gravity=65)
	public ListSingleSelection<String>mode = new ListSingleSelection<String>("Normal", "Precision", "Speed");
	
	@Tunable(description="Number of Ensemble Runs", groups={"AutoSOME Basic Tuning"}, gravity=66)
	public int ensembleRuns = 50;

	@Tunable(description="P-Value Threshold", groups={"AutoSOME Basic Tuning"}, gravity=67)
	public double pvalue = 0.05;

	@Tunable(description="Number of Threads (No. CPUs)", groups={"AutoSOME Basic Tuning"}, gravity=68)
	public int numThreads = 1;
	
	@Tunable(description="Normalization mode", groups={"Data Normalization"}, 
			 params="displayState=expanded", gravity=75)
	public ListSingleSelection<String>normalization = 
		new ListSingleSelection<String>("Custom", "No normalization", "Expression data 1", "Expression data 2");

	@Tunable(description="Log2 Scaling", groups={"Data Normalization"}, gravity=76)
	public boolean logscaling = false;
	
	@Tunable(description="Unit Variance", groups={"Data Normalization"}, gravity=76)
	public boolean unitvar = false;
	
	@Tunable(description="Median Centering", groups={"Data Normalization"}, gravity=77)
	public ListSingleSelection<String> medianCentering = 
	new ListSingleSelection<String>("None", "Genes", "Arrays", "Both");
	
	@Tunable(description="Sum of Squares=1", groups={"Data Normalization"}, gravity=78)
	public ListSingleSelection<String> sumSqr = 
		new ListSingleSelection<String>("None", "Genes", "Arrays", "Both");
	
	@Tunable(description="Missing value handling", groups={"Data Normalization"}, gravity=79)
	public ListSingleSelection<String> fillMissing = 
		new ListSingleSelection<String>("Row Mean", "Row Median", "Column Mean", "Column Median");
	
	@Tunable(description="Perform Fuzzy Clustering", groups={"Fuzzy Cluster Network Settings"}, 
			 params="displayState=expanded", gravity=100)
	public boolean performFuzzy = false;
	
	@Tunable(description="Source Data", groups={"Fuzzy Cluster Network Settings"}, gravity=101)
	public ListSingleSelection<String>fuzzyInput = 
		new ListSingleSelection<String>("Nodes (Genes)", "Attributes (Array)");
	
	@Tunable(description="Distance Metric", groups={"Fuzzy Cluster Network Settings"}, gravity=102)
	public ListSingleSelection<String>distanceMetric = 
		new ListSingleSelection<String>("Uncentered Correlation", "Pearson's Correlation", "Euclidean");
	
	@Tunable(description="Maximum number of edges to display in fuzzy network", 
			 groups={"Fuzzy Cluster Network Settings"}, gravity=103)
	public int maxEdges = 2000;
    
	@ContainsTunables
	public AdvancedProperties advancedAttributes;
	
	@Tunable(description="Choose Visualization", groups={"Data Output"}, gravity=125)
	public ListSingleSelection<String> dataVisualization = 
		new ListSingleSelection<String>("Network", "Heatmap");
	
	@Tunable(description="Show Visualization when complete", groups={"Data Output"}, gravity=126)
	public boolean showViz = false;

	
	public AutoSOMEContext() {
		distanceMetric.setSelectedValue("Uncentered Correlation");
		dataVisualization.setSelectedValue("Network");
		mode.setSelectedValue("Normal");
		advancedAttributes = new AdvancedProperties("__autosomeCluster", false);
	}
	
	public void setNetwork(CyNetwork network)
	{
		if (this.network != null && this.network.equals(network))
			return;
		this.network = network;
		if (attributeList == null)
			attributeList = new AttributeList(network, true);
		else
			attributeList.setNetwork(network);
	}

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}
	
	public List<String> getParams() {
		List<String> params = new ArrayList<String>();
		params.add("showViz="+showViz);
		params.add("dataVisualization="+dataVisualization.getSelectedValue());
		params.add("maxEdges="+maxEdges);
		params.add("distanceMetric="+distanceMetric.getSelectedValue());
		params.add("fuzzyInput="+fuzzyInput.getSelectedValue());
		params.add("performFuzzy="+performFuzzy);
		params.add("fillMissing="+fillMissing.getSelectedValue());
		params.add("sumSqr="+sumSqr.getSelectedValue());
		params.add("medianCentering="+medianCentering.getSelectedValue());
		params.add("unitvar="+unitvar);
		params.add("logscaling="+logscaling);
		params.add("normalization="+normalization.getSelectedValue());
		params.add("numThreads="+numThreads);
		params.add("pvalue="+pvalue);
		params.add("ensembleRuns"+ensembleRuns);
		params.add("mode="+mode.getSelectedValue());
		params.add("ignoreMissing="+ignoreMissing);
		params.add("selectedOnly="+selectedOnly);
		params.add("addributeList="+attributeList.getNodeAttributeList().toString());
		return params;	
	}
	
	public Settings getSettings() {
		Settings settings = new Settings();
		settings.ensemble_runs = ensembleRuns;
		settings.mst_pval = pvalue;
		settings.threads = numThreads;
		settings.logNorm = logscaling;
		settings.unitVar = unitvar;
		settings.distMatrix = performFuzzy;
		if (mode.getSelectedValue().equals("Normal")) {
			settings.som_iters = 500;
			settings.de_resolution=32;
		} else if (mode.getSelectedValue().equals("Speed")) {
			settings.som_iters = 250;
			settings.de_resolution=16;
		} else {
			settings.som_iters=1000;
			settings.de_resolution=64;
		}

		if (medianCentering.getSelectedValue().equals("Both")) {
			settings.medCenter=true;
			settings.medCenterCol=true;
		} else if (medianCentering.getSelectedValue().equals("Genes")) {
			settings.medCenter=true;
			settings.medCenterCol=false;
		} else if (medianCentering.getSelectedValue().equals("Arrays")) {
			settings.medCenter=false;
			settings.medCenterCol=true;
		} else {
			settings.medCenter=false;
			settings.medCenterCol=false;
		}
		
		if (sumSqr.getSelectedValue().equals("Both")) {
			settings.sumSqrRows=true;
			settings.sumSqrCol=true;
		} else if (sumSqr.getSelectedValue().equals("Genes")) {
			settings.sumSqrRows=true;
			settings.sumSqrCol=false;
		} else if (sumSqr.getSelectedValue().equals("Arrays")) {
			settings.sumSqrRows=false;
			settings.sumSqrCol=true;
		} else {
			settings.sumSqrRows=false;
			settings.sumSqrCol=false;
		}
		
		settings.fillMissing = !ignoreMissing;
		settings.distMatrix = performFuzzy;
		if (performFuzzy) {
			if (fuzzyInput.getSelectedValue().equals("Nodes (Genes)")) {
				settings.FCNrows = true;
			} else if (fuzzyInput.getSelectedValue().equals("Attributes (Array)")) {
				settings.FCNrows=false;
			}
			
			if (distanceMetric.getSelectedValue().equals("Uncentered Correlation")) {
				settings.dmDist=3;
			} else if (distanceMetric.getSelectedValue().equals("Pearson's Correlation")) {
				settings.dmDist=2;
			} else if (distanceMetric.getSelectedValue().equals("Euclidean")) {
				settings.dmDist=1;
			}
		}
		
		if (fillMissing.getSelectedValue().equals("Row Mean")) {
			settings.mvMedian=false;
			settings.mvCol=false;
		} else if (fillMissing.getSelectedValue().equals("Row Median")) {
			settings.mvMedian=true;
			settings.mvCol=false;
		} else if (fillMissing.getSelectedValue().equals("Column Mean")) {
			settings.mvMedian=false;
			settings.mvCol=true;
		} else if (fillMissing.getSelectedValue().equals("Column Median")) {
			settings.mvMedian=true;
			settings.mvCol=true;
		}
		return settings;
	}
}

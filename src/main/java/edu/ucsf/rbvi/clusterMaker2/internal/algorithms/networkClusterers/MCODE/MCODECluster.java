/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
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
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;

// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class MCODECluster extends AbstractNetworkClusterer  {
	public static String SHORTNAME = "mcode";
	public static String NAME = "MCODE Cluster";
	public final static String GROUP_ATTRIBUTE = "__MCODEGroups.SUID";
	
	final static int FIRST_TIME = 0;
	final static int RESCORE = 1;
	final static int REFIND = 2;
	final static int INTERRUPTION = 3;
	int analyze = FIRST_TIME;

	MCODEParameterSet currentParamsCopy;

	RunMCODE runMCODE;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public MCODEContext context = null;
	
	public MCODECluster(MCODEContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }
	
	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public void updateSettings() {

		if (context.selectedOnly)
			currentParamsCopy.setScope(MCODEParameterSet.SELECTION);
		else
			currentParamsCopy.setScope(MCODEParameterSet.NETWORK);

		currentParamsCopy.setIncludeLoops(context.includeLoops);

		currentParamsCopy.setDegreeCutoff(context.degreeCutoff);

		currentParamsCopy.setHaircut(context.haircut);

		currentParamsCopy.setFluff(context.fluff);

		currentParamsCopy.setNodeScoreCutoff(context.scoreCutoff);

		currentParamsCopy.setKCore(context.kCore);

		currentParamsCopy.setMaxDepthFromStart(context.maxDepth);
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		updateSettings();

		NodeCluster.init();
		if(currentParamsCopy.getScope().equals(MCODEParameterSet.SELECTION)) {
			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
			Long[] selectedNodesRGI = new Long[selectedNodes.size()];
			int c = 0;
			for (CyNode node: selectedNodes) 
				selectedNodesRGI[c++] = node.getSUID();
			currentParamsCopy.setSelectedNodes(selectedNodesRGI);
		}

		MCODECurrentParameters.getInstance().setParams(currentParamsCopy, "MCODE Result", ModelUtils.getNetworkName(network));

		runMCODE = new RunMCODE(RESCORE, clusterAttributeName, network, monitor);
		List<NodeCluster> clusters = runMCODE.run(monitor);
		if (canceled) {
			monitor.showMessage(TaskMonitor.Level.INFO,"Canceled by user");
			return;
		}

		// Now, sort our list of clusters by score
		clusters = NodeCluster.rankListByScore(clusters);
		createGroups = context.advancedAttributes.createGroups;

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.setStatusMessage("Creating groups");

		List<List<CyNode>> nodeClusters = createGroups(network, clusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, nodeClusters);
		monitor.setStatusMessage("Done.  MCODE results:\n"+results);

	}

	public void cancel() {
		canceled = true;
		runMCODE.cancel();
	}

	public void setParams(List<String>params) {
		params.add("scope="+currentParamsCopy.getScope());
		params.add("includeLoops="+currentParamsCopy.isIncludeLoops());
		params.add("degreeCutoff="+currentParamsCopy.getDegreeCutoff());
		params.add("kCore="+currentParamsCopy.getKCore());
		params.add("maxDepth="+currentParamsCopy.getMaxDepthFromStart());
		params.add("nodeScoreCutoff="+currentParamsCopy.getNodeScoreCutoff());
		params.add("fluff="+currentParamsCopy.isFluff());
		params.add("haircut="+currentParamsCopy.isHaircut());
	}
}

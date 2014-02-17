/* vim: set ts=2: */
/**
 * Copyright (c) 20118 The Regents of the University of California.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This abstract class is the base class for all of the attribute clusterers provided by
 * clusterMaker.  Fundamentally, an attribute clusterer is an algorithm which functions to
 * partition nodes or node attributes based on properties of the attributes.
 */
public class KClusterAttributes {

	@Tunable (description="Estimate k using silhouette", 
	          groups={"K-Cluster parameters"}, gravity=1.0)
	public boolean useSilhouette = false;

	@Tunable (description="Maximum number of clusters", 
	          groups={"K-Cluster parameters"}, dependsOn="useSilhouette=true", gravity=2.0)
	public int kMax = 0;

	@Tunable (description="Number of clusters", 
	          groups={"K-Cluster parameters"}, dependsOn="useSilhouette=false", gravity=3.0)
	public int kNumber = 0;

	@Tunable (description="Initialize cluster centers from most central elements", 
	          groups={"K-Cluster parameters"}, gravity=4.0)
	public boolean initializeNearCenter = false;

	public KClusterAttributes() {
	}

	/* TODO: This doesn't quite work right.  Ideally, if this is called and
	 * the number changes (either kNumber or kMax) the value in the tunable
	 * would also change.  This doesn't work -- when we change the value via
	 * setValue, it doesn't update the UI element, so the user isn't informaed
	 */
	public void updateKEstimates(CyNetwork network, boolean selectedOnly) {
		// We also want to update the number our "guestimate" for k
		double nodeCount = (double)network.getNodeCount();
		if (selectedOnly) {
			int selNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true).size();
			if (selNodes > 0) nodeCount = (double)selNodes;
		}

		double kinit = Math.sqrt(nodeCount/2);
		if (kinit > 1)
			kNumber = (int)kinit;
		else
			kNumber = 1;

		kMax = (int)kinit*2;
	}

	public void addParams(List<String> params) {
		params.add("useSilhouette="+useSilhouette);
		params.add("kMax="+kMax);
		params.add("kNumber="+kNumber);
		params.add("initializeNearCenter="+initializeNearCenter);
	}
}

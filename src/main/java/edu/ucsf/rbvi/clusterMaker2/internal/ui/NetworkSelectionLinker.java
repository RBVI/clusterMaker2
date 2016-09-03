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
package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

/**
 */
public class NetworkSelectionLinker implements RowsSetListener {
	CyRootNetwork rootNetwork;
	CyEventHelper eventHelper;
	ClusterManager clusterManager;
	CyNetworkViewManager viewManager;
	boolean ignoreSelection = false;

	public NetworkSelectionLinker(CyRootNetwork rootNetwork, CyEventHelper eventHelper, 
	                              ClusterManager mgr) {
		this.rootNetwork = rootNetwork;
		this.eventHelper = eventHelper;
		this.clusterManager = mgr;
		this.viewManager = clusterManager.getService(CyNetworkViewManager.class);
	}

	public void handleEvent(RowsSetEvent e) {
		if (!e.containsColumn(CyNetwork.SELECTED) || ignoreSelection)
			return;

		// System.out.println("Select");
		CyNetworkView currentNetworkView = clusterManager.getNetworkView();

		ignoreSelection = true;
		Map<CyNetwork, Boolean> stateMap = new HashMap<CyNetwork, Boolean>();
		for (CySubNetwork subNetwork: rootNetwork.getSubNetworkList()) {
			if (e.getSource().equals(subNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS))) {
				for (RowSetRecord record: e.getColumnRecords(CyNetwork.SELECTED)) {
					Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
					Boolean value = (Boolean)record.getValue();

					for (CySubNetwork sub2: rootNetwork.getSubNetworkList()) {
						if (subNetwork.equals(sub2) || sub2.getDefaultNodeTable().getRow(suid) == null)
							continue;
						// System.out.println("Selecting row "+suid);
						sub2.getDefaultNodeTable().getRow(suid).set(CyNetwork.SELECTED, value);
					}
				}
			}
			if (viewManager.viewExists(subNetwork)) {
				for (CyNetworkView view: viewManager.getNetworkViews(subNetwork)) {
					if (!view.equals(currentNetworkView)) {
						view.updateView();
					}
				}
			}
		}
		eventHelper.flushPayloadEvents();
		ignoreSelection = false;
	}
}

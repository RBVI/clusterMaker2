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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class AttributeList {
	CyNetwork network;
	boolean nodesOnly = false;

	@Tunable(description="Node attributes for cluster", groups="Array sources", 
	         tooltip="You must choose at least 2 node columns for an attribute cluster", gravity=50 )
	public ListMultipleSelection<String> nodeAttributeList = null;

	@Tunable(description="Edge column for cluster", groups="Array sources",
	         tooltip="You may only chose 1 edge column for an attribute cluster" , gravity=51)
	public ListSingleSelection<String> edgeAttributeList = null;

	@Tunable(description="Edges are assymetric (not common)", groups="Array sources", gravity=52)
	public boolean assymetric = false;

	public AttributeList(CyNetwork network) {
		this(network, false);
	}

	public AttributeList(CyNetwork network, boolean nodesOnly) {
		this.network = network;
		if (network != null) {
			nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
			if (!nodesOnly)
				edgeAttributeList = ModelUtils.updateEdgeAttributeList(network, edgeAttributeList);
		}
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
		if (!nodesOnly)
			edgeAttributeList = ModelUtils.updateEdgeAttributeList(network, edgeAttributeList);
	}

	public CyNetwork getNetwork() { return network; }

	public List<String> getNodeAttributeList() {
		if (nodeAttributeList == null) return null;
		List<String> attrs = nodeAttributeList.getSelectedValues();
		if (attrs == null || attrs.isEmpty()) return null;
		if ((attrs.size() == 1) &&
		    (attrs.get(0).equals("--None--"))) return null;
		return attrs;
	}

	public String getEdgeAttribute() {
		if (edgeAttributeList == null) return null;
		String attr = edgeAttributeList.getSelectedValue();
		if (attr == null || attr.equals("--None--")) return null;
		return attr;
	}
}

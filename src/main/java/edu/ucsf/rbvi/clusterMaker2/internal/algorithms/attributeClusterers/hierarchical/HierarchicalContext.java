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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

public class HierarchicalContext {
	CyNetwork network;

	@Tunable(description="Linkage")
	public ListSingleSelection<ClusterMethod> linkage = 
		new ListSingleSelection<ClusterMethod>(HierarchicalCluster.linkageTypes);

	@Tunable(description="Distance Metric")
	public ListSingleSelection<DistanceMetric> metric = 
		new ListSingleSelection<DistanceMetric>(BaseMatrix.distanceTypes);

	@ContainsTunables
	AttributeList attributeList = null;

	@Tunable(description="Only use selected nodes/edges for cluster", groups={"Clustering Parameters"})
	public boolean selectedOnly = false;

	@Tunable(description="Cluster attributes as well as nodes", groups={"Clustering Parameters"})
	public boolean clusterAttributes = true;

	@Tunable(description="Ignore nodes/edges with no data", groups={"Clustering Parameters"})
	public boolean ignoreMissing = true;

	@Tunable(description="Set missing data to zero (not common)", 
	         groups={"Clustering Parameters", "Advanced Parameters"}, params="displayState=collapsed")
	public boolean zeroMissing = false;

	@Tunable(description="Adjust loops (not common)", groups={"Clustering Parameters", "Advanced Parameters"})
	public boolean adjustDiagonals = false;

	@Tunable(description="Create groups from clusters", groups={"Clustering Parameters"})
	public boolean createGroups = false;


	public HierarchicalContext() {
		linkage.setSelectedValue(ClusterMethod.AVERAGE_LINKAGE);
	}

	public List<String> getParams(Matrix matrix) {
		List<String> params = new ArrayList<String>();
		params.add("linkage="+linkage.getSelectedValue().toString());
		params.add("metric="+metric.getSelectedValue().toString());
		params.add("nodeAttributeList="+attributeList.getNodeAttributeList().toString());
		params.add("edgeAttribute="+attributeList.getEdgeAttribute());
		params.add("selectedOnly="+selectedOnly);
		params.add("clusterAttributes="+clusterAttributes);
		params.add("ignoreMissing="+ignoreMissing);
		params.add("zeroMissing="+zeroMissing);
		params.add("createGroups="+createGroups);
		params.add("adjustDiagonals="+adjustDiagonals);
		if (adjustDiagonals)
			params.add("diagonals="+matrix.getValue(0,0));
		return params;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		if (attributeList == null)
			attributeList = new AttributeList(network);
		else
			attributeList.setNetwork(network);
	}

	public CyNetwork getNetwork() { return network; }

	public DistanceMetric getDistanceMetric() {
		return metric.getSelectedValue();
	}

	public ClusterMethod getLinkage() {
		return linkage.getSelectedValue();
	}
}

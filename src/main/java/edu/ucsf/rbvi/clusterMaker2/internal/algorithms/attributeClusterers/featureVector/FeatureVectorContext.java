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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.featureVector;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;

public class FeatureVectorContext {
	CyNetwork network;

	@Tunable(description="Distance Metric", gravity=2.0)
	public ListSingleSelection<DistanceMetric> metric = 
		new ListSingleSelection<DistanceMetric>(BaseMatrix.distanceTypes);

	@Tunable(description="Node attributes for cluster", groups="Array sources", 
	         tooltip="You must choose at least 2 node columns for an attribute cluster", gravity=50 )
	public ListMultipleSelection<String> nodeAttributeList = null;

	@Tunable(description="Only use selected nodes/edges for cluster", groups={"Clustering Parameters"}, gravity=60)
	public boolean selectedOnly = false;

	@Tunable(description="Ignore nodes/edges with no data", groups={"Clustering Parameters"}, gravity=62)
	public boolean ignoreMissing = true;

	@Tunable(description="Create a new network with results", groups={"Clustering Parameters"}, gravity=63)
	public boolean createNewNetwork = true;

	@Tunable(description="Only create edges if nodes are closer than this", groups={"Clustering Parameters"}, 
	         dependsOn="createNewNetwork=true", gravity=64)
	public double edgeCutoff = 0.01;

	@Tunable(description="Edge attribute to use for distance values", 
	         groups={"Clustering Parameters", "Advanced Parameters"}, params="displayState=collapsed", gravity=70)
	public String edgeAttribute = "FeatureDistance";

	@Tunable(description="Set missing data to zero (not common)", 
	         groups={"Clustering Parameters", "Advanced Parameters"}, gravity=71)
	public boolean zeroMissing = false;


	public FeatureVectorContext() {
	}

	public List<String> getParams(Matrix matrix) {
		List<String> params = new ArrayList<String>();
		params.add("metric="+metric.getSelectedValue().toString());
		if (nodeAttributeList.getSelectedValues() != null)
			params.add("nodeAttributeList="+nodeAttributeList.getSelectedValues().toString());
		params.add("selectedOnly="+selectedOnly);
		params.add("edgeAttribute="+edgeAttribute);
		params.add("ignoreMissing="+ignoreMissing);
		params.add("zeroMissing="+zeroMissing);
		return params;
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
	}

	public CyNetwork getNetwork() { return network; }

	public DistanceMetric getDistanceMetric() {
		return metric.getSelectedValue();
	}

}

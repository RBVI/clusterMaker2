/* vim: set ts=2: */
/**
 * Copyright (c) 2011 The Regents of the University of California.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This abstract class is the base class for all of the network filters provided by
 * clusterMaker.  Fundamentally, a network filters is an algorithm which functions to
 * modify the results of a previous cluster algorithm by filtering the results.
 */
public abstract class AbstractFilterContext implements ClusterAlgorithmContext {

	public AbstractFilterContext() { 
	}

	public AbstractFilterContext(AbstractFilterContext origin) { 
	}

	/**
 	 * Return the list of attributes that might be considered cluster attributes.  Essentially,
 	 * this returns the list of INTEGER attributes.
 	 *
 	 * @return the list of INTEGER node attributes
 	 */
	public List<String> getClusterAttributeList (CyNetwork network) {
		if (network == null)
			return new ArrayList<String>();
		Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns();
		List<String> intList = new ArrayList<String>();
		for (CyColumn column: columns) {
			if (column.getType().equals(Integer.class)) {
				intList.add(column.getName());
			}
		}
		return intList;
	}

	/**
 	 * Return the attribute that is referenced by the last cluster run.
 	 *
 	 * @return the last attribute
 	 */
	public String getDefaultAttribute(CyNetwork network) {
		if (network == null) return null;

		// Get the cluster type
		CyTable networkTable = network.getDefaultNetworkTable();
		if (networkTable.getColumn(ClusterManager.CLUSTER_TYPE_ATTRIBUTE) == null) return null;

		return network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
	}

}

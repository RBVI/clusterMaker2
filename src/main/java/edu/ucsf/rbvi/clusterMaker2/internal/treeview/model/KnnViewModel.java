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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;

// System imports
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;


// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

// TreeView imports
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TVModel;

/**
 * The ClusterVizModel provides the data that links the results of a cluster run
 * in Cytoscape with the Java TreeView code
 *
 */
public class KnnViewModel extends TreeViewModel {
	private String [] clusterHeaders = {"NODEID", "GROUP"};

	public KnnViewModel(TaskMonitor monitor, CyNetwork network, CyNetworkView view, ClusterManager clusterManager) {
		super(monitor, network, view, clusterManager);

		// Now we have a sort of TreeView model, modify it to add the GROUP information
		if (gidFound()) {
			// We have Gene clusters
			HeaderInfo geneHeader = getGeneHeaderInfo();
			HeaderInfo gtrHeader = getGtrHeaderInfo();
			geneHeader.addName("GROUP", geneHeader.getNumNames());
			// System.out.println("Number of names = "+geneHeader.getNumNames());
			// System.out.println("Number of headers = "+geneHeader.getNumHeaders());
			for (int row = 0; row < geneHeader.getNumHeaders(); row++) {
				geneHeader.setHeader(row, "GROUP", gtrHeader.getHeader(row, "GROUP"));
			}
			gidFound(false);
		}

		if (aidFound()) {
			// We have Gene clusters
			HeaderInfo arrayHeader = getArrayHeaderInfo();
			HeaderInfo atrHeader = getAtrHeaderInfo();
			arrayHeader.addName("GROUP", arrayHeader.getNumNames());
			for (int row = 0; row < arrayHeader.getNumHeaders(); row++) {
				arrayHeader.setHeader(row, "GROUP", atrHeader.getHeader(row, "GROUP"));
			}
			aidFound(false);
		}
	}

	protected String[] getClusterHeaders() {
		return new String [] {"NODEID", "GROUP"};
	}
}

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

// System imports
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans.KMeansCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmedoid.KMedoidCluster;

// TreeView imports
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.FileSet;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LoadException;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewApp;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.KnnViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.KnnViewModel;

/**
 * The ClusterViz class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class KnnView extends TreeView {
	public static String SHORTNAME = "knnview";
	public static String NAME =  "JTree KnnView";

	private static String appName = "ClusterManager KnnView";

	public KnnView(ClusterManager manager) {
		super(manager);
	}

	public KnnView(PropertyConfig propConfig) {
		super(propConfig);
	}

	public void setVisible(boolean visibility) {
		if (viewFrame != null)
			viewFrame.setVisible(visibility);
	}

	public String getAppName() {
		return appName;
	}

	// ClusterViz methods
	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public boolean isAvailable() {
		if (ModelUtils.hasAttribute(myNetwork, myNetwork, ClusterManager.CLUSTER_TYPE_ATTRIBUTE)) {
			String type = myNetwork.getRow(myNetwork).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
			if (!type.equals(KMeansCluster.GROUP_ATTRIBUTE) &&
			    !type.equals(KMedoidCluster.GROUP_ATTRIBUTE) &&
			    !type.equals("autosome heatmap") &&
			    !type.equals("PAM") &&
			    !type.equals("HOMACH"))
				return false;
		}

		if (ModelUtils.hasAttribute(myNetwork, myNetwork, ClusterManager.CLUSTER_NODE_ATTRIBUTE) ||
		    ModelUtils.hasAttribute(myNetwork, myNetwork, ClusterManager.CLUSTER_ATTR_ATTRIBUTE)) {
			return true;
		}

		return false;
	}

	protected void startup() {
		CyProperty cyProperty = manager.getService(CyProperty.class, 
		                                           "(cyPropertyName=cytoscape3.props)");
		// Get our data model
		dataModel = new KnnViewModel(monitor, myNetwork, myView, manager);

		// Set up the global config
		setConfigDefaults(new PropertyConfig(cyProperty, globalConfigName(),"ProgramConfig"));

		// Set up our configuration
		PropertyConfig documentConfig = new PropertyConfig(cyProperty, getShortName(),"DocumentConfig");
		dataModel.setDocumentConfig(documentConfig);

		// Create our view frame
		KnnViewFrame frame = new KnnViewFrame(this, appName);

		// Set the data model
		frame.setDataModel(dataModel);
		frame.setLoaded(true);
		frame.addWindowListener(this);
		frame.setVisible(true);
		geneSelection = frame.getGeneSelection();
		geneSelection.addObserver(this);
		arraySelection = frame.getArraySelection();
		arraySelection.addObserver(this);
		manager.registerService(this, RowsSetListener.class, new Properties());
	}
}

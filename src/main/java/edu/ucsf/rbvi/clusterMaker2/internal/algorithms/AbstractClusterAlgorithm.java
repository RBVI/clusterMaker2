package org.cytoscape.myapp.internal.algorithms;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;

import cytoscape.task.ui.JTaskConfig;

import clusterMaker.ui.ClusterTask;

public abstract class AbstractClusterAlgorithm implements ClusterAlgorithm {
	
	// Common class values
		protected ClusterProperties clusterProperties = null;
		protected PropertyChangeSupport pcs;
		protected boolean debug = false;
		protected boolean createGroups = false;
		protected String clusterAttributeName = null;
		protected boolean canceled = false;
		protected ClusterResults results;
		protected String GROUP_ATTRIBUTE = "_cluster";
		
		public AbstractClusterAlgorithm() {
			pcs = new PropertyChangeSupport(new Object());
		}
		
		/************************************************************************
		 * Abstract inteface -- override these methods!                         *
		 ***********************************************************************/

		public abstract String getShortName();
		public abstract String getName();
		public abstract void updateSettings();
		public abstract JPanel getSettingsPanel();
		public abstract void doCluster(TaskMonitor monitor);

		/************************************************************************
		 * Convenience routines                                                 *
		 ***********************************************************************/

		public void initializeProperties() {
			clusterProperties = new ClusterProperties(getShortName());
		}

		protected void advancedProperties() {
			clusterProperties.add(new Tunable("advancedGroup", "Cytoscape Advanced Settings",
			                                  Tunable.GROUP, new Integer(3),
			                                  new Boolean(true), null, Tunable.COLLAPSABLE));
			clusterProperties.add(new Tunable("clusterAttrName", "Cluster attribute", 
			                                  Tunable.STRING, clusterAttributeName));
			clusterProperties.add(new Tunable("createGroups", "Create metanodes with results", 
			                                  Tunable.BOOLEAN, new Boolean(createGroups)));
			clusterProperties.add(new Tunable("debug", "Enable debugging", 
			                                   Tunable.BOOLEAN, new Boolean(debug))); 
		}
		
		public void updateSettings(boolean force) {
			Tunable t = clusterProperties.get("debug");
			if ((t != null) && (t.valueChanged() || force))
				debug = ((Boolean) t.getValue()).booleanValue();
			t = clusterProperties.get("clusterAttrName");
			if ((t != null) && (t.valueChanged() || force))
				clusterAttributeName = (String) t.getValue();
			t = clusterProperties.get("createGroups");
			if ((t != null) && (t.valueChanged() || force))
				createGroups = ((Boolean) t.getValue()).booleanValue();
		}
		
		public void revertSettings() {
			clusterProperties.revertProperties();
		}

		public ClusterProperties getSettings() {
			return clusterProperties;
		}

		public String toString() { return getName(); }

		public void halt() { canceled = true; }

		public boolean halted() { return canceled; }

		public ClusterResults getResults() { return results; }

		public PropertyChangeSupport getPropertyChangeSupport() {return pcs;}

		public JTaskConfig getDefaultTaskConfig() { return ClusterTask.getDefaultTaskConfig(false); }
		
		public static double mean(Double[] vector) {
			double result = 0.0;
			for (int i = 0; i < vector.length; i++) {
				result += vector[i].doubleValue();
			}
			return (result/(double)vector.length);
		}
		
		// Inefficient, but simple approach to finding the median
		public static double median(Double[] vector) {
			// Clone the input vector
			Double[] vectorCopy = new Double[vector.length];
			for (int i = 0; i < vector.length; i++) {
				vectorCopy[i] = new Double(vector[i].doubleValue());
			}
		
			// sort it
			Arrays.sort(vectorCopy);
		
			// Get the median
			int mid = vector.length/2;
			if (vector.length%2 == 1) {
				return (vectorCopy[mid].doubleValue());
			}
			return ((vectorCopy[mid-1].doubleValue()+vectorCopy[mid].doubleValue()) / 2);
		}



}





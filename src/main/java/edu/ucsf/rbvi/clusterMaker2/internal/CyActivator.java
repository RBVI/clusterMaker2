package edu.ucsf.rbvi.clusterMaker2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

// Java imports
import java.util.Properties;

// Cytoscape imports
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkTaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.tasks.ClusterMakerSettingsTaskFactory;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.clusterMaker2.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			// if haveGUI is false, we don't want to provide any hooks to the treeview
			haveGUI = false;
		}

		// Create our context object.  This will probably keep track of all of the
		// registered clustering algorithms, settings, etc.
		ClusterManager clusterManager = new ClusterManager();

		// Create our Settings menu
		ClusterMakerSettingsTaskFactory settingsTask = new ClusterMakerSettingsTaskFactory(clusterManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.ClusterMaker");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "clusterMaker");
		settingsProps.setProperty(IN_MENU_BAR, "true");
		settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, settingsTask, NetworkTaskFactory.class, settingsProps);
	}

}

package org.cytoscape.myapp.internal.algorithms;


//import cytoscape.layout.LayoutProperties;
import org.cytoscape.view.layout.*;

public class ClusterProperties extends LayoutProperties {

	/**
	 * Constructor.
	 *
	 * @param propertyPrefix String representing the prefix to be used
	 *                       when pulling properties from the property
	 *                       list.
	 */
	public ClusterProperties(String propertyPrefix) {
		super(propertyPrefix);
		setModuleType("clusterMaker");
	}
}

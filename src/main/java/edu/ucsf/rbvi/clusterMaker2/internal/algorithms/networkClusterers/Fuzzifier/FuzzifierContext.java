package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;

public class FuzzifierContext implements ClusterAlgorithmContext {
		
	CyNetwork network;
	
	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Threshold for Fuzzy Membership in a Cluster", groups={"Fuzzifier Advanced Settings"}, params="displayState=collapsed, slider=true",gravity=20.0)
	public BoundedDouble membershipThreshold = new BoundedDouble(0.0, 0.2, 1.0, false, false);
			
	@Tunable(description = "Maximum number of threads", groups={"Fuzzifier Advanced Settings"}, gravity=21.0)
	public int maxThreads = 0;
		
	@Tunable(description = "Distance Metric", groups={"Fuzzifier Advanced Settings"}, gravity=22.0)
	public ListSingleSelection<DistanceMetric> distanceMetric = new ListSingleSelection<DistanceMetric>(DistanceMetric.getDistanceMetricList());
	
	@Tunable(description = "The attributes to consider while clustering", groups={"Fuzzifier Advanced Settings"}, gravity=23.0)
	public ListMultipleSelection<String> attributeList;
		
	@ContainsTunables
	public AdvancedProperties advancedAttributes;	
	
	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();
	
	public FuzzifierContext() {
		advancedAttributes = new AdvancedProperties("__fuzzifierCluster", false);
	}
	
	public FuzzifierContext(FuzzifierContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__fuzzifierCluster", false);
		if (origin.edgeAttributeHandler != null)
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		
		
		membershipThreshold = origin.membershipThreshold;
		maxThreads = origin.maxThreads;
						
		distanceMetric = new ListSingleSelection<DistanceMetric>(DistanceMetric.VALUE_IS_CORRELATION, DistanceMetric.UNCENTERED_CORRELATION, 
								DistanceMetric.CORRELATION, DistanceMetric.ABS_UNCENTERED_CORRELATION,DistanceMetric.ABS_CORRELATION,
								DistanceMetric.SPEARMANS_RANK, DistanceMetric.KENDALLS_TAU, DistanceMetric.EUCLIDEAN, DistanceMetric.CITYBLOCK);
		
		// Retrieving the possible node attributes, required for selecting data to be considered for clustering
		List<CyColumn> columnList =  (List<CyColumn>) network.getDefaultEdgeTable().getColumns();
		List<String> columnNameList = new ArrayList<String>();
		for (CyColumn column : columnList){
			columnNameList.add(column.getName());
		}
		
		attributeList = new ListMultipleSelection<String>(columnNameList);
		
	}	

	public void setUIHelper(TunableUIHelper helper) {
		edgeAttributeHandler.setUIHelper(helper);
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;

		if (edgeAttributeHandler == null)
			edgeAttributeHandler = new EdgeAttributeHandler(network);
		else
			edgeAttributeHandler.setNetwork(network);
	}

	public CyNetwork getNetwork() { return network; }

	public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}
		
}

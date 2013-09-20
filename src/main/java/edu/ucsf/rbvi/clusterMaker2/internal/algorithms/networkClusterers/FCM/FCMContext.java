package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;

public class FCMContext implements ClusterAlgorithmContext {
	
	CyNetwork network;
	
	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Number of clusters", gravity=19.0)
	public int cNumber = -1;
	/*
	@Tunable(description = "Weak edge weight pruning threshold", groups={"FCM Advanced Settings"}, params="displayState=collapsed",gravity=20.0)
	public double clusteringThresh = 1e-15;
	*/
	@Tunable(description = "Threshold for Fuzzy Membership in a Cluster", groups={"FCM Advanced Settings"}, params="displayState=collapsed, slider=true",gravity=20.0)
	public BoundedDouble membershipThreshold = new BoundedDouble(0.0, 0.2, 1.0, false, false);
	
	@Tunable(description = "Number of iterations", groups={"FCM Advanced Settings"}, gravity=21.0)
	public int iterations = 16;
	
	@Tunable(description = "Maximum number of threads", groups={"FCM Advanced Settings"}, gravity=23.0)
	public int maxThreads = 0;
	
	@Tunable(description = "Maximum Number of clusters", groups={"FCM Advanced Settings"}, gravity=24.0)
	public int cMax = 10;
	
	@Tunable(description = "Estimate the number of clusters", groups={"FCM Advanced Settings"}, gravity=25.0)
	public boolean estimateClusterNumber = true;
			
	@Tunable(description = "Fuzziness Index", groups={"FCM Advanced Settings"}, gravity=26.0)
	public double fIndex = 1.5;
	
	@Tunable(description = " Margin allowed for change in fuzzy memberships, to act as end criterion ", groups={"FCM Advanced Settings"}, gravity=27.0)
	public double beta = 0.01;
	
	@Tunable(description = "Distance Metric", groups={"FCM Advanced Settings"}, gravity=28.0)
	public ListSingleSelection<DistanceMetric> distanceMetric = new ListSingleSelection<DistanceMetric>(DistanceMetric.getDistanceMetricList());
	/*
	@Tunable(description = "The attributes to consider while clustering", groups={"FCM Advanced Settings"}, gravity=29.0)
	public ListMultipleSelection<String> attributeList;
	*/	
	@ContainsTunables
	public AdvancedProperties advancedAttributes;
	
	@ContainsTunables
	public NetworkVizProperties vizProperties = new NetworkVizProperties();

	public FCMContext() {
		advancedAttributes = new AdvancedProperties("__fcmCluster", false);
	}
	
	public FCMContext(FCMContext origin) {
		if (origin.advancedAttributes != null)
			advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
		else
			advancedAttributes = new AdvancedProperties("__fcmCluster", false);
		if (origin.edgeAttributeHandler != null)
			edgeAttributeHandler = new EdgeAttributeHandler(origin.edgeAttributeHandler);
		
		
		membershipThreshold = origin.membershipThreshold;
		iterations = origin.iterations;
		maxThreads = origin.maxThreads;
		cMax = origin.cMax;
		cNumber = origin.cNumber;
		fIndex = origin.fIndex;
		beta = origin.beta;
		
		/*
		// Retrieving the possible node attributes, required for selecting data to be considered for clustering
		List<CyColumn> columnList =  (List<CyColumn>) network.getDefaultEdgeTable().getColumns();
		List<String> columnNameList = new ArrayList<String>();
		for (CyColumn column : columnList){
			columnNameList.add(column.getName());
		}
		
		attributeList = new ListMultipleSelection<String>(columnNameList);
		*/
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
	
	public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }
	
}

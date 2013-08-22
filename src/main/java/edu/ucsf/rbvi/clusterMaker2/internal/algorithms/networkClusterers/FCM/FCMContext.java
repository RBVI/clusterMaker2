package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;

public class FCMContext implements ClusterAlgorithmContext {
	
	CyNetwork network;
	
	@ContainsTunables
	public EdgeAttributeHandler edgeAttributeHandler;
	
	@Tunable(description = "Weak edge weight pruning threshold", groups={"FCM Advanced Settings"}, params="displayState=collapsed",gravity=20.0)
	public double clusteringThresh = 1e-15;
	
	@Tunable(description = "Number of iterations", groups={"FCM Advanced Settings"}, gravity=21.0)
	public int iterations = 16;
	
	@Tunable(description = "Maximum number of threads", groups={"FCM Advanced Settings"}, gravity=23.0)
	public int maxThreads = 0;
	
	@Tunable(description = "Maximum Number of clusters", groups={"FCM Advanced Settings"}, gravity=24.0)
	public int cMax = 10;
	
	@Tunable(description = "Number of clusters", groups={"FCM Advanced Settings"}, gravity=25.0)
	public int cNumber = -1;
	
	@Tunable(description = "Fuzziness Index", groups={"FCM Advanced Settings"}, gravity=26.0)
	public double fIndex = 1.5;
	
	@Tunable(description = " Margin allowed for change in fuzzy memberships, to act as end criterion ", groups={"FCM Advanced Settings"}, gravity=27.0)
	public double beta = 0.01;
	
	@Tunable(description = "Distance Metric", groups={"FCM Advanced Settings"}, gravity=28.0)
	public ListSingleSelection<DistanceMetric> distanceMetric;
	
	@Tunable(description = "The attributes to consider while clustering", groups={"FCM Advanced Settings"}, gravity=29.0)
	public ListMultipleSelection<String> attributeList;
		
	@ContainsTunables
	public AdvancedProperties advancedAttributes;

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
		
		
		clusteringThresh = origin.clusteringThresh;
		iterations = origin.iterations;
		maxThreads = origin.maxThreads;
		cMax = origin.cMax;
		cNumber = origin.cNumber;
		fIndex = origin.fIndex;
		beta = origin.beta;
				
		distanceMetric = new ListSingleSelection<DistanceMetric>(DistanceMetric.VALUE_IS_CORRELATION, DistanceMetric.UNCENTERED_CORRELATION, 
								DistanceMetric.CORRELATION, DistanceMetric.ABS_UNCENTERED_CORRELATION,DistanceMetric.ABS_CORRELATION,
								DistanceMetric.SPEARMANS_RANK, DistanceMetric.KENDALLS_TAU, DistanceMetric.EUCLIDEAN, DistanceMetric.CITYBLOCK);
		
		// Retrieving the possible node attributes, required for selecting data to be considered for clustering
		List<CyColumn> columnList =  (List<CyColumn>) network.getDefaultNodeTable().getColumns();
		List<String> columnNameList = new ArrayList<String>();
		for (CyColumn column : columnList){
			columnNameList.add(column.getName());
		}
		
		attributeList = new ListMultipleSelection<String>(columnNameList);
		
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

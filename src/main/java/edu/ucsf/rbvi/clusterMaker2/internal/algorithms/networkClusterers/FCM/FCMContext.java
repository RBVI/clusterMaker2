package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

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
	public double beta;
	
	
	public ListSingleSelection<DistanceMetric> metric;
	
	@Tunable(description = "Distance Metric", groups={"FCM Advanced Settings"}, gravity=28.0)
	public DistanceMetric getMetric(){
		return metric.getSelectedValue();
	}
	
	public void setMetric(DistanceMetric newMetric){
		
		metric.setSelectedValue(newMetric);
		System.out.println("Setting the value of Distance Metric to: " + metric.getSelectedValue()  );
	}
	
	
	public ListMultipleSelection<String> attributeList;
	
	@Tunable(description = "The attribute to use to get the weights", groups={"FCM Advanced Settings"}, gravity=29.0)
	public List<String> getAttributeList(){
		return attributeList.getSelectedValues();
	}
	
	public void setAttributeList(List<String> newAttributeList){
		
		attributeList.setSelectedValues(newAttributeList);
		System.out.println("Setting the Attribute List to: " + attributeList.getSelectedValues() );
	}
    
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
		
		//metric = origin.metric;
		//attributeList = origin.attributeList;
		metric.setSelectedValue(origin.metric.getSelectedValue());		
		attributeList.setSelectedValues(origin.attributeList.getSelectedValues());
				
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

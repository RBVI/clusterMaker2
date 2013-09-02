package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.KClusterAttributes;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.SplitCost;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.SummaryMethod;

public class HopachPAMContext{
	
	@Tunable(description="Distance metric", 
			 groups={"Basic HOPACH Tuning"}, gravity=10)
	public ListSingleSelection<DistanceMetric> metric =
		new ListSingleSelection<DistanceMetric>(BaseMatrix.distanceTypes);

	@Tunable(description="Split cost type",  
			 groups={"Basic HOPACH Tuning"},gravity=11)
	public ListSingleSelection<SplitCost> splitCost =
		new ListSingleSelection<SplitCost>(SplitCost.values());

	@Tunable(description="Value summary method",  
			 groups={"Basic HOPACH Tuning"},gravity=12)
	public ListSingleSelection<SummaryMethod> summaryMethod =
		new ListSingleSelection<SummaryMethod>(SummaryMethod.values());

	@Tunable(description="Maximum number of spliting level",  
			 groups={"Basic HOPACH Tuning"},gravity=13)
	public int maxLevel = 9;
	
	@Tunable(description="Maximum number of clusters at each level",  
			 groups={"Basic HOPACH Tuning"},gravity=14)
	public int K = 9;
		
	@Tunable(description="Maximum number of subclusters at each level",  
			 groups={"Basic HOPACH Tuning"},gravity=15)
	public int L = 9;
	
	@Tunable(description="Force splitting at initial level",  
			 groups={"Basic HOPACH Tuning"},gravity=16)
	public boolean forceInitSplit = false;
	
	@Tunable(description="Minimum cost reduction for collapse",  
			 groups={"Basic HOPACH Tuning"},gravity=17)
	public double minCostReduction = 0.0;
	
	@ContainsTunables
	public AttributeList attributeList = null;
		
	@Tunable(description="Use only selected nodes/edges for cluster",
			groups={"HOPACH Parameters"}, gravity=100)
	public boolean selectedOnly = false;

	@Tunable(description="Cluster attributes as well as nodes",
			groups={"HOPACH Parameters"}, gravity=101)
	public boolean clusterAttributes = false;

	@Tunable(description="Create groups from clusters", groups={"Visualization Options"}, gravity=150)
	public boolean createGroups = false;

	@Tunable(description="Show HeatMap when complete", groups={"Visualization Options"}, gravity=151)
	public boolean showUI = false;

	private CyNetwork network;

	public KClusterAttributes kcontext = new KClusterAttributes();
	
	public HopachPAMContext() {
		kcontext.useSilhouette = false;
	}
	
	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		if (attributeList == null)
			attributeList = new AttributeList(network, true);
		else
			attributeList.setNetwork(network);

	}

	public CyNetwork getNetwork() { return network; }

	public DistanceMetric getDistanceMetric() {
		return metric.getSelectedValue();
	}

	public List<String> getParams() {
		List<String> params = new ArrayList<String>();
		params.add("metric="+metric.getSelectedValue().toString());
		params.add("nodeAttributeList="+attributeList.getNodeAttributeList().toString());
		params.add("selectedOnly="+selectedOnly);
		params.add("clusterAttributes="+clusterAttributes);
		params.add("createGroups="+createGroups);
		return params;
	}


}

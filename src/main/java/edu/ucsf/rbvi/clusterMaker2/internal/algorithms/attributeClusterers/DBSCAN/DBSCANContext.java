package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.KClusterAttributes;

public class DBSCANContext {
	CyNetwork network;

	@ContainsTunables
	public KClusterAttributes kcluster = new KClusterAttributes();

	@Tunable(description="Distance Metric", gravity=11)
	public ListSingleSelection<DistanceMetric> metric = 
		new ListSingleSelection<DistanceMetric>(BaseMatrix.distanceTypes);
	
	@ContainsTunables
	public AttributeList attributeList = null;

	public boolean selectedOnly = false;
	
	@Tunable(description="Use only selected nodes/edges for cluster", 
	         groups={"FFT Parameters"}, gravity=100)
	public boolean getselectedOnly() { return selectedOnly; }
	
	public void setselectedOnly(boolean sel) {
		if (network != null && this.selectedOnly != sel) kcluster.updateKEstimates(network, sel);
		this.selectedOnly = sel;
	}

	@Tunable(description="Cluster attributes as well as nodes", 
	         groups={"FFT Parameters"}, gravity=101)
	public boolean clusterAttributes = false;
	
	@Tunable(description="Create groups from clusters", groups={"Visualization Options"}, gravity=150)
	public boolean createGroups = false;

	@Tunable(description="Show HeatMap when complete", groups={"Visualization Options"}, gravity=151)
	public boolean showUI = false;


	public DBSCANContext() {
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		if (attributeList == null)
			attributeList = new AttributeList(network);
		else
			attributeList.setNetwork(network);

		kcluster.updateKEstimates(network, selectedOnly);
	}

	public CyNetwork getNetwork() { return network; }

	public DistanceMetric getDistanceMetric() {
		return metric.getSelectedValue();
	}

	public List<String> getParams() {
		List<String> params = new ArrayList<String>();
		kcluster.addParams(params);		
		params.add("metric="+metric.getSelectedValue().toString());
		params.add("nodeAttributeList="+attributeList.getNodeAttributeList().toString());
		params.add("edgeAttribute="+attributeList.getEdgeAttribute());
		params.add("selectedOnly="+selectedOnly);
		params.add("clusterAttributes="+clusterAttributes);
		params.add("createGroups="+createGroups);
		return params;
	}
}



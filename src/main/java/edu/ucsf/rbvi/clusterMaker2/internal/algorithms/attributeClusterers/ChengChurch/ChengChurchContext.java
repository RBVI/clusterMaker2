package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;

public class ChengChurchContext {
	CyNetwork network;

	@Tunable(description = "Number of Biclusters", gravity=1.0)
	public int nClusters = 1;
	
	@Tunable(description = "Maximum Mean Squared Residue allowed for a Bicluster", gravity=2.0)
	public double delta = 1.0;
	
	@Tunable(description="Threshold for Multiple Node Deletion", gravity=3)
	public BoundedDouble alpha = new BoundedDouble(1.0, 1.1, Double.MAX_VALUE, true, true);
	
	@ContainsTunables
	public AttributeList attributeList = null;

	public boolean selectedOnly = false;
	
	@Tunable(description="Use only selected nodes/edges for cluster", 
	         groups={"ChengChurch Parameters"}, gravity=100)
	public boolean getselectedOnly() { return selectedOnly; }
	
	public void setselectedOnly(boolean sel) {
		//if (network != null && this.selectedOnly != sel) kcluster.updateKEstimates(network, sel);
		this.selectedOnly = sel;
	}

	@Tunable(description="Cluster attributes as well as nodes", 
	         groups={"ChengChurch Parameters"}, gravity=101)
	public boolean clusterAttributes = false;
	
	@Tunable(description="Create groups from clusters", groups={"Visualization Options"}, gravity=150)
	public boolean createGroups = false;

	@Tunable(description="Show HeatMap when complete", groups={"Visualization Options"}, gravity=151)
	public boolean showUI = false;


	public ChengChurchContext() {
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;
		if (attributeList == null)
			attributeList = new AttributeList(network);
		else
			attributeList.setNetwork(network);

		//kcluster.updateKEstimates(network, selectedOnly);
	}

	public CyNetwork getNetwork() { return network; }

	public List<String> getParams() {
		List<String> params = new ArrayList<String>();
		params.add("nodeAttributeList="+attributeList.getNodeAttributeList().toString());
		params.add("edgeAttribute="+attributeList.getEdgeAttribute());
		params.add("selectedOnly="+selectedOnly);
		params.add("clusterAttributes="+clusterAttributes);
		params.add("createGroups="+createGroups);
		return params;
	}

}

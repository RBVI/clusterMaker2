package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalizationContext {
	 private CyNetwork network;
	 public ClusterManager manager;
	
	// Normalization
	@Tunable
	(description = "Normalization", 
	exampleStringValue = "None",
	tooltip = "<html>The algorithm assumes value between 0 and 1.  Basic normalization normalizes each attribute to 0-1.</html>",
	groups = "Attribute normalization", gravity = 1.0)
  public ListSingleSelection<String> normalization = new ListSingleSelection<String>("Basic", "None");
	
	// Two-tailed attributes : absolute value, pos values, neg values
	@Tunable
	(description = "Two-tailed values",
	exampleStringValue = "Absolute value",
	tooltip = "<html>The algorithm assumes value between 0 and 1</html>",
	groups = "Attribute normalization", gravity = 2.0)
	public ListSingleSelection<String> twoTailedValues = new ListSingleSelection<String>("Absolute value", "Only positive values", "Only negative values");
	
	public NormalizationContext(ClusterManager manager) {
    this(manager, manager.getNetwork());
  }

	public NormalizationContext(ClusterManager manager, CyNetwork network) {
		this.manager = manager;
    this.network = network;
	}
	
  public void setNetwork(CyNetwork network) {
    this.network = network;
  }

  public CyNetwork getNetwork() {
    return network;
  }

  public Map<CyIdentifiable,double[]> normalizeEdges(List<String> attributeList) {
    List<CyEdge> edgeList = network.getEdgeList();
    return normalize(attributeList, edgeList);
  }

  public Map<CyIdentifiable,double[]> normalizeNodes(List<String> attributeList) {
    List<CyNode> nodeList = network.getNodeList();
    return normalize(attributeList, nodeList);
  }

  public Map<CyIdentifiable,double[]> normalize(List<String> attributeList, List<? extends CyIdentifiable> objs) {
    Map<CyIdentifiable, double[]> returnMap = new HashMap<>();
    if (network == null) network = manager.getNetwork();

    int nAttributes = attributeList.size();

    boolean negativeOnly = twoTailedValues.getSelectedValue().equals("Only negative values");
    boolean positiveOnly = twoTailedValues.getSelectedValue().equals("Only positive values");

    // Go through the attributes and find the minimum and maximum values
    Map<String, double[]> minMax = new HashMap<>();
    for (String attr: attributeList) {
      minMax.put(attr, new double[]{Double.MAX_VALUE, Double.MIN_VALUE});
    }

    // Get all of the values
    for (CyIdentifiable obj: objs) {
      CyRow row = network.getRow(obj);
      double[] values = new double[attributeList.size()];
      for (int index = 0; index < nAttributes; index++) {
        Double v;
        try {
          v = row.get(attributeList.get(index), Double.class);
        } catch (ClassCastException e) {
          Integer intV = row.get(attributeList.get(index), Integer.class);
          v = Double.valueOf(intV);
        }
        if (v == null)
          values[index] = 0.0d;   // values[index] = Double.NaN;  -- if we're going to use NaN we need to account for it everywhere
        else if (v < 0) {
          if (positiveOnly)
            values[index] = 0.0d;
          else
            values[index] = Math.abs(v);
        } else if (negativeOnly)
          values[index] = 0.0d;
        else
          values[index] = v;

        double[] myMinMax = minMax.get(attributeList.get(index));
        myMinMax[0] = Math.min(myMinMax[0], values[index]);
        myMinMax[1] = Math.max(myMinMax[1], values[index]);
      }
      returnMap.put(obj, values);
    }

    if (normalization.getSelectedValue().equals("None"))
      return returnMap;

    // OK, now that we have the minMax, do the normalization, handling two-tailed values as we go
    for (int index = 0; index < nAttributes; index++) {
      double[] myMinMax = minMax.get(attributeList.get(index));
      for (CyIdentifiable obj: returnMap.keySet()) {
        double value = returnMap.get(obj)[index];
        value = (value - myMinMax[0])/myMinMax[1];
        returnMap.get(obj)[index] = value;
      }
    }

    return returnMap;
  }

}

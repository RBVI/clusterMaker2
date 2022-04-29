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
		this.manager = manager;
    network = this.manager.getNetwork();
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

    int nAttributes = attributeList.size();

    // Get all of the values
    for (CyIdentifiable obj: objs) {
      CyRow row = network.getRow(obj);
      double[] values = new double[attributeList.size()];
      for (int index = 0; index < nAttributes; index++) {
        Double v = row.get(attributeList.get(index), Double.class);
        if (v == null)
          values[index] = Double.NaN;
        else
          values[index] = v;
      }
      returnMap.put(obj, values);
    }

    if (normalization.getSelectedValue().equals("None"))
      return returnMap;

    // Go through the attributes and find the minimum and maximum values
    Map<String, double[]> minMax = new HashMap<>();
    for (String attr: attributeList) {
      minMax.put(attr, new double[]{Double.MAX_VALUE, Double.MIN_VALUE});
    }

    for (CyIdentifiable obj: returnMap.keySet()) {
      double[] values = returnMap.get(obj);
      for (int index = 0; index < values.length; index++) {
        Double value = values[index];
        double[] myMinMax = minMax.get(attributeList.get(index));
        if (value < myMinMax[0])
          myMinMax[0] = value;
        if (value > myMinMax[1])
          myMinMax[1] = value;
      }
    }

    // Now, adjust the min/max based on how the user want's to handle two-tailed values
    for (int index = 0; index < nAttributes; index++) {
      double[] myMinMax = minMax.get(attributeList.get(index));
      myMinMax = adjustMinMax(myMinMax);
      minMax.put(attributeList.get(index), myMinMax);
    }

    // OK, now that we have the minMax, do the normalization, handling two-tailed values as we go
    for (int index = 0; index < nAttributes; index++) {
      double[] myMinMax = minMax.get(attributeList.get(index));
      for (CyIdentifiable obj: returnMap.keySet()) {
        double value = returnMap.get(obj)[index];
        value = normalize(value, myMinMax);
        returnMap.get(obj)[index] = value;
      }
    }

    return returnMap;
  }

  double normalize(double value, double[] minMax) {
    if (value < 0 && twoTailedValues.getSelectedValue().equals("Absolute value")) {
      value = Math.abs(value);
    } else if (value < 0 && twoTailedValues.getSelectedValue().equals("Negative only")) {
      value = Math.abs(value);
    } else if (value > 0 && twoTailedValues.getSelectedValue().equals("Negative only")) {
      value = 0.0;
    } else if (value < 0 && twoTailedValues.getSelectedValue().equals("Positive only")) {
      value = 0.0;
    }

    value = (value - minMax[0])/minMax[1];
    return value;
  }

  double[] adjustMinMax(double[] minMax) {
    if (minMax[0] < 0) {
      if (twoTailedValues.getSelectedValue().equals("Absolute value")) {
        double min = Math.min(Math.abs(minMax[0]), Math.abs(minMax[1]));
        double max = Math.max(Math.abs(minMax[0]), Math.abs(minMax[1]));
        minMax[0] = min; minMax[1] = max;
      } else if (twoTailedValues.getSelectedValue().equals("Negative only")) {
        minMax[1] = Math.abs(minMax[0]);
        minMax[0] = 0.0;
      } else if (twoTailedValues.getSelectedValue().equals("Positive only")) {
        minMax[0] = 0.0;
      }
    }
    return minMax;
  }
	
}

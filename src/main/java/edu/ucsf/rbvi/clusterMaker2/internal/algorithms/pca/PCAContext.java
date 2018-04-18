/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import java.util.List;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

/**
 *
 * @author root
 */
public class PCAContext {
	CyNetwork network;

	@Tunable(description="Node attributes for PCA", groups={"Array Sources"}, 
	         longDescription="Select the node table columns to be used for calculating PCA.  "+
	                         "Note that at least 2 node columns are usually required.",
	         exampleStringValue="gal1RGexp,gal4RGExp,gal80Rexp",
	         tooltip="You must choose at least 2 node columns for an attribute PCA", gravity=7.0)
	public ListMultipleSelection<String> nodeAttributeList = null;

	@Tunable(description = "Only use selected nodes for PCA", groups={"Array Sources"}, gravity=8.0,
	         longDescription="If this is set to ```true```, only the selected nodes will be "+
	                         "included in the resulting tSNE plot",
	         exampleStringValue="false")
	public boolean selectedOnly = false;

	@Tunable(description="Ignore nodes with no data", groups={"Array Sources"}, gravity=9.0,
	         longDescription="Ignore any nodes that have missing data.  If this is not selected, "+
	                         "missing values will be set to 0.",
	         exampleStringValue="true")
	public boolean ignoreMissing = true;

	@Tunable(description="Type of matrix to use for PCA",
	         tooltip="If all of the data is of the same type, use covariance, otherwise choose correlation",
	         longDescription="This provides the option of changing how the matrix will be constructed for "+
	                         "PCA.  If all of the data is of the same type, you should use ```covariance```.  If "+
	                         "on the other hand, your data is of different types, ```use correlation```",
	         exampleStringValue="covariance",
					 groups={"PCA Parameters"},
					 gravity=10.0)
	public ListSingleSelection<String> matrixType = new ListSingleSelection<String>("covariance", "correlation");

	@Tunable(description="Standardize data?",
	         tooltip="This will standardize the data such that each column has 0 mean and stdev of 1",
	         longDescription="If set to ```true```, standardize the data by ensuring that each column "+
	                         "has a mean of 0 and a standard deviation of 1",
	         exampleStringValue="false",
					 groups={"PCA Parameters"},
					 gravity=11.0)
	public boolean standardize = false;

	@Tunable(description = "Create PCA Result Panel", 
	         longDescription="If set to ```true```, show the PCA results panel which will allow selection of "+
	                         "all of the nodes in each of the principal components",
	         exampleStringValue="false",
	         groups={"Result Options"}, gravity=83.0)
	public boolean pcaResultPanel = false;

	@Tunable(description = "Create PCA scatter plot", 
	         longDescription="If set to ```true```, show the PCA scatter plot "+
	                         "after completing the calculation",
	         exampleStringValue="true",
	         groups={"Result Options"}, gravity=84.0)
	public boolean pcaPlot = true;

	@Tunable(description = "Minimum variance for components (%)",
	         longDescription="The minimum variance that a component must have to be included in the results.",
	         exampleStringValue="10.0",
	         groups={"Result Options"}, gravity=85.0)
	public double minVariance = 10.0;

	public PCAContext(){

	}

	public void setNetwork(CyNetwork network){
		if (this.network != null && this.network.equals(network))
			return;

		this.network = network;

		if (network != null)
			nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
	}

	public List<String> getNodeAttributeList() {
		if (nodeAttributeList == null) return null;
		List<String> attrs = nodeAttributeList.getSelectedValues();
		if (attrs == null || attrs.isEmpty()) return null;
		if ((attrs.size() == 1) &&
		    (attrs.get(0).equals(ModelUtils.NONEATTRIBUTE))) return null;
		return attrs;
	}	

	// public String getPCAType() { return pcaType.getSelectedValue(); }
	// public String getInputValue() { return inputValue.getSelectedValue(); }

	public CyNetwork getNetwork() { return network; }

	// public void setUIHelper(TunableUIHelper helper) { edgeAttributeHandler.setUIHelper(helper); }

}

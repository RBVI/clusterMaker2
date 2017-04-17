package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;



public class tSNEContext {
	CyNetwork network;

	//Tunables
	@ContainsTunables
	public AttributeList attributeList = null;


	public boolean selectedOnly = false;
	@Tunable(description="Use only selected nodes/edges for cluster",
			groups={"t-SNE Advanced Settings"}, gravity=65)
	public boolean getselectedOnly() { return selectedOnly; }
	public void setselectedOnly(boolean sel) {
		if (network != null && this.selectedOnly != sel) 
		this.selectedOnly = sel;
	}

	@Tunable(description="Ignore nodes with missing data",
			groups={"t-SNE Advanced Settings"}, gravity=66)
	public boolean ignoreMissing = true;

	@Tunable(description="Initial Dimensions", groups={"t-SNE Advanced Settings"}, gravity=66, format="#0")
	public int dimensions=-1;

	@Tunable(description="Perplexity", groups={"t-SNE Advanced Settings"}, gravity=67)
	public double perplixity=20;

	@Tunable(description="Number of Iterations", groups={"t-SNE Advanced Settings"}, gravity=68)
	public int iterations=2000;

	/*
	 * Add at some point
	 *
	 * @Tunable(description="Use Principal Component Analysis to pre-filter data", groups={"t-SNE Advanced Settings"}, gravity=69)
	 * public boolean usePCA = false;
	 */

	public tSNEContext(){
	}

	public tSNEContext(tSNEContext origin) {

		if (attributeList == null){
			attributeList = new AttributeList(network);
		} else{
			attributeList.setNetwork(network);
		}
	}

	public void setNetwork(CyNetwork network) {
		if (this.network != null && this.network.equals(network))
			return; // Nothing to see here....

		this.network = network;

		if (attributeList == null){
			attributeList = new AttributeList(network);
		} else{
			attributeList.setNetwork(network);
		}
	}

	public CyNetwork getNetwork() { return network; }
}

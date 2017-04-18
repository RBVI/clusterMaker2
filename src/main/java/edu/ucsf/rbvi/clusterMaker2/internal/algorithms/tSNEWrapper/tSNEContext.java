package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEWrapper;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;

import com.jujutsu.tsne.barneshut.TSneConfiguration;


public class tSNEContext implements TSneConfiguration {
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

	@Tunable(description="Ignore nodes with missing data", groups={"t-SNE Advanced Settings"}, gravity=66)
	public boolean ignoreMissing = true;

	@Tunable(description="Initial Dimensions", groups={"t-SNE Advanced Settings"}, gravity=66, format="#0")
	public int dimensions=-1;

	@Tunable(description="Perplexity", groups={"t-SNE Advanced Settings"}, gravity=67)
	public double perplexity=20;

	@Tunable(description="Number of Iterations", groups={"t-SNE Advanced Settings"}, gravity=68)
	public int iterations=2000;

	@Tunable(description="Use Barnes-Hut approximation", groups={"t-SNE Advanced Settings"}, gravity=69)
	public boolean useBarnesHut=false;

	@Tunable(description="Theta value for Barnes-Hut", dependsOn="useBarnesHut=true", groups={"t-SNE Advanced Settings"}, gravity=70)
	public BoundedDouble theta=new BoundedDouble(0.0, 0.9, 1.0, false, false);

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

	double[][] Xin = null;
	public double[][] getXin() {
		return Xin;
	}

	public void setXin(double[][] xin) {
		Xin = xin;
	}

	int outputDims = 2;
	public int getOutputDims() {
		return outputDims;
	}

	public void setOutputDims(int n) {
		outputDims = n;
	}

	public int getInitialDims() {
		return dimensions;
	}

	public void setInitialDims(int initial_dims) {
		dimensions = initial_dims;
	}

	public double getPerplexity() {
		return perplexity;
	}

	public void setPerplexity(double perplexity) {
		this.perplexity = perplexity;
	}

	public int getMaxIter() {
		return iterations;
	}

	public void setMaxIter(int max_iter) {
		iterations = max_iter;
	}

	boolean usePca = true;
	public boolean usePca() {
		return usePca;
	}

	public void setUsePca(boolean use_pca) {
		usePca = use_pca;
	}

	public double getTheta() {
		return theta.getValue();
	}

	public void setTheta(double theta) {
		this.theta.setValue(theta);
	}

	boolean silent = false;
	public boolean silent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean printError() {
		return false;
	}

	public void setPrintError(boolean print_error) {
	}

	public int getXStartDim() {
		if (Xin != null && Xin[0] != null) return Xin[0].length;
		return 0;
	}

	public int getNrRows() {
		if (Xin != null) return Xin.length;
		return 0;
	}

}

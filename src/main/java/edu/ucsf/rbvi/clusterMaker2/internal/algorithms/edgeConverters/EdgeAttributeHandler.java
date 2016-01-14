package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters;


import java.awt.Button;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.BasicTunableHandlerFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.DistanceConverter1;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.DistanceConverter2;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.LogConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.NegLogConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.NoneConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.SCPSConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.ThresholdHeuristic;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.HistogramDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.HistoChangeListener;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class EdgeAttributeHandler implements HistoChangeListener, RequestsUIHelper {
	

	private CyMatrix matrix = null;
	private CyNetwork network = null;
	
	private List<EdgeWeightConverter>converters = null;

	private TunableUIHelper helper = null;
	
	private ListSingleSelection<String> attribute ;
	
	@Tunable(description = "Array Sources", groups={"Source for array data"}, params="displayState=expanded", gravity=10.0)
	public ListSingleSelection<String> getattribute(){
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		return attribute;
	}
	public void setattribute(ListSingleSelection<String> attr) { }
	
	
	@Tunable(description = "Cluster only selected nodes", groups={"Source for array data"}, gravity=11.0)
	public boolean selectedOnly ;
	
	
	@Tunable(description = "Edge weight conversion", groups={"Source for array data"}, gravity=12.0)
	public ListSingleSelection<EdgeWeightConverter> edgeWeighter;
	
	public BoundedDouble edgeCutOff;
	@Tunable(description="Edge cut off", 
	         listenForChange={"selectedOnly", "attribute", "edgeWeighter", "EdgeHistogram"},
	         groups={"Source for array data", "Edge weight cutoff"}, 
	         params="slider=true", gravity=13.0, 
	         tooltip="Edges less than this value will not be included")
	public BoundedDouble getedgeCutOff() {
		updateBounds();
		return edgeCutOff;
	}
	public void setedgeCutOff(BoundedDouble value) { }
	
	public boolean edgeHistogram = false;
	@Tunable(description="Show/Hide Edge Weight Histogram", 
	         groups={"Source for array data", "Edge weight cutoff"}, context="gui", gravity=14.0)
	public boolean getEdgeHistogram() { return edgeHistogram; }
	public void setEdgeHistogram(boolean eh) {
		edgeHistogram = eh;
		if (edgeHistogram) {
			// Popup the histogram dialog
			createHistogramDialog();
		} else {
			// Dispose of the histogram dialog
			if (histo != null)
				histo.setVisible(false);
		}
	}

	@Tunable(description = "Assume edges are undirected", 
	         groups={"Source for array data", "Array data adjustments"}, gravity=15.0)
	public boolean undirectedEdges = true;
	
	@Tunable(description = "Adjust loops before clustering", 
	         groups={"Source for array data", "Array data adjustments"}, gravity=16.0)
	public boolean adjustLoops = true;
	
	private HistogramDialog histo = null;
	
	// TODO: Convert this to a listener
	public EdgeAttributeHandler(CyNetwork network) {
		this(network, true);
	}

	public EdgeAttributeHandler(CyNetwork network, boolean initialize) {
		this.network = network;
		// Create all of our edge weight converters
		converters = new ArrayList<EdgeWeightConverter>();
		converters.add(new NoneConverter());
		converters.add(new DistanceConverter1());
		converters.add(new DistanceConverter2());
		converters.add(new LogConverter());
		converters.add(new NegLogConverter());
		converters.add(new SCPSConverter());

		if (initialize)
			initializeTunables();
	}

	public EdgeAttributeHandler(EdgeAttributeHandler clone) {
		converters = clone.converters;
		selectedOnly = clone.selectedOnly;
		attribute = new ListSingleSelection<String>(clone.attribute.getPossibleValues());
		attribute.setSelectedValue(clone.attribute.getSelectedValue());
		edgeCutOff = new BoundedDouble(clone.edgeCutOff.getLowerBound(), clone.edgeCutOff.getValue(),
		                               clone.edgeCutOff.getUpperBound(), false, false);
		adjustLoops = clone.adjustLoops;
		undirectedEdges = clone.undirectedEdges;
		edgeWeighter = new ListSingleSelection<EdgeWeightConverter>(clone.edgeWeighter.getPossibleValues());
		edgeWeighter.setSelectedValue(clone.edgeWeighter.getSelectedValue());
	}

	public void initializeTunables() {
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		
		EdgeWeightConverter[] edgeWeightConverters = converters.toArray(new EdgeWeightConverter[1]);
		if (edgeWeightConverters.length > 0){
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>(edgeWeightConverters);	
			edgeWeighter.setSelectedValue(edgeWeightConverters[0]);
		}
		else{
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>();
		}
		
		edgeCutOff = new BoundedDouble(0.0, 0.0, 100.0, false, false);
	}

	public void setNetwork(CyNetwork network) {
		this.network = network;
		attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
		this.matrix = null;
	}

	public BoundedDouble updateBounds() {
		double cutOff = 0.0;
		double max = 100.0;
		double min = 0.0;

		if (attribute == null || attribute.getSelectedValue().equals("--None--")) {
			// System.out.println("Setting bounds to: "+min+","+max);
			edgeCutOff.setBounds(min, max);
			return edgeCutOff;
		}

		// System.out.println("Getting distance matrix");
		// this.matrix = new DistanceMatrix(network, attribute.getSelectedValue(), 
		//                                  selectedOnly, edgeWeighter.getSelectedValue());
		this.matrix = CyMatrixFactory.makeLargeMatrix(network, attribute.getSelectedValue(), 
		                                              selectedOnly, edgeWeighter.getSelectedValue(),
																									undirectedEdges, edgeCutOff.getValue());
		max = matrix.getMaxValue();
		min = matrix.getMinValue();

		// System.out.println("Setting cutoff to: "+cutOff);
		if (max != edgeCutOff.getUpperBound() || min != edgeCutOff.getLowerBound()) {
			// System.out.println("Changing bounds to: "+min+","+max);
			edgeCutOff.setBounds(min, max);
		}

		// System.out.println("Setting bounds to: "+min+"-"+max+" and cutOff to "+cutOff);
		return edgeCutOff;
	}

	public void histoValueChanged(double cutoffValue) {
		// System.out.println("New cutoff value: "+cutoffValue);
		edgeCutOff.setValue(cutoffValue);
	}

	public void createHistogramDialog() {
		if (this.matrix == null)
			getMatrix();

		ThresholdHeuristic thueristic = new ThresholdHeuristic(matrix);

		// TODO: There really needs to be a better way to calculate the number of bins
		int nbins = 100;
		if (matrix.nRows()*matrix.nColumns() < 100)
			nbins = 10;
		// else if (dataArray.length > 10000)
		// 	nbins = 1000;
		String title = "Histogram for "+attribute.getSelectedValue()+" edge attribute";
		histo = new HistogramDialog(helper.getParent(), title, matrix, nbins, thueristic);
		histo.pack();
		histo.setVisible(true);
		histo.addHistoChangeListener(this);
	}

	public CyMatrix getMatrix() {
		if (this.matrix == null) {
			if (attribute.getSelectedValue() == null) return null;
			this.matrix = CyMatrixFactory.makeLargeMatrix(network, attribute.getSelectedValue(), 
			                                              selectedOnly, edgeWeighter.getSelectedValue(),
																										undirectedEdges, edgeCutOff.getValue());
		}

		if (adjustLoops)
			this.matrix.adjustDiagonals();

		return this.matrix;
	}

	public void setParams(List<String> params) {
		if (adjustLoops)
			params.add("adjustLoops");
		if (edgeCutOff != null)
			params.add("edgeCutOff="+edgeCutOff.getValue().toString());
		if (selectedOnly)
			params.add("selectedOnly");
		if (undirectedEdges)
			params.add("undirectedEdges");
		params.add("converter="+edgeWeighter.getSelectedValue().getShortName());
		params.add("dataAttribute="+attribute.getSelectedValue());
	}

	public EdgeWeightConverter getConverter(String converterName) {
		if (converterName == null)
			return null;

		for (EdgeWeightConverter ewc: converters) {
			if (converterName.equals(ewc.getShortName()))
				return ewc;
		}
		return null;
	}

	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;
	}
}

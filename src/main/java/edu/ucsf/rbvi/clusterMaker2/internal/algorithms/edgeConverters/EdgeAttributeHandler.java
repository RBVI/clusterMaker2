package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters;


import java.awt.Button;
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
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.DistanceConverter1;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.DistanceConverter2;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.LogConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.NegLogConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.NoneConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.SCPSConverter;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.ThresholdHeuristic;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.HistogramDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.HistoChangeListener;

public class EdgeAttributeHandler implements HistoChangeListener {
	
	public static final String NONEATTRIBUTE = "--None--";

	private DistanceMatrix matrix = null;
	private CyNetwork network = null;
	
	private List<EdgeWeightConverter>converters = null;
	
	private ListSingleSelection<String> attribute ;
	
	@Tunable(description = "Array Sources", groups={"Source for array data"}, gravity=10.0)
	public ListSingleSelection<String> getattribute(){
		updateAttributeList();
		return attribute;
	}
	public void setattribute(ListSingleSelection<String> attr) { }
	
	
	@Tunable(description = "Cluster only selected nodes", groups={"Source for array data"}, gravity=11.0)
	public boolean selectedOnly ;
	
	
	@Tunable(description = "Edge weight conversion", groups={"Source for array data"}, gravity=12.0)
	public ListSingleSelection<EdgeWeightConverter> edgeWeighter;
	
	@Tunable(description="Cut off value for edge consideration", 
	         groups={"Source for array data", "Edge weight cutoff"}, 
	         params="slider=true", gravity=13.0)
	public BoundedDouble edgeCutOff; // TODO: set the bounds based on the range of the converted edges
	
	public boolean edgeHistogram = false;
	@Tunable(description="Set Edge Cutoff Using Histogram", 
	         groups={"Source for array data", "Edge weight cutoff"}, context="gui", gravity=14.0)
	public boolean getEdgeHistogram() { return edgeHistogram; }
	public void setEdgeHistogram(boolean eh) {
		edgeHistogram = eh;
		if (edgeHistogram) {
			// Popup the histogram dialog
		} else {
			// Dispose of the histogram dialog
		}
	}

	@Tunable(description = "Assume edges are undirected", 
	         groups={"Source for array data", "Array data adjustments"}, gravity=15.0)
	public boolean undirectedEdges = false;
	
	@Tunable(description = "Adjust loops before clustering", 
	         groups={"Source for array data", "Array data adjustments"}, gravity=16.0)
	public boolean adjustLoops = false;
	
	private HistogramDialog histo = null;
	
	// TODO: Convert this to a listener
	public EdgeAttributeHandler(CyNetwork network) {
		this.network = network;
		// Create all of our edge weight converters
		converters = new ArrayList<EdgeWeightConverter>();
		converters.add(new NoneConverter());
		converters.add(new DistanceConverter1());
		converters.add(new DistanceConverter2());
		converters.add(new LogConverter());
		converters.add(new NegLogConverter());
		converters.add(new SCPSConverter());

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
		updateAttributeList();
		
		EdgeWeightConverter[] edgeWeightConverters = converters.toArray(new EdgeWeightConverter[1]);
		if (edgeWeightConverters.length > 0){
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>(edgeWeightConverters);	
			edgeWeighter.setSelectedValue(edgeWeightConverters[0]);
		}
		else{
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>();
		}
		
		updateBounds();
	}

	public void setNetwork(CyNetwork network) {
		this.network = network;
		updateAttributeList();
	}

	public void	updateAttributeList() {
		List<String> attributeArray = getAllAttributes();
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null) {
				newAttribute.setSelectedValue(attribute.getSelectedValue());
			}
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				newAttribute.setSelectedValue(attribute.getSelectedValue());
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));
			attribute = newAttribute;
		}
		else{
			attribute = new ListSingleSelection<String>("None");
		}
	}

	public void updateBounds() {
		// TODO: calculate bounds based on data
		edgeCutOff =  new BoundedDouble(0.0, 0.0, 100.0, false, false);
	}

	public void histoValueChanged(double cutoffValue) {
		// System.out.println("New cutoff value: "+cutoffValue);
		edgeCutOff.setValue(cutoffValue);
	}

	public void createHistogramDialog() {
		if (this.matrix == null)
			this.matrix = new DistanceMatrix(network, attribute.getSelectedValue(), selectedOnly, edgeWeighter.getSelectedValue());

		ThresholdHeuristic thueristic = new ThresholdHeuristic(matrix);

		double dataArray[] = matrix.getEdgeValues();

		// TODO: There really needs to be a better way to calculate the number of bins
		int nbins = 100;
		if (dataArray.length < 100)
			nbins = 10;
		// else if (dataArray.length > 10000)
		// 	nbins = 1000;
		String title = "Histogram for "+attribute.getSelectedValue()+" edge attribute";
		histo = new HistogramDialog(title, dataArray, nbins,thueristic);
		histo.pack();
		histo.setVisible(true);
		histo.addHistoChangeListener(this);
	}

	public DistanceMatrix getMatrix() {
		if (this.matrix == null) {
			if (attribute.getSelectedValue() == null) return null;
			this.matrix = new DistanceMatrix(network, attribute.getSelectedValue(), selectedOnly, edgeWeighter.getSelectedValue());
		}

		matrix.setUndirectedEdges(undirectedEdges);

		if (edgeCutOff != null)
			matrix.setEdgeCutOff(edgeCutOff.getValue());

		if (adjustLoops)
			matrix.adjustLoops();

		return this.matrix;
	}

	public void setParams(List<String> params) {
		if (adjustLoops)
			params.add("adjustLoops");
		if (edgeCutOff != null)
			params.add("edgeCutOff="+edgeCutOff.toString());
		if (selectedOnly)
			params.add("selectedOnly");
		if (undirectedEdges)
			params.add("undirectedEdges");
		params.add("converter="+edgeWeighter.getSelectedValue().getShortName());
		params.add("dataAttribute="+attribute.getSelectedValue());
	}

	public EdgeWeightConverter getConverter(String converterName) {
		for (EdgeWeightConverter ewc: converters) {
			if (converterName.equals(ewc.getShortName()))
				return ewc;
		}
		return null;
	}

	private void getAttributesList(List<String>attributeList, CyTable attributes) {
		Collection<CyColumn> names = attributes.getColumns();
		java.util.Iterator<CyColumn> itr = names.iterator();
		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == Double.class ||
					column.getType() == Integer.class) {
					attributeList.add(column.getName());
			}
		}
	
	}

	private List<String> getAllAttributes() {
		String[] attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, network.getDefaultEdgeTable());
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}
	
	
	

}





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
	//private boolean adjustLoops = true;
	//private boolean undirectedEdges = true;
	//private boolean selectedOnly = false;
	private EdgeWeightConverter converter = null;
	
	
	
	private Double setEdgeCutOff = null;
	private String[] attributeArray = new String[1];
	private List<EdgeWeightConverter>converters = null;
	
	private ListSingleSelection<String> attribute ;
	
	@Tunable(description = "Array Sources", groups={"Source for array data"})
	public String getattribute(){
		return attribute.getSelectedValue();
	}
	public void setattribute() { 
	}
	
	
	@Tunable(description = "Cluster only selected nodes", groups={"Source for array data"})
	public boolean selectedOnly ;
	
	
	@Tunable(description = "Edge weight conversion")
	public ListSingleSelection<EdgeWeightConverter> edgeWeighter;
	
	@Tunable(description="Cut off value for edge consideration", groups={"Source for array data", "Edge weight cutoff"}, params="slider=true")
	public BoundedDouble edgeCutOff;
	
	public boolean edgeHistogram = false;
	@Tunable(description="Set Edge Cutoff Using Histogram", 
	         groups={"Source for array data", "Edge weight cutoff"}, context="gui")
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
	         groups={"Source for array data", "Array data adjustments"})
	public boolean undirectedEdges ;
	
	@Tunable(description = "Adjust loops before clustering", 
	         groups={"Source for array data", "Array data adjustments"})
	public boolean adjustLoops ;
	
	

	private String dataAttribute = null;

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
		converter = converters.get(0); // Initialize to the None converter

		initializeTunables();
	}

	public void initializeTunables() {
		
		attributeArray = getAllAttributes();
		if (attributeArray.length > 0){
			attribute = new ListSingleSelection<String>(attributeArray);	
			attribute.setSelectedValue(attributeArray[0]);
		}
		else{
			attribute = new ListSingleSelection<String>("None");
		}
		
		selectedOnly = false;
		
		EdgeWeightConverter[] edgeWeightConverters = converters.toArray(new EdgeWeightConverter[1]);
		if (edgeWeightConverters.length > 0){
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>(edgeWeightConverters);	
			edgeWeighter.setSelectedValue(edgeWeightConverters[0]);
		}
		else{
			edgeWeighter = new ListSingleSelection<EdgeWeightConverter>();
		}
		
		edgeCutOff =  new BoundedDouble(0.0, 0.0, 1.0, true, true);
		undirectedEdges = true;
		adjustLoops = true;
		

		updateAttributeList();
	}

	public void	updateAttributeList() {
		attributeArray = getAllAttributes();
		attribute = new ListSingleSelection<String>(attributeArray);	
		if (dataAttribute == null && attributeArray.length > 0)
			dataAttribute = attributeArray[0];
	}

	public void histoValueChanged(double cutoffValue) {
		// System.out.println("New cutoff value: "+cutoffValue);
		edgeCutOff.setValue(cutoffValue);
	}

	public void createHistogramDialog() {
		if (this.matrix == null)
			this.matrix = new DistanceMatrix(network, dataAttribute, selectedOnly, converter);

		ThresholdHeuristic thueristic = new ThresholdHeuristic(matrix);

		double dataArray[] = matrix.getEdgeValues();

		// TODO: There really needs to be a better way to calculate the number of bins
		int nbins = 100;
		if (dataArray.length < 100)
			nbins = 10;
		// else if (dataArray.length > 10000)
		// 	nbins = 1000;
		String title = "Histogram for "+dataAttribute+" edge attribute";
		histo = new HistogramDialog(title, dataArray, nbins,thueristic);
		histo.pack();
		histo.setVisible(true);
		histo.addHistoChangeListener(this);
	}

	public DistanceMatrix getMatrix() {
		if (this.matrix == null) {
			if (dataAttribute == null) return null;
			this.matrix = new DistanceMatrix(network, dataAttribute, selectedOnly, converter);
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
		params.add("converter="+converter.getShortName());
		params.add("dataAttribute="+dataAttribute);
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

	private String[] getAllAttributes() {
		attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, network.getDefaultEdgeTable());
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return attrArray;
	}
	
	
	

}





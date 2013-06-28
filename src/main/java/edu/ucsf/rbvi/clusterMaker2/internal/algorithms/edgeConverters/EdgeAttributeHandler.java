package org.cytoscape.myapp.internal.algorithms.edgeConverters;


import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.BasicTunableHandlerFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;


import org.cytoscape.myapp.internal.algorithms.ClusterProperties;
import org.cytoscape.myapp.internal.algorithms.DistanceMatrix;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.EdgeWeightConverter;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.DistanceConverter1;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.DistanceConverter2;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.LogConverter;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.NegLogConverter;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.NoneConverter;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.SCPSConverter;

import org.cytoscape.myapp.internal.algorithms.edgeConverters.ThresholdHeuristic;

import org.cytoscape.myapp.internal.ui.HistogramDialog;
import org.cytoscape.myapp.internal.ui.HistoChangeListener;

public class EdgeAttributeHandler implements TunableSetter, ActionListener, HistoChangeListener{
	
	public static final String NONEATTRIBUTE = "--None--";

	private ClusterProperties clusterProperties;
	private DistanceMatrix matrix = null;
	//private boolean adjustLoops = true;
	//private boolean undirectedEdges = true;
	//private boolean selectedOnly = false;
	private EdgeWeightConverter converter = null;
	private boolean supportAdjustments = false;
	
	
	
	private Double setEdgeCutOff = null;
	private String[] attributeArray = new String[1];
	private List<EdgeWeightConverter>converters = null;
	
	@Tunable(description = "Source for array data", groups = "General")
	private int attributeListGroup;
	
	@Tunable(description = "Array Sources")
	private ListSingleSelection<String> attribute ;
	
	@Tunable(description = "Array Sources")
	private String getAttribute(){
		return attribute.getSelectedValue();
	}
	
	private void setAtribute(String newAttribute){
		
		attribute.setSelectedValue(newAttribute);
		System.out.println("Setting the value of Array Sources to: " + attribute.getSelectedValue()  );
	}
	
	@Tunable(description = "Cluster only selected nodes")
	private boolean selectedOnly ;
	
	@Tunable(description = "Cluster only selected nodes")
	private boolean getSelectedOnly(){
		return selectedOnly;
	}
	
	private void setSelectedOnly(boolean newSelectedOnly){
		selectedOnly = newSelectedOnly;
		System.out.println("Setting the value of Cluster only selected nodes to: " +  selectedOnly );

	}
	
	@Tunable(description = "Edge weight conversion")
	private ListSingleSelection<EdgeWeightConverter> edgeWeighter;
	
	@Tunable(description = "Edge weight conversion")
	private EdgeWeightConverter getEdgeWeighter(){
		return edgeWeighter.getSelectedValue();
	}
	
	private void setEdgeWeighter(EdgeWeightConverter newEdgeWeighter){
		
		edgeWeighter.setSelectedValue(newEdgeWeighter);
		System.out.println("Setting the value of Edge Weight Conversion to: " + edgeWeighter.getSelectedValue()  );
	}
	
	@Tunable(params="slider=true")
	private BoundedDouble edgeCutOff;
	
	@Tunable()
	private Double getEdgeCutOff(){
		return edgeCutOff.getValue();
	}
	private void setEdgeCutOff(Double newEdgeCutOff){
		edgeCutOff.setValue(newEdgeCutOff);
		System.out.println("Setting the value of Edge CutOff to: " + edgeCutOff.getValue()  );

	}
	
	@Tunable(description = "Edge weight cutoff")
	private int edgeCutoffGroup;
	/*
	@Tunable(description = "Set Edge Cutoff Using Histogram")
	private Button edgeHistogram = null;
*/
	@Tunable(description = "Array data adjustments")
	private int options_panel1 ;
	
	@Tunable(description = "Assume edges are undirected")
	private boolean undirectedEdges ;
	
	@Tunable(description = "Adjust loops before clustering")
	private boolean adjustLoops ;
	
	

	private String dataAttribute = null;

	private HistogramDialog histo = null;
	
	public EdgeAttributeHandler(ClusterProperties clusterProperties, boolean supportAdjustments) {
		this.clusterProperties = clusterProperties;
		this.supportAdjustments = supportAdjustments;
		// Create all of our edge weight converters
		converters = new ArrayList<EdgeWeightConverter>();
		converters.add(new NoneConverter());
		converters.add(new DistanceConverter1());
		converters.add(new DistanceConverter2());
		converters.add(new LogConverter());
		converters.add(new NegLogConverter());
		converters.add(new SCPSConverter());
		converter = converters.get(0); // Initialize to the None converter

		if (clusterProperties != null)
			initializeTunables(clusterProperties);
	}

	public void initializeTunables(ClusterProperties clusterProperties) {
		
		
		attributeListGroup = 5;
				
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
		
		edgeCutoffGroup = 2;
		edgeCutOff =  new BoundedDouble(0.0, 0.0, 1.0, true, true);
		options_panel1 = 2;
		undirectedEdges = true;
		adjustLoops = true;
		

		updateAttributeList();
	}

/*	public void updateSettings(boolean force) {
		Tunable t = clusterProperties.get("edgeWeighter");
		if ((t != null) && (t.valueChanged() || force)) {
			int index = ((Integer) t.getValue()).intValue();
			if (index < 0) index = 0;
			converter = converters.get(index);
		}

		t = clusterProperties.get("edgeCutOff");
		if ((t != null) && (t.valueChanged() || force)) {
			edgeCutOff = (Double) t.getValue();
		}

		t = clusterProperties.get("selectedOnly");
		if ((t != null) && (t.valueChanged() || force))
			selectedOnly = ((Boolean) t.getValue()).booleanValue();

		t = clusterProperties.get("undirectedEdges");
		if ((t != null) && (t.valueChanged() || force))
			undirectedEdges = ((Boolean) t.getValue()).booleanValue();

		t = clusterProperties.get("adjustLoops");
		if ((t != null) && (t.valueChanged() || force))
			adjustLoops = ((Boolean) t.getValue()).booleanValue();

		t = clusterProperties.get("attribute");
		if ((t != null) && (t.valueChanged() || force)) {
			if (attributeArray.length == 1) {
				dataAttribute = attributeArray[0];
			} else {
				int index = ((Integer) t.getValue()).intValue();
				if (index < 0) index = 0;
				dataAttribute = attributeArray[index];
			}
			if (dataAttribute != null) {
				Tunable et = clusterProperties.get("edgeHistogram");
				et.clearFlag(Tunable.IMMUTABLE);
			}
			// tunableChanged(t);
		}
	}
*/
	public void	updateAttributeList() {
		Tunable attributeTunable = clusterProperties.get("attribute");
		attributeArray = getAllAttributes();
		attributeTunable.setLowerBound((Object)attributeArray);
		if (dataAttribute == null && attributeArray.length > 0)
			dataAttribute = attributeArray[0];
		tunableChanged(attributeTunable);
	}

	public void histoValueChanged(double cutoffValue) {
		// System.out.println("New cutoff value: "+cutoffValue);
		Tunable edgeCutoff = clusterProperties.get("edgeCutOff");
		edgeCutoff.removeTunableValueListener(this);
		edgeCutoff.setValue(cutoffValue);
		edgeCutoff.addTunableValueListener(this);
	}

	public void tunableChanged(Tunable tunable) {
		// If the tunable that changed is the edge cutoff tunable, 
		// just update the histogram, if it's up
		
		if (tunable.getName().equals("edgeCutOff")) {
			edgeCutOff = (Double) tunable.getValue();
			setEdgeCutOff = edgeCutOff;
			if (histo != null) {
				// No backs!
				histo.removeHistoChangeListener(this);
				histo.setLineValue(edgeCutOff);
				histo.addHistoChangeListener(this);
			}
			return;
		}

		updateSettings(false);
		Tunable edgeCutOffTunable = clusterProperties.get("edgeCutOff");
		if (edgeCutOffTunable == null || dataAttribute == null) 
			return;

		Tunable t = clusterProperties.get("edgeHistogram");
		t.clearFlag(Tunable.IMMUTABLE);

		this.matrix = new DistanceMatrix(dataAttribute, selectedOnly, converter);
		double dataArray[] = matrix.getEdgeValues();
		double range = matrix.getMaxWeight() - matrix.getMinWeight();
		edgeCutOffTunable.setUpperBound(matrix.getMaxWeight());
		edgeCutOffTunable.setLowerBound(matrix.getMinWeight());
		// We need to be a little careful.  There are two ways to set the
		// edgeCutOff: 1) With the GUI, which will always be right; 2) via
		// a CyCommand.  In the second case, we don't want to reset the edge
		// cut off when the attribute or converter changes because we might have
		// already set the edge cutoff.
		if (setEdgeCutOff == null ||
		    setEdgeCutOff > matrix.getMaxWeight() || 
		    setEdgeCutOff < matrix.getMinWeight()) {
			edgeCutOffTunable.setValue(matrix.getMinWeight());
		} else {
			edgeCutOffTunable.setValue(setEdgeCutOff);
		}
		edgeCutOff = (Double) edgeCutOffTunable.getValue();

		if (histo != null) {
			histo.updateData(dataArray);
			histo.pack();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (this.matrix == null)
			this.matrix = new DistanceMatrix(dataAttribute, selectedOnly, converter);

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
			this.matrix = new DistanceMatrix(dataAttribute, selectedOnly, converter);
		}

		matrix.setUndirectedEdges(undirectedEdges);

		if (edgeCutOff != null)
			matrix.setEdgeCutOff(edgeCutOff.doubleValue());

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
		while(itr.hasNext()){
			if (itr.next().getType() == float.class ||
					itr.next().getType() == int.class) {
					attributeList.add(itr.next().getName());
			}
		}
	
	}

	private String[] getAllAttributes() {
		attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, Cytoscape.getEdgeAttributes());
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return attrArray;
	}
	
	
	

}





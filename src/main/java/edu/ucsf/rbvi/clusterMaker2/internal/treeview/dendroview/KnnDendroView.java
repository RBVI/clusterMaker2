/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: KnnDendroView.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/09/21 17:18:55 $
 * $Name:  $
 *
 * This file is part of Java TreeView
 * Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. Modified by Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 * This software is provided under the GNU GPL Version 2. In particular, 
 *
 * 1) If you modify a source file, make a comment in it containing your name and the date.
 * 2) If you distribute a modified version, you must do it under the GPL 2.
 * 3) Developers are encouraged but not required to notify the Java TreeView maintainers at alok@genome.stanford.edu when they make a useful addition. It would be nice if significant contributions could be merged into the main distribution.
 *
 * A full copy of the license can be found in gpl.txt or online at
 * http://www.gnu.org/licenses/gpl.txt
 *
 * END_HEADER 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollBar;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.MainPanel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.MessagePanel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;

/**
 *  This class encapsulates a dendrogram view, which is the classic Eisen
 *  treeview. It uses a drag grid panel to lay out a bunch of linked
 *  visualizations of the data, a la Eisen. In addition to laying out
 *  components, it also manages the GlobalZoomMap. This is necessary since both
 *  the GTRView (gene tree) and KnnGlobalView need to know where to lay out genes
 *  using the same map.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.2 $ $Date: 2006/09/21 17:18:55 $
 */
public class KnnDendroView extends DendroView implements ConfigNodePersistent, MainPanel, Observer  {

	/**
	 *  Constructor for the KnnDendroView object
	 *
	 * @param  tVModel   model this KnnDendroView is to represent
	 * @param  vFrame  parent ViewFrame of KnnDendroView
	 */
	public KnnDendroView(DataModel tVModel, ViewFrame vFrame) {
		super(tVModel, null, vFrame, "KnnDendrogram");
	}
	public KnnDendroView(DataModel tVModel, ConfigNode root, ViewFrame vFrame) {
		super(tVModel, root, vFrame, "KnnDendrogram");
	}
	/**
	 *  This method should be called only during initial setup of the modelview
	 *
	 *  It sets up the views and binds them all to config nodes.
	 *
	 */
	protected void setupViews() {
		DataModel knnModel = getDataModel();
		hintpanel = new MessagePanel("Usage Hints");
		statuspanel = new MessagePanel("View Status");

		colorPresets = new ColorPresets(root);
		cpresetEditor = new ColorPresetEditor(colorPresets);
		ColorExtractor colorExtractor = new ColorExtractor();
		colorExtractor.setDefaultColorSet(colorPresets.getDefaultColorSet());
		colorExtractor.bindConfig(root);
		colorExtractor.setMissing(DataModel.NODATA, DataModel.EMPTY);

		KnnArrayDrawer kArrayDrawer = new KnnArrayDrawer();
		kArrayDrawer.setColorExtractor(colorExtractor);
		arrayDrawer = kArrayDrawer;
		//XXX shouldn't need to observer, should be immuable?
		((Observable) getDataModel()).addObserver(arrayDrawer);

		globalview = new GlobalView();

		// scrollbars, mostly used by maps
		globalXscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0,1,0,1);
		globalYscrollbar = new JScrollBar(JScrollBar.VERTICAL,0,1,0,1);
		zoomXscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0,1,0,1);
		zoomYscrollbar = new JScrollBar(JScrollBar.VERTICAL,0,1,0,1);

		zoomXmap = new MapContainer();
		zoomXmap.setDefaultScale(12.0);
		zoomXmap.setScrollbar(zoomXscrollbar);
		zoomYmap = new MapContainer();
		zoomYmap.setDefaultScale(12.0);
		zoomYmap.setScrollbar(zoomYscrollbar);

		// globalmaps tell globalview, atrview, and gtrview
		// where to draw each data point.
	// the scrollbars "scroll" by communicating with the maps.
		globalXmap = new MapContainer();
		globalXmap.setDefaultScale(2.0);
		globalXmap.setScrollbar(globalXscrollbar);
		globalYmap = new MapContainer();
		globalYmap.setDefaultScale(2.0);
		globalYmap.setScrollbar(globalYscrollbar);

		globalview.setXMap(globalXmap);
		globalview.setYMap(globalYmap);

		globalview.setZoomYMap(getZoomYmap());
		globalview.setZoomXMap(getZoomXmap());

		arraynameview = new ArrayNameView(getDataModel().getArrayHeaderInfo());

		leftTreeDrawer = new LeftTreeDrawer();
		gtrview = new GTRView();
		gtrview.setMap(globalYmap);
		gtrview.setLeftTreeDrawer(leftTreeDrawer);

		invertedTreeDrawer = new InvertedTreeDrawer();
		atrview = new ATRView();
		atrview.setMap(globalXmap);
		atrview.setInvertedTreeDrawer(invertedTreeDrawer);

		atrzview = new ATRZoomView();
		atrzview.setZoomMap(getZoomXmap());
		atrzview.setInvertedTreeDrawer(invertedTreeDrawer);

		zoomview = new ZoomView();
		zoomview.setYMap(getZoomYmap());
		zoomview.setXMap(getZoomXmap());
		zoomview  .setArrayDrawer(arrayDrawer);
		globalview.setArrayDrawer(arrayDrawer);

		arraynameview.setMapping(getZoomXmap());

		textview = new TextViewManager(getDataModel().getGeneHeaderInfo());
		textview.setMap(getZoomYmap());

		doDoubleLayout();

		// reset persistent popups
		settingsFrame = null;

		// color extractor
		colorExtractor.bindConfig(getFirst("ColorExtractor"));

		// set data first to avoid adding auto-genereated contrast to documentConfig.
		kArrayDrawer.setDataMatrix(knnModel.getDataMatrix());
		kArrayDrawer.bindConfig(getFirst("ArrayDrawer"));

		// responsible for adding and removing components...
		bindTrees();
		
		globalXmap.bindConfig(getFirst("GlobalXMap"));
		globalYmap.bindConfig(getFirst("GlobalYMap"));
		getZoomXmap().bindConfig(getFirst("ZoomXMap"));
		getZoomYmap().bindConfig(getFirst("ZoomYMap"));

		textview.bindConfig(getFirst("TextViewParent"));
		arraynameview.bindConfig(getFirst("ArrayNameView"));

		// perhaps I could remember this stuff in the MapContainer...
		DataMatrix dataMatrix = getDataModel().getDataMatrix();
		globalXmap.setIndexRange(0, dataMatrix.getNumCol() - 1);
		globalYmap.setIndexRange(0, dataMatrix.getNumRow() - 1);
		getZoomXmap().setIndexRange(-1, -1);
		getZoomYmap().setIndexRange(-1, -1);

		globalXmap.notifyObservers();
		globalYmap.notifyObservers();
		getZoomXmap().notifyObservers();
		getZoomYmap().notifyObservers();
	}
	
}


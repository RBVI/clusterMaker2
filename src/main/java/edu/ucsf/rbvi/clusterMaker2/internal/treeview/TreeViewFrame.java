/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TreeViewFrame.java,v $w
 * $Revision: 1.69 $
 * $Date: 2007/02/03 04:58:36 $
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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.swing.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview.DendroView;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.DataModelWriter;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TVModel;

/**
 * This class is the main window of java treeview.
 * In practice, it serves as the base class for the LinkedViewFrame and
 * the AppletViewFrame.
 * 
 * @author aloksaldanha
 *
 */
public class TreeViewFrame extends ViewFrame {
	/** override in subclasses? */
	private static String appName = "TreeView Classic";

	public String getAppName() {
		return appName;
	}

	public TreeViewFrame(TreeViewApp treeview) {
		this(treeview, appName);
	}

	public TreeViewFrame(TreeViewApp treeview, String appName) {
		super(appName);
		this.appName = appName;
		treeView = treeview;
		loaded = false;
		setWindowActive(true);

		setupPresets();

		centerOnscreen();
		setLoaded(false);
	}

	protected void setupPresets() {
	}

	public void closeWindow() {
		if (running != null) {
			running.syncConfig();
		}
		super.closeWindow();
	}

	public void scrollToGene(int i) {
		running.scrollToGene(i);
	}

	public void scrollToArray(int i) {
		running.scrollToArray(i);
	}

	/**
	 * Sets up the following: 1) geneSelection and 2) arraySelection, the 
	 * two selection objects. It is important that these are
	 * set up before any plugins are instantiated. This is called before
	 * setupRunning by setDataModel.
	 */
	protected void setupExtractors() {
		ConfigNode documentConfig = getDataModel().getDocumentConfig();
		// extractors...
		DataMatrix matrix = getDataModel().getDataMatrix();
		int ngene = matrix.getNumRow();
		int nexpr = matrix.getNumCol();
		geneSelection = new TreeSelection(ngene);
		arraySelection = new TreeSelection(nexpr);
	}

	protected void setupRunning() {
		DendroView dv = new DendroView(getDataModel(), this);
		running = dv;
	}

	// Observer
	public void update(Observable observable, Object object) {
	}

	/**
	 * This should be called whenever the loaded status changes It's
	 * responsibility is to change the look of the main view only
	 */

	public void setLoaded(boolean flag) {
		// reset persistent popups
		loaded = flag;
		getContentPane().removeAll();
		// getContentPane().add(menubar, BorderLayout.NORTH);
		if (loaded) {
			if (running == null) {
				JOptionPane.showMessageDialog(this, "TreeViewFrame 253: No data to display");
			} else {
				getContentPane().add((JComponent) running);
				// getContentPane().add((JComponent) settingsPane);
				setTitle(getAppName() + " : " + dataModel.getSource());
				treeView.getGlobalConfig().store();
			}
		} else {
			setTitle(getAppName());
		}

		validate();
		repaint();
	}

	public boolean getLoaded() {
		return loaded;
	}

	public boolean warnSelectionEmpty() {
		TreeSelectionI treeSelection = getGeneSelection();
		if ((treeSelection == null)
				|| (treeSelection.getNSelectedIndexes() <= 0)) {

			JOptionPane.showMessageDialog(this,
					"Cannot generate gene list, no gene selected");
			return false;
		}
		return true;
	}

	public TreeViewApp getApp() {
		return treeView;
	}

	public double noData() {
		return DataModel.NODATA;
	}

	TreeViewApp treeView;

	private boolean loaded;

	protected MainPanel running;

	protected DataModel dataModel;

	/**
	 * Setter for dataModel, also sets extractors, running.
	 * 
	 * @throws LoadException
	 */
	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
		setupExtractors();
		setupRunning();
	}

	/** Getter for dataModel */
	public DataModel getDataModel() {
		return dataModel;
	}

	public MainPanel[] getMainPanelsByName(String name) {
		if (running != null) {
			// is the current running a match?
			if (name.equals(running.getName())) {
				MainPanel [] list = new MainPanel[1];
				list[0] = running;
				return list;
			}
			// okay, is the current running a linkedPanel?
			try {
				LinkedPanel linked = (LinkedPanel) running;
				return linked.getMainPanelsByName(name);
			} catch (ClassCastException e) {
				// fall through to end
			}
		} else {
			// fall through to end
		}
		MainPanel [] list = new MainPanel[0];
		return list;
	}

	public MainPanel[] getMainPanels() {
		if (running == null) {
			MainPanel [] list = new MainPanel[0];
			return list;			
		}
		// okay, is the current running a linkedPanel?
		try {
			LinkedPanel linked = (LinkedPanel) running;
			return linked.getMainPanels();
		} catch (ClassCastException e) {
			// fall through to end
		}

		MainPanel [] list = new MainPanel[1];
		list[0] = running;
		return list;
	}
}

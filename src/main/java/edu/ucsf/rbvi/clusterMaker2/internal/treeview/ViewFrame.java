/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ViewFrame.java,v $
 * $Revision: 1.34 $
 * $Date: 2006/10/04 16:34:01 $
 * $Name:  $
 *
 * This file is part of Java TreeView
 * Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. 
 * Modified by Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 * This software is provided under the GNU GPL Version 2. In particular,
 *
 * 1) If you modify a source file, make a comment in it containing your name and the date.
 * 2) If you distribute a modified version, you must do it under the GPL 2.
 * 3) Developers are encouraged but not required to notify the Java TreeView 
 *    maintainers at alok@genome.stanford.edu when they make a useful addition. 
 *    It would be nice if significant contributions could be merged into the main distribution.
 *
 * A full copy of the license can be found in gpl.txt or online at
 * 
 * http://www.gnu.org/licenses/gpl.txt *
 * END_HEADER
 */

package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.DataModelWriter;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.ReorderedDataModel;

/**
 *  Any frame that wants to contain MainPanels must extend this.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.34 $ $Date: 2006/10/04 16:34:01 $
 */
public abstract class ViewFrame extends JFrame implements Observer {
	// must override in subclass...
	/**
	 *  This is to ensure that we can observe the MainPanels when they change.
	 *
	 * @param  observable  The MainPanel or other thing which changed.
	 * @param  object      Generally null.
	 */
	public abstract void update(Observable observable, Object object);


	/**
	 * This routine should return any instances of the plugin
	 * of the indicated name (i.e. it will loop over all instantiated
	 * MainPanel calling their getName() properties, find all that are
	 * equal to the indicated string, and return all matching ones
	 */
	public abstract MainPanel[] getMainPanelsByName(String name);
	
	/**
	 * 
	 * @return all mainPanels managed by this viewFrame
	 */
	public abstract MainPanel[] getMainPanels();
	
	/**
	 *  Centers the frame onscreen.
	 *
	 * @param  rectangle  A rectangle describing the outlines of the screen.
	 */
	private void center(Rectangle rectangle) {
		Dimension dimension  = getSize();
		setLocation((rectangle.width - dimension.width) / 3 + rectangle.x, (rectangle.height - dimension.height) / 3 + rectangle.y);
	}


	/**  Determines dimension of screen and centers frame onscreen. */
	public void centerOnscreen() {
		// trying this for mac...
		Toolkit toolkit      = Toolkit.getDefaultToolkit();
		Dimension dimension  = toolkit.getScreenSize();
		Rectangle rectangle  = new Rectangle(dimension);

		// XXX should drag out of global config
		// setSize(rectangle.width * 3 / 4, rectangle.height * 4 / 5);
		int width = rectangle.width * 3 / 4;
		int height = rectangle.height * 4 / 5;
		if (width > 800) width = 800;
		if (height > 600) height = 600;
		setSize(width, height);
		center(rectangle);
	}


	/**  Sets a listener on self, so taht we can grab focus when activated, and close ourselves when closed.*/
	private void setupWindowListener() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(
			new WindowAdapter() {
				public void windowActivated(WindowEvent windowEvent) {
					setWindowActive(true);
				}


				public void windowClosing(WindowEvent windowEvent) {
					closeWindow();
				}


				public void windowDeactivated(WindowEvent windowEvent) {
					setWindowActive(false);
				}
			});
	}

	/**
	 *  Constructor for the ViewFrame object
	 * Sets title and window listeners
	 *
	 * @param  title  Title for the viewframe.
	 */
	public ViewFrame(String title) {
		super(title);
		setupWindowListener();
	}


	/**  construts an untitled <code>ViewFrame</code> */
	public ViewFrame() {
		super();
		setupWindowListener();
	}


	/**
	 *  Keep track of when active, so that clicks don't get passed through too much.
	 *
	 * @param  flag  The new windowActive value
	 */
	protected void setWindowActive(boolean flag) {
		windowActive = flag;
	}


	/**
	 *  Keep track of when active, so that clicks don't get passed through too much.
	 *
	 * @return    True if window is active.
	 */
	public boolean windowActive() {
		return windowActive;
	}


	/**  Keep track of when active, so that clicks don't get passed through too much. */
	private boolean windowActive;


	/**  close window cleanly. 
	* causes documentConfig to be stored. 
	*/
	public void closeWindow() {
		// CyLogger.getLogger(ViewFrame.class).info("ViewFrame.dispose");
		dispose();
	}


	/**
	 *  required by all <code>ModelPanel</code>s
	 *
	 * @return   The shared TreeSelection object for genes.
	 */
	public TreeSelectionI getGeneSelection() {
		return geneSelection;
	}
	protected void setGeneSelection(TreeSelectionI newSelection) {
		geneSelection = newSelection;
	}

	/**
	 *  required by all <code>ModelPanel</code>s
	 *
	 * @return   The shared TreeSelection object for arrays.
	 */
	public TreeSelectionI getArraySelection() {
		return arraySelection;
	}
	protected void setArraySelection(TreeSelectionI newSelection) {
		arraySelection = newSelection;
	}

	/**
	 *  used by data model to signal completion of loading.
	 * The <code>ViewFrame</code> will react by reconfiguring it's widgets.
	 *
	 * @param  b  The new loaded value
	 */
	public abstract void setLoaded(boolean b);


	/**
	 *  returns special nodata value.
	 *  generally, just cribs from the <code>DataModel</code>
	 *
	 * @return    A special double which means nodata available.
	 */

	public abstract double noData();

	/**
	 *  Gets the loaded attribute of the ViewFrame object
	 *
	 * @return    True if there is currently a model loaded.
	 */
	public abstract boolean getLoaded();


	/**
	 *  Gets the shared <code>DataModel</code>
	 *
	 * @return    Gets the shared <code>DataModel</code>
	 */
	public abstract DataModel getDataModel();

	/**
	 *  Sets the shared <code>DataModel</code>
	 *
	 * @return    Sets the shared <code>DataModel</code>
	 * @throws LoadException
	 */
	public abstract void setDataModel(DataModel model);

	/**
	 *  Should scroll all MainPanels in this view frame to the specified gene.
	 *  The index provided is respect to the TreeSelection object.
	 *
	 * @param  i  gene index in model to scroll the mainpanel to.
	 */
	public abstract void scrollToGene(int i);
	public abstract void scrollToArray(int i);

	/**  The shared selection objects */
	TreeSelectionI geneSelection = null;
	TreeSelectionI arraySelection = null;

	public void deselectAll() {
	  geneSelection.deselectAllIndexes();
	  arraySelection.deselectAllIndexes();
	}

	/***
	* This routine causes all data views to 
	* select and scroll to a particular gene.
	*/
	public void seekGene(int i) {
	  geneSelection.deselectAllIndexes();
	  geneSelection.setIndex(i, true);
	  geneSelection.notifyObservers();
	  scrollToGene(i);
	}

	/***
	* This routine causes all data views to 
	* select and scroll to a particular array.
	*/
	public void seekArray(int i) {
	  arraySelection.deselectAllIndexes();
	  arraySelection.setIndex(i, true);
	  arraySelection.notifyObservers();
	  scrollToArray(i);
	}
	
	/**
	* This routine extends the selected range to include the index 
	* i.
	*/
	public void extendRange(int i) {
	  if (geneSelection.getMinIndex() == -1)
		seekGene(i);
	  geneSelection.setIndex(i, true);
	  geneSelection.notifyObservers();

	  scrollToGene(i);
	}
	
	public boolean geneIsSelected(int i) {
	  return getGeneSelection().isIndexSelected(i);
	}

	public abstract TreeViewApp getApp();
	 
	 /**
	 *  Gets the key corresponding to a particular number.
	 *
	 * @param  i  The number
	 * @return    The VK_blah key value
	 */
	 protected int getKey(int i) {
		 switch (i) {
			case 0:
				return KeyEvent.VK_0;
		    case 1:
				return KeyEvent.VK_1;
			case 2:
				return KeyEvent.VK_2;
			case 3:
				return KeyEvent.VK_3;
			case 4:
				return KeyEvent.VK_4;
			case 5:
				return KeyEvent.VK_5;
			case 6:
				return KeyEvent.VK_6;
			case 7:
				return KeyEvent.VK_7;
			case 8:
				return KeyEvent.VK_8;
			case 9:
				return KeyEvent.VK_9;
		 }
		 return 0;
	 }
	 
	public void showSubDataModel(int[] indexes, String source, String name) {
		if (indexes.length == 0) {
			JOptionPane.showMessageDialog(this, "No Genes to show summary of!");
			return;
		}
		ReorderedDataModel dataModel = new ReorderedDataModel(getDataModel(), indexes);
		if (source != null) dataModel.setSource(source);
		if (name != null) dataModel.setName(name);
		ViewFrame window = getApp().openNew();
		window.setDataModel(dataModel);
		window.setLoaded(true);
		window.setVisible(true);
	}	 

}


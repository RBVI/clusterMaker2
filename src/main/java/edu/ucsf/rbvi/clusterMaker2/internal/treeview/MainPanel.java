/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: MainPanel.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/07/24 19:57:06 $
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

import javax.swing.JMenu;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *  implementing objects are expected to be subclasses of component. The purpose
 *  of this class is to provide an interface for LinkedView, whereby different views
 *  can be added to a tabbed panel. This is meant to eventually become a plugin interface.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.9 $ $Date: 2005/07/24 19:57:06 $
 */
public interface MainPanel {
	
	/**
	 *  This syncronizes the sub compnents with their persistent storage.
	 */
	public void syncConfig();

	/**
	* this method gets the config node on which this component is based, or null.
	*/
	public ConfigNode getConfigNode();

	/**
	 *  Add items related to settings
	 *
	 * @param  menu  A menu to add items to.
	 */
	// public void populateSettingsMenu(JMenu menu);


	/**
	 *  Add items which do some kind of analysis
	 *
	 * @param  menu  A menu to add items to.
	 */
	// public void populateAnalysisMenu(JMenu menu);


	/**
	 *  Add items which allow for export, if any.
	 *
	 * @param  menu  A menu to add items to.
	 */
	// public void populateExportMenu(JMenu menu);


	/**
	 *  ensure a particular gene is visible. Used by Find.
	 *
	 *	The index is relative to the shared TreeSelection object
	 * associated with the enclosing ViewFrame.
	 *
	 * @param  i  Index of gene to make visible
	 */
	public void scrollToGene(int i);
	public void scrollToArray(int i);
	
	/**
	 * 
	 * @return name suitable for displaying in tab
	 */
	public String getName();
	
	/**
	 *
	 * @return Icon suitable for putting in tab, or in minimized window.
	 */
	public ImageIcon getIcon();
}


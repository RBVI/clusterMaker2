/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TreeViewApp.java,v $
 * $Revision: 1.5 $
 * $Date: 2007/07/13 02:42:45 $
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
 * END_HEADER */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.*;

import javax.swing.ToolTipManager;

// import clusterMaker.treeview.XmlConfig;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;

/**
 *  This class defines the treeview application.
 *  In practice, it holds the common functionality of the LinkedViewApp and the
 *  AppletApp.
 *  
 *  The main responsibilities of this class are to 
 *  - hold static global configuration variables, such as version and URLs
 *  - hold link to global XmlConfig object
 *  - keep track of all open windows
 *  - at one point, it kept track of plugins, but now the PluginManager does that.
 *  
 *  The actual Gui handling of a given
 *  window is done by TreeViewFrame, which represents a single document. Because
 *  multiple documents can be open at a given time, there can be multiple TVFrame
 *  objects. however, there can be only one TreeView object. what this really means
 *  is that TreeView itself just manages two resources, the window list and the global
 *  config. Anything that effects these resources should bottleneck through TreeView.
 *  
 *  1/16/2003 by popular demand (with the exception of my advisor) I have decided
 *  to try and make an applet version. as a first cut, I'm just going to make this
 *  class extend applet and pop open a jframe.
 
 * 9/24/2003 this has now been superceded by the applet.ButtonApplet class.
 * <p>
 * The main difference between the apps is which <code>ViewFrame</code> they use. <code>TreeViewApp</code> uses <code>TreeViewFrame</code>.
 * <p>
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version   $Revision: 1.5 $ $Date: 2007/07/13 02:42:45 $
 */
public abstract class TreeViewApp implements WindowListener {

	/**  Version of application */
	public final static String versionTag  = "1.1.1";

	/**
	 *  The release-level version tag for the whole application.
	 *
	 * @return    a string representing the versionTag
	 */
	public static String getVersionTag() {
		return versionTag;
	}

	/**  holds all open windows */
	protected java.util.Vector windows;
	/**  holds global config */
	private PropertyConfig globalConfig;


	/**  Constructor for the TreeViewApp object.
	* Opens up a globalConfig from the default location.
	*/
	public TreeViewApp() {
		this (null);
	}

	/**
	* Constructor for the TreeViewApp object
	* takes configuration from the passed in XmlConfig.
	*/
	public TreeViewApp(PropertyConfig propertyConfig) {
		windows = new java.util.Vector();

		setConfigDefaults(propertyConfig);
		
		try {
			ToolTipManager ttm  = ToolTipManager.sharedInstance();
			ttm.setEnabled(true);
		} catch (Exception e) {
		}
	}

	private boolean exitOnWindowsClosed = true;

	public void setConfigDefaults(PropertyConfig propertyConfig) {
		globalConfig = propertyConfig;
	}

	public ViewFrame openNew() {
		TreeViewFrame tvFrame = new TreeViewFrame(this);
		tvFrame.addWindowListener(this);
		return tvFrame;
	}

	/**
	 *  returns an XmlConfig representing global configuration variables
	 *
	 * @return    The globalConfig value
	 */
	public PropertyConfig getGlobalConfig() {
		return globalConfig;
	}

	/**
	 *  A WindowListener, which allows other windows to react to another window being opened.
	 *
	 * @param  e  A window opening event. Used to add the window to the windows list.
	 */
	public void windowOpened(WindowEvent e) {
		windows.addElement((ViewFrame) e.getWindow());
		rebuildWindowMenus();
	}


	/**
	 *  rebuilds all the window menus. Should be called whenever a <code>ViewFrame</code> is
	 *  created, destroyed, or changes its name. The first two cases are handled by
	 *  <code>TreeViewApp</code>, the <code>ViewFrame</code> itself should call this method when it changes its name.
	 */
	public void rebuildWindowMenus() {
		int max  = windows.size();
		for (int i = 0; i < max; i++) {
			ViewFrame source  = (ViewFrame) windows.elementAt(i);
//			rebuildWindowMenu( source.getWindowMenu());
//			source.rebuildWindowMenu(windows);
		}
	}



	/**
	 *  A WindowListener, which allows other windows to react to another window being closed.
	 *
	 * @param  e  A window closing event. Used to remove the window from the windows list.
	 */
	public void windowClosed(WindowEvent e) {
		windows.removeElement(e.getWindow());
		if (windows.isEmpty() && exitOnWindowsClosed) {
			endProgram();
		}
		rebuildWindowMenus();
	}


	/**
	 * loops over the list of windows the <code>TreeViewApp</code> has
	 * collected through the WindowListener interface. just closes the window;
	 * other bookkeeping stuff is done by the <code>windowClosed</code>
	 * method.
	 */
	public void closeAllWindows() {
		Enumeration e  = windows.elements();
		while (e.hasMoreElements()) {
			ViewFrame f  = (ViewFrame) e.nextElement();
			f.closeWindow();
		}
	}

	public ViewFrame [] getWindows() {
		ViewFrame [] frames = new ViewFrame[windows.size()];
		int i = 0;
		Enumeration e  = windows.elements();
		while (e.hasMoreElements()) {
			frames[i++] = (ViewFrame) e.nextElement();
		}
		return frames;
	}
	
	/** Stores the globalconfig, closes all windows, and then exits. */
	private void endProgram() {
		try {
			if (globalConfig != null) {
				globalConfig.store();
			}
			closeAllWindows();
			// System.exit(0);
		} catch (java.security.AccessControlException e) {
			//we're probably running in a applet here...
		}
	}


	public void windowActivated(WindowEvent e) {
		//nothing
	}


	public void windowClosing(WindowEvent e) {
		//nothing
	}


	public void windowDeactivated(WindowEvent e) {
		//nothing
	}


	public void windowDeiconified(WindowEvent e) {
		//nothing
	}


	public void windowIconified(WindowEvent e) {
		//nothing
	}


	/**
	 *  Get a per-user file in which to store global config info.
	 *  Read the code to find out exactly how it guesses.
	 *
	 * @return    A system-specific guess at a global config file name.
	 */
	protected static String globalConfigName() {
		return "TreeViewConfig";
	}

	/**
	 * @param exitOnWindowsClosed the exitOnWindowsClosed to set
	 */
	public void setExitOnWindowsClosed(boolean exitOnWindowsClosed) {
		this.exitOnWindowsClosed = exitOnWindowsClosed;
	}
}


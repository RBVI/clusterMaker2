/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: LinkedPanel.java,v $
 * $Revision: 1.31 $
 * $Date: 2006/10/04 16:34:01 $
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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LinkedPanel extends JTabbedPane implements MainPanel {
	/**
	 * This class enables you to put a MainPanel in a separate window.
	 * the separate window is not a ViewFrame and should be thought of as
	 * subordinate to the ViewFrame that holds the LinkedPanel, although
	 * there's no way to enforce that from java without always having the
	 * subwindow on top.
	 */
	private class MainPanelFrame extends JFrame {
		MainPanel mainPanel;
		/**
		 * @param mp main panel to display
		 */
		public MainPanelFrame(MainPanel mp) {
			super();
			mainPanel = mp;
			final WindowListener listener = new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
//					dockMainPanelDialog(MainPanelFrame.this);
					removeDialog(MainPanelFrame.this);
				}
				public void windowClosed(WindowEvent e) {}
				public void windowIconified(WindowEvent e) {}
				public void windowDeiconified(WindowEvent e) {}
			};
			addWindowListener(listener);

			JButton dockButton = new JButton("Dock");
			dockButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeWindowListener(listener);
					dockMainPanelDialog(MainPanelFrame.this);
				}
			});

			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeWindowListener(listener);
					removeDialog(MainPanelFrame.this);
				}
			});
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(dockButton);
			buttonPanel.add(closeButton);
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			getContentPane().add((Component) mainPanel,BorderLayout.CENTER);
			setTitle(mp.getName() + ": " + viewFrame.getDataModel().getSource());
		}

		/**
		 * @return main panel displayed by dialog
		 */
		public MainPanel getMainPanel() {
			return mainPanel;
		}
	}
	public LinkedPanel(ViewFrame viewFrame) {
		super();
		setName("LinkedPanel");
		setViewFrame(viewFrame);
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ConfigNode viewsNode = getConfigNode();
				if (viewsNode != null) {
					viewsNode.setAttribute("selected", getSelectedIndex(), 0);
				}
			}
		});
	}

	ViewFrame viewFrame;
	/** Setter for viewFrame */
	public void setViewFrame(ViewFrame viewFrame) {
		this.viewFrame = viewFrame;
	}
	/** Getter for viewFrame */
	public ViewFrame getViewFrame() {
		return viewFrame;
	}

	/**
	 *  This syncronizes the sub compnents with their persistent storage.
	 */
	public void syncConfig() {
		int n = getComponentCount();
		for (int i = 0; i < n; i++) {
			MainPanel modelView = (MainPanel) getComponentAt(i);
			modelView.syncConfig();
		}
	}

	ConfigNode configNode = null;
	/** Setter for configNode */
	public void setConfigNode(ConfigNode configNode) {
		this.configNode = configNode;
	}

	/**
	* this method gets the config node on which this component is based, or null.
	*/
	public ConfigNode getConfigNode() {
		return configNode;
	}

	/**
	 *  Add items related to settings
	 *
	 * @param  menu  A menu to add items to.
	 */
	 public void populateSettingsMenu(JMenu menu) {
	 }


	/**
	 *  Add items which do some kind of analysis
	 *
	 * @param  menu  A menu to add items to.
	 */
	public void populateAnalysisMenu(JMenu menu) {
	}


	/**
	 *  Add items which allow for export, if any.
	 *
	 * @param  menu  A menu to add items to.
	 */
	 public void populateExportMenu(JMenu menu) {
	 }


	/**
	 *  ensure a particular index is visible. Used by Find.
	 *
	 * @param  index  Index of gene in cdt to make visible
	 */
	public void scrollToGene(int index) {
		int n = getComponentCount();
		for (int i = 0; i < n; i++) {
			MainPanel modelView = (MainPanel) getComponentAt(i);
			modelView.scrollToGene(index);
		}
	}
	public void scrollToArray(int index) {
		int n = getComponentCount();
		for (int i = 0; i < n; i++) {
			MainPanel modelView = (MainPanel) getComponentAt(i);
			modelView.scrollToArray(index);
		}
	}

	public void addTab(MainPanel mp) {
		addTab(mp.getName(), mp.getIcon(), (Component) mp, "What's this button do?");
		mp.getConfigNode().setAttribute("dock", 1, -1);
	}

	/**
	 * used to hold list of open mp dialogs
	 */
	Vector mpdialogs = new Vector();
	public void addDialog(MainPanel mp) {
		final MainPanelFrame nmp = new MainPanelFrame(mp);
		mpdialogs.add(nmp);
		Rectangle r = viewFrame.getBounds();
		r.height -=10;
		r.width -= 10;
		r.x += 10;
		r.y += 10;
		nmp.setBounds(r);
		nmp.setVisible(true);
		mp.getConfigNode().setAttribute("dock", 0, -1);
	}
	/**
	 * removed ConfigNode of mainpanel as well as dialog window
	 * 
	 * @param mp mainpanel to remove
	 */
	public void removeDialog(MainPanel mp) {
		Enumeration e = mpdialogs.elements();
		while (e.hasMoreElements()) {
			MainPanelFrame mpd = (MainPanelFrame) e.nextElement();
			if (mpd.getMainPanel() == mp)
				removeDialog(mpd);
		}
	}
	/**
	 * removed ConfigNode of mainpanel as well as dialog window
	 * 
	 * @param mp mainpanel to remove
	 */
	public void removeDialog(MainPanelFrame mpd) {
		mpdialogs.remove(mpd);
		ConfigNode viewsNode = getConfigNode();
		viewsNode.remove(mpd.getMainPanel().getConfigNode());
		mpd.dispose();
	}
	public void dockMainPanelDialog(MainPanelFrame mpd) {
		MainPanel mp = mpd.getMainPanel();
		mpdialogs.remove(mpd);
		mpd.setVisible(false);
		addTab(mp);
		mpd.dispose();
		mp.getConfigNode().setAttribute("dock", 1, -1);
	}
	public void detachCurrent() {
		Component current =  getSelectedComponent();
		if (current != null) {
			MainPanel mp = (MainPanel) current;
			remove(current);
			addDialog(mp);
			mp.getConfigNode().setAttribute("dock", 0, -1);
		}
	}
	public void removeCurrent() {
		Component current =  getSelectedComponent();
		if (current != null) {
			MainPanel cPanel = (MainPanel) current;
			cPanel.syncConfig();
			ConfigNode viewsNode = getConfigNode();
			viewsNode.remove(cPanel.getConfigNode());
			remove(current);
		}
	}
	


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.MainPanel#getIcon()
	 */
	public ImageIcon getIcon() {
		// can't nest linked panels yet.
		return null;
	}
	public MainPanel[] getMainPanelsByName(String name) {
		Vector matches = new Vector();
		// check the detached plugins
		Enumeration e = mpdialogs.elements();
		while (e.hasMoreElements()) {
			MainPanelFrame mpd = (MainPanelFrame) e.nextElement();
			MainPanel mp = mpd.getMainPanel();
			if (name.equals(mp.getName()))
				matches.add(mp);
		}
		Component [] docked = this.getComponents();

		// check the docked plugins
		for (int i =0; i < docked.length; i++) {
			MainPanel mp = (MainPanel) docked[i];
			if (name.equals(mp.getName())) {
				matches.add(mp);
			}
		}
		
		Object [] comps = matches.toArray();
		MainPanel [] ret = new MainPanel[comps.length];
		for (int i = 0 ; i < comps.length; i++)
			ret[i] = (MainPanel) comps[i];
		return ret;
	}
	public MainPanel[] getMainPanels() {
		Vector matches = new Vector();
		// check the detached plugins
		Enumeration e = mpdialogs.elements();
		while (e.hasMoreElements()) {
			MainPanelFrame mpd = (MainPanelFrame) e.nextElement();
			MainPanel mp = mpd.getMainPanel();
			matches.add(mp);
		}
		Component [] docked = this.getComponents();

		// check the docked plugins
		for (int i =0; i < docked.length; i++) {
			MainPanel mp = (MainPanel) docked[i];
			matches.add(mp);
		}
		
		Object [] comps = matches.toArray();
		MainPanel [] ret = new MainPanel[comps.length];
		for (int i = 0 ; i < comps.length; i++)
			ret[i] = (MainPanel) comps[i];
		return ret;
	}
}

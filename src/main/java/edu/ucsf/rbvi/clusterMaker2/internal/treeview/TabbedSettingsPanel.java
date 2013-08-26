/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TabbedSettingsPanel.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/08/19 01:36:13 $
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


import javax.swing.*;
/**
 * This class is actually a settings panel container. You can put multiple settings panels
 * within this container and tab between them. It does not provide a save/cancel button,
 * for that use a SettingsPanelHolder.
 * 
 * @author aloksaldanha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TabbedSettingsPanel extends JTabbedPane implements SettingsPanel {

	public void setSelectedIndex(int i) {
		synchronizeFrom(i);
		super.setSelectedIndex(i);
	}
	public void synchronizeTo() {
		int n = getTabCount();
		for (int i = 0; i < n; i++) {
			synchronizeTo(i);
		}
	}
	public void synchronizeTo(int i) {
		((SettingsPanel) getComponentAt(i)).synchronizeTo();
	}
	
	
	
	public void synchronizeFrom() {
		int n = getTabCount();
		for (int i = 0; i < n; i++) {
			((SettingsPanel) getComponentAt(i)).synchronizeFrom();
		}
	}
	public void synchronizeFrom(int i) {
		((SettingsPanel) getComponentAt(i)).synchronizeFrom();
	}
	public void addSettingsPanel(String name, SettingsPanel sP) {
		addTab(name, (java.awt.Component) sP);
	}
}


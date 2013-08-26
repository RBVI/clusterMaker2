/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ModelessSettingsDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/12/21 03:28:14 $
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
import java.awt.event.*;
import java.awt.*;
/**
* this is a dialog which displays a modeless settings dialog.
* it includes a close button, which will dispose of the dialog when it is pressed.
* it could be extended to include a hide button, which would not dispose but just hide.
*/
public class ModelessSettingsDialog extends JDialog {
  SettingsPanel settingsPanel;
  JDialog settingsFrame;

  public ModelessSettingsDialog(JFrame frame, String title, SettingsPanel panel) {
	super(frame, title, false);
	settingsPanel = panel;
	settingsFrame = this;
	JPanel inner = new JPanel();
	inner.setLayout(new BorderLayout());
	inner.add((Component) panel, BorderLayout.CENTER);
	inner.add(new ButtonPanel(), BorderLayout.SOUTH);
	getContentPane().add(inner); 
	pack();
  }
  public void setVisible(boolean visible) {
	  settingsPanel.synchronizeFrom();
	  super.setVisible(visible);
  }
  
  class ButtonPanel extends JPanel {
	ButtonPanel() {
	  
		  JButton save_button = new JButton("Close");
		  save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			  settingsPanel.synchronizeTo();
			  settingsFrame.dispose();
			}
		  });
		  add(save_button);
	  JButton cancel_button = new JButton("Cancel");
	  cancel_button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		  settingsPanel.synchronizeFrom();
		  settingsFrame.dispose();
		}
	  });
//	  add(cancel_button);
	}
  }
}

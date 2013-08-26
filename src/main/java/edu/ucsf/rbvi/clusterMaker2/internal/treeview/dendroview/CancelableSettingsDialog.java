/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: CancelableSettingsDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/12/21 03:28:13 $
 * $Name:  $
 *
 * This file is part of Java TreeView
 * Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. Modified by 
 * Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 * This software is provided under the GNU GPL Version 2. In particular,
 *
 * 1) If you modify a source file, make a comment in it containing your name and the date.
 * 2) If you distribute a modified version, you must do it under the GPL 2.
 * 3) Developers are encouraged but not required to notify the Java TreeView 
 * maintainers at alok@genome.stanford.edu when they make a useful addition. It would 
 * be nice if significant contributions could be merged into the main distribution.
 *
 * A full copy of the license can be found in gpl.txt or online at
 * http://www.gnu.org/licenses/gpl.txt
 *
 * END_HEADER
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.SettingsPanel;

/**
 *  this is a dialog which displays a single cancelable settings panel
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.6 $ $Date: 2004/12/21 03:28:13 $
 */
public class CancelableSettingsDialog extends JDialog {
	SettingsPanel settingsPanel;
	JDialog settingsFrame;


	/**
	 *
	 * @param  frame  <code>JFrame</code> to block on
	 * @param  title  a title for the dialog
	 * @param  panel  A <code>SettingsPanel</code> to feature in the dialog.
	 */
	public CancelableSettingsDialog(JFrame frame, String title, SettingsPanel panel) {
		super(frame, title, true);
		settingsPanel = panel;
		settingsFrame = this;
		JPanel inner  = new JPanel();
		inner.setLayout(new BorderLayout());
		inner.add((Component) panel, BorderLayout.CENTER);
		inner.add(new ButtonPanel(), BorderLayout.SOUTH);
		getContentPane().add(inner);
		pack();
	}


	class ButtonPanel extends JPanel {
		ButtonPanel() {
			JButton save_button    = new JButton("Save");
			save_button.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							settingsPanel.synchronizeTo();
							settingsFrame.dispose();
						} catch (java.lang.OutOfMemoryError ex) {
							JPanel temp = new JPanel();
							temp. add(new JLabel("Out of memory, try smaller pixel settings or allocate more RAM"));
							temp. add(new JLabel("see Chapter 3 of Help->Documentation... for Out of Memory and Image Export)"));
							
							JOptionPane.showMessageDialog(CancelableSettingsDialog.this,  temp);
						}
					}
				});
			add(save_button);

			JButton cancel_button  = new JButton("Cancel");
			cancel_button.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						settingsPanel.synchronizeFrom();
						settingsFrame.dispose();
					}
				});
			add(cancel_button);
		}
	}
}


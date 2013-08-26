/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: MessagePanel.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/12/21 03:28:13 $
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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;

public class MessagePanel extends JScrollPane {
    
    protected MessageCanvas messagecanvas;
    protected Vector messages;

    private String title;

		class MessageCanvas extends JPanel {
			
			public void paintComponent(Graphics g) {
				
				// Make sure we're anti aliased
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 

				int xoff = 0;
				int yoff = 0;
				FontMetrics metrics = getFontMetrics(g.getFont());
				int ascent = metrics.getAscent();
				int height = 0;
				Enumeration e = messages.elements();
				Dimension size = getSize();
				g.clearRect(0, 0, size.width, size.height);
				
				height += ascent;
				g.drawString(title,-xoff, height);

				
				while (e.hasMoreElements()) {
					String message = (String) e.nextElement();
					if (message == null) continue;
					height += ascent;
					g.drawString(message, -xoff, height);
				}
			}
			
			public Dimension getPreferredSize() {
				FontMetrics metrics = getFontMetrics(getFont());
				int ascent = metrics.getAscent();
				// for title...
				int height = ascent;
				int width = metrics.stringWidth(title);
				Enumeration e = messages.elements();
				while (e.hasMoreElements()) {
					String message = (String) e.nextElement();
					if (message == null) continue;
					height += ascent;
					int length = metrics.stringWidth(message);
					if (width < length) {width = length;}
				}
				return new Dimension(width, height);
			}
			
		}	
		
		public MessagePanel() {
			this(null);
		}

	public MessagePanel(String t) {
		super();
		title = t;
		messages = new Vector(5,5);
		messagecanvas = new MessageCanvas();
		messagecanvas.setBackground(Color.white);
		messagecanvas.setForeground(Color.black);
		setViewportView(messagecanvas);
	}
    
		public void setMessages(String [] m) {
			resetMessages();
			int i;
			for (i = 0; i < m.length; i++) {
				addMessage(m[i]);
			}
			layoutMessages();
		}

		public void resetMessages() {
			messages.removeAllElements();
		}
		
		public void addMessage(String message) {
			messages.addElement(message);
		}
		
		public void layoutMessages() {
			revalidate();
			repaint();
		}
		private JLabel getTitleLabel() {
			return new JLabel(title);
		}
}

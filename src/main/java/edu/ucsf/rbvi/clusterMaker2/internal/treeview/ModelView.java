/* BEGIN_HEADER                                              Java TreeView
*
* $Author: alokito $
* $RCSfile: ModelView.java,v $
* $Revision: 1.14 $
* $Date: 2005/12/05 05:27:53 $
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

import javax.swing.*;

/**
* superclass, to hold info and code common to all model views
*
* This currently consists of buffer management, status and hints panels.
* Interestingly, but necessarily, it has no dependancy on any models.
*/
public abstract class ModelView extends JPanel 
                                implements java.util.Observer,
                                MouseListener {
	protected ViewFrame    viewFrame = null;
	protected MessagePanel hint = null;
	protected MessagePanel status = null;
	protected boolean      hasMouse = false;
	
	/* here so that subclass will work with BufferedModelView too */
	protected boolean      offscreenValid    = false;
	protected boolean      offscreenChanged  = false;
	protected Dimension    offscreenSize     = null;
	/**
	* holds actual thing to be displayed...
	*/
	protected JComponent panel;
	
	protected ModelView() {
		super(false);
		setBackground(Color.white);
	}
	
	/** 
	* viewName, returns name of view suitable for printing
	* perhaps this should be replaced by reflection?
	*
	* @return String containing name of view.
	*/
	abstract public String viewName();
	
	public void setViewFrame(ViewFrame m) {viewFrame = m;}
	public ViewFrame getViewFrame() {return viewFrame;}
	public void setHintPanel(MessagePanel h) {hint = h;}
	public void setStatusPanel(MessagePanel s) {status = s;}
	
	private String[] default_hint = null;
	public String[]  getHints() {
		if (default_hint == null) {
			default_hint = new String [] {"No hints for " + viewName()};
		}
		return default_hint;
	}
	
	private String[] default_status = null;
	/**
	* Strings describing status to user, suitable for display.
	*
	* @return Array of strings, representing status
	*/
	public String[]  getStatus() {
		if (default_status == null) {
			default_status = new String []
			{"No status info for " + viewName()};
		}
		return default_status;
	}
	
	
	public JComponent getComponent() {return panel;}
	
	
	
	/** 
	* Update the double buffer, if buffered
	* Otherwise, just called by paintComponent to paint the main component.
	*
	* called only when offscreen buffer is marked as invalid, or if
	* the onscreen size has changed.
	*
	*
	* note: now actually called by paintcomponent to update the swing double buffer.
	
	*/
	abstract protected void updateBuffer(Graphics g);
	
	/** 
	* This is a stub so that components which work with this will also work with the ModelViewBuffered.
	* importantly, no buffer is ever actually allocated.
	*/
	public synchronized void paintComponent(Graphics g) {
		Rectangle clip = g.getClipBounds();
		g.setColor(Color.white);
		g.fillRect(clip.x,clip.y,clip.width, clip.height);
		
		
		Dimension reqSize = getSize();
		if (reqSize == null) { return;}
		// monitor size changes
		if ((offscreenSize == null) ||
			(reqSize.width != offscreenSize.width) ||
			(reqSize.height != offscreenSize.height)) {
				offscreenChanged = true;
				offscreenSize = reqSize;
			}
			
			if (isEnabled()) {
				offscreenValid = false;
				updateBuffer(g);
				paintComposite(g);
			}
			//	System.out.println("Exiting " + viewName() + " to clip " + clip );
	}
	
	/** 
	This call is to be used to add a quick addition to the
	component which you don't want to put on the doublebuffer. The
	composite could potentially be another buffer.  
	
	Currently, this is only used by globalview for adding the zoom
	rect and focus rect.
	*/
	public void paintComposite(Graphics g) {
		return;
	}
	
	public void addNotify() {
		super.addNotify();
	}

	public Window enclosingWindow() {
		 Object f = getParent ();
		  while (! (f instanceof Window))
		    f = ((Component) f).getParent ();
		  return  (Window) f;
	}
	
	/**
	* This does the following:
	*  1) requests focus
	*  2) sets status and hint panels appropriately
	*  3) keeps track of whether we have the mouse.
	*/
	public void mouseEntered(MouseEvent e) {
		if (viewFrame == null) {
			// CyLogger.getLogger(ModelView.class).warn("viewFrame null in ModelView.mouseEntered. Instance " + this);
			return;
		}
		
		Window frame = enclosingWindow();
		if (frame.isActive()) {
			requestFocus();
			if (hint != null) {hint.setMessages(getHints());}
			try {
				if (status != null) {status.setMessages(getStatus());}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.toString());
			}
		}
		hasMouse = true;
	}
	/**
	* keeps track of when mouse not present.
	*/
	public void mouseExited(MouseEvent e) {
		hasMouse = false;
	}
	/* a bunch of stubs so we can claim to be a MouseListener	*/
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {} 
	public void mouseDragged(MouseEvent e) {}	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}


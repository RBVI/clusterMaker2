/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ModelViewProduced.java,v $
 * $Revision: 1.7 $
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
import java.awt.image.MemoryImageSource;

/**
 * superclass, to hold info and code common to all model views
 *
 * This adds buffer management to the modelview.
 * Interestingly, but necessarily, it has no dependancy on any models.
 */
public abstract class ModelViewProduced extends ModelView {
	protected int []	   offscreenPixels = null;
	protected MemoryImageSource offscreenSource = null;
    protected Image        offscreenBuffer = null;
    protected Graphics     offscreenGraphics = null;
	protected int offscreenScanSize = 0;
    protected boolean      rotateOffscreen = false;


    protected ModelViewProduced() {
		super();
    }


    /**
	* this method sets up all the instance variables.
	* XXX - THIS FAILS ON MAC OS X
	* since mac os x doesn't let you call getGraphics on the Image if it's generated from
	* a pixels array... hmm...
	*/
	protected void ensureCapacity(Dimension req) {
		if (offscreenBuffer == null) {
			createNewBuffer(req.width, req.height);
		} else {
			int w = offscreenBuffer.getWidth(null);
			int h = offscreenBuffer.getHeight(null);
			if ((w < req.width) || (h < req.height)) {
				if (w < req.width) { w = req.width;}
				if (h < req.height) { h = req.height;}
				// should I try to free something?
				createNewBuffer(w,h);
			}
		}
    }

	private synchronized void createNewBuffer(int w, int h) {
		// should I be copy over pixels instead?
		offscreenPixels = new int[w * h];
		offscreenScanSize = w;
		offscreenSource = new MemoryImageSource(w, h, offscreenPixels, 0, w);
		offscreenSource.setAnimated(true);
		offscreenBuffer = createImage(offscreenSource);
	}
    /* 
     * The double buffer in Swing
     * doesn't seem to be persistent across draws. for instance, every
     * time another window obscures one of ourwindows and then moves,
     * a repaint is triggered by most VMs.
     *
     * We apparently need to maintain our own persistent offscreen
     * buffer for speed reasons...
     */
	 public synchronized void paintComponent(Graphics g) {
		 Rectangle clip = g.getClipBounds();
		 //	System.out.println("Entering " + viewName() + " to clip " + clip );
		 
		 Dimension newsize = getSize();
		 if (newsize == null) { return;}
		 
		 Dimension reqSize;
		 reqSize = newsize;
		 // monitor size changes
		 if ((offscreenBuffer == null) ||
			 (reqSize.width != offscreenSize.width) ||
			 (reqSize.height != offscreenSize.height)) {
				 
			 offscreenSize = reqSize;
			 ensureCapacity(offscreenSize);
			 offscreenChanged = true;
			 offscreenValid = false;
		 } else {
			 offscreenChanged = false;
		 }
		 // update offscreenBuffer if necessary
		 int backgoundInt = (255 << 24) | (255 << 16) | (255 << 8) | 255;
		 for (int i =0; i < offscreenPixels.length; i++) {
			 offscreenPixels[i] = backgoundInt;
		 }
		 if (isEnabled()) {
			 if ((offscreenSize.width > 0) && (offscreenSize.height > 0)) {
				 updatePixels();
				 offscreenValid = true;
			 }
		 }
		 
		 g.drawImage(offscreenBuffer, 0, 0, null);
		 paintComposite(g);
		 //	System.out.println("Exiting " + viewName() + " to clip " + clip );
	 }
    
	/**
	* method to update the offscreenPixels.
	* don't forget to call offscreenSource.newPixels(); !
	*/
	abstract protected void updatePixels();
}

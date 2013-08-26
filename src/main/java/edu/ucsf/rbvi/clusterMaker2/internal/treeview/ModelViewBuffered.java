/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ModelViewBuffered.java,v $
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


import java.awt.*;
import java.awt.image.MemoryImageSource;

/**
 * superclass, to hold info and code common to all model views
 *
 * This adds buffer management to the modelview.
 * Interestingly, but necessarily, it has no dependancy on any models.
 */
public abstract class ModelViewBuffered extends ModelView {
	protected int []	   offscreenPixels = null;
    protected Image        offscreenBuffer = null;
    protected Graphics     offscreenGraphics = null;

    protected boolean      rotateOffscreen = false;


    protected ModelViewBuffered() {
		super();
    }

    /**
	* this method does no management of instance variables.
	*/
	public Image ensureCapacity(Image i, Dimension req) {
		if (i == null) {
			return createImage(req.width, req.height);
		}
		
		int w = i.getWidth(null);
		int h = i.getHeight(null);
		if ((w < req.width) || (h < req.height)) {
			if (w < req.width) { w = req.width;}
			if (h < req.height) { h = req.height;}
			// should I try to free something?
			Image n = createImage(w, h);
			n.getGraphics().drawImage(i, 0,0,null);
			return n;
		} else {
			return i;
		}
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
		}
		
		int w = offscreenBuffer.getWidth(null);
		int h = offscreenBuffer.getHeight(null);
		if ((w < req.width) || (h < req.height)) {
			if (w < req.width) { w = req.width;}
			if (h < req.height) { h = req.height;}
			// should I try to free something?
			createNewBuffer(w,h);
		}
    }

	private synchronized void createNewBuffer(int w, int h) {
		offscreenPixels = new int[w * h];
		MemoryImageSource source = new MemoryImageSource(w, h, offscreenPixels, 0, w);
		source.setAnimated(true);
		Image n = createImage(source);
		if (offscreenBuffer != null) {
			// should I be copying over pixels instead?
			n.getGraphics().drawImage(offscreenBuffer, 0,0,null);
		}
		offscreenBuffer = n;
		offscreenGraphics = offscreenBuffer.getGraphics();
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
		offscreenBuffer = ensureCapacity(offscreenBuffer, offscreenSize);
		offscreenGraphics = offscreenBuffer.getGraphics();
	    try {
		((Graphics2D) offscreenGraphics).setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_OFF);
	    } catch (java.lang.NoClassDefFoundError err) {
		// ignore if Graphics2D not found...
	    }
	    offscreenChanged = true;
	} else {
	    offscreenChanged = false;
	}

	// update offscreenBuffer if necessary
	g.setColor(Color.white);
	g.fillRect(clip.x,clip.y,clip.width, clip.height);
	if (isEnabled()) {
	    if ((offscreenSize.width > 0) && (offscreenSize.height > 0)) {
		updateBuffer(offscreenGraphics);
		offscreenValid = true;
	    }
	} else {
//	  	System.out.println(viewName() + " not enabled");
	    Graphics tg= offscreenBuffer.getGraphics();
	    tg.setColor(Color.white);
	    tg.fillRect
		(0, 0, offscreenSize.width, offscreenSize.height);
	}
	
	if (g != offscreenGraphics) { // sometimes paint directly
	    g.drawImage(offscreenBuffer, 0, 0, this);
	}
	paintComposite(g);
//	System.out.println("Exiting " + viewName() + " to clip " + clip );
    }
    
}

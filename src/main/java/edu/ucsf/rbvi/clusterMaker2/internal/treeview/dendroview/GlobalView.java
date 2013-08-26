	/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: GlobalView.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/16 19:13:45 $
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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Observable;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelViewProduced;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

class GlobalView extends ModelViewProduced 
                 implements  MouseMotionListener, MouseListener, KeyListener {    

	private ArrayDrawer drawer;

	/**
	 * Points to track candidate selected rows/cols
	 * should reflect where the mouse has actually been
	 */
	private Point startPoint = new Point();
	private Point endPoint = new Point();

	/**
	 * This rectangle keeps track of where the drag rect was drawn
	 */
	private Rectangle dragRect = new Rectangle();

	/**
	 * Rectangle to track yellow selected rectangle (pixels)
	 */
	private Rectangle selectionRect = null;

	/**
	 * Rectangle to track blue zoom rectangle (pixels)
	 */
	private Rectangle zoomRect = null;

	protected TreeSelectionI geneSelection;
	protected TreeSelectionI arraySelection;
	protected MapContainer xmap, ymap;
	protected MapContainer zoomXmap, zoomYmap;

	/**
 	 * GlobalView also likes to have an globalxmap and globalymap 
	 * (both of type MapContainer) to help it figure out where to draw things.
	 */
	public GlobalView() {
		super();
		panel = this;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

	}

	public Dimension getPreferredSize() {
		Dimension p = new Dimension(xmap.getRequiredPixels(),
		                            ymap.getRequiredPixels());
		return p;
	}

	public String[]  getStatus() {
		String [] status = new String[4];
		if ((geneSelection == null) || (arraySelection == null)) {
			status[0] = "ERROR: GlobalView improperly configured";
			status[1] = " geneSelection is null";
			status[2] = " thus, gene selection will not work.";
			status[3] = "";
		} else {
			int sx = arraySelection.getMinIndex();
			int ex = arraySelection.getMaxIndex();
	
			int sy = geneSelection.getMinIndex();
			int ey = geneSelection.getMaxIndex();
	
			status[0] = (ey - sy + 1)  + " genes selected";
			status[1] = (ex - sx + 1) + " arrays selected";
			status[2] = "Genes from " + sy + " to " + ey;
			status[3] = "Arrays from " + sx + " to " + ex;
		}
		return status;
	}

	private static final String [] hints = {
		"use arrow keys to move selection",
		"click and drag to select genes",
		"- hold shift to select arrays too"
	};

	public String[]  getHints() {
		return hints;
	}

	/** 
 	 * Set geneSelection
	 *
	 * @param geneSelection The TreeSelection which is set by selecting genes in the GlobalView
	 */
	public void setGeneSelection(TreeSelectionI geneSelection) {
		if (this.geneSelection != null)
			this.geneSelection.deleteObserver(this);	
		this.geneSelection = geneSelection;
		this.geneSelection.addObserver(this);
	}

	/** 
	 * Set arraySelection
	 *
	 * @param arraySelection The TreeSelection which is set by selecting arrays in the GlobalView
	 */
	public void setArraySelection(TreeSelectionI arraySelection) {
		if (this.arraySelection != null)
			this.arraySelection.deleteObserver(this);	
		this.arraySelection = arraySelection;
		this.arraySelection.addObserver(this);
	}


	/** 
	 * Set ArrayDrawer
	 *
	 * @param arrayDrawer The ArrayDrawer to be used as a source
	 */
	public void setArrayDrawer(ArrayDrawer arrayDrawer) {
		if (drawer != null)
			drawer.deleteObserver(this);	
		drawer = arrayDrawer;
		drawer.addObserver(this);
	}

	/** 
	 * Get ArrayDrawer
	 *
	 * @return The current ArrayDrawer
	 */
	public ArrayDrawer getArrayDrawer() {
		return drawer;
	}

	/** 
	 * set the xmapping for this view
	 *
	 * @param m   the new mapping
	 */
	public void setXMap(MapContainer m) {
		if (xmap != null) {
			xmap.deleteObserver(this);	    
		}
		xmap = m;
		xmap.addObserver(this);
	}

	/** DEPRECATE
	 * set the ymapping for this view
	 *
	 * @param m   the new mapping
	 */
	public void setYMap(MapContainer m) {
		if (ymap != null) {
			ymap.deleteObserver(this);	    
		}
		ymap = m;
		ymap.addObserver(this);
	}

	/**
	 * get the xmapping for this view
	 *
	 * @return  the current mapping
	 */
	public MapContainer getXMap() {return xmap;}

	/**
	 * get the ymapping for this view
	 *
	 * @return   the current mapping
	 */
	public MapContainer getYMap() {return ymap;}


	/** DEPRECATE
	 * set the xmapping for this view
	 *
	 * @param m   the new mapping
	 */
	public void setZoomXMap(MapContainer m) {
		if (zoomXmap != null) {
			zoomXmap.deleteObserver(this);	    
		}
		zoomXmap = m;
		zoomXmap.addObserver(this);
	}

	/** DEPRECATE
	 * set the ymapping for this view
	 *
	 * @param m   the new mapping
	 */
	public void setZoomYMap(MapContainer m) {
		if (zoomYmap != null) {
			zoomYmap.deleteObserver(this);	    
		}
		zoomYmap = m;
		zoomYmap.addObserver(this);
	}


	public String viewName() {return "GlobalView";}

	//Canvas Methods
	protected boolean hasDrawn = false;
	/**
	 * This method updates the graphics object directly by asking the 
	 * ArrayDrawer to draw on it directly. The alternative is to have a 
	 * pixel buffer which you update using updatePixels.
	 */
	protected void updateBuffer(Graphics g) {
		if (offscreenChanged) {
			xmap.setAvailablePixels(offscreenSize.width);
			ymap.setAvailablePixels(offscreenSize.height);
			
			if (hasDrawn == false) {
				// total kludge, but addnotify isn't working correctly...
				xmap.recalculateScale();
				ymap.recalculateScale();
				hasDrawn = true;
			}
			xmap.notifyObservers();
			ymap.notifyObservers();
		}
		if (offscreenValid == false) {
			// clear the pallette...
			g.setColor(Color.white);
			g.fillRect (0,0, offscreenSize.width, offscreenSize.height);
			g.setColor(Color.black);

			Rectangle destRect = 
						new Rectangle (0,0,xmap.getUsedPixels(), ymap.getUsedPixels());

			Rectangle sourceRect = 
						new Rectangle (xmap.getIndex(0), ymap.getIndex(0),
			                       xmap.getIndex(destRect.width) - xmap.getIndex(0), 
			                       ymap.getIndex(destRect.height) - ymap.getIndex(0));
			if ((sourceRect.x >= 0) && (sourceRect.y >= 0))
				drawer.paint(g, sourceRect, destRect, null);
		}
	}

	/** 
	 * This method updates a 
	 * pixel buffer. The alternative is to update the graphics object
	 * directly by calling updateBuffer.
	 */
	protected void updatePixels() {
		if (offscreenChanged) {
			offscreenValid = false;
			xmap.setAvailablePixels(offscreenSize.width);
			ymap.setAvailablePixels(offscreenSize.height);

			if (hasDrawn == false) {
				// total kludge, but addnotify isn't working correctly...
				xmap.recalculateScale();
				ymap.recalculateScale();
				hasDrawn = true;
			}
			xmap.notifyObservers();
			ymap.notifyObservers();
		}
		if (offscreenValid == false) {
			
			Rectangle destRect = 
			          new Rectangle (0,0, xmap.getUsedPixels(), ymap.getUsedPixels());
			Rectangle sourceRect = 
			          new Rectangle (xmap.getIndex(0), ymap.getIndex(0),
			                           xmap.getIndex(destRect.width) - xmap.getIndex(0), 
			                           ymap.getIndex(destRect.height) - ymap.getIndex(0));

			if ((sourceRect.x >= 0) && (sourceRect.y >= 0))
				drawer.paint(offscreenPixels, sourceRect, destRect, offscreenScanSize);
			offscreenSource.newPixels();
		}
	}

	public synchronized void paintComposite (Graphics g) {
		// composite the rectangles...
		if (selectionRect != null) {	    
			g.setColor(Color.white);
			g.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
			g.setColor(Color.black);
			g.drawRect(selectionRect.x-1, selectionRect.y-1, selectionRect.width+2, selectionRect.height+2);

			// Draw the zoomRect second so that it shows up
			if (zoomRect != null) {
				g.setColor(Color.magenta);
				g.drawRect(zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height);
				g.drawRect(zoomRect.x-1, zoomRect.y-1, zoomRect.width+2, zoomRect.height+2);
				g.setColor(Color.black);
				g.drawRect(zoomRect.x-2, zoomRect.y-2, zoomRect.width+4, zoomRect.height+4);
			}
		}
	}

	private void repaintRect(Graphics g, Rectangle r) {
		if (r == null) return;

		for (int i = 0; i <= r.width; i++) { // top & bottom
			drawer.paintPixel(g, xmap, ymap, i + r.x, r.y, null);
			drawer.paintPixel(g, xmap, ymap, i + r.x, r.y + r.height, null);
		}
		for (int i = 1; i < r.height; i++) {// sides
			drawer.paintPixel(g, xmap, ymap, r.x, i + r.y, null);
			drawer.paintPixel(g, xmap, ymap, r.x + r.width, i + r.y, null);
		}
	}

	protected void recalculateOverlay() {
		if ((geneSelection == null) ||(arraySelection == null)) {
			return;
		}

		int spx, spy, epx, epy;
		spx = xmap.getPixel(arraySelection.getMinIndex());
		// last pixel of last block
		epx = xmap.getPixel(arraySelection.getMaxIndex() + 1) - 1; 
		
		spy = ymap.getPixel(geneSelection.getMinIndex());
		epy = ymap.getPixel(geneSelection.getMaxIndex() + 1) - 1;

		if (epy < spy) epy = spy; // correct for roundoff error above

		if (selectionRect == null) {
			selectionRect = new Rectangle(spx, spy,
			                                epx - spx, epy - spy);
		} else {
			selectionRect.setBounds(spx, spy,
			                        epx - spx, epy - spy);
		}
	}

	protected void recalculateZoom() {
		if (selectionRect == null) return;
		int spx, epx, spy, epy;
		try {
			spx = xmap.getPixel(zoomXmap.getIndex(0));
			epx = xmap.getPixel(zoomXmap.getIndex(zoomXmap.getUsedPixels())) - 1;
	
			spy = ymap.getPixel(zoomYmap.getIndex(0));
			epy = ymap.getPixel(zoomYmap.getIndex(zoomYmap.getUsedPixels())) - 1;
		} catch (java.lang.ArithmeticException e) {
			// silently ignore div zero exceptions, which arise when 
			// some dimension is zero and fillmap is selected...
			return;
		}
		
		if (zoomRect == null) {
			zoomRect = new Rectangle(spx, spy, epx - spx, epy - spy);
		} else {
			zoomRect.setBounds(spx, spy, epx - spx, epy - spy);
		}
	}

    
	// Observer Methods
	public void update(Observable o, Object arg) {
		if (o == geneSelection) {
			if (arraySelection.getNSelectedIndexes() == 0) {
				if (geneSelection.getNSelectedIndexes() != 0) {
					// select all arrays if some genes selected...
					arraySelection.selectAllIndexes();
					// notifies self...
					arraySelection.notifyObservers();
					return;
				}
			}
			recalculateOverlay();
		} else if (o == arraySelection) {
			if (geneSelection.getNSelectedIndexes() == 0) {
				if (arraySelection.getNSelectedIndexes() != 0) {
					// select all genes if some arrays selected...
					geneSelection.selectAllIndexes();
					// notifies self...
					geneSelection.notifyObservers();
					return;
				}
			} 
			recalculateOverlay();
		} else if ((o == xmap) || o == ymap) {
			recalculateZoom(); // it moves around, you see...
			recalculateOverlay();
			offscreenValid = false;
		} else if ((o == zoomYmap) || (o == zoomXmap)) {
			recalculateZoom();
		/*
			if (o == zoomXmap) {
				if ((zoomYmap.getUsedPixels() == 0) && (zoomXmap.getUsedPixels() != 0)) {
					zoomYmap.setIndexRange(ymap.getMinIndex(), ymap.getMaxIndex());
					zoomYmap.notifyObservers();
				}
			} else if (o == zoomYmap) {
				if ((zoomXmap.getUsedPixels() == 0) && (zoomYmap.getUsedPixels() != 0)) {
					zoomXmap.setIndexRange(xmap.getMinIndex(), xmap.getMaxIndex());
					zoomXmap.notifyObservers();
				}
			}
		 */
			if ((status != null) && hasMouse)
				{status.setMessages(getStatus());}
		} else if (o == drawer) {
			/* signal from drawer means that it need to
			 * draw something different. 
			 */
			offscreenValid = false;
		} else {
			// CyLogger.getLogger(GlobalView.class).warn("GlobalView got weird update : " + o);
		}
		repaint();
	}

	// Mouse Listener 
	public void mousePressed(MouseEvent e) {
		if (enclosingWindow().isActive() == false) return;

		startPoint.setLocation(xmap.getIndex(e.getX()), ymap.getIndex(e.getY()));
		endPoint.setLocation(startPoint.x, startPoint.y);
		dragRect.setLocation(startPoint.x, startPoint.y);
		dragRect.setSize(endPoint.x - dragRect.x, endPoint.y - dragRect.y);

		drawBand(dragRect);
	}

	public void mouseReleased(MouseEvent e) {
		if (enclosingWindow().isActive() == false) return;
		mouseDragged(e);
		drawBand(dragRect);	
		if (e.isShiftDown()) {
			selectRectangle(startPoint, endPoint);
		} else {
			Point start = new Point(xmap.getMinIndex(), startPoint.y);
			Point end = new Point(xmap.getMaxIndex(), endPoint.y);
			selectRectangle(start, end);
		}
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {
		// rubber band?
		drawBand(dragRect);	
		endPoint.setLocation(xmap.getIndex(e.getX()), ymap.getIndex(e.getY()));	
		if (e.isShiftDown()) {
			dragRect.setLocation(startPoint.x, startPoint.y);
			dragRect.setSize(0,0);
			dragRect.add(endPoint.x, endPoint.y);
		} else {
			dragRect.setLocation(xmap.getMinIndex(), startPoint.y);
			dragRect.setSize(0,0);
			dragRect.add(xmap.getMaxIndex(), endPoint.y);
		}
		drawBand(dragRect);
	}

	private void drawBand(Rectangle l) { 
		Graphics g = getGraphics();
		g.setXORMode(getBackground());
		int x = xmap.getPixel(l.x);
		int y = ymap.getPixel(l.y);
		int w = xmap.getPixel(l.x + l.width  + 1) - x;
		int h = ymap.getPixel(l.y + l.height + 1) - y;
		g.drawRect(x, y, w, h);
		g.setPaintMode();
	}

	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		// System.out.println("Key typed: "+c);
		switch (c) {
		case '+':
			// System.out.println("Equals");
			xmap.setScale(xmap.getScale()*2);
			ymap.setScale(ymap.getScale()*2);
			break;
		case '-':
			// System.out.println("Minus");
			double xScale = xmap.getScale()/2;
			double yScale = ymap.getScale()/2;
			if (xScale >= 1.0)
			xmap.setScale(xScale);
			if (yScale >= 1.0)
				ymap.setScale(yScale);
			break;
		default:
			break;
		}
	}

	// KeyListener 
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		// System.out.println("Key pressed: "+c);
		startPoint.setLocation(arraySelection.getMinIndex(), geneSelection.getMinIndex());
		endPoint.setLocation (arraySelection.getMaxIndex(), geneSelection.getMaxIndex());

		if (e.isControlDown()) {
			// System.out.print("Control-");
			switch (c) {
			case KeyEvent.VK_UP:
				// System.out.println("Up");
				startPoint.translate(0, -1); 
				endPoint.translate(0, 1); 
				break;
			case KeyEvent.VK_DOWN:
				// System.out.println("Down");
				startPoint.translate(0, 1); 
				endPoint.translate(0, -1); 
				break;
			case KeyEvent.VK_LEFT:
				// System.out.println("Left");
				startPoint.translate(1, 0); 
				endPoint.translate(-1, 0); 
				break;
			case KeyEvent.VK_RIGHT:
				// System.out.println("Right");
				startPoint.translate(-1, 0); 
				endPoint.translate(1, 0); 
				break;
			default:
				return;
			}
		} else {
			switch (c) {
			case KeyEvent.VK_UP:
				// System.out.println("Up");
				startPoint.translate(0, -1); 
				endPoint.translate(0, -1); 
				break;
			case KeyEvent.VK_DOWN:
				// System.out.println("Down");
				startPoint.translate(0, 1); 
				endPoint.translate(0, 1); 
				break;
			case KeyEvent.VK_LEFT:
				// System.out.println("Left");
				startPoint.translate(-1, 0); 
				endPoint.translate(-1, 0); 
				break;
			case KeyEvent.VK_RIGHT:
				// System.out.println("Right");
				startPoint.translate(1, 0); 
				endPoint.translate(1, 0); 
				break;
			case KeyEvent.VK_SHIFT:
				// should we do something if shift is pressed during drag?
			 	return;
			default:
				return;
			}
		}
		// make sure it all fits...
		int overx = 0; int overy = 0;
		if (startPoint.x < xmap.getMinIndex()) {
			overx += startPoint.x - xmap.getMinIndex();
		}
		if (startPoint.y < ymap.getMinIndex()) {
			overy += startPoint.y - ymap.getMinIndex();
		}
		if (endPoint.x > xmap.getMaxIndex()) {
			overx += endPoint.x - xmap.getMaxIndex();
		}
		if (startPoint.y < ymap.getMinIndex()) {
			overy += startPoint.y - ymap.getMinIndex();
		}
		startPoint.x -= overx;
		endPoint.x -= overx;
		startPoint.y -= overy;
		endPoint.y -= overy;

		selectRectangle(startPoint, endPoint);
	}

	private void selectRectangle(Point start, Point end) {
		// sort so that ep is upper left corner
		if (end.x < start.x) {
			int x = end.x;
			end.x = start.x;
			start.x = x;
		}
		if (end.y < start.y) {
			int y = end.y;
			end.y = start.y;
			start.y = y;
		}

		// nodes
		geneSelection.setSelectedNode(null);
		// genes...
		geneSelection.deselectAllIndexes();
		for (int i = start.y; i <= end.y; i++) {
			geneSelection.setIndex(i, true);
		}

		// arrays...
		arraySelection.setSelectedNode(null);
		arraySelection.deselectAllIndexes();
		for (int i = start.x; i <= end.x; i++) {
			arraySelection.setIndex(i, true);
		}

		geneSelection.notifyObservers();
		arraySelection.notifyObservers();

	}

}

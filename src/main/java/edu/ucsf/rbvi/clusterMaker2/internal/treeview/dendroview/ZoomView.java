/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ZoomView.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/16 19:13:46 $
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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import javax.swing.JOptionPane;

// import cytoscape.logger.CyLogger;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelViewProduced;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

/**
 * Implements zoomed in view of the data array
 * 
 * @author Alok Saldanha <alok@genome.stanford.edu>
 * @version Alpha
 * 

 * The zoom view listens for mouse motion so that it can report status
 * and usage appropriately.

*/
class ZoomView extends ModelViewProduced implements MouseMotionListener {    
    private int overx, overy;

    /**
     * Allocate a new ZoomView
     */
    public ZoomView() {
	super();
	panel = this;

	setToolTipText("This Turns Tooltips On");
	addMouseListener(this);
	addMouseMotionListener(this);
    }

	private static final String [] hints = {
		"Mouse over to get info",
	};
	public String[]  getHints() {
		return hints;
	}
	
	/**
	* showVal indicates whether the zoom should draw the value of each cell on the canvas on top of the corresponding square. Used to display IUPAC symbols for alignment view.
	*/
	boolean showVal = false;
	/**
	* showVal indicates whether the zoom should draw the value of each cell on the canvas on top of the corresponding square. Used to display IUPAC symbols for alignment view.
	*/
	public boolean getShowVal() {
		return showVal;
	}
	/**
	* showVal indicates whether the zoom should draw the value of each cell on the canvas on top of the corresponding square. Used to display IUPAC symbols for alignment view.
	*/
	public void setShowVal(boolean showVal) {
		this.showVal = showVal;
	}

	
    public Dimension getPreferredSize() {
		// return super.getPreferredSize();
		return new Dimension(xmap.getRequiredPixels(), ymap.getRequiredPixels());
    }
    /** 
     * Set geneSelection
     *
     * @param geneSelection The TreeSelection which is set by selecting genes in the ZoomView
     */
    public void setGeneSelection(TreeSelectionI geneSelection) {
	  if (this.geneSelection != null) this.geneSelection.deleteObserver(this);	
	  this.geneSelection = geneSelection;
	  if (this.geneSelection != null) this.geneSelection.addObserver(this);
    }

		    /** 
     * Set arraySelection
     *
     * @param arraySelection The TreeSelection which is set by selecting genes in the ZoomView
     */
    public void setArraySelection(TreeSelectionI arraySelection) {
	  if (this.arraySelection != null) this.arraySelection.deleteObserver(this);	
	  this.arraySelection = arraySelection;
	  if (this.arraySelection != null) this.arraySelection.addObserver(this);
    }

    /** 
     * Set ArrayDrawer
     *
     * @param arrayDrawer The ArrayDrawer to be used as a source
     */
    public void setArrayDrawer(ArrayDrawer arrayDrawer) {
		if (drawer != null) drawer.deleteObserver(this);	
		drawer = arrayDrawer;
		if (drawer != null) drawer.addObserver(this);
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
	if (xmap != null) xmap.deleteObserver(this);	    
	xmap = m;
	if (xmap != null) xmap.addObserver(this);
    }

    /**
     * set the ymapping for this view
     *
     * @param m   the new mapping
     */
    public void setYMap(MapContainer m) {
	if (ymap != null)  ymap.deleteObserver(this);	    
	ymap = m;
	if (ymap != null) ymap.addObserver(this);
    }

    /**
     * get the xmapping for this view
     *
     * @return   the current mapping
     */
    public MapContainer getXMap() {return xmap;}
    public MapContainer getZoomXmap() {return xmap;}

    /**
     * get the ymapping for this view
     *
     * @return   the current mapping
     */
    public MapContainer getYMap() {return ymap;}
    public MapContainer getZoomYmap() {return ymap;}

	
    // method from ModelView
    public String viewName() { return "ZoomView";}

    // method from ModelView
    public String[]  getStatus() {
		try {
			if (xmap.contains(overx) && 
			ymap.contains(overy)) {
				statustext[0] = "Row:    " + (overy + 1);
				if (geneHI != null && overy >= 0) {
					int realGene = overy;
					statustext[0] += " (" + geneHI.getHeader(realGene,1) + ")";
				}
				statustext[1] = "Column: " + (overx + 1);
				if (arrayHI != null && overx >= 0) {
					statustext[1] += " (" + arrayHI.getHeader(overx, 0) + ")";
				}
				
				if (drawer.isMissing(overx, overy)) {
					statustext[2] = "Value:  No Data";		
				} else if (drawer.isEmpty(overx, overy)) {
					statustext[2] = "";
				} else {
					statustext[2] = "Value:  " + drawer.getSummary(overx, overy);
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			// ignore silently?
		}
	return statustext;
    }

		// method from ModelView
		public void updateBuffer(Graphics g) {	
			if (offscreenChanged) {
				xmap.setAvailablePixels(offscreenSize.width);
				ymap.setAvailablePixels(offscreenSize.height);
				xmap.notifyObservers();
				ymap.notifyObservers();
			}
			if (offscreenValid == false) {
				
				// clear the pallette...
				g.setColor(Color.white);
				g.fillRect
				(0,0, offscreenSize.width, offscreenSize.height);
				g.setColor(Color.black);
				
				
				destRect.setBounds(0,0,xmap.getUsedPixels(), ymap.getUsedPixels());
				
				sourceRect.setBounds(xmap.getIndex(0), ymap.getIndex(0),
				xmap.getIndex(destRect.width) - xmap.getIndex(0), 
				ymap.getIndex(destRect.height) - ymap.getIndex(0));
				
				if ((sourceRect.x >= 0) && (sourceRect.y >= 0))
					drawer.paint(g, sourceRect, destRect, null);
				
			}
		}

		public void paintComposite(Graphics g) {
				if (getShowVal()) {
					// need to draw values on screen!
					try {
						((CharArrayDrawer)drawer).paintChars(g, xmap, ymap, destRect);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "ZoomView had trouble compositing:" + e);
						setShowVal(false);
					}
				}
		}
    
	public void updatePixels() {	
		if (offscreenChanged) {
			xmap.setAvailablePixels(offscreenSize.width);
			ymap.setAvailablePixels(offscreenSize.height);
			xmap.notifyObservers();
			ymap.notifyObservers();
		}
		if (offscreenValid == false) {
			
			destRect.setBounds(0,0,xmap.getUsedPixels(), ymap.getUsedPixels());
			
			sourceRect.setBounds(xmap.getIndex(0), ymap.getIndex(0),
			xmap.getIndex(destRect.width) - xmap.getIndex(0), 
			ymap.getIndex(destRect.height) - ymap.getIndex(0));
			
			if ((sourceRect.x >= 0) && (sourceRect.y >= 0))  {
				drawer.paint(offscreenPixels, sourceRect, destRect, offscreenScanSize);
			}
			offscreenSource.newPixels();
		}
	}
    /**
     * Watch for updates from ArrayDrawer and the two maps
     * The appropriate response for both is to trigger a redraw.
     */
	 public void update(Observable o, Object arg) {	
		 if (o == drawer) {
			 //	    System.out.println("got drawer update");
			 offscreenValid = false;
		 } else if ((o == xmap) || ( o == ymap)) {
			 offscreenValid = false;	    
		 } else if ((o == geneSelection) || (o == arraySelection)) {
			 /*
			 if (cdtSelection.getNSelectedArrays() == 0) {
				if (cdtSelection.getNSelectedGenes() != 0) {
					cdtSelection.selectAllArrays();
					cdtSelection.notifyObservers();
				}
			} else {
				*/
				// Hmm... it almost seems like you could get rid of the zoom map as a mechanism of communication... but not quite, because the globalview, textview and atrzview depend on it to know what is visible in the zoom window.
				MapContainer zoomXmap = getZoomXmap();
				MapContainer zoomYmap = getZoomYmap();
				zoomYmap.setIndexRange(geneSelection.getMinIndex(),  geneSelection.getMaxIndex());
				zoomXmap.setIndexRange(arraySelection.getMinIndex(), arraySelection.getMaxIndex());
				
				zoomXmap.notifyObservers();
				zoomYmap.notifyObservers();

		 } else {
			 // CyLogger.getLogger(ZoomView.class).warn("ZoomView got weird update : " + o);
		 }
		 
		 if (offscreenValid == false) {
			 repaint();	
		 }
    }

    // MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	int ooverx = overx;
	int oovery = overy;
	overx = xmap.getIndex(e.getX());
	overy = ymap.getIndex(e.getY());
	if (oovery != overy || ooverx != overx)
	    if (status != null) 
		status.setMessages(getStatus());
    }

	public String getToolTipText(MouseEvent e) {
/* Do we want to do mouseovers if value already visible? 
		if (getShowVal()) return null; // don't do tooltips and vals at same time.
*/
		String ret = "";
		if (drawer != null) {
			int geneRow = overy;
			if (xmap.contains(overx) && ymap.contains(overy)) {
				if (drawer.isMissing(overx, geneRow)) {
					ret = "No data";
				} else if (drawer.isEmpty(overx, geneRow)) {
					ret = null;
				} else {
					ret = "" + drawer.getSummary(overx, geneRow);
				}
			}
		}
		return ret;
	}
	
	public void setHeaders(HeaderInfo ghi, HeaderInfo ahi) {
	  geneHI = ghi;
	  arrayHI = ahi;
	}
	protected TreeSelectionI geneSelection;
	protected TreeSelectionI arraySelection;
    private ArrayDrawer drawer;
    private String [] statustext = new String [] {"Mouseover Selection","",""};
    private Rectangle sourceRect = new Rectangle();
    private Rectangle destRect = new Rectangle();
    private MapContainer xmap, ymap;
	private HeaderInfo arrayHI, geneHI; // to get gene and array names...
}


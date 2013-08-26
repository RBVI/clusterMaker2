/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: GTRView.java,v $
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
import java.awt.event.*;
import java.util.Observable;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderSummary;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LinearTransformation;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelViewBuffered;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

/**
 * Draws a gene tree to show the relations between genes
 * 
 * This object requires a MapContainer to figure out the offsets for the genes.
 */

public class GTRView extends ModelViewBuffered implements
    MouseListener, KeyListener {

    /** 
     * Constructor. You still need to specify a map to have this thing draw.
     */
	 public GTRView() {
	   super();
	   panel = this;
	   destRect = new Rectangle();
	   
	   addMouseListener(this);
	   addKeyListener(this);
	 }

    
	private static final String [] hints = {
		"Click to select node",
		" - use arrow keys to navigate tree",
	};
	public String[]  getHints() {
		return hints;
	}
    /** 
     * Set the drawer
     *
     * @param d    The new drawer
     */
    public void setLeftTreeDrawer(LeftTreeDrawer d) {
	if (drawer != null)
	    drawer.deleteObserver(this);
	drawer = d;
	drawer.addObserver(this);
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
     * Set the map
     *
     * @param m The new map to be used for determining the spacing
     * between indexes.
     */
	 public void setMap(MapContainer m) {
	   if (map != null)
		 map.deleteObserver(this);
	   map = m;
	   map.addObserver(this);
	   offscreenValid = false;
	   repaint();
	 }

    /** 
     * Synchronizes TreeSelection with selectedNode.
     * 
     * sets the TreeSelection to reflect the span of the selected node.
     * sets the selected node of the TreeSelection to this node.
     * Notifies observers.
	 * Should be called whenever the internal pointer to selected node 
	 * is changed.
     */
	 private void synchMap() {
	   if ((selectedNode != null) && (geneSelection != null)) {	    
		 int start  = (int) (selectedNode.getLeftLeaf().getIndex());
		 int end    = (int) (selectedNode.getRightLeaf().getIndex());
		 geneSelection.deselectAllIndexes();
		 geneSelection.selectIndexRange(start, end);
		 geneSelection.setSelectedNode(selectedNode.getId());
		 geneSelection.notifyObservers();
	   }
	   if ((status != null) && hasMouse)
	   {status.setMessages(getStatus());}
	 }
	 
	 	protected HeaderSummary headerSummary = new HeaderSummary();
	/** Setter for headerSummary */
	public void setHeaderSummary(HeaderSummary headerSummary) {
		this.headerSummary = headerSummary;
	}
	/** Getter for headerSummary */
	public HeaderSummary getHeaderSummary() {
		return headerSummary;
	}

	 public void setSelectedNode(TreeDrawerNode n) {
		 if (selectedNode == n) return;
		 if (getYScaleEq() != null) {
			 if (selectedNode != null) {
				 drawer.paintSubtree(offscreenGraphics, 
				 getXScaleEq(), getYScaleEq(),
				 destRect, selectedNode, false);
			 }
			 
			 selectedNode = n;
			 
			 if (selectedNode != null) {
				 drawer.paintSubtree(offscreenGraphics, 
				 getXScaleEq(), getYScaleEq(),
				 destRect, selectedNode, true);
			 }
		 } else {
			 selectedNode = n;
		 }
		 synchMap();
		 //	   offscreenValid = false;
		 repaint();
	 }
	 private void selectParent() {
	   TreeDrawerNode current = selectedNode;
	   selectedNode = current.getParent();
	   if (selectedNode == null) {
		   selectedNode = current;
		   return;
	   }
	   if (current == selectedNode.getLeft())
		   current = selectedNode.getRight();
	   else
		   current = selectedNode.getLeft();
	   drawer.paintSubtree(offscreenGraphics, 
		getXScaleEq(), getYScaleEq(),
		destRect, current, true);
	   drawer.paintSingle(
	   	offscreenGraphics, getXScaleEq(), getYScaleEq(),
		destRect, selectedNode, true);
			 
	   synchMap();
	   repaint();
	 }
	 private void selectRight() {
	   if (selectedNode.isLeaf()) return;
	   TreeDrawerNode current = selectedNode;
	   selectedNode = current.getRight();
	   drawer.paintSingle(offscreenGraphics,
	   	getXScaleEq(), getYScaleEq(), destRect, current, false);
	    drawer.paintSubtree(offscreenGraphics, 
			 getXScaleEq(), getYScaleEq(),
			 destRect, current.getLeft(), false);
	    synchMap();
		repaint();
	 }
	 private void selectLeft() {
		 if (selectedNode.isLeaf()) return;
		 TreeDrawerNode current = selectedNode;
		 selectedNode = current.getLeft();
		 
		 drawer.paintSingle(offscreenGraphics,
		  getXScaleEq(), getYScaleEq(), destRect, current, false);
		 drawer.paintSubtree(offscreenGraphics, 
		  getXScaleEq(), getYScaleEq(),
		  destRect, current.getRight(), false);
		 synchMap();
		 repaint();
	 }

	 /**
	 * expect updates to come from map, geneSelection and drawer
	 */
	 public void update(Observable o, Object arg) {
		 if (o == map) {
			 // System.out.println("Got an update from map");
			 offscreenValid = false;
			 repaint();
		 }  else if (o == drawer) {
			 //System.out.println("Got an update from drawer");
			 offscreenValid = false;
			 repaint();
		 }  else if (o == geneSelection) {
			 TreeDrawerNode cand = null;
			 if (geneSelection.getNSelectedIndexes() > 0) {
					// This clause selects the array node if only a single array is selected.
				 if (geneSelection.getMinIndex() == geneSelection.getMaxIndex()) {
				 	cand = drawer.getLeaf(geneSelection.getMinIndex());
				 	// this clause selects the root node if all genes are selected.
				 } else if (
						 (geneSelection.getMinIndex() == map.getMinIndex()) 
						 &&
						 (geneSelection.getMaxIndex() == map.getMaxIndex())) {
					 cand = drawer.getRootNode();
				 }
			 }
			 if ((cand != null) && (cand.getId() != geneSelection.getSelectedNode())) {
				 String id = cand.getId();
				 geneSelection.setSelectedNode(id);
				 geneSelection.notifyObservers();
			 } else{
				 setSelectedNode(drawer.getNodeById(geneSelection.getSelectedNode()));
			 }
		 } else {
			 // CyLogger.getLogger(GTRView.class).warn(viewName() + "Got an update from unknown " + o);
		 }
	 }
	public void setZoomMap(MapContainer m) {
	}

    // method from ModelView
    public String viewName() { return "GTRView";}

    // method from ModelView
		public String[]  getStatus() {
			String [] status;
			if (selectedNode != null) {
				if (selectedNode.isLeaf()) {
					status = new String [2];
					status[0] = "Leaf Node " + selectedNode.getId();
					status[1] = "Pos " + selectedNode.getCorr();
					
				} else {
					int [] nameIndex = getHeaderSummary().getIncluded();
					status = new String [nameIndex.length * 2];
					HeaderInfo gtrInfo = getViewFrame().getDataModel().getGtrHeaderInfo();
					String [] names = gtrInfo.getNames();
					for (int i = 0; i < nameIndex.length; i++) {
						status[2*i] = names[nameIndex[i]] +":";
						status[2*i+1] = " " +gtrInfo.getHeader(gtrInfo.getHeaderIndex(selectedNode.getId()))[ nameIndex[i]];
					}
				}
			} else {
				status = new String [2];
				status[0] = "Select Node to ";
				status[1] = "view annotation.";
			}
			return status;
		}

    // method from ModelView
	public void updateBuffer(Graphics g) {
//		System.out.println("GTRView updateBuffer() called offscreenChanged " + offscreenChanged + " valid " + offscreenValid + " yScaleEq " + getYScaleEq());
		if (offscreenChanged == true) offscreenValid = false;
		if ((offscreenValid == false) && (drawer != null)) {
			map.setAvailablePixels(offscreenSize.height);
			
			// clear the pallette...
			g.setColor(Color.white);
			g.fillRect
			(0,0, offscreenSize.width, offscreenSize.height);
			g.setColor(Color.black);
			
			//	calculate Scaling
			destRect.setBounds(0,0, offscreenSize.width,map.getUsedPixels());
			setXScaleEq( new LinearTransformation
			(drawer.getCorrMin(), destRect.x,
			drawer.getCorrMax(), destRect.x + destRect.width));
			
			setYScaleEq(new LinearTransformation
			(map.getIndex(destRect.y), destRect.y,
			map.getIndex(destRect.y + destRect.height), 
			destRect.y + destRect.height));
			// System.out.println("yScaleEq " + getYScaleEq());
			// draw
			drawer.paint(g, 
			getXScaleEq(), getYScaleEq(),
			destRect, selectedNode);
		} else {
			//	        System.out.println("didn't update buffer: valid = " + offscreenValid + " drawer = " + drawer);
		}
	}


    // Mouse Listener 
    public void mouseClicked(MouseEvent e) {
	if (enclosingWindow().isActive() == false) return;
	if ((drawer != null) && (getXScaleEq() != null)) {
	  if (drawer == null) {
		  // CyLogger.getLogger(GTRView.class).warn("GTRView.mouseClicked() : drawer is null");
	  }
	  if (getXScaleEq() == null) {
		  //CyLogger.getLogger(GTRView.class).warn("GTRView.mouseClicked() : xscaleEq is null");
	  }
	    // the trick is translating back to the normalized space...
	    setSelectedNode
		(drawer.getClosest (getYScaleEq().inverseTransform(e.getY()),
				    getXScaleEq().inverseTransform(e.getX()),
				    getXScaleEq().getSlope() / getYScaleEq().getSlope())
		 );
	}
    }

	// method from KeyListener
	public void keyPressed(KeyEvent e) {
	  if (selectedNode == null) {return;}
	  int c = e.getKeyCode();	
	  switch (c) {
		case KeyEvent.VK_UP:
		  selectParent(); break;
		case KeyEvent.VK_LEFT:
		  if (selectedNode.isLeaf() == false)
			selectLeft();
		  break;
		case KeyEvent.VK_RIGHT:
		  if (selectedNode.isLeaf() == false)
			selectRight();
		  break;
		case KeyEvent.VK_DOWN:
			if (selectedNode.isLeaf() == false) {
			  TreeDrawerNode right = selectedNode.getRight();
			  TreeDrawerNode left = selectedNode.getLeft();
			  if (right.getRange() >  left.getRange()) {
				selectRight(); 
			  } else { 
				selectLeft();
			  }
			}
			break;
	  }

	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	private TreeSelectionI geneSelection;
	private LinearTransformation xScaleEq;
	/** Setter for xScaleEq */
	public void setXScaleEq(LinearTransformation xScaleEq) {
		this.xScaleEq = xScaleEq;
	}
	/** Getter for xScaleEq */
	public LinearTransformation getXScaleEq() {
		return xScaleEq;
	}
	private LinearTransformation yScaleEq;
	/** Setter for yScaleEq */
	public void setYScaleEq(LinearTransformation yScaleEq) {
		this.yScaleEq = yScaleEq;
	}
	/** Getter for yScaleEq */
	public LinearTransformation getYScaleEq() {
		return yScaleEq;
	}
	private MapContainer map;
	private LeftTreeDrawer drawer = null;
	private TreeDrawerNode selectedNode = null;
	private Rectangle destRect = null;
	/**
	 * @param nodeName
	 */
	public void scrollToNode(String nodeName) {
		TreeDrawerNode node = drawer.getNodeById(nodeName);
		if (node != null) {
			int index = (int) node.getIndex();
			if (map.isVisible(index) == false) {
				map.scrollToIndex(index);
				map.notifyObservers();
			}
		}
	}
}

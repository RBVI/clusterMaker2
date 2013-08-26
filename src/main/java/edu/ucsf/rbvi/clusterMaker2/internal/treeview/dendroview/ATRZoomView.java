/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ATRZoomView.java,v $
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

import java.awt.*;
import java.awt.event.*;
import java.util.Observable;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderSummary;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LinearTransformation;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelView;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

/**
 *  Draws an array tree to show the relations between arrays This object requires
 *  a MapContainer to figure out the offsets for the arrays. Furthermore, it sets
 *  up a scrollbar to scroll the tree, although there is currently no way to specify
 *  how large you would like the scrollable area to be, so the height is just set
 *  to match the available space.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */

public class ATRZoomView extends ModelView implements
		MouseListener, KeyListener {
	/**  Constructor, sets up AWT components  */
	public ATRZoomView() {
		super();

		panel = new JPanel();
		scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);
		destRect = new Rectangle();

		panel.setLayout(new BorderLayout());
		panel.add(this, BorderLayout.CENTER);
		panel.add(scrollbar, BorderLayout.EAST);

		addMouseListener(this);
		addKeyListener(this);
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


	private final static String[] hints = {
			"Click to select node",
			" - use arrow keys to navigate tree",
			};


	/*inherit description*/
	public String[] getHints() {
		return hints;
	}


	/**
	 *  Set the selected node, update the arraySelection, and redraw
	 *Does nothing if the node is already selected.
	 *
	 * @param  n  The new node to be selected. 
	 */
	public void setSelectedNode(TreeDrawerNode n) {
		if (selectedNode == n) {
			return;
		}
		/*
		if (selectedNode != null)
		 drawer.paintSubtree(offscreenGraphics,
		 xScaleEq, yScaleEq,
		 destRect, selectedNode, false);
		selectedNode = n;
	   if (selectedNode != null)
		 drawer.paintSubtree(offscreenGraphics,
		xScaleEq, yScaleEq,
		destRect, selectedNode, true);
		*/
		selectedNode = n;
		offscreenValid = false;
		if ((status != null) && hasMouse) {
			status.setMessages(getStatus());
		}
		synchMap();
		repaint();
	}


	private void synchMap() {
		if ((selectedNode != null) && (arraySelection != null)) {
			int start  = (int) (selectedNode.getLeftLeaf().getIndex());
			int end    = (int) (selectedNode.getRightLeaf().getIndex());
			
			if(viewFrame.getDataModel().getDataMatrix().getNumCol() > viewFrame.getDataModel().getDataMatrix().getNumUnappendedCol())
			{
				end = Math.max(viewFrame.getDataModel().getDataMatrix().getNumCol(), end); 
			}
			
			arraySelection.deselectAllIndexes();
			arraySelection.setSelectedNode(selectedNode.getId());
			arraySelection.selectIndexRange(start, end);
			arraySelection.notifyObservers();
		}
	}


	/**
	 *  Set arraySelection
	 *
	 * @param  arraySelection  The TreeSelection which clicking on this tree will modify.
	 */
	public void setArraySelection(TreeSelectionI arraySelection) {
		if (this.arraySelection != null) {
			this.arraySelection.deleteObserver(this);
		}
		this.arraySelection = arraySelection;
		this.arraySelection.addObserver(this);
	}


	/**
	 *  Set the drawer
	 *
	 * @param  d  The new drawer
	 */
	public void setInvertedTreeDrawer(InvertedTreeDrawer d) {
		if (drawer != null) {
			drawer.deleteObserver(this);
		}
		drawer = d;
		drawer.addObserver(this);
	}


	/**
	 *  Set the zoom map
	 *
	 *  Specifies where to draw leaves of tree.
	 */
	public void setZoomMap(MapContainer m) {
		if (zoomMap != null) {
			zoomMap.deleteObserver(this);
		}
		zoomMap = m;
		zoomMap.addObserver(this);
	}


	/**
	 *  expect updates to come from arraySelection, zoomMap and drawer
	 *
	 * @param  o    Observable sending update
	 * @param  arg  Argument, typically null
	 */
	public void update(Observable o, Object arg) {
		if (o == drawer) {
			//System.out.println("Got an update from drawer");
			offscreenValid = false;
			repaint();
		} else if (o == zoomMap) {
			// will call offscreenvalid, repaint() itself
			// could have been a translation...
			offscreenValid = false;
			repaint();
		} else if (o == arraySelection) {
			setSelectedNode(drawer.getNodeById(arraySelection.getSelectedNode()));
		} else {
			System.out.println(viewName() + "Got an update from unknown " + o);
		}
	}


	/**
	 *  Need to blit another part of the buffer to the screen when the scrollbar moves.
	 *
	 * @param  evt  scrollbar adjustment event
	 */
	public void adjustmentValueChanged(AdjustmentEvent evt) {
		repaint();
	}


	/**
	 *  Implementation of abstract method
	 *
	 * @return    returns name of this ModelView
	 */
	public String viewName() {
		return "ATRZoomView";
	}


	/**
	 *  Gets some user-interpretatble status information for the ATRZoomView object
	 *
	 * @return    Text describing selected node correlation
	 */
		public String[]  getStatus() {
			String [] status;
			if (selectedNode != null) {
				int [] nameIndex = getHeaderSummary().getIncluded();
				status = new String [nameIndex.length * 2];
				HeaderInfo atrInfo = getViewFrame().getDataModel().getAtrHeaderInfo();
				String [] names = atrInfo.getNames();
				for (int i = 0; i < nameIndex.length; i++) {
					status[2*i] = names[nameIndex[i]] +":";
					int index = atrInfo.getHeaderIndex(selectedNode.getId());
					if (index >= 0)
						status[2*i+1] = " " +atrInfo.getHeader(atrInfo.getHeaderIndex(selectedNode.getId()))[ nameIndex[i]];
					else
						status[2*i+1] = " ";
				}
			} else {
				status = new String [2];
				status[0] = "Select Node to ";
				status[1] = "view annotation.";
			}
			return status;
		}


	/**
	 *  updates buffer to reflect current state
	 *
	 * @param  g  Graphics object to draw to
	 */
	public void updateBuffer(Graphics g) {
		if (offscreenChanged == true) {
			offscreenValid = false;
		}
		if (offscreenValid == false) {
			if ((drawer != null) && (selectedNode != null)) {
				zoomMap.setAvailablePixels(offscreenSize.width);

				// clear the pallette...
				g.setColor(Color.white);
				g.fillRect
						(0, 0, offscreenSize.width, offscreenSize.height);
				g.setColor(Color.black);

				// don't bother drawing leaf
				if (selectedNode.isLeaf()) {
					return;
				}

				//	calculate Scaling
				destRect.setBounds(0, 0, zoomMap.getUsedPixels(), offscreenSize.height);
				g.setClip(destRect.x, destRect.y, destRect.width,
						destRect.height);
				xScaleEq = new LinearTransformation
						(zoomMap.getIndex(destRect.x), destRect.x,
						zoomMap.getIndex(destRect.x + destRect.width),
						destRect.x + destRect.width);
				yScaleEq = new LinearTransformation
						(selectedNode.getMinCorr(), destRect.y,
						drawer.getCorrMax(), destRect.y + destRect.height);

				// draw
				drawer.paint(g,
						xScaleEq, yScaleEq,
//						destRect, selectedNode);
// 5/13/2004 - this allows us to see colors in the zoomed dendrogram.
						destRect, null);
			} else {
				// most likely, no selection...
				// clear the pallette...
				g.setColor(Color.white);
				g.fillRect(0, 0, offscreenSize.width, offscreenSize.height);
			}
		}
	}


	/**
	 *  On mouseclick, select a node
	 *
	 * @param  e  Mouse clicking event
	 */
	public void mouseClicked(MouseEvent e) {
		if (this == null) {
			return;
		}
		if (isEnabled() == false) {
			return;
		}
		if (enclosingWindow().isActive() == false) {
			return;
		}
		if (drawer != null && xScaleEq != null && yScaleEq != null) {
			// the trick is translating back to the normalized space...
			setSelectedNode
					(drawer.getClosest(xScaleEq.inverseTransform(e.getX()),
					yScaleEq.inverseTransform(e.getY()),
			// weight must have correlation slope on top
					yScaleEq.getSlope() / xScaleEq.getSlope())
					);
		}
	}


	// method from KeyListener
	/**
	 *  Use keypress to navigate nodes
	 *
	 * up selects parent of current.
	 * left selects left child.
	 * right selects right child
	 * down selects child with most descendants.
	 */
	public void keyPressed(KeyEvent e) {
		if (selectedNode == null) {
			return;
		}

		int c                = e.getKeyCode();
		TreeDrawerNode cand  = null;
		switch (c) {
						case KeyEvent.VK_UP:
							cand = selectedNode.getParent();
							break;
						// hey, the tree is upside down!
						case KeyEvent.VK_LEFT:
							if (selectedNode.isLeaf() == false) {
								cand = selectedNode.getRight();
							}
							break;
						case KeyEvent.VK_RIGHT:
							if (selectedNode.isLeaf() == false) {
								cand = selectedNode.getLeft();
							}
							break;
						case KeyEvent.VK_DOWN:
							if (selectedNode.isLeaf() == false) {
								TreeDrawerNode right  = selectedNode.getRight();
								TreeDrawerNode left   = selectedNode.getLeft();
								if (right.getRange() > left.getRange()) {
									cand = right;
								} else {
									cand = left;
								}
							}
							break;
		}
		if (cand != null) {
			setSelectedNode(cand);
		}
	}


	/**
	 *  Ignore key releases
	 *
	 * @param  e  Key release event
	 */
	public void keyReleased(KeyEvent e) { }


	/**
	 *  Ignore key types
	 *
	 * @param  e  Key type event
	 */
	public void keyTyped(KeyEvent e) { }


	private TreeSelectionI arraySelection;
	private LinearTransformation xScaleEq, yScaleEq;
	private MapContainer zoomMap;
	private JScrollBar scrollbar;
	private InvertedTreeDrawer drawer = null;
	private TreeDrawerNode selectedNode = null;
	private Rectangle destRect = null;
}


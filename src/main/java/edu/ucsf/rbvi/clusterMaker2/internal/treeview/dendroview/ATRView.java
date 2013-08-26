/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ATRView.java,v $
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
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelViewBuffered;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

/**
 *  Draws an array tree to show the relations between arrays This object requires
 *  a MapContainer to figure out the offsets for the arrays. Furthermore, it sets
 *  up a scrollbar to scroll the tree, although there is currently no way to specify
 *  how large you would like the scrollable area to be.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */

public class ATRView extends ModelViewBuffered implements 
		MouseListener, KeyListener {
	/**  Constructor, sets up AWT components  */
	public ATRView() {
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


	private final static String[] hints = {
			"Click to select node",
			" - use arrow keys to navigate tree",
			};


	/*inherit description*/
	public String[] getHints() {
		return hints;
	}


	/**
	 *  Set the selected node and redraw
	 *
	 * @param  n  The new node to be selected Does nothing if the node is already selected.
	 */
	public void setSelectedNode(TreeDrawerNode n) {
		if (selectedNode == n) {
			return;
		}
		if (selectedNode != null) {
			drawer.paintSubtree(offscreenGraphics,
					xScaleEq, yScaleEq,
					destRect, selectedNode, false);
		}
		selectedNode = n;
		if (selectedNode != null) {
			if (xScaleEq != null) {
				drawer.paintSubtree(offscreenGraphics,
						xScaleEq, yScaleEq,
						destRect, selectedNode, true);
			}
		}

		if ((status != null) && hasMouse) {
			status.setMessages(getStatus());
		}
		synchMap();
		repaint();
	}

	/** make sure the selected array range reflects the selected node, if any. */
	private void synchMap() {
		if ((selectedNode != null) && (arraySelection != null)) {
			int start  = (int) (selectedNode.getLeftLeaf().getIndex());
			int end    = (int) (selectedNode.getRightLeaf().getIndex());
			arraySelection.deselectAllIndexes();
			arraySelection.setSelectedNode(selectedNode.getId());
			arraySelection.selectIndexRange(start, end);
			arraySelection.notifyObservers();
		}
		   if ((status != null) && hasMouse)
		   {status.setMessages(getStatus());}
	}



	/**
	 *  Set  <code>TreeSelection</code> object which coordinates the shared selection state.
	 *
	 * @param  arraySelection  The <code>TreeSelection</code> which is set by selecting arrays in the
	 *      </code>GlobalView</code>
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
	 *  Set the map.
	 *      For the ATRView, this determines where the leaves of the tree will be.
	 *
	 * @param  m  The new map to be used for determining the spacing between indexes.
	 */
	public void setMap(MapContainer m) {
		if (map != null) {
			map.deleteObserver(this);
		}
		map = m;
		map.addObserver(this);
	}




	/**
	 *  expect updates to come from map, arraySelection and drawer
	 *
	 * @param  o    The observable which sent the update
	 * @param  arg  Argument for this update, typically null.
	 */
	public void update(Observable o, Object arg) {
		if (isEnabled() == false) {
			return;
		}

		if (o == map) {
			offscreenValid = false;
			repaint();
		} else if (o == drawer) {
			offscreenValid = false;
			repaint();
		} else if (o == arraySelection) {
			TreeDrawerNode cand = null;
			if (arraySelection.getNSelectedIndexes() > 0) {
				// This clause selects the array node if only a single array is selected.
				if (arraySelection.getMinIndex() == arraySelection.getMaxIndex()) {
					cand = drawer.getLeaf(arraySelection.getMinIndex());
				}
				// this clause selects the root node if all arrays are selected.
				if (arraySelection.getMinIndex() == map.getMinIndex()) {
					if (arraySelection.getMaxIndex() == map.getMaxIndex()) {
						cand = drawer.getRootNode();
					}
				}
			}
			// Only notify observers if we're changing the selected node.
			if ((cand != null) && (cand.getId() != arraySelection.getSelectedNode())) {
				arraySelection.setSelectedNode(cand.getId());
				arraySelection.notifyObservers();
			} else {
				setSelectedNode(drawer.getNodeById(arraySelection.getSelectedNode()));
			}
		} else {
			System.out.println(viewName() + "Got an update from unknown " + o);
		}
	}


	/**
	 *  Need to blit another part of the buffer to the screen when the scrollbar moves.
	 *
	 * @param  evt  The adjustment event generated by the scrollbar
	 */
	public void adjustmentValueChanged(AdjustmentEvent evt) {
		repaint();
	}


	// method from ModelView
	/**
	 *  Implements abstract method from ModelView. In this case, returns "ATRView"
	 *
	 * @return    name of this subclass of modelview
	 */
	public String viewName() {
		return "ATRView";
	}

    // method from ModelView
	/**
	 *  Gets the status attribute of the ATRView object The status is some information
	 *  which the user might find useful.
	 *
	 * @return    The status value
	 */
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
					HeaderInfo atrInfo = getViewFrame().getDataModel().getAtrHeaderInfo();
					String [] names = atrInfo.getNames();
					for (int i = 0; i < nameIndex.length; i++) {
						status[2*i] = names[nameIndex[i]] +":";
						status[2*i+1] = " " +atrInfo.getHeader(atrInfo.getHeaderIndex(selectedNode.getId()))[ nameIndex[i]];
					}
				}
			} else {
				status = new String [2];
				status[0] = "Select Node to ";
				status[1] = "view annotation.";
			}
			return status;
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

	/*inherit description*/
	public void updateBuffer(Graphics g) {
		if (offscreenChanged == true) {
			offscreenValid = false;
		}
		if ((offscreenValid == false) && (drawer != null)) {
			map.setAvailablePixels(offscreenSize.width);

			// clear the pallette...
			g.setColor(Color.white);
			g.fillRect
					(0, 0, offscreenSize.width, offscreenSize.height);
			g.setColor(Color.black);

			//	calculate Scaling
			destRect.setBounds(0, 0, map.getUsedPixels(), offscreenSize.height);
			xScaleEq = new LinearTransformation
					(map.getIndex(destRect.x), destRect.x,
					map.getIndex(destRect.x + destRect.width),
					destRect.x + destRect.width);
			yScaleEq = new LinearTransformation
					(drawer.getCorrMin(), destRect.y,
					drawer.getCorrMax(), destRect.y + destRect.height);

			// draw
			drawer.paint(g,
					xScaleEq, yScaleEq,
					destRect, selectedNode);
		} else {
			//	    System.out.println("didn't update buffer: valid = " + offscreenValid + " drawer = " + drawer);
		}
	}


	// Mouse Listener
	/**
	 *  When a mouse is clicked, a node is selected.
	 */
	public void mouseClicked(MouseEvent e) {
		if (isEnabled() == false) {
			return;
		}
		if (this == null) {
			return;
		}
		if (enclosingWindow().isActive() == false) {
			return;
		}
		if (drawer != null) {
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
	 *  Arrow keys are used to change the selected node.
	 *
	 * up selects parent of current.
	 * left selects left child.
	 * right selects right child
	 * down selects child with most descendants.
	 *
	 */
	public void keyPressed(KeyEvent e) {
		if (selectedNode == null) {
			return;
		}

		int c                = e.getKeyCode();
		TreeDrawerNode cand  = null;
		switch (c) {
		case KeyEvent.VK_UP:
			selectParent();
		break;
		case KeyEvent.VK_LEFT:
			if (selectedNode.isLeaf() == false) {
				selectLeft();
			}
		break;
		case KeyEvent.VK_RIGHT:
			if (selectedNode.isLeaf() == false) {
				selectRight();
			}
		break;
		case KeyEvent.VK_DOWN:
			if (selectedNode.isLeaf() == false) {
				TreeDrawerNode right  = selectedNode.getRight();
				TreeDrawerNode left   = selectedNode.getLeft();
				if (right.getRange() > left.getRange()) {
					selectRight();
				} else {
					selectLeft();
				}
			}
		break;
		}
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
			xScaleEq, yScaleEq,
			destRect, current, true);
		   drawer.paintSingle(
		   	offscreenGraphics, xScaleEq, yScaleEq,
			destRect, selectedNode, true);
				 
		   synchMap();
		   repaint();
		 }
	 
	private void selectRight() {
		if (selectedNode.isLeaf()) {
			return;
		}
		TreeDrawerNode current  = selectedNode;
		selectedNode = current.getRight();
		drawer.paintSingle(offscreenGraphics,
				xScaleEq, yScaleEq, destRect, current, false);
		drawer.paintSubtree(offscreenGraphics,
				xScaleEq, yScaleEq,
				destRect, current.getLeft(), false);
		synchMap();
		repaint();
	}


	private void selectLeft() {
		if (selectedNode.isLeaf()) {
			return;
		}
		TreeDrawerNode current  = selectedNode;
		selectedNode = current.getLeft();
		drawer.paintSingle(offscreenGraphics,
				xScaleEq, yScaleEq, destRect, current, false);
		drawer.paintSubtree(offscreenGraphics,
				xScaleEq, yScaleEq,
				destRect, current.getRight(), false);
		synchMap();
		repaint();
	}


	/**
	 *  Key releases are ignored.
	 *
	 * @param  e  The keyevent
	 */
	public void keyReleased(KeyEvent e) { }


	/**
	 *  Key types are ignored.
	 *
	 * @param  e  the keypress.
	 */
	public void keyTyped(KeyEvent e) { }


	private TreeSelectionI arraySelection;
	private LinearTransformation xScaleEq, yScaleEq;
	private MapContainer map;
	private JScrollBar scrollbar;
	private InvertedTreeDrawer drawer = null;
	private TreeDrawerNode selectedNode = null;
	private Rectangle destRect = null;
}


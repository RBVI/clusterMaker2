
/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ArrayNameView.java,v $
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

import javax.swing.JScrollPane;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderSummary;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelView;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

/**
 *  Renders the names of the arrays.
 *
 * Actually, renders the first element in a HeaderInfo as vertical text. Could easily be generalized.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */
public class ArrayNameView extends ModelView implements MouseListener, ConfigNodePersistent {

	private final String d_face        = "Lucida Sans Regular";
	private final int d_style          = 0;
	private final int d_size           = 12;

	/**  HeaderInfo containing the names of the arrays. */
	protected HeaderInfo headerInfo = null;
	protected DataModel dataModel = null;
	
	public HeaderInfo getHeaderInfo() {
		return headerInfo;
	}
	
	public DataModel getDataModel()
	{
		return dataModel;
	}
	
	public void setHeaderInfo(HeaderInfo headerInfo) {
		this.headerInfo = headerInfo;
	}
	
	public void setDataModel(DataModel dataModel) {
			
			if(dataModel != null)
			{
				((Observable)dataModel).deleteObserver(this);
			}
			this.dataModel = dataModel;
			((Observable)dataModel).addObserver(this);
	}
	
	private ConfigNode root            = null;

	private String face;
	private int style;
	private int size;

	private MapContainer map;
	private int fontsize               = 10;
	private int maxlength              = 0;
	private boolean dragging           = false;
	private boolean backBufferValid    = false;
	private Image backBuffer;

	private JScrollPane scrollPane;

	/* inherit description */
	public String viewName() {
		return "ArrayNameView";
	}


	/* inherit description */
	public String[] getHints() {
		String[] hints  = {
				"Click and drag to scroll",
				};
		return hints;
	}


	/**
	 *  Constructs an <code>ArrayNameView</code> with the given <code>HeaderInfo</code> 
	 * as a source of array names.
	 *
	 * @param  hInfo  Header containing array names as first row.
	 */
	public ArrayNameView(HeaderInfo hInfo) {
		super();
		headerInfo = hInfo;
		headerSummary = new HeaderSummary();
		headerSummary.setIncluded(new int [] {0});
		scrollPane = new JScrollPane(this);
		scrollPane.setBorder(null);
		panel = scrollPane;
		addMouseListener(this);
	}


	// Canvas methods
	public void updateBuffer(Graphics g) {
		Rectangle rect = new Rectangle(0, 0, offscreenSize.width, offscreenSize.height);
		updateBuffer(g, rect);
	}

	public void updateBuffer(Image buf) {
		Rectangle rect = new Rectangle(0, 0, buf.getWidth(null), buf.getHeight(null));
		updateBuffer(buf.getGraphics(), rect);
	}
	

	public void updateBuffer(Graphics g, Rectangle offscreenRect) {

		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.white);
		g2d.fillRect(offscreenRect.x, offscreenRect.y, offscreenRect.width, offscreenRect.height);
		g2d.setColor(Color.black);

		int start            = map.getIndex(0);
		int end              = map.getIndex(map.getUsedPixels()) - 1;
		int gidRow           = headerInfo.getIndex("GID");

		if (gidRow == -1) {gidRow = 0;}

		int colorIndex       = headerInfo.getIndex("FGCOLOR");
		g2d.setFont(new Font(face, style, size));
		FontMetrics metrics = getFontMetrics(g.getFont());
		int ascent = metrics.getAscent();

		// Make sure we're anti aliased
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 

    // draw backgrounds first...
    int bgColorIndex = headerInfo.getIndex("BGCOLOR");
    if (bgColorIndex > 0) {
	    Color back = g.getColor();
	    for (int j = start; j <= end;j++) {
			    String [] strings = headerInfo.getHeader(j);
			    try {
			    g2d.setColor(TreeColorer.getColor(strings[bgColorIndex]));
			    } catch (Exception e) {
				    // ingore...
			    }
			    g2d.fillRect(offscreenRect.x, offscreenRect.y+map.getMiddlePixel(j) - ascent / 2, offscreenRect.width, ascent);
	    }
	    g2d.setColor(back);
    }

		Color back = g2d.getColor();
		for (int j = start;j <= end;j++) {
			try { 
				String out = headerSummary.getSummary(headerInfo, j);

				String[] headers  = headerInfo.getHeader(j);
				if (out != null) {
					if ((arraySelection == null) || arraySelection.isIndexSelected(j)) {
						if (colorIndex > 0)
							g2d.setColor(TreeColorer.getColor(headers[colorIndex]));

						printRotatedString(g2d, out, offscreenRect.x, 
						                   offscreenRect.y+offscreenRect.height, 
						                   map.getMiddlePixel(j)+ascent/2);

						if (colorIndex > 0)
							g.setColor(back);
					} else {
						g2d.setColor(Color.gray);

						printRotatedString(g2d, out, offscreenRect.x, 
						                   offscreenRect.y+offscreenRect.height, 
						                   map.getMiddlePixel(j)+ascent/2);

						g2d.setColor(back);
					}
				}
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			}
		}
	}

	private void printRotatedString(Graphics2D g2d, String str, int x, int y, int offset) {
		g2d.translate(x, y);
		g2d.rotate(-90 * 3.14159/180);
		// Not sure why we need to fudge the offset, but it looks better if we do
		g2d.drawString(str, 0, offset-2);
		g2d.rotate(90 * 3.14159/180);
		g2d.translate(-x, -y);
	}

	/**
	 *  Used to space the array names.
	 *
	 * @param  im  A new mapcontainer.
	 */
	public void setMapping(MapContainer im) {
		if (map != null) {
			map.deleteObserver(this);
		}
		map = im;
		map.addObserver(this);
	}


	private int oldHeight = 0;

	/**
	 *  This method is called when the selection is changed. It causes the component
	 *  to recalculate it's width, and call repaint.
	 */
	private void selectionChanged() {
		offscreenValid = false;
		backBufferValid = false;

		int start                = map.getMinIndex();
		int end                  = map.getMaxIndex();
		int gidRow               = headerInfo.getIndex("GID");
		if (gidRow == -1) {
			gidRow = 0;
		}

		if (map.getScale() <= 20.0) 
			size = (int)map.getScale();
		else
			size = 20;

		FontMetrics fontMetrics  = getFontMetrics
				(new Font(face, style, size));
		maxlength = 1;
		for (int j = start; j < end; j++) {
			String[] headers  = headerInfo.getHeader(j);
				String out = headerSummary.getSummary(headerInfo, j);
/*
				String out        = headers[gidRow];
				*/
			if (out == null) {
				continue;
			}
			int length        = fontMetrics.stringWidth(out);
			if (maxlength < length) {
				maxlength = length;
			}
		}

		Rectangle visible        = getVisibleRect();
		setPreferredSize(new Dimension(map.getUsedPixels(), maxlength));
		revalidate();

		repaint();

		if (maxlength > oldHeight) {
			//	    System.out.println("old height "  + oldHeight +" new height " + maxlength + ", visible " + visible);
			visible.y += maxlength - oldHeight;
			//	    System.out.println("new visible " + visible);
			scrollRectToVisible(visible);
		}
		oldHeight = maxlength;

		/* The rest is done inside paintComponent...
	// calculate maxlength
	int start = map.getIndex(0);
	int end =   map.getIndex(map.getUsedPixels());
 	repaint();
	if (maxlength > oldHeight) {
	    //	    System.out.println("old height "  + oldHeight +" new height " + maxlength + ", visible " + visible);
	    visible.y += maxlength - oldHeight;
	    //	    System.out.println("new visible " + visible);
	    scrollRectToVisible(visible);
	}
	oldHeight = maxlength;
	*/
	}

	// Observer
	/**
	 *  Expects to see updates only from the map, when the array name spacing changes.
	 *
	 */
	public void update(Observable o, Object arg) {
		if (o == map || o == dataModel) {
			selectionChanged();
		  } else if (o == arraySelection) {
			selectionChanged(); // which genes are selected changed
		  } else {
			System.out.println("ArrayNameView got funny update!");
		}
	}

	// MouseListener
	/**
	 *  Starts external browser if the urlExtractor is enabled.
	 */
	public void mouseClicked(MouseEvent e) {
		// now, want mouse click to signal browser...
		int index  = map.getIndex(e.getX());
		if (map.contains(index)) {
			// Highlight node in Cytoscape??
		}
	}

	//FontSelectable
	/*inherit description */
	public String getFace() {
		return face;
	}


	/*inherit description */
	public int getPoints() {
		return size;
	}


	/*inherit description */
	public int getStyle() {
		return style;
	}


	/*inherit description */
	public void setFace(String string) {
        if ((face == null) ||(!face.equals(string))) {
			face = string;
			if (root != null)
			root.setAttribute("face", face, d_face);
			setFont(new Font(face, style, size));
			backBufferValid = false;
			repaint();
		}
	}

	/*inherit description */
	public void setPoints(int i) {
		if (size != i) {
			size = i;
			if (root != null)
			root.setAttribute("size", size, d_size);
			setFont(new Font(face, style, size));
			backBufferValid = false;
			repaint();
		}
	}


	/*inherit description */
	public void setStyle(int i) {
		if (style != i) {
			style = i;
			backBufferValid = false;
			if (root != null)
			root.setAttribute("style", style, d_style);
			setFont(new Font(face, style, size));
			repaint();
		}
	}

	private HeaderSummary headerSummary;
	/** Setter for headerSummary */
	public void setHeaderSummary(HeaderSummary headerSummary) {
		this.headerSummary = headerSummary;
	}
	/** Getter for headerSummary */
	public HeaderSummary getHeaderSummary() {
		return headerSummary;
	}

	/*inherit description */
	public void bindConfig(ConfigNode configNode) {
		root = configNode;
		if (configNode.fetchFirst("ArraySummary") == null) {
			getHeaderSummary().bindConfig(configNode.create("ArraySummary"));
			getHeaderSummary().setIncluded(new int [] {0});
		} else {
			getHeaderSummary().bindConfig(configNode.fetchFirst("ArraySummary"));
		}
		setFace(root.getAttribute("face", d_face));
		setStyle(root.getAttribute("style", d_style));
		setPoints(root.getAttribute("size", d_size));
	}
	private TreeSelectionI arraySelection;
    /** 
     * Set geneSelection
     *
     * @param geneSelection The TreeSelection which is set by selecting genes in the GlobalView
     */
    public void setArraySelection(TreeSelectionI arraySelection) {
	  if (this.arraySelection != null)
	    this.arraySelection.deleteObserver(this);	
	  this.arraySelection = arraySelection;
	  this.arraySelection.addObserver(this);
    }
}


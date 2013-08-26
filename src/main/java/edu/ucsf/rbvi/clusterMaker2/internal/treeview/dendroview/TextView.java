/* BEGIN_HEADER											  Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: TextView.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/16 19:13:45 $

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

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderSummary;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ModelView;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

public class TextView extends ModelView implements KeyListener, AdjustmentListener,
												   MouseListener, MouseMotionListener {

	public String[]  getHints() {
		String [] hints = {
		"Click and drag to scroll",
		};
		return hints;
	}

	/**
	 * should really take a HeaderSummary instead of HeaderInfo, since the mapping should be managed by
	 * Dendroview so that the SummaryView can use the same HeaderSummary.
	 */
	public TextView(HeaderInfo hI) {
		super();
		headerInfo = hI;
		col = -1;
		
		//  could set up headerSummary...
		int GIDIndex = headerInfo.getIndex("GID");
		if (GIDIndex == -1) {
			headerSummary.setIncluded(new int [] {1});
		} else {
			headerSummary.setIncluded(new int [] {2});
		}
	
	//		int yorfIndex = headerInfo.getIndex("YORF");
	//	int nameIndex = headerInfo.getIndex("NAME");
	
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		scrollPane = new JScrollPane(this);
		scrollPane.setBorder(null);
		panel = scrollPane;
	}
	
	
	public TextView(HeaderInfo hI, int col) {
		super();
		headerInfo = hI;
		this.col = col;
		
	//  could set up headerSummary...
		int GIDIndex = headerInfo.getIndex("GID");
		if (GIDIndex == -1) {
			headerSummary.setIncluded(new int [] {1});
		} else {
			headerSummary.setIncluded(new int [] {2});
		}
	
	//		int yorfIndex = headerInfo.getIndex("YORF");
	//	int nameIndex = headerInfo.getIndex("NAME");
	
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		scrollPane = new JScrollPane(this);
		scrollPane.setBorder(null);
		panel = scrollPane;
	}

	public String viewName() { return "TextView";}

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
		// clear the pallette...
		g.setColor(Color.white);
		g.fillRect(offscreenRect.x, offscreenRect.y, offscreenRect.width, offscreenRect.height);
		g.setColor(Color.black);
		
		if ((map.getMinIndex() >= 0) &&
			(offscreenRect.height > 0)) {
			
			int start = map.getIndex(0);
			int end =   map.getIndex(map.getUsedPixels());
			g.setFont(new Font(face, style, size));
			FontMetrics metrics = getFontMetrics(g.getFont());
			int ascent = metrics.getAscent();
	
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
			
			// draw backgrounds first...
			int bgColorIndex = headerInfo.getIndex("BGCOLOR");
			if (bgColorIndex > 0) {
				Color back = g.getColor();
				for (int j = start; j < end;j++) {
					if ((geneSelection == null) || geneSelection.isIndexSelected(j)) {
						String [] strings = headerInfo.getHeader(j);
						try {
						g.setColor(TreeColorer.getColor(strings[bgColorIndex]));
						} catch (Exception e) {
							// ignore
						}
						g.fillRect(offscreenRect.x, offscreenRect.y + map.getMiddlePixel(j) - ascent / 2, offscreenRect.width, ascent);
					}
				}
				g.setColor(back);
			}
	
			// now, foreground text
			int fgColorIndex = headerInfo.getIndex("FGCOLOR");
			for (int j = start; j < end;j++) {
				String out = null;
			
				if(col == -1) {
					out = headerSummary.getSummary(headerInfo, j);
				} else {
					String [] summaryArray = headerSummary.getSummaryArray(headerInfo, j);
					if ((summaryArray != null) && (col < summaryArray.length))
						out = summaryArray[col];
				}

				if (out != null) {
					Color back = g.getColor();
					if ((geneSelection == null) || geneSelection.isIndexSelected(j)) {
						String [] strings = headerInfo.getHeader(j);
						if (fgColorIndex > 0) {
							g.setColor(TreeColorer.getColor(strings[fgColorIndex]));
						}
						g.drawString(out, offscreenRect.x, offscreenRect.y + map.getMiddlePixel(j) + ascent / 2);
						if (fgColorIndex > 0) g.setColor(back);
					} else {
						g.setColor(Color.gray);
						g.drawString(out, offscreenRect.x, offscreenRect.y + map.getMiddlePixel(j) + ascent / 2);
						g.setColor(back);
					}
				}
			}
			
		} else {		// some kind of blank default image?
			// backG.drawString("Select something already!", 0, offscreenSize.height / 2 );
		}
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

	public void setMap(MapContainer im) { 
		if (map != null) {
			map.deleteObserver(this);
		}
		map = im;
		if (map != null) map.addObserver(this);
	}

	/** This method is called when the selection is changed. It causes the component to 
	 * recalculate it's width, and call repaint.
	 */
	private void selectionChanged() {
		if (map.getScale() <= 20.0) 
			size = (int)map.getScale();
		else
			size = 20;

		maxlength = 1;
		FontMetrics fontMetrics = getFontMetrics(new Font(face, style, size));
		int start = map.getIndex(0);
		int end =   map.getIndex(map.getUsedPixels());
		 
		for (int j = start; j < end;j++) {
			int actualGene = j;
			 String out = headerSummary.getSummary(headerInfo, actualGene);
			 if (out == null) continue;
			 int length = fontMetrics.stringWidth(out);
			 if (maxlength < length) {
				 maxlength = length;
			}
		}
		setPreferredSize(new Dimension(maxlength, map.getUsedPixels()));
		revalidate();
		repaint();
	}

	// Observer
	public void update(Observable o, Object arg) {	
		if (o == map) {
		selectionChanged(); // gene locations changed
	  } else if (o == geneSelection) {
		selectionChanged(); // which genes are selected changed
	  } else if(o == headerSummary) { //  annotation selection changed
	  	selectionChanged();
	  } else {
		System.out.println("Textview got funny update!");
	  }
	}
	// MouseListener 
	public void mouseClicked(MouseEvent e) {
	  // now, want mouse click to signal browser...
	  int index = map.getIndex(e.getY());
	  if (map.contains(index)) {
	  	if(col != -1)
	  	{
				// Select node in cytoscape?
	  		// viewFrame.displayURL(urlExtractor.getUrl(index, headerInfo.getNames()[col]));
	  	}
	  	else
	  	{
				// Select node in cytoscape?
	  		// viewFrame.displayURL(urlExtractor.getUrl(index));
	  	}
	  }
	}

	// MouseMotionListener

	public void mousePressed(MouseEvent e) {
		dragging = true;
		xstart_pos = e.getX();
		//		xoff_pos = scrollbar.getValue();
		 
		 
	}

	public void mouseDragged(MouseEvent e) {
	if (dragging) {
		int xoff = (e.getX() * (maxlength - offscreenSize.width)) / offscreenSize.width;
		// adjustScrollbar(xoff);
	}
	} 

	public void mouseReleased(MouseEvent e) {
		dragging = false;
	}
	// KeyListener 
	public void keyPressed(KeyEvent e) {
	int xoff = 0;//scrollbar.getValue();

	int c = e.getKeyCode();
	switch (c) {
	case KeyEvent.VK_UP:
		break;
	case KeyEvent.VK_DOWN:
		break;
	case KeyEvent.VK_LEFT:
		xoff -= scrollstep; break;
	case KeyEvent.VK_RIGHT:
		xoff += scrollstep; break;
	default:
		return;
	}
	adjustScrollbar(xoff);
	}
	// AdjustmentListener
	public void adjustmentValueChanged(AdjustmentEvent evt) {
	offscreenValid = false;
	repaint();
	}
	private void adjustScrollbar(int offset) {
	//	scrollbar.setValue(offset);
	offscreenValid = false;
	repaint();
	}

	
	//FontSelectable
	public String getFace() {
		return face;
	}
	public int getPoints() {
		return size;
	}
	public int getStyle() {
		return style;
	}

	 public void setFace(String string) {
		if ((face == null) ||(!face.equals(string))) {
			face = string;
			if (root != null)
			root.setAttribute("face", face, d_face);
						setFont(new Font(face, style, size));
			repaint();
		}
	 }
	
	public void setPoints(int i) {
		if (size != i) {
			size = i;
			if (root != null)
			  root.setAttribute("size", size, d_size);
			setFont(new Font(face, style, size));
			repaint();
		}
	}

	public void setStyle(int i) {
		if (style != i) {
			style = i;
			if (root != null)
			root.setAttribute("style", style, d_style);
			setFont(new Font(face, style, size));
			repaint();
		}
	}
	
	
	
	public void bindConfig(ConfigNode configNode)
	{
		root = configNode;
		setFace(root.getAttribute("face", d_face));
		setStyle(root.getAttribute("style", d_style));
		setPoints(root.getAttribute("size", d_size));
		getHeaderSummary().bindConfig(root.fetchOrCreate("GeneSummary"));
	}

	private final int scrollstep = 5;
	private final String d_face = "Lucida Sans Regular";
	private final int d_style = 0;
	private final int d_size = 12;

	protected HeaderInfo  headerInfo   = null;
	protected HeaderSummary headerSummary = new HeaderSummary();
	/** Setter for headerSummary */
	public void setHeaderSummary(HeaderSummary headerSummary) {
		this.headerSummary = headerSummary;
	}
	/** Getter for headerSummary */
	public HeaderSummary getHeaderSummary() {
		return headerSummary;
	}
	private ConfigNode root  = null;

	private String face;
	private int style;
	private int size;

	private TreeSelectionI geneSelection;
	private MapContainer map;
	private JScrollBar scrollbar;
	private int maxlength = 0;
	private int xstart_pos, xoff_pos;
	private int col;
	private boolean dragging = false;
	
	
	private JScrollPane scrollPane;

}

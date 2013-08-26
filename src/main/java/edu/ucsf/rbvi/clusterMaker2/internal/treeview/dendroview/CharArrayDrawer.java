/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: CharArrayDrawer.java,v $
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

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
/**
 *  Class for Drawing A Colored Grid Representation of a Sequence Alignment from an annotation column.
 *
 *      Each cell in the view corresponds to a character in the alignment.
 *      The color of the pixels is determined by the CharColorExtractor, which is passed
 *      in the char to be converted. <p>
 *
 *      The ArrayDrawer is Observable. It setsChanged() itself when the annotation is
 *      changed, but you have to call notifyObservers() yourself. Notifications from
 *      the CharColorExtractor, however, are immediately passed on to listeners. <p>
 *
 *      Upon setting a data array, ArrayDrawer will keep a reference to the data array,
 *      and may refer to it when it asked to draw things. Of course, it may form
 *      some kind of internal buffer- you're advised to call setData() if you change
 *      the data, and not to change the data unless you call setData() too. <p>
 *
 *      The ArrayDrawer can draw on a Graphics object. It requires a source rectangle
 *      in units of array indexes, to determine which array values to render, and
 *      a destination rectangle to draw them to. <p>
 *
 *      At some point, we many want to allow arrays of ints to specify source rows
 *      and columns to grab data from for non-contiguous views.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $ 
 *
 */
public class CharArrayDrawer extends ArrayDrawer {
	/**  Constructor does nothing but set defaults  */
	public CharArrayDrawer() {
		super();
	}
	
		/**
	 *  Set CharColorExtractor for future draws
	 *
	 * @param  colorExtractor  A CharColorExtractor to draw required pixels
	 */
	public void setColorExtractor(CharColorExtractor colorExtractor) {
		if (this.colorExtractor != null) {
			this.colorExtractor.deleteObserver(this);
		}
		this.colorExtractor = colorExtractor;
		colorExtractor.addObserver(this);
		setChanged();
	}



	/**
	 *  Gets the colorExtractor attribute of the ArrayDrawer object
	 *
	 * @return    The colorExtractor value
	 */
	public CharColorExtractor getColorExtractor() {
		return colorExtractor;
	}


	/**
	 *  Set the source of the data.
	 *
	 * @param  info       A HeaderInfo containing the column of aligned sequence
	 * @param  name            The name of the column
	 */
	public void setHeaderInfo(HeaderInfo info, String name) {
		if ((headerInfo != info) || (headerName != name)) {
			headerInfo = info;
			headerName = name;
			setChanged();
		}
	}

	/**
	 *  Paint the array values onto pixels. This method will do averaging if multiple
	 *  values map to the same pixel.
	 *
	 * @param  pixels    The pixel buffer to draw to.
	 * @param  source    Specifies Rectangle of values to draw from
	 * @param  dest      Specifies Rectangle of pixels to draw to
	 * @param  scanSize  The scansize for the pixels array (in other words, the width of the image)
	 * @param  geneOrder the order of the genes. The source rect y values are taken to mean indexes into this array. If the gene order is null, the indexes from the source rect are used as indexes into the data matrix.
	 */
	public void paint(int[] pixels, Rectangle source, Rectangle dest, int scanSize, int [] geneOrder) {
		if (headerInfo == null) {
			System.out.println("header info wasn't set");
		}
		// ynext will hold the first pixel of the next block.
		int ynext       = dest.y;
		// geneFirst holds first gene which contributes to this pixel.
		int geneFirst  = 0;
		// gene will hold the last gene to contribute to this pixel.
		for (int gene = 0; gene < source.height; gene++) {
			int ystart     = ynext;
			ynext = dest.y + (dest.height + gene * dest.height) / source.height;
			// keep incrementing until block is at least one pixel high
			if (ynext == ystart) {
				continue;
			}
			// xnext will hold the first pixel of the next block.
			int xnext      = dest.x;

			// arrayFirst holds first gene which contributes to this pixel.
			int arrayFirst  = 0;
			for (int array = 0; array < source.width; array++) {
				int xstart  = xnext;
				xnext = dest.x + (dest.width + array * dest.width) / source.width;
				if (xnext == xstart) {
					continue;
				}
				try {
					int r   = 0;
					int g   = 0;
					int b   = 0;
					int count    = 0;
					for (int i = geneFirst; i <= gene; i++) {
						for (int j = arrayFirst; j <= array; j++) {
							int actualGene = source.y + i;
							if (geneOrder != null) actualGene = geneOrder[actualGene];
							Color thisC = getColor(j + source.x,  actualGene);
							r += thisC.getRed();
							g += thisC.getGreen();
							b += thisC.getBlue();
							count++;
						}
					}
					int t_color;
					if (count == 0) {
						t_color = getColorExtractor().getMissing().getRGB();
					} else {
						Color consensus = new Color(r/count, g/count, b/count);
						t_color = consensus.getRGB();
					}
					for (int x = xstart; x < xnext; x++) {
						for (int y = ystart; y < ynext; y++) {
							pixels[x + y * scanSize] = t_color;
						}
					}
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					//			System.out.println("out of bounds, " + (i + source.x) + ", " + (array + source.y));
				}
				arrayFirst = array + 1;
			}
			geneFirst = gene + 1;
		}
	}

	public void paintChars(Graphics g, MapContainer xmap, MapContainer ymap, Rectangle destRect) {
		// Make sure we're anti aliased
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 

		// need to draw values on screen!
		for (int row = ymap.getIndex(destRect.y); row < ymap.getIndex(destRect.height); row++) {
			for (int col = xmap.getIndex(destRect.x); col < xmap.getIndex(destRect.width); col++) {
				int overx = xmap.getPixel(col);
				int overy =  ymap.getPixel(row+1);
				g.drawString(getSummary(col, row), overx, overy);
			}
		}
	}
	public void paintChars(Graphics g, Rectangle sourceRect, Rectangle destRect) {
		// Make sure we're anti aliased
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 

		// need to draw values on screen
		FontMetrics metrics = g.getFontMetrics();
		int ascent = metrics.getAscent();
		for (int row = 0; row < sourceRect.height; row++) {
			for (int col = 0; col < sourceRect.width; col++) {
				int overx = destRect.x + (col *destRect.width) / sourceRect.width;
				int overy = destRect.y + ((row +1) *destRect.height) / sourceRect.height;

				// need next x, last y for centering.
				int noverx = destRect.x + ((col+1) *destRect.width) / sourceRect.width;
				int lovery = destRect.y + ((row) *destRect.height) / sourceRect.height;

				String summary= getSummary(col + sourceRect.x, row + sourceRect.y);
				int width = metrics.stringWidth(summary);

				int cx = ((noverx - overx) - width) / 2;
				int cy = ((overy - lovery) - ascent) / 2;

				
				g.drawString(summary, overx + cx, overy - cy);
			}
		}
	}
	
	
	
	/**
	 *  Get char for a given array element
	 *
	 * @param  x  x coordinate of array element
	 * @param  y  y coordinate of array element
	 * @return    value of array element, or nodata if not found
	 */
	public char getChar(int x, int y) {
		String aln = headerInfo.getHeader(y, headerName);
		try {
			if (aln != null) return aln.charAt(x);
		} catch (IndexOutOfBoundsException e) {
		}
		return '\0';
	}


	
	public String getSummary(int x, int y) {
		return "" +  getChar(x, y);
	}
	
	public boolean isMissing(int x, int y) {
		String aln = headerInfo.getHeader(y, headerName);
		char test;
		try {
			if (aln != null) test = aln.charAt(x);
			return false;
		} catch (IndexOutOfBoundsException e) {
		}
		return true;
	}
	
	public boolean isEmpty(int x, int y) {
		return false;
	}

		/** how many rows are there to draw? */
	public int getNumRow() {
		if ((headerInfo != null) && (headerName != null)) {
			return headerInfo.getNumHeaders();
		}
		return 0;
	}

	/** how many cols are there to draw? */
	public int getNumCol() {
		try {
			if ((headerInfo != null) && (headerName != null)) {
				int max = 0;
				for (int i = 0; i < headerInfo.getNumHeaders(); i++) {
					String header = headerInfo.getHeader(i, headerName);
					if (header != null) {
						int length = header.length();
						if(length > max) max = length;
					}
				}
				return max;
			}
		} catch (java.lang.NullPointerException e) {
			// CyLogger.getLogger(CharArrayDrawer.class).error("CharArrayDrawer.getNumCol() got error " + e);
			e.printStackTrace();
		}
		return 0;
	}

		/**
	 *  Get Color for a given array element
	 *
	 * @param  x  x coordinate of array element
	 * @param  y  y coordinate of array element
	 * @return    color for array element, or nodata if not found
	 */
	public Color getColor(int x, int y) {
		return colorExtractor.getColor(getChar(x, y));
	}


	/**  resets the ArrayDrawer to a default state.  */
	protected void setDefaults() {
		headerInfo = null;
	}


	/**  Used to convert data values into colors */
	protected CharColorExtractor colorExtractor;
	/**  The column of aligned sequence to be rendered. */
	protected HeaderInfo headerInfo;
	protected String headerName;
}

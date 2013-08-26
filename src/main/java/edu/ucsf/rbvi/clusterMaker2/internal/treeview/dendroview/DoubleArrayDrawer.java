/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: DoubleArrayDrawer.java,v $
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

import java.awt.Color;
import java.awt.Rectangle;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
/**
 *  Class for Drawing A Colored Grid Representation of a Data Matrix.
 *
 *      Each cell in the view corresponds to an element in the array.
 *      The color of the pixels is determined by the ColorExtractor, which is passed
 *      in the value to be converted. <p>
 *
 *      The ArrayDrawer is Observable. It setsChanged() itself when the data array is
 *      changed, but you have to call notifyObservers() yourself. Notifications from
 *      the ColorExtractor, however, are immediately passed on to listeners. <p>
 *
 *      Upon setting a data array, ArrayDrawer will set a reference to the data array,
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
 * @version    $Revision: 1.1 $ $Date: 2006/08/16 19:13:46 $ 
 *
 */
public class DoubleArrayDrawer extends ArrayDrawer {
	/**  Constructor does nothing but set defaults  */
	public DoubleArrayDrawer() {
		super();
	}

	/**
	 *  Set ColorExtractor for future draws
	 *
	 * @param  colorExtractor  A ColorExtractor to draw required pixels
	 */
	public void setColorExtractor(ColorExtractor colorExtractor) {
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
	public ColorExtractor getColorExtractor() {
		return colorExtractor;
	}


	/**
	 *  Set the source of the data.
	 *
	 * @param  matrix       A DataMatrix of values to be rendered.
	 */
	public void setDataMatrix(DataMatrix  matrix) {
		if (dataMatrix != matrix) {
			dataMatrix = matrix;
			setChanged();
		}
	}

	/** sets contrast to 4 times the mean. Works well in practice. */
	public void recalculateContrast() {
		double mean  = 0.0;
		int count    = 0;
		int nRow = dataMatrix.getNumRow();
		int nCol = dataMatrix.getNumCol();
		for (int row = 0; row < nRow; row++) {
			for (int col = 0; col < nCol; col++) {
				double val = dataMatrix.getValue(row, col);
				if (val == DataModel.NODATA) continue;
				if (val == DataModel.EMPTY) continue;
				mean += Math.abs(val);
				count++;
			}
		}
	  
	  mean /= count;

	  colorExtractor.setContrast(mean * 4);
	  colorExtractor.notifyObservers();
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
		if (dataMatrix == null) {
			System.out.println("data matrix wasn't set");
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
					double val   = 0;
					int count    = 0;
					for (int i = geneFirst; i <= gene; i++) {
						for (int j = arrayFirst; j <= array; j++) {
							int actualGene = source.y + i;
							if (geneOrder != null) actualGene = geneOrder[actualGene];
							double thisVal  = dataMatrix.getValue(j + source.x,  actualGene);

							if (thisVal == DataModel.EMPTY) {
								val = DataModel.EMPTY;
								count =1;
								break;
							}
							if (thisVal != DataModel.NODATA) {
								count++;
								val += thisVal;
							}
						}
						if (val == DataModel.EMPTY) break;
					}
					if (count == 0) {
						val = DataModel.NODATA;
					} else {
						val /= count;
					}
					int t_color  = colorExtractor.getARGBColor(val);
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

	/**
	 *  Get value for a given array element
	 *
	 * @param  x  x coordinate of array element
	 * @param  y  y coordinate of array element
	 * @return    value of array element, or DataModel.NODATA if not found
	 */
	public double getValue(int x, int y) {
		if (dataMatrix == null) {
			System.out.println("dataMatrix was not set in DoubleArrayDrawer");
		}
		return dataMatrix.getValue(x,y);
	}

	public String getSummary(int x, int y) {
		return "" + getValue(x, y);
	}

	public boolean isMissing(int x, int y) {
		return (getValue(x, y) == DataModel.NODATA);
	}

	public boolean isEmpty(int x, int y) {
		return (getValue(x, y) == DataModel.EMPTY);
	}
	
	/** how many rows are there to draw? */
	public int getNumRow() {
		if (dataMatrix != null) {
			return dataMatrix.getNumRow();
		}
		return 0;
	}

	/** how many cols are there to draw? */
	public int getNumCol() {
		if (dataMatrix != null) {
			return dataMatrix.getNumCol();
		}
		return 0;
	}
	/**
	 *  Get Color for a given array element
	 *
	 * @param  x  x coordinate of array element
	 * @param  y  y coordinate of array element
	 * @return    color for array element, or DataModel.NODATA if not found
	 */
	public Color getColor(int x, int y) {
		return colorExtractor.getColor(getValue(x, y));
	}


	/**  resets the ArrayDrawer to a default state.  */
	protected void setDefaults() {
		dataMatrix = null;
	}


	/**  Used to convert data values into colors */
	protected ColorExtractor colorExtractor;
	/**  The array of data values to be rendered. */
	protected DataMatrix dataMatrix;
}


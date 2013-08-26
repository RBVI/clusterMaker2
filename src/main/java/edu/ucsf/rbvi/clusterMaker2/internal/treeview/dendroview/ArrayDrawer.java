/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ArrayDrawer.java,v $
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
import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;

/**
 *  Class for Drawing A Colored Grid Representation of a Matrix.
 *
 *      Each cell in the view corresponds to an element in an array.
 *      The color of the pixels is determined by subclasses. <p>
 *
 *      The ArrayDrawer is Observable. It setsChanged() itself when the data array is
 *      changed, but you have to call notifyObservers() yourself.  <p>
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
public abstract class ArrayDrawer extends Observable implements Observer {

	/**
	 *  Get Color for a given array element
	 *
	 * @param  x  x coordinate of array element
	 * @param  y  y coordinate of array element
	 * @return    color for array element, or nodata if not found
	 */
	public abstract Color getColor(int x, int y);

	/**  resets the ArrayDrawer to a default state.  */
	protected abstract void setDefaults();

	/** is the element missing? */
	public abstract boolean isMissing(int x, int y);

	/** is the element empty? */
	public abstract boolean isEmpty(int x, int y);

	/** String representing value of element */
	public abstract String getSummary(int x, int y);

	/** how many rows are there to draw? */
	public abstract int getNumRow();

	/** how many cols are there to draw? */
	public abstract int getNumCol();

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
	public abstract void paint(int[] pixels, Rectangle source, Rectangle dest, int scanSize, int [] geneOrder);

	/**  Constructor does nothing but set defaults  */
	public ArrayDrawer() {
		setDefaults();
	}


	/**
	 *  binds this arraydrawer to a particular ConfigNode.
	 *
	 * @param  configNode  confignode to bind to
	 */
	public void bindConfig(ConfigNode configNode) {
		root = configNode;
	}

	/**
	 *  Paint the view of the Pixels
	 *
	 * @param  g       The Graphics element to draw on
	 * @param  source  Specifies Rectangle of values to draw from
	 * @param  dest    Specifies Rectangle of pixels to draw to
	 * @param geneOrder a desired reordered subset of the genes, or null if you want order from cdt.
	 */
	public void paint(Graphics g, Rectangle source, Rectangle dest, int [] geneOrder) {
		int ynext  = dest.y;
		for (int j = 0; j < source.height; j++) {
			int ystart  = ynext;
			ynext = dest.y + (dest.height + j * dest.height) / source.height;

			int xnext   = dest.x;
			for (int i = 0; i < source.width; i++) {
				int xstart  = xnext;
				xnext = dest.x + (dest.width + i * dest.width) / source.width;
				int width   = xnext - xstart;
				int height  = ynext - ystart;
				if ((width > 0) && (height > 0)) {
					try {
							int actualGene = source.y + j;
							if (geneOrder != null) actualGene = geneOrder[actualGene];
						Color t_color  = getColor(i + source.x,actualGene);
						g.setColor(t_color);
						g.fillRect(xstart, ystart, width, height);
					} catch (java.lang.ArrayIndexOutOfBoundsException e) {
						//			System.out.println("out of bounds, " + (i + source.x) + ", " + (j + source.y));
					}
				}
			}
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
	 */
	public void paint(int[] pixels, Rectangle source, Rectangle dest, int scanSize) {
		paint(pixels, source, dest, scanSize, null);
	}



	/**
	 *  Method to draw a single point (x,y) on grapics g using xmap and ymap
	 *
	 * @param  g     Graphics to draw to
	 * @param  xmap  Mapping from indexes to pixels
	 * @param  ymap  Mapping from indexes to pixels
	 * @param  x     x coordinate of data in array
	 * @param  y     y coordinate of data in array
	 * @param geneOrder a desired reordered subset of the genes, or null if you want order from cdt.
	 */
	public void paintPixel(Graphics g, MapContainer xmap, MapContainer ymap,
			int x, int y, int [] geneOrder) {
		try {
							int actualGene =ymap.getIndex(y);
							if (geneOrder != null) actualGene = geneOrder[actualGene];
			Color t_color  = getColor(xmap.getIndex(x), actualGene);
			g.setColor(t_color);
			g.fillRect(x, y, 1, 1);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
		}
	}


	/**
	 *  This drawer can only draw from a single, unchangng model This method may not
	 *  be necessary. Neither may the observer/observable stuff.
	 *
	 * @param  o    Object sending update
	 * @param  arg  Argument, usually null
	 */
	 public void update(Observable o, Object arg) {
		 setChanged();
		 notifyObservers();
	 }

	private ConfigNode root;
	public ConfigNode getRoot() {
		return root;
	}
	public void setRoot(ConfigNode root) {
		this.root = root;
	}
}

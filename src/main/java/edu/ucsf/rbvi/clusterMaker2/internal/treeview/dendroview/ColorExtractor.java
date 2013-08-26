/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ColorExtractor.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/07/13 02:33:47 $
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
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ContrastSelectable;

/**
 *  The purpose of this class is to convert a data value into a color.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.2 $ $Date: 2007/07/13 02:33:47 $
 */

public class ColorExtractor extends Observable implements ConfigNodePersistent, ContrastSelectable {
	private ColorSet defaultColorSet;
	private double default_contrast     = 3.0;
	private final ColorSet colorSet     = new ColorSet();// Will be backed by confignode when we get one...
	private boolean m_logTranform       = false;
	private double m_logCenter = 1.0;
	private double m_logBaseDivisor;
	private double m_logBase;
	/**  Constructor for the ColorExtractor object */
	public ColorExtractor() {

		// set a default defaultColorSet... should be superceded by a user setting...
		defaultColorSet = new ColorSet();
		defaultColorSet.setUp(ColorSet.decodeColor("#FEFF00"));
		defaultColorSet.setZero(ColorSet.decodeColor("#000000"));
		defaultColorSet.setDown(ColorSet.decodeColor("#1BB7E5"));
		defaultColorSet.setMissing(ColorSet.decodeColor("#909090"));
		defaultColorSet.setEmpty(ColorSet.decodeColor("#FFFFFF"));
		setDefaultColorSet(defaultColorSet);
		setLogBase(2.0);
	}


	/**
	 *  Sets the default colors to be used if a config node if a config node is not bound to us.
	 *  Also used in setDefaults() to figure out what the default colors are.
	 */
	public void setDefaultColorSet(ColorSet set) {
		defaultColorSet = set;
	}


	/**
	 *  binds this ColorExtractor to a particular ConfigNode. This makes colors persistent
	 *
	 * @param  configNode  confignode to bind to
	 */
	public void bindConfig(ConfigNode configNode) {
		if (root != configNode) {
			root = configNode;
			ConfigNode cand  = root.fetchFirst("ColorSet");
			if (cand == null) {
				cand = root.create("ColorSet");
			}
			colorSet.bindConfig(cand);
			synchFloats();
			contrast = root.getAttribute("contrast", getContrast());
			setLogCenter(root.getAttribute("logcenter", 1.0));
			setLogBase(root.getAttribute("logbase", 2.0));
			m_logTranform = (root.getAttribute("logtransform", 0) == 1);
			setChanged();
		}
	}

	/**
	 *  Set contrast value for future draws
	 *
	 * @param  contrastValue  The desired contrast value
	 */
	public void setContrast(double contrastValue) {
		if (contrast != contrastValue) {
			contrast = contrastValue;
			if (root != null) {
				root.setAttribute("contrast", contrast, default_contrast);
			}
			setChanged();
		}
	}

	public void setLogTransform(boolean transform) {
		if (transform != m_logTranform) {
			m_logTranform = transform;
			if (root != null) {
				root.setAttribute("logtransform", transform?1:0, 0);
			}
			setChanged();
		}
	}
	public void setLogCenter(double center) {
		if (m_logCenter != center) {
			m_logCenter = center;
			if (root != null) {
				root.setAttribute("logcenter", center, 1.0);
			}
			setChanged();
		}
	}

	public double getLogCenter() {
		return m_logCenter;
	}

	public void setLogBase(double base) {
		if (m_logBase != base) {
			m_logBase = base;
			m_logBaseDivisor = Math.log(base);
			if (root != null) {
				root.setAttribute("logbase", base, 2.0);
			}
			setChanged();
		}
	}

	public double getLogBase() {
		return m_logBase;
	}
	public boolean getLogTransform() {
		return m_logTranform;
	}
	
	/**
	 *  Get contrast value
	 *
	 * @return    contrastValue The current contrast value
	 */
	public double getContrast() {
		return contrast;
	}


	/**
	 *  This call sets the values which stand for missing or empty data. By default,
	 *  missing data is drawn gray and empty data is drawn white. Empty data only occurs
	 *  in some KNN views right now, and means that the square does not represent data, and is only
	 * there as a spacer.
	 *
	 * @param  missing  The new missing value
	 * @param  empty    The new empty value
	 */
	public void setMissing(double missing, double empty) {
		this.nodata = missing;
		this.empty = empty;

		setChanged();
	}


	/**
	 *  The color for positive data values. This is blended with the zero colors using the contrast.
	 */
	public Color getUp() {
		return colorSet.getUp();
	}


	/**
	 *  The color for zero data values.
	 */
	public Color getZero() {
		return colorSet.getZero();
	}


	/**
	 * The color for negative numbers. This is blended with the zero colors using the contrast.
	 */
	public Color getDown() {
		return colorSet.getDown();
	}


	/**
	 *  The color for missing data.
	 */
	public Color getMissing() {
		return colorSet.getMissing();
	}


	/**
	 * The empty is a color to be used for cells which do not correspond to data, like in
	 * the KnnView. These cells are just used for spacing.
	 */
	public Color getEmpty() {
		return colorSet.getEmpty();
	}


	private void synchFloats() {
		synchFloats(colorSet.getUp(), upColor);
		synchFloats(colorSet.getZero(), zeroColor);
		synchFloats(colorSet.getDown(), downColor);
		synchFloats(colorSet.getMissing(), missingColor);
		synchFloats(colorSet.getEmpty(), emptyColor);
	}


	private void synchFloats(Color newColor, float[] comp) {
		comp[0] = (float) newColor.getRed() / 256;
		comp[1] = (float) newColor.getGreen() / 256;
		comp[2] = (float) newColor.getBlue() / 256;
	}


	/**
	 *  The color for positive data values. This is blended with the zero colors using the contrast.
	 */
	public void setUpColor(String newString) {
		if (ColorSet.encodeColor(colorSet.getUp()).equals(newString)) {
			return;
		}
		colorSet.setUp(ColorSet.decodeColor(newString));
		synchFloats(colorSet.getUp(), upColor);
		setChanged();
	}


	/**
	 *  Set zeroColor value for future draws
	 *  The color for zero data values.
	 */
	public void setZeroColor(String newString) {
		if (ColorSet.encodeColor(colorSet.getZero()).equals(newString)) {
			return;
		}
		colorSet.setZero(ColorSet.decodeColor(newString));
		synchFloats(colorSet.getZero(), zeroColor);
		setChanged();
	}


	/**
	 *  Set downColor value for future draws
	 * The color for negative numbers. This is blended with the zero colors using the contrast.
	 */
	public void setDownColor(String newString) {
		if (ColorSet.encodeColor(colorSet.getDown()).equals(newString)) {
			return;
		}
		colorSet.setDown(ColorSet.decodeColor(newString));
		synchFloats(colorSet.getDown(), downColor);
		setChanged();
	}


	/**
	 *  The color for missing data.
	 */
	public void setMissingColor(String newString) {
		if (ColorSet.encodeColor(colorSet.getMissing()).equals(newString)) {
			return;
		}
		colorSet.setMissing(ColorSet.decodeColor(newString));
		synchFloats(colorSet.getMissing(), missingColor);
		setChanged();
	}


	/**
	 * The empty is a color to be used for cells which do not correspond to data	 */
	public void setEmptyColor(String newString) {
		if (newString == null) {
			return;
		}
		if (ColorSet.encodeColor(colorSet.getEmpty()).equals(newString)) {
			return;
		}
		colorSet.setEmpty(ColorSet.decodeColor(newString));
		synchFloats(colorSet.getEmpty(), emptyColor);
		setChanged();
	}


	/**
	 *  The color for positive data values. This is blended with the zero colors using the contrast.
	 */
	public void setUpColor(Color newColor) {
		if (colorSet.getUp().equals(newColor)) {
			return;
		}
		colorSet.setUp(newColor);
		synchFloats(colorSet.getUp(), upColor);
		setChanged();
	}


	/**
	 *  Set zeroColor value for future draws
	 *  The color for zero data values.
	 */
	public void setZeroColor(Color newColor) {
		if (colorSet.getZero().equals(newColor)) {
			return;
		}
		colorSet.setZero(newColor);
		synchFloats(colorSet.getZero(), zeroColor);
		setChanged();
	}


	/**
	 *  Set downColor value for future draws
	 * The color for negative numbers. This is blended with the zero colors using the contrast.
	 */
	public void setDownColor(Color newColor) {
		if (colorSet.getDown().equals(newColor)) {
			return;
		}
		colorSet.setDown(newColor);
		synchFloats(colorSet.getDown(), downColor);
		setChanged();
	}


	/**
	 *  The color for missing data.
	 */
	public void setMissingColor(Color newColor) {
		if (colorSet.getMissing().equals(newColor)) {
			return;
		}
		colorSet.setMissing(newColor);
		synchFloats(colorSet.getMissing(), missingColor);
		setChanged();
	}


	/**
	 *  Set emptyColor value for future draws
	 * The empty is a color to be used for cells which do not correspond to data	 */
	public void setEmptyColor(Color newColor) {
		if (newColor == null) {
			return;
		}
		if (colorSet.getEmpty().equals(newColor)) {
			return;
		}
		colorSet.setEmpty(newColor);
		synchFloats(colorSet.getEmpty(), emptyColor);
		setChanged();
	}


	/**
	 *  Gets the color corresponding to a particular data value.
	 *
	 * @param  dval  double representing value we want color for
	 * @return       The color value
	 */
	public Color getColor(double dval) {
		/*
		if (dval == nodata) {
			return new Color(missingColor[0], missingColor[1], missingColor[2]);
		} else if (dval == empty) {
			return new Color(emptyColor[0], emptyColor[1], emptyColor[2]);
		} else {
		  // calculate factor...
		  double factor;
		  if (dval < 0) {
			factor = -dval /contrast;
		  } else {
			factor = dval / contrast;
		  }
		  if (factor >1.0)  factor  =  1.0;
		  if (factor < 0.0) factor =  0.0;
		  float ffactor = (float) factor;
		  float ff1 = (float) (1.0 - factor);
		  //calculate colors...
		  float [] comp = new float[3];
		  if (dval < 0) {
			for (int i =0; i < 3; i++) {
			  comp[i] = downColor[i] * ffactor + zeroColor[i] * ff1;
			}
		  } else {
			for (int i =0; i < 3; i++) {
			  comp[i] = upColor[i] * ffactor + zeroColor[i] * ff1;
			}
		  }
		  */
		float[] comp  = getFloatColor(dval);
		Color color   = new Color(comp[0], comp[1], comp[2]);
		return color;
//		}
	}


	/**
	 *  Gets the floatColor attribute of the ColorExtractor object
	 *
	 * @param  dval  Description of the Parameter
	 * @return       The floatColor value
	 */
	public float[] getFloatColor(double dval) {
		if (dval == nodata) {
//			System.out.println("value " + dval + " was nodata");
			return missingColor;
//			return new Color(missingColor[0], missingColor[1], missingColor[2]);
		} else if (dval == empty) {
//			System.out.println("value " + dval + " was empty");
			return emptyColor;
//			return new Color(emptyColor[0], emptyColor[1], emptyColor[2]);
		} else {

			if (m_logTranform) {
				dval = Math.log(dval/m_logCenter)/m_logBaseDivisor; 
			}
			// calculate factor...
			double factor;
			if (dval < 0) {
				factor = -dval / contrast;
			} else {
				factor = dval / contrast;
			}
			if (factor > 1.0) {
				factor = 1.0;
			}
			if (factor < 0.0) {
				factor = 0.0;
			}
			float ffactor  = (float) factor;
			float ff1      = (float) (1.0 - factor);

			//calculate colors...
			float[] comp   = new float[3];
			if (dval < 0) {
				for (int i = 0; i < 3; i++) {
					comp[i] = downColor[i] * ffactor + zeroColor[i] * ff1;
				}
			} else {
				for (int i = 0; i < 3; i++) {
					comp[i] = upColor[i] * ffactor + zeroColor[i] * ff1;
				}
			}
			return comp;
		}
	}


	/**  prints out a description of the state to standard out*/
	public void printSelf() {
		System.out.println("upColor " + upColor[0] + ", " + upColor[1] + ", " + upColor[2]);
		System.out.println("downColor " + downColor[0] + ", " + downColor[1] + ", " + downColor[2]);
		System.out.println("zeroColor " + zeroColor[0] + ", " + zeroColor[1] + ", " + zeroColor[2]);
		System.out.println("missingColor " + missingColor[0] + ", " + missingColor[1] + ", " + missingColor[2]);
		System.out.println("emptyColor " + emptyColor[0] + ", " + emptyColor[1] + ", " + emptyColor[2]);
	}


	/**
	 *  Gets the aRGBColor attribute of the ColorExtractor object
	 *
	 * @param  dval  Description of the Parameter
	 * @return       The aRGBColor value
	 */
	public int getARGBColor(double dval) {
		float[] comp  = getFloatColor(dval);

		return (
				(255 << 24)
				 |
				((int) (255 * comp[0]) << 16)
				 |
				((int) (255 * comp[1]) << 8)
				 |
				(int) (255 * comp[2])
				);
	}


	/**  resets the ColorExtractor to a default state.  */
	public void setDefaults() {
		setUpColor(ColorSet.encodeColor(defaultColorSet.getUp()));
		setZeroColor(ColorSet.encodeColor(defaultColorSet.getZero()));
		setDownColor(ColorSet.encodeColor(defaultColorSet.getDown()));
		setMissingColor(ColorSet.encodeColor(defaultColorSet.getMissing()));
		setEmptyColor(ColorSet.encodeColor(defaultColorSet.getEmpty()));
		setContrast(default_contrast);
		synchFloats();
		setChanged();
	}


	private ConfigNode root;
	private double contrast = default_contrast;

	// stores magic values with special meaning
	private double nodata, empty;

	// store as r,g,b ints for cross-platform goodness...
	private final float[] upColor = new float[3];
	private final float[] zeroColor = new float[3];
	private final float[] downColor = new float[3];
	private final float[] missingColor = new float[3];
	private final float[] emptyColor = new float[3];


}


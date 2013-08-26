/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: CharColorSet.java,v $
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

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
/**
 *  This class represents a set of colors which can be used by a color extractor to translate char
 * values into colors. The max char value is set by a constant, usually 128.
 * 
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:46 $
 */
public class CharColorSet implements ConfigNodePersistent {
	public static final int maxChar = 128; // maximum char value which can be translated

	private String default_missingColor  = "#FFFFFF";
	private String default_emptyColor    = "#FFFFFF";
	private String default_name          = null;
	private String name;
	private Color missing, empty;
	private Color charColors [] = new Color[maxChar]; // holds char colors
	private ConfigNode root              = null;


	/**  Constructor for the ColorSet object,
	*   uses default values
	*/
	public CharColorSet() {
		super();
		setAADefaults();
	}


	/**
	 *  Constructor for the ColorSet object
	 *
	 * @param  name     inital name
	 * @param  missing  string representing inital missing color
	 * @param  empty    string representing inital empty color
	 */
	public CharColorSet(String name, String missing, String empty) {
		this();
		setName(name);
		setMissing(missing);
		setEmpty(empty);
	}


	private void setAADefaults() {
		missing = decodeColor(default_missingColor);
		empty = decodeColor(default_emptyColor);
		for (char i = 0; i < maxChar; i++) {
			charColors[i] = null;
		}

		charColors['D'] = decodeColor("#E60A0A");
		charColors['E'] = charColors['D'];
		charColors['C'] = decodeColor("#E6E600");
		charColors['M'] = charColors['C'];
		charColors['K'] = decodeColor("#145AFF");
		charColors['R'] = charColors['K'];
		charColors['S'] = decodeColor("#FA9600");
		charColors['T'] = charColors['S'];
		charColors['F'] = decodeColor("#3232AA");
		charColors['Y'] = charColors['F'];
		charColors['N'] = decodeColor("#00DCDC");
		charColors['Q'] = charColors['N'];
		charColors['G'] = decodeColor("#323232");
		charColors['L'] = decodeColor("#0F820F");
		charColors['V'] = charColors['L'];
		charColors['I'] = charColors['L'];
		charColors['A'] = decodeColor("#000000");
		charColors['W'] = decodeColor("#B45AB4");
		charColors['H'] = decodeColor("#8282D2");
		charColors['P'] = decodeColor("#DC9682");
		for (char i = 0; i < maxChar; i++) {
			if (charColors[i] != null) {
				String uc = "" + i;
				String lc = uc.toLowerCase();
				charColors[lc.charAt(0)] = charColors[i];
			}
		}
	}


	/**
	 * copies colors and name from other color set.
	 */
	public void copyStateFrom(CharColorSet other) {
		setMissing(other.getMissing());
		setEmpty(other.getEmpty());
		setName(other.getName());
	}


	/**
	* sets colors and name to reflect <code>ConfigNode</code>
	*/
	public void bindConfig(ConfigNode root) {
		this.root = root;
		missing = decodeColor(root.getAttribute("missing", default_missingColor));
		empty = decodeColor(root.getAttribute("empty", default_emptyColor));
		name = root.getAttribute("name", default_name);
	} 

	/**
	 * String represnetation of class.
	 */
	public String toString() {
		return "CharColorSet " + getName() + "\n" +
				"missing: " + getMissing().toString() + "\t" +
				"empty: " + getEmpty().toString() + "\t";
	}


	/**
	 * Color for missing values.
	 */
	public Color getMissing() {
		return missing;
	}


	/**
	 * Color for empty values.
	 */
	public Color getEmpty() {
		return empty;
	}


	/**
	* The name of this color set
	 */
	public String getName() {
		return name;
	}


	public Color getColor(char c) {
		Color cand = null;
		if (c < maxChar) {
			cand = charColors[c];
		} else {
			System.out.println("passed in char " + c + " greater than maxChar " + maxChar + " to CharColorSet.java");
		}

		if (cand == null) {
			return getMissing();
		}
		return cand;
	}
	
	/**
	 * Color for missing values.
	 */
	public void setMissing(String newString) {
		missing = decodeColor(newString);
		if (root != null) {
			root.setAttribute("missing", newString, default_missingColor);
		}
	}


	/**
	 * Color for empty values.
	 */
	public void setEmpty(String newString) {
		empty = decodeColor(newString);
		if (root != null) {
			root.setAttribute("empty", newString, default_emptyColor);
		}
	}
	public void setColor(char c, String newString) {
	}

	/**
	 * Color for missing values.
	 */
	public void setMissing(Color newColor) {
		missing = newColor;
		if (root != null) {
			root.setAttribute("missing", encodeColor(missing), default_missingColor);
		}
	}


	/**
	 * Color for empty values.
	 */
	public void setEmpty(Color newColor) {
		empty = newColor;
		if (root != null) {
			root.setAttribute("empty", encodeColor(empty), default_emptyColor);
		}
	}

	public void setColor(char c, Color newColor) {
	}

	/**
	* The name of this color set
	 */
	public void setName(String name) {
		this.name = name;
		if (root != null) {
			root.setAttribute("name", name, default_name);
		}
	}


	/**
	 *  Convert a color from a hex string to a Java <code>Color</code> object.
	 *
	 * @param  colorString  hex string, such as #FF11FF
	 * @return              The corresponding java color object.
	 */
	public final static Color decodeColor(String colorString) {
		return ColorSet.decodeColor(colorString);
	}


	/**
	 * Convert a java <code>Color</code> object to a hex string.
	 *
	 * @param  color  A java color object
	 * @return        The corresponding hex string
	 */
	public final static String encodeColor(Color color) {
		return ColorSet.encodeColor(color);
	}
}


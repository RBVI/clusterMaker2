/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ColorSet.java,v $
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

import java.awt.Color;
import java.io.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;

/**
 *  This class represents a set of colors which can be used by a color extractor to translate data values into colors. 
 * 
 * NOTE: This class has been superceded by
 *  the ConfigColorSet in the edu.stanford.genetics.treeview package, although I
 *  am not likely to actually rewrite any of this code spontaneously.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */
public class ColorSet implements ConfigNodePersistent {
	private String default_upColor       = "#FEFF00";
	private String default_zeroColor     = "#000000";
	private String default_downColor     = "#1BB7E5";
	private String default_missingColor  = "#909090";
	private String default_emptyColor    = "#FFFFFF";
	private String default_name          = null;

	private String name;
	private Color up, zero, down, missing, empty;
	private ConfigNode root              = null;


	/**  Constructor for the ColorSet object,
	*   uses default values
	*/
	public ColorSet() {
		super();
		setDefaults();
	}


	/**
	 *  Constructor for the ColorSet object
	 *
	 * @param  name     inital name
	 * @param  up       string representing inital up color
	 * @param  zero     string representing inital down color
	 * @param  down     string representing inital zero color
	 * @param  missing  string representing inital missing color
	 * @param  empty    string representing inital empty color
	 */
	public ColorSet(String name, String up, String zero, String down, String missing, String empty) {
		super();
		setName(name);
		setUp(up);
		setZero(zero);
		setDown(down);
		setMissing(missing);
		setEmpty(empty);
	}


	private void setDefaults() {
		up = decodeColor(default_upColor);
		zero = decodeColor(default_zeroColor);
		down = decodeColor(default_downColor);
		missing = decodeColor(default_missingColor);
		empty = decodeColor(default_emptyColor);
	}


	/**
	 * copies colors and name from other color set.
	 */
	public void copyStateFrom(ColorSet other) {
		setUp(other.getUp());
		setZero(other.getZero());
		setDown(other.getDown());
		setMissing(other.getMissing());
		setEmpty(other.getEmpty());
		setName(other.getName());
	}


	/**
	* sets colors and name to reflect <code>ConfigNode</code>
	*/
	public void bindConfig(ConfigNode root) {
		this.root = root;
		up = decodeColor(root.getAttribute("up", default_upColor));
		zero = decodeColor(root.getAttribute("zero", default_zeroColor));
		down = decodeColor(root.getAttribute("down", default_downColor));
		missing = decodeColor(root.getAttribute("missing", default_missingColor));
		empty = decodeColor(root.getAttribute("empty", default_emptyColor));
		name = root.getAttribute("name", default_name);
	}


	/**
	 * Simple test program, prints out default color set using toString.
	 * 
	 * if argument present, saves EisenFormat to specified file.
	 * 
	 * @param  argv  optional arguments from command line.
	 */
	public final static void main(String[] argv) {
		ColorSet test  = new ColorSet();
		try {
			test.loadEisen(argv[0]);
			test.setName(argv[0]);
			System.out.println(test.toString());
		} catch (Exception e) {
			System.out.println("Couldn't load file " + argv[0] + ": " + e);
		}
		if (argv.length > 1) {
			try {
				test.saveEisen(argv[1]);
			} catch (Exception e) {
				System.out.println("Couldn't save file " + argv[1] + ": " + e);
			}

		}
	}


	/*inherit description*/
	public String toString() {
		return "ColorSet " + getName() + "\n" +
				"up: " + getUp().toString() + "\t" +
				"zero: " + getZero().toString() + "\t" +
				"down: " + getDown().toString() + "\t" +
				"missing: " + getMissing().toString() + "\t" +
				"empty: " + getEmpty().toString() + "\t";
	}


	/**
	 *  extract values from Eisen-formatted file specified by the string argument The
	 *  Eisen format is a 16 byte file. The first four bytes are interpreted as RBGA
	 *  values specifying red, green, blue and alpha values from 0-255 (00 - FF in base
	 *  16) for up-regulated genes, the next four are the values for unchanged, then
	 *  down regulated, then the color for missing values.
	 *
	 * @param  file             file to load from
	 * @exception  IOException  throw if problems with file
	 */
	public void loadEisen(String file) throws IOException {
		loadEisen(new File(file));
	}


	/**
	 *  extract values from Eisen-formatted file
	 *
	 * @param  file             file to load from
	 * @exception  IOException  throw if problems with file
	 */
	public void loadEisen(File file) throws IOException {
		FileInputStream stream  = new FileInputStream(file);
		up = unpackEisen(stream);
		zero = unpackEisen(stream);
		down = unpackEisen(stream);
		missing = unpackEisen(stream);
		stream.close();
	}


	/**
	 *  save values to Eisen-formatted file specified by the String
	 *
	 * @param  file             file to store to
	 * @exception  IOException  throw if problems with file
	 */
	public void saveEisen(String file) throws IOException {
		saveEisen(new File(file));
	}


	/**
	 *  save values to Eisen-formatted file sp
	 *
	 * @param  file             file to store to
	 * @exception  IOException  throw if problems with file
	 */
	public void saveEisen(File file) throws IOException {
		FileOutputStream stream  = new FileOutputStream(file);
		packEisen(up, stream);
		packEisen(zero, stream);
		packEisen(down, stream);
		packEisen(missing, stream);
		stream.close();
	}


	private Color unpackEisen(InputStream stream) throws IOException {
		int red    = stream.read();
		int green  = stream.read();
		int blue   = stream.read();
		int alpha  = stream.read();
		return new Color(red, green, blue, alpha);
	}


	private void packEisen(Color out, OutputStream stream) throws IOException {
		stream.write(out.getRed());
		stream.write(out.getGreen());
		stream.write(out.getBlue());
		stream.write(out.getAlpha());
	}


	/**
	 * Color for positive values.
	 */
	public Color getUp() {
		return up;
	}


	/**
	 * color for zero values
	 */
	public Color getZero() {
		return zero;
	}


	/**
	 * Color for negative values.
	 */
	public Color getDown() {
		return down;
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


	/**
	 * Color for positive values.
	 */
	public void setUp(String newString) {
		up = decodeColor(newString);
		if (root != null) {
			root.setAttribute("up", newString, default_upColor);
		}
	}


	/**
	 * color for zero values
	 */
	public void setZero(String newString) {
		zero = decodeColor(newString);
		if (root != null) {
			root.setAttribute("zero", newString, default_zeroColor);
		}
	}


	/**
	 * Color for negative values.
	 */
	public void setDown(String newString) {
		down = decodeColor(newString);
		if (root != null) {
			root.setAttribute("down", newString, default_downColor);
		}
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


	/**
	 * Color for positive values.
	 */
	public void setUp(Color newColor) {
		up = newColor;
		if (root != null) {
			root.setAttribute("up", encodeColor(up), default_upColor);
		}
	}


	/**
	 * color for zero values
	 */
	public void setZero(Color newColor) {
		zero = newColor;
		if (root != null) {
			root.setAttribute("zero", encodeColor(zero), default_zeroColor);
		}
	}


	/**
	 * Color for negative values.
	 */
	public void setDown(Color newColor) {
		down = newColor;
		if (root != null) {
			root.setAttribute("down", encodeColor(down), default_downColor);
		}
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
		return Color.decode(colorString);//will this work?
	}


	/**
	 * Convert a java <code>Color</code> object to a hex string.
	 *
	 * @param  color  A java color object
	 * @return        The corresponding hex string
	 */
	public final static String encodeColor(Color color) {
		int red    = color.getRed();
		int green  = color.getGreen();
		int blue   = color.getBlue();

		return "#" + hex(red) + hex(green) + hex(blue);
	}


	private final static String hex(int buf) {
		int hi   = buf / 16;
		int low  = buf % 16;
		return hexChar(hi) + hexChar(low);
	}


	private final static String hexChar(int i) {
		switch (i) {
						case 0:
							return "0";
						case 1:
							return "1";
						case 2:
							return "2";
						case 3:
							return "3";
						case 4:
							return "4";
						case 5:
							return "5";
						case 6:
							return "6";
						case 7:
							return "7";
						case 8:
							return "8";
						case 9:
							return "9";
						case 10:
							return "A";
						case 11:
							return "B";
						case 12:
							return "C";
						case 13:
							return "D";
						case 14:
							return "E";
						case 15:
							return "F";
		}
		return "F";
	}
}


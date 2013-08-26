/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: ColorPresets.java,v $
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

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DummyConfigNode;

/**
 *  This class encapsulates a list of Color presets. This is the class to edit the
 *  default presets in...
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */

public class ColorPresets implements ConfigNodePersistent {
	private ConfigNode root;
	private final static int dIndex            = 1;// which preset to use if not by confignode?


	/**
	 *  creates a new ColorPresets object and binds it to the node adds default Presets
	 *  if none are currently set.
	 *
	 * @param  parent  node to bind to
	 */
	public ColorPresets(ConfigNode parent) {
		super();
		bindConfig(parent);
		int nNames  = getPresetNames().length;
		if (nNames == 0) {
			addDefaultPresets();
		}
	}


	/**  Constructor for the ColorPresets object */
	public ColorPresets() {
		super();
		root = new DummyConfigNode("ColorPresets");
	}


	/**
	 *  returns default preset, for use when opening a new file which has no color settings
	 */
	public int getDefaultIndex() {
		return root.getAttribute("default", dIndex);
	}


	/**
	 *  True if there a particular preset which we are to default to.
	 *
	 */
	public boolean isDefaultEnabled() {
		return (getDefaultIndex() != -1);
	}


	/**
	 *  Gets the default <code>ColorSet</code>, according to this preset.
	 */
	public ColorSet getDefaultColorSet() {
		int defaultPreset  = getDefaultIndex();
		try {
			return getColorSet(defaultPreset);
		} catch (Exception e) {
			return getColorSet(0);
		}
	}


	/**
	 *  Sets the default to be the i'th color preset.
	 */
	public void setDefaultIndex(int i) {
		root.setAttribute("default", i, dIndex);
	}


	/**  holds the default color sets, which can be added at any time to the extant set */
	public static ColorSet[] defaultColorSets;

	static {
		defaultColorSets = new ColorSet[7];
		defaultColorSets[0] = new ColorSet("RedYellow", "#FF0000", "#000000", "#FEFF00", "#909090", "#FFFFFF");
		defaultColorSets[1] = new ColorSet("YellowCyan", "#FEFF00", "#000000", "#1BB7E5", "#909090", "#FFFFFF");
		defaultColorSets[2] = new ColorSet("YellowPurple", "#FEFF00", "#000000", "#CC00CC", "#909090", "#FFFFFF");
		defaultColorSets[3] = new ColorSet("GreenPurple", "#00FF00", "#000000", "#CC00CC", "#909090", "#FFFFFF");
		defaultColorSets[4] = new ColorSet("YellowBlue", "#FEFF00", "#000000", "#0000FF", "#909090", "#FFFFFF");
		defaultColorSets[5] = new ColorSet("OrangeBlue", "#FF7F00", "#000000", "#0000FF", "#909090", "#FFFFFF");
		defaultColorSets[6] = new ColorSet("RedGreen", "#FF0000", "#000000", "#00FF00", "#909090", "#FFFFFF");
		// defaultColorSets[7] = new ColorSet("OrangePurple", "#FF7F00", "#000000", "#CC00CC", "#909090", "#FFFFFF");
		// defaultColorSets[8] = new ColorSet("YellowGreen", "#FEFF00", "#000000", "#00FF00", "#909090", "#FFFFFF");
	}


	/**  Adds the default color sets to the current presets */
	public void addDefaultPresets() {
		for (int i = 0; i < defaultColorSets.length; i++) {
			addColorSet(defaultColorSets[i]);
		}
	}


	/**
	 *  returns String [] of preset names for display
	 */
	public String[] getPresetNames() {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		String astring[]          = new String[aconfigNode.length];
		ColorSet temp           = new ColorSet();
		for (int i = 0; i < aconfigNode.length; i++) {
			temp.bindConfig(aconfigNode[i]);
			astring[i] = temp.getName();
		}
		return astring;
	}


	/**
	 *  The current number of available presets.
	 */
	public int getNumPresets() {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		return aconfigNode.length;
	}


	/*inherit description */
	public String toString() {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		ColorSet tmp            = new ColorSet();
		String [] names = getPresetNames();
		String ret = "No Presets";
		if (names.length > 0) {
			ret              = "Default is " + names[getDefaultIndex()] + " index " + 
			getDefaultIndex() + "\n";
		}
		for (int index = 0; index < aconfigNode.length; index++) {
			tmp.bindConfig(aconfigNode[index]);
			ret += tmp.toString() + "\n";
		}
		return ret;
	}


	/**
	 *  returns the color set for the ith preset or null, if any exceptions are thrown.
	 *
	 */
	public ColorSet getColorSet(int index) {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		try {
			ColorSet ret  = new ColorSet();
			ret.bindConfig(aconfigNode[index]);
			return ret;
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 *  returns the color set for this name or null, if name not found in kids
	 */
	public ColorSet getColorSet(String name) {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		ColorSet ret            = new ColorSet();
		for (int i = 0; i < aconfigNode.length; i++) {
			ret.bindConfig(aconfigNode[i]);
			if (name.equals(ret.getName())) {
				return ret;
			}
		}
		return null;
	}


	/**
	 *  constructs and adds a <code>ColorSet</code> with the specified attributes.
	 */
	public void addColorSet(String name, String up, String zero, String down, String missing) {
		ColorSet preset  = new ColorSet();
		preset.bindConfig(root.create("ColorSet"));
		preset.setName(name);
		preset.setUp(up);
		preset.setZero(zero);
		preset.setDown(down);
		preset.setMissing(missing);
	}


	/**
	 *  actually copies state of colorset, does not add the colorset itself but a copy.
	 */
	public void addColorSet(ColorSet set) {
		ColorSet preset  = new ColorSet();
		if (root != null) {
			preset.bindConfig(root.create("ColorSet"));
		}
		preset.copyStateFrom(set);
	}


	/*inherit description */
	public void bindConfig(ConfigNode configNode) {
		root = configNode;
		int nNames  = getPresetNames().length;
		if (nNames == 0) {
			addDefaultPresets();
		}
	}


	/**
	 * Remove color set permanently from presets
	 *
	 * @param  i  index of color set
	 */
	public void removeColorSet(int i) {
		ConfigNode aconfigNode[]  = root.fetch("ColorSet");
		root.remove(aconfigNode[i]);
	}


	private ConfigNode createSubNode() {
		return root.create("ColorSet");
	}

}


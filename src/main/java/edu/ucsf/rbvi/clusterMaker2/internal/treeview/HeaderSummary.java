/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: HeaderSummary.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/12/05 05:27:53 $
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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.util.Observable;
/**
* this class generates a single string summary of a HeaderInfo.
*/
public class HeaderSummary extends Observable implements ConfigNodePersistent {
	/**
	 * 
	 */
	public HeaderSummary() {
		super();
	}
	int [] included = new int [] {1};
	
	public void setIncluded(int [] newIncluded) {
		included = newIncluded;
		synchronizeTo();
		setChanged();
		notifyObservers();
	}
	public int [] getIncluded() {
		return included;
	}
	/**
	* returns the best possible summary for the specified index.
	*
	* If no headers are applicable, will return the empty string.
	*/
	public String getSummary(HeaderInfo headerInfo, int index) {
		String [] strings = null;
		try {
		    strings = headerInfo.getHeader(index);
				// for (int i = 0; i < strings.length; i++) {
				// 	System.out.println("header["+index+"]["+i+"] = "+strings[i]);
				// }
		} catch (java.lang.ArrayIndexOutOfBoundsException aie) {
		    // CyLogger.getLogger(HeaderSummary.class).warn("index " + index + " out of bounds on headers, continuing");
			return null;
		}
		if (strings == null) return null;

		StringBuffer out = new StringBuffer();
		int count =0;
		if (included.length == 0) {
			return "";
		}
		for (int i =0; i < included.length; i++) {
			try {
				String test = strings[included[i]];
				if (test != null) {
					if (count != 0) out.append(", ");
					out.append(test);
					count++;
				}
			} catch (java.lang.ArrayIndexOutOfBoundsException aie) {
				// out.append(strings[1]);
			}
		}
		if (count == 0) {
			return "";
		} else {
			return out.toString();
		}
	}
	
	
	public String [] getSummaryArray(HeaderInfo headerInfo, int index) {
		String [] strings = null;
		try {
		    strings = headerInfo.getHeader(index);
		} catch (java.lang.ArrayIndexOutOfBoundsException aie) {
		    // CyLogger.getLogger(HeaderSummary.class).warn("index " + index + " out of bounds on headers, continuing");
			return null;
		}
		if (strings == null) return null;

		
		if (included.length == 0) {
			return null;
		}
		String [] out = new String[included.length];
		int count =0;
		for (int i =0; i < included.length; i++) {
			try {
				String test = strings[included[i]];
				out[count] = test;
				count++;
			} catch (java.lang.ArrayIndexOutOfBoundsException aie) {
				// out.append(strings[1]);
			}
		}
		return out;
	}
	private ConfigNode root;
	public void bindConfig(ConfigNode configNode) {
		root  = configNode;
		synchronizeFrom();
	}
	private void synchronizeFrom() {
		if (root == null) return;
		if (root.hasAttribute("included")) {
			String incString = root.getAttribute("included", "1");
			if (incString.equals("")) {
				setIncluded(new int [0]);
			} else {
				int numComma = 0;
				for (int i = 0; i < incString.length(); i++) {
					if (incString.charAt(i) == ',')
						numComma++;
				}
				int [] array = new int[numComma+1];
				numComma = 0;
				int last = 0;
				for (int i = 0; i < incString.length(); i++) {
					if (incString.charAt(i) == ',') {
						Integer x = new Integer(incString.substring(last, i));
						array[numComma++] = x.intValue();
						last = i+1;
					}
				}
				try {
					array[numComma] = (new Integer(incString.substring(last))).intValue();
				} catch (NumberFormatException e) {
					// CyLogger.getLogger(HeaderSummary.class).warn("HeaderSummary has trouble restoring included list from "+incString);
				}
				setIncluded(array);
			}
		}
	}
	private void synchronizeTo() {
		if (root == null) return;
		int [] vec = getIncluded();
		StringBuffer temp = new StringBuffer();
		if (vec.length > 0) temp.append(vec[0]);
		for (int i = 1; i < vec.length; i++) {
			temp.append(",");
			temp.append(vec[i]);
		}
		root.setAttribute("included", temp.toString(), "1");
	}
}

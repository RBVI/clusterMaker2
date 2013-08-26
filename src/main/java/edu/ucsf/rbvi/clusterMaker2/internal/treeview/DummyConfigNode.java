/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: DummyConfigNode.java,v $
 * $Revision: 1.9 $
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

import java.util.*;
// vector

/**
 *  This interface defines a ConfigNode without persistence...
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.9 $ $Date: 2005/12/05 05:27:53 $
 */
public class DummyConfigNode implements ConfigNode {
	private Vector kids;
	private Hashtable attr;
	private String name;
	private static final String NULLVALUE = "Null String";

	/**
	 *  create and return a subnode which has the indicated name
	 *
	 * @param  tname  name of subnode to create
	 * @return        newly created node
	 */
	public ConfigNode create(String tname) {
	DummyConfigNode child  = new DummyConfigNode(tname);
		kids.addElement(child);
		return child;
	}


	/**
	 *  Constructor for the DummyConfigNode object
	 *
	 * @param  tname  name of the (parentless) node to create
	 */
	public DummyConfigNode(String tname) {
		super();
		name = tname;
		kids = new Vector();
		attr = new Hashtable();
	}


	/**
	 *  fetch all nodes with the name
	 *
	 * @param  byname  type of nodes to search for
	 * @return      array of matching nodes
	 */
	public ConfigNode[] fetch(String byname) {
		if (byname == null) {
			return null;
		}
	int matching      = 0;
		for (int i = 0; i < kids.size(); i++) {
			if (byname.equals(((DummyConfigNode) kids.elementAt(i)).name)) {
				matching++;
			}
		}

	ConfigNode[] ret  = new DummyConfigNode[matching];
		matching = 0;

		for (int i = 0; i < kids.size(); i++) {
			if (byname.equals(((DummyConfigNode) kids.elementAt(i)).name)) {
				ret[matching] = (ConfigNode) kids.elementAt(i);
				matching++;
			}
		}
		return ret;
	}


	/**
	 *  fetch first node by name
	 *
	 * @param  byname  type of node to search for
	 * @return        first matching node
	 */
	public ConfigNode fetchFirst(String byname) {
		for (int i = 0; i < kids.size(); i++) {
			if (byname.equals(((DummyConfigNode) kids.elementAt(i)).name)) {
				return (ConfigNode) kids.elementAt(i);
			}
		}
		return null;
	}


	/**
	 *  remove particular subnode
	 *
	 * @param  configNode  node to remove
	 */
	public void remove(ConfigNode configNode) {
		kids.removeElement(configNode);
	}


	/**
	 *  remove all subnodes by name
	 *
	 * @param  byname type of node to remove
	 */
	public void removeAll(String byname) {
		for (int i = kids.size() - 1; i >= 0; i--) {
			if (byname.equals(((DummyConfigNode) kids.elementAt(i)).name)) {
				kids.removeElementAt(i);
			}
		}
	}


	/**
	 *  set attribute to be last in list
	 *
	 * @param  configNode  The new last value
	 */
	public void setLast(ConfigNode configNode) {
		kids.removeElement(configNode);
		kids.addElement(configNode);
	}


	/**
	 *  get double attribute
	 *
	 * @param  string  name of attribude
	 * @param  d       a default value to return
	 * @return         The attribute value
	 */
	public double getAttribute(String string, double d) {
	Object o  = attr.get(string);
		if ((o == null) || ( o == NULLVALUE)) {
			return d;
		}
		return ((Double) o).doubleValue();
	}

	/**
	 * determine if a particular attribute is defined for this node.
	 */
	 public boolean hasAttribute(String string) {
	   Object o  = attr.get(string);
	   if (o == null) {
		 return false;
	   } else {
		 return true;
	   }
	 }
	 
	/**
	 *  get int attribute
	 *
	 * @param  string  name of attribue
	 * @param  i       default int value
	 * @return         The attribute value
	 */
	public int getAttribute(String string, int i) {
	Object o  = attr.get(string);
		if ((o == null) || (o == NULLVALUE)) {
			return i;
		}
		return ((Integer) o).intValue();
	}


	/**
	 *  get String attribute
	 *
	 * @param  string1  attribute to get
	 * @param  string2  Default value
	 * @return          The attribute value
	 */
	public String getAttribute(String string1, String string2) {
	Object o  = attr.get(string1);
		if (o == null) {
			return string2;
		}
		if (o == NULLVALUE) {
			return null;
		}
		return (String) o;
	}


	/**
	 *  set double attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public void setAttribute(String att, double val, double dval) {
		attr.put(att, new Double(val));
	}


	/**
	 *  set int attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public void setAttribute(String att, int val, int dval) {
		attr.put(att, new Integer(val));
	}


	/**
	 *  set String attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public void setAttribute(String att, String val, String dval) {
		if (att == null ) {
			// CyLogger.getLogger(DummyConfigNode.class).warn("attibute to DummyConfig was null!");
		}
		if (val == null) {
			val = NULLVALUE;
		}
		attr.put(att, val);
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.ConfigNode#fetchOrCreate(java.lang.String)
	 */
	public ConfigNode fetchOrCreate(String string) {
		ConfigNode cand = fetchFirst(string);
		if (cand == null)
			return create(string);
		else 
			return cand;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.ConfigNode#store()
	 */
	public void store() {
		// null op, since dummy.
		// System.err.println("Trying to save dummy config")
	}
}


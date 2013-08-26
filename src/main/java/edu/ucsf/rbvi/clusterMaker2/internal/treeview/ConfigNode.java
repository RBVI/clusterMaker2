/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ConfigNode.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/03/05 22:17:30 $
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


/**
 *  Defines an interface for storage of key-value pairs. Essentially all the configuration information for Java TreeView is stored using this interface. You will encounter two implementing classes. The first, most common one is an inner class of XmlConfig, which simply presents an interface to edit an xml document. Thus, when you mess with that inner class through this interface, you're actually writing XML. The second is the DummyConfigNode, which you can use for prototyping stuff or if you just want to use this interface to store key-value pairs in a non-persistant fashion.
 * 
 * The easiest way to make an object persistant across different runs of the program is to bind it to a ConfigNode returned by XmlConfig (which is bound to a file on disk), and then just store all state informaion in the ConfigNode. Whenever the XmlConfig is saved, it will automatically save the state of your object. just make sure you save it before you exit!
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.6 $ $Date: 2005/03/05 22:17:30 $
 */
public abstract interface ConfigNode {
	/**
	 *  create and return a subnode which has the indicated name
	 *
	 * @param  name  name for subnode
	 * @return      newly created subnode
	 */
	public abstract ConfigNode create(String name);


	/**
	 *  fetch all nodes with the name
	 *
	 * @param  name  type of nodes to search for
	 * @return      array of matching nodes
	 */
	public abstract ConfigNode[] fetch(String name);


	/**
	 *  fetch first node by name
	 *
	 * @param  string  type of node to search for
	 * @return        first matching node, or null if doesn't exist
	 */
	public abstract ConfigNode fetchFirst(String string);

	/**
	 *  fetch or create node by name
	 *
	 * @param  string  type of node to search for
	 * @return        first matching node, or newly created node if doesn't exist
	 */
	public abstract ConfigNode fetchOrCreate(String string);


	/**
	 *  remove particular subnode
	 *
	 * @param  configNode  node to remove
	 */
	public abstract void remove(ConfigNode configNode);


	/**
	 *  remove all subnodes with a given name
	 *
	 * @param  string name of nodes to remove
	 */
	public abstract void removeAll(String string);


	/**
	 *  set attribute to be last in list
	 *
	 * @param  configNode  configNode to be made last of children
	 */
	public abstract void setLast(ConfigNode configNode);

	/**
	 * determine if a particular attribute is defined for this node.
	 *
	 * @param  string  name of attribute
	 */
	 public boolean hasAttribute(String string);

	 
	/**
	 *  get a double attribute
	 *
	 * @param  string  name of attribude
	 * @param  d       a default value to return
	 * @return         The attribute value
	 */
	public abstract double getAttribute(String string, double d);


	/**
	 *  get an int attribute
	 *
	 * @param  string  name of attribue
	 * @param  i       default int value
	 * @return         The attribute value
	 */
	public abstract int getAttribute(String string, int i);


	/**
	 *  get a String attribute
	 *
	 * @param  string1  attribute to get
	 * @param  string2  Default value
	 * @return          The attribute value
	 */
	public abstract String getAttribute(String string1, String string2);


	/**
	 *  set a double attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public abstract void setAttribute(String att, double val, double dval);


	/**
	 *  set an int attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public abstract void setAttribute(String att, int val, int dval);


	/**
	 *  set a String attribute
	 *
	 * @param  att   name of attribute
	 * @param  val   The new attribute value
	 * @param  dval  The default value
	 */
	public abstract void setAttribute(String att, String val, String dval);

	/**
	 * store the subtree corresponding to this node.
	 * 
	 * note: in the XmlConfigNode implementation, this actually stores the whole tree.
	 * 
	 */
	public void store();

}


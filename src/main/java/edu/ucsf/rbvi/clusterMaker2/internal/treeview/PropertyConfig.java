/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: scooter $
 * $RCSfile: PropertyConfig.java,v $
 * $Revision: 1.15 $
 * $Date: 2006/09/25 22:56:44 $
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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.Reader;
import java.io.StringReader;
import java.lang.StringBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.property.CyProperty;

import org.xml.sax.InputSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a generic class for maintaining a configuration registry
 * for documents. The root element is managed by this class, and
 * configuration should be stored in children of the root.
 *
 * The class is actually implemented as wrapper around Cytoscape's
 * property mechanism.
 */
public class PropertyConfig {
	static int depth = 0;

	/**
	 * Construct new configuration information source
	 * 
	 * @param xmlFile xml file associated with configuration info
	 */
	public PropertyConfig(CyProperty<Properties> cyProps, String prefix, String tag) {
		// Get the prefix.tag property (and all of its children) from Cytoscape properties
		this.prefix = prefix;
		cyProps = cyProps;

		Properties props = cyProps.getProperties();
		String xml = props.getProperty(prefix+"."+tag);
		// System.out.println("Property "+prefix+"."+tag+" from Cytoscape = "+xml);
		this.root = xmlParse(xml, tag);
		if (this.root == null)
			this.root = new PropertyConfigNode(tag);
	}

	/**
	 * returns node if it exists, otherwise makes a new one.
	 */
	public ConfigNode getNode(String name) {
		ConfigNode t =root.fetchFirst(name);
		// just return if exists
		if (t != null) return t;
		//otherwise, create and return
		return root.create(name);
	}

	/**
	 * returns node if it exists, otherwise makes a new one.
	 */
	public ConfigNode getRoot() {
		return root;
	}

	/**
	 * Store current configuration data structure into property sheet
	 *
	 */
	public void store() {
		// The configuration data is stored in a non-binary tree where
		// each node in the tree can have children and a value.  In order
		// to serialize this, and avoid potential naming problems, I've
		// added indices to indicate children.
		Properties props = cyProps.getProperties();
		String config = getProperty(root);
		// System.out.println("Setting property "+prefix+"."+root.getName()+" to "+config);
		props.setProperty(prefix+"."+root.getName(),config);
	}
    
	public String toString() {
		return "PropertyConfig object based on prefix " + prefix + "\n";
	}

	private String getProperty(PropertyConfigNode node) {
		// First, store our attributes
		String element = node.getStartElement();
		// Now, handle all of our children
		for (String key: node.getChildKeys()) {
			for (PropertyConfigNode child: node.getChildren(key)) {
				element += getProperty(child);
			}
		}
		element += node.getEndElement();
		return element;
	}

	private PropertyConfigNode xmlParse(String xml, String docTag) {
		if (xml == null) 
			return null;

		Document doc = null;

		// System.out.println("parsing "+xml);
		try {
			// Parse the config
			DocumentBuilderFactory dbf =  DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Reader is = new StringReader(xml);
			doc = db.parse(new InputSource(is));
			doc.getDocumentElement().normalize();
			if (doc.getDocumentElement().getNodeName() != docTag)
				return null;

			depth = 0;
		} catch (Exception e) {
			return null;
		}

		// Now create all of our config nodes, top-down
		return createConfigFromXML(doc.getDocumentElement());
	}

	private PropertyConfigNode createConfigFromXML(Element node) {
		PropertyConfigNode newConfigNode = new PropertyConfigNode(node.getTagName());
		// Get any attributes
		NamedNodeMap attrs = node.getAttributes();
		depth++;
		for (int attrIndex = 0; attrIndex < attrs.getLength(); attrIndex++) {
			Attr attrNode = (Attr)attrs.item(attrIndex);
			// Clean up any entity references we set on storage
			String value = attrNode.getValue().replace("&amp;","&");
			newConfigNode.setAttribute(attrNode.getName(), value, null);
		}
		// Now get all of our children and add them
		NodeList children = node.getChildNodes();
		int nChildren = children.getLength();
		for (int childIndex = 0; childIndex < nChildren; childIndex++) {
			Element child = (Element)children.item(childIndex);
			newConfigNode.add(createConfigFromXML(child));
		}
		--depth;
		return newConfigNode;
	}

	private String indent() {
		String dStr = "";
		for (int i = 0; i < depth; i++) {
			dStr += "\t";
		}
		return dStr;
	}

	// inner class, used to implement ConfigNode
	private class PropertyConfigNode implements ConfigNode {
		protected HashMap<String,List<PropertyConfigNode>> children;
		protected String name; // The name of the value
		protected HashMap<String,Object> attributes;

		public PropertyConfigNode(String name) {
			children = new HashMap<String, List<PropertyConfigNode>>();
			attributes = new HashMap<String, Object>();
			this.name = name;
		}

		public void store() {
			PropertyConfig.this.store();
		}

		public ConfigNode create(String name) {
			PropertyConfigNode kid = new PropertyConfigNode(name);
			List<PropertyConfigNode> list;
			if (children.containsKey(name)) {
				list = children.get(name);
			} else {
				list = new ArrayList<PropertyConfigNode>();
			}
			list.add(kid);
			children.put(name, list);
			return kid;
		}

		public ConfigNode[] fetch(String name) {
			ConfigNode[] ret = new ConfigNode[0];
			if (children.containsKey(name)) {
				List<PropertyConfigNode> list = children.get(name);
				ret = (ConfigNode[])list.toArray(ret);
			}
			return ret;
		}
	
		public ConfigNode fetchFirst(String string) {
			if (children.containsKey(string)) {
				List<PropertyConfigNode> list = children.get(string);
				return list.get(0);
			}

			return null;
		}
		public ConfigNode fetchOrCreate(String string) {
			ConfigNode t = fetchFirst(string);
			// just return if exists
			if (t != null) return t;
			//otherwise, create and return
			return create(string);
		}

		public boolean equals(Object cn) {
			// This is not right -- we should compare the full prefix
			return (((PropertyConfigNode)cn).name == name);
		}
    
		public void remove(ConfigNode configNode) {
			String childName = ((PropertyConfigNode)configNode).getName();
			if (!children.containsKey(childName))
				return;

			List<PropertyConfigNode>childList = children.get(childName);
			childList.remove(((PropertyConfigNode)configNode));
			children.put(childName, childList);
			PropertyConfig.this.changed = true;
		}

		public void removeAll(String string) {
			ConfigNode [] ret = fetch(string);
			for (int i = 0; i < ret.length; i++) {
				remove(ret[i]);
			}
		}

		public void setLast(ConfigNode configNode) {
			String childName = ((PropertyConfigNode)configNode).getName();
			List<PropertyConfigNode>childList = getChildren(childName);
			childList.remove(((PropertyConfigNode)configNode));
			childList.add(((PropertyConfigNode)configNode));
			children.put(childName, childList);
			PropertyConfig.this.changed = true;
		}

		public void add(ConfigNode configNode) {
			String childName = ((PropertyConfigNode)configNode).getName();
			List<PropertyConfigNode>childList = getChildren(childName);
			childList.add(((PropertyConfigNode)configNode));
			children.put(childName, childList);
			PropertyConfig.this.changed = true;
		}
		
		/**
		 * determine if a particular attribute is defined for this node.
		 */
		public boolean hasAttribute(String string) {
			return attributes.containsKey(string);
		}

		public double getAttribute(String string, double d) {
			if (!attributes.containsKey(string))
				return d;

			double val = 0;
			Object attr = attributes.get(string);
			if (attr.getClass() == Integer.class)
				val = ((Integer)attr).doubleValue();
			else if (attr.getClass() == Double.class)
				val = ((Double)attr).doubleValue();
			else if (attr.getClass() == String.class)
				val = Double.parseDouble((String)attr);

			return val;
		}

		public int getAttribute(String string, int i) {
			if (!attributes.containsKey(string))
				return i;

			int val = 0;
			Object attr = attributes.get(string);
			if (attr.getClass() == Integer.class)
				val = ((Integer)attr).intValue();
			else if (attr.getClass() == Double.class)
				val = ((Double)attr).intValue();
			else if (attr.getClass() == String.class)
				val = Integer.parseInt((String)attr);
			return val;
		}

		public String getAttribute(String string, String dval) {
			if (!attributes.containsKey(string))
				return dval;

			String val = (String)attributes.get(string);
			return val;
		}

		public void setAttribute(String att, double val, double dval) {
			double cur = getAttribute(att, dval);
			if (cur != val) {
				PropertyConfig.this.changed = true;
				attributes.put(att, new Double(val));
			}
		}

		public void setAttribute(String att, int val, int dval) {
			int cur = getAttribute(att, dval);
			if (cur != val) {
				PropertyConfig.this.changed = true;
				attributes.put(att, new Integer(val));
			}
		}

		public void setAttribute(String att, String val, String dval) {
			String cur = getAttribute(att, dval);
			if ((cur == null) || (!cur.equals(val))) {
				PropertyConfig.this.changed = true;
				attributes.put(att, val);
			}
		}
		public String toString() {
			String ret = "Node:" + name + "\n";
			for (String keys: children.keySet()) {
				for (PropertyConfigNode node: children.get(keys)) {
					ret += " " + node.name + "\n";
				}
			}
			return ret;
		}


		public String getName() { return name; }

		public List<String> getChildKeys() { 
			List<String> list = new ArrayList<String>();
			list.addAll(children.keySet());
			return list;
		}

		public List<PropertyConfigNode> getChildren(String key) { 
			if (children.containsKey(key))
				return children.get(key);
			else
				return new ArrayList<PropertyConfigNode>();
		}

		public String getStartElement() {
			String element = "<"+name;
			for (String key: attributes.keySet()) {
				String value = attributes.get(key).toString().replace("&","&amp;");
				element += " "+key+"=\""+value+"\"";
			}
			if (children.keySet() != null || children.keySet().size() > 0)
				element += ">";
			else
				element += "/>";
			return element;
		}

		public String getEndElement() {
			if (children.keySet() != null || children.keySet().size() > 0)
				return "</"+name+">";
			else
				return "";
		}
	}
    
	private CyProperty<Properties> cyProps = null;
	private PropertyConfigNode root = null;
	private String prefix = null;
	private boolean changed = false;
    
	/**
	 * This is a non-object-oriented general purpose static method 
	 * to create a window listener that will call ConfigNode.store()
	 * when the window it is listening to is closed. There is nothing
	 * particular to the PropertyConfigNode class about it, but I can't think
	 * of a better place to put it. 
	 * 
	 * Wherenever a settings panel which affects the config is closed, we want those changes to be saved.
	 *
	 * returns a WindowListener which will store theconfig every time a window it listens on is closed.
	 * 
	 * @param node node to store
	 * @return window listener to attach to windows
	 */
	public static WindowListener getStoreOnWindowClose(final ConfigNode node) {
		// don't share, or you might end up listening to stale old windows...
		// do window listeners keep a pointer to the things they listen to, or is it the other way around?
		// it seems like it's probably the other way around.
		// in which case, it's bad for observable things to stay around for longer than their observers.
		// anyways, the overhead of making a new one is pretty small.
		return new WindowListener() {
				public void windowActivated(WindowEvent e) {
					// nothing...
				}
				public void windowClosed(WindowEvent e) {
					node.store();
				}
				public void windowClosing(WindowEvent e) {
					// nothing...
				}
				 public void windowDeactivated(WindowEvent e) {
					// nothing...
				}
				public void windowDeiconified(WindowEvent e) {
					// nothing...
				}
				public void windowIconified(WindowEvent e) {
					// nothing...
				}
				public void windowOpened(WindowEvent e) {
					// nothing...
				}
		};
	}
}

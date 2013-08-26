/*
 * Created on Mar 5, 2005
 *
 * Copyright Alok Saldnaha, all rights reserved.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;

import java.awt.Frame;
import java.util.Observable;
import java.util.Observer;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DummyConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;

/**
 * 
 * This class produces a reordered version of the parent DataModel
 * It can a subset, if the integer arrays passed in have fewer members, or a 
 * superset, if the arrays passed in have more members.
 * Gaps can be introduced between genes or arrays by inserting "-1" at an index.
 * 
 * @author aloksaldanha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReorderedDataModel extends Observable implements DataModel {
	/**
	 * @author aloksaldanha
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	private class SubDataMatrix implements DataMatrix {
		public double getValue(int col, int row) {
			if (geneIndex != null) row = geneIndex[row];
			if (arrayIndex != null) col = arrayIndex[col];
			if ((row == -1) || (col == -1)) {
				return DataModel.EMPTY;
			} else {
				return parent.getDataMatrix().getValue(col, row);
			}
		}

		public void setValue(double value, int col, int row) {
			if (geneIndex != null) row = geneIndex[row];
			if (arrayIndex != null) col = arrayIndex[col];
			if ((row == -1) || (col == -1)) {
				return;
			} else {
				parent.getDataMatrix().setValue(value, col, row);
			}
		}

		public int getNumRow() {
			if (geneIndex != null) 
				return geneIndex.length;
			else
				return parent.getDataMatrix().getNumRow();
		}

		public int getNumCol() {
			if (arrayIndex != null) 
				return arrayIndex.length;
			else
				return parent.getDataMatrix().getNumCol();
		}

		public int getNumUnappendedCol() {
			return parent.getDataMatrix().getNumUnappendedCol();
		}

		public double getMinValue() {
			return parent.getDataMatrix().getMinValue();
		}

		public double getMaxValue() {
			return parent.getDataMatrix().getMaxValue();
		}

	}
	/**
	 * 
	 * Generates subset of trHeaderInfo of parent.
	 */
	private class ReorderedTrHeaderInfo implements HeaderInfo {
		private HeaderInfo parentHeaderInfo;
		private int [] reorderedIndex;
		private ReorderedTrHeaderInfo(HeaderInfo hi, int [] ri) {
			parentHeaderInfo = hi;
			reorderedIndex = ri;
		}
		public String[] getHeader(int i) {
			int index = reorderedIndex[i];
			if (index == -1)
				return null;
			else 
				return parentHeaderInfo.getHeader(index);
		}

		public String getHeader(int i, String name) {
			int index = reorderedIndex[i];
			if (index == -1)
				return null;
			else 
				return parentHeaderInfo.getHeader(index, name);
		}
		public String getHeader(int rowIndex, int columnIndex) {
			  return (getHeader(rowIndex))[columnIndex];
		}

		public String[] getNames() {
			return parentHeaderInfo.getNames();
		}

		public int getNumNames() {
			return parentHeaderInfo.getNumNames();
		}

		public int getNumHeaders() {
			return reorderedIndex.length;
		}

		public int getIndex(String name) {
			return parentHeaderInfo.getIndex(name);
		}

		public int getHeaderIndex(String id) {
			int parentIndex = parentHeaderInfo.getHeaderIndex(id);
			for (int i = 0; i < reorderedIndex.length; i++)
				if (reorderedIndex[i] == parentIndex)
					return i;
			return -1;
		}

		public void addObserver(Observer o) {
			parentHeaderInfo.addObserver(o);
		}
		public void deleteObserver(Observer o) {
			parentHeaderInfo.deleteObserver(o);
		}
		public boolean addName(String name, int location) {return false;}
		public boolean setHeader(int i, String name, String value) {return false;}
		public boolean getModified() {return false;}
		public void setModified(boolean mod) {}		
	}

	/**
	 * 
	 * Represents reordered HeaderInfo of parent.
	 */
	private class ReorderedHeaderInfo implements HeaderInfo {
		private HeaderInfo parentHeaderInfo;
		int [] reorderedIndex;
		private ReorderedHeaderInfo(HeaderInfo hi, int [] ri) {
			parentHeaderInfo = hi;
			reorderedIndex = ri;
		}
		public String[] getHeader(int i) {
			int index = reorderedIndex[i];
			if (index == -1)
				return null;
			return parentHeaderInfo.getHeader(index);
		}

		public String getHeader(int i, String name) {
			int index = reorderedIndex[i];
			if (index == -1)
				return null;
			return parentHeaderInfo.getHeader(index, name);
		}
		public String getHeader(int rowIndex, int columnIndex) {
			String [] header = getHeader(rowIndex);
			if (header != null)
				return header[columnIndex];
			else 
				return "";
		}

		public String[] getNames() {
			return parentHeaderInfo.getNames();
		}

		public int getNumNames() {
			return parentHeaderInfo.getNumNames();
		}

		public int getNumHeaders() {
			return reorderedIndex.length;
		}

		public int getIndex(String name) {
			return parentHeaderInfo.getIndex(name);
		}

		public int getHeaderIndex(String id) {
			int parentIndex = parentHeaderInfo.getHeaderIndex(id);
			if (reorderedIndex[parentIndex] == parentIndex) 
				return parentIndex;
			else {
				for (int i = 0; i < reorderedIndex.length; i++)
					if (reorderedIndex[i] == parentIndex)
						return i;
			}
			return -1;
		}
		public void addObserver(Observer o) {
			parentHeaderInfo.addObserver(o);
		}
		public void deleteObserver(Observer o) {
			parentHeaderInfo.deleteObserver(o);
		}
		public boolean addName(String name, int location) {return false;}
		public boolean setHeader(int i, String name, String value) {return false;}
		public boolean getModified() {return false;}
		public void setModified(boolean mod) {}		
	}

	/**
	 * Builds data model which corresponds to a reordered version of the source datamodel, 
	 * as specified by geneIndex
	 * 
	 * @param source
	 * @param geneIndex
	 */
	public ReorderedDataModel(DataModel source, int [] geneIndex) {
		this(source, geneIndex, null);
	}
	/**
	 * Builds data model which corresponds to a reordered version of the source datamodel, 
	 * as specified by geneIndex and arrayIndex.
	 * 
	 * @param source
	 * @param geneIndex
	 */
	public ReorderedDataModel(DataModel source, int [] geneIndex, int [] arrayIndex) {
		this.geneIndex = geneIndex;
		this.arrayIndex = arrayIndex;
		if (geneIndex != null) {
			GeneHeaderInfo = new ReorderedHeaderInfo(source.getGeneHeaderInfo(), geneIndex);
			GtrHeaderInfo = new ReorderedHeaderInfo(source.getGtrHeaderInfo(), geneIndex);
		}
		if (arrayIndex != null) {
			ArrayHeaderInfo = new ReorderedHeaderInfo(source.getArrayHeaderInfo(), arrayIndex);
			AtrHeaderInfo = new ReorderedHeaderInfo(source.getAtrHeaderInfo(), arrayIndex);
		}
		
		this.parent =source;
		this.source = "Subset " +parent.getSource();
		this.name = "Subset of " +parent.getName();
	}
	private HeaderInfo GtrHeaderInfo;
	private HeaderInfo GeneHeaderInfo;
	private HeaderInfo AtrHeaderInfo;
	private HeaderInfo ArrayHeaderInfo;
	private DataMatrix subDataMatrix = new SubDataMatrix();
	private DataModel parent;
	private int [] geneIndex;
	private int [] arrayIndex;
	private ConfigNode documentConfig = new DummyConfigNode("SubDataModel");
	
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getDocumentConfig()
	 */
	public ConfigNode getDocumentConfig() {
		// TODO Auto-generated method stub
		return documentConfig;
	}

	String source;
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getSource()
	 */
	public String getSource() {
		return source;
	}
	public void setSource(String string) {
		source = string;
	}
	String name;
	public String getName() {
		return name;
	}
	public void setName(String string) {
		name = string;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#setModelForCompare(edu.stanford.genetics.treeview.DataModel)
	 */
	public void setModelForCompare(DataModel dm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getGeneHeaderInfo()
	 */
	public HeaderInfo getGeneHeaderInfo() {
		if (GeneHeaderInfo == null)
			return parent.getGeneHeaderInfo();
		else
			return GeneHeaderInfo;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getArrayHeaderInfo()
	 */
	public HeaderInfo getArrayHeaderInfo() {
		if (ArrayHeaderInfo == null)
			return parent.getArrayHeaderInfo();
		else
			return ArrayHeaderInfo;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getGtrHeaderInfo()
	 */
	public HeaderInfo getGtrHeaderInfo() {
		if (GtrHeaderInfo == null)
			return parent.getGtrHeaderInfo();
		else
			return GtrHeaderInfo;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getAtrHeaderInfo()
	 */
	public HeaderInfo getAtrHeaderInfo() {
		if (AtrHeaderInfo == null)
			return parent.getAtrHeaderInfo();
		else
			return AtrHeaderInfo;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getType()
	 */
	public String getType() {
		// TODO Auto-generated method stub
		return "ReorderedDataModel";
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#getDataMatrix()
	 */
	public DataMatrix getDataMatrix() {
		// TODO Auto-generated method stub
		return subDataMatrix;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#append(edu.stanford.genetics.treeview.DataModel)
	 */
	public void append(DataModel m) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.DataModel#removeAppended()
	 */
	public void removeAppended() {
		// TODO Auto-generated method stub

	}

	public boolean aidFound() {
		return parent.aidFound();
	}

	public boolean gidFound() {
		// the following causes a mismatch if not all genes were selected.
//		return parent.gidFound();
		return false;
	}
	public boolean getModified() {
		return false;
	}
	public boolean isLoaded() {
		return true;
	}

	public boolean isSymmetrical() { return parent.isSymmetrical(); }
}

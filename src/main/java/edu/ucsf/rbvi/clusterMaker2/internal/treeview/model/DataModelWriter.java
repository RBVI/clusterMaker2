/*
 * Created on Mar 7, 2005
 *
 * Copyright Alok Saldnaha, all rights reserved.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.*;

/**
 * 
 *The purpose of this class is to write a DataModel out to flat file format.
 *
 */
public class DataModelWriter {
	DataModel dataModel;
	public DataModelWriter(DataModel source) {
		dataModel= source;
	}
	/**
	 * Write all parts of Datamodel out to disk
	 * 
	 * @param fileSet fileset to write to
	 */
	public void writeAll(FileSet fileSet) {
		writeAtr(fileSet.getAtr());
		writeGtr(fileSet.getGtr());
		writeCdt(fileSet.getCdt());
	}
	public void writeIncremental(FileSet fileSet) {
		if (dataModel.aidFound() && 
				dataModel.getAtrHeaderInfo().getModified()) {
			writeAtr(fileSet.getAtr());
		}
		if (dataModel.gidFound() && 
				dataModel.getGtrHeaderInfo().getModified()) {
			writeGtr(fileSet.getGtr());
		}
		// cdt is not mutable (yet)
	}
	/**
	 * write out atr to file
	 * @param atr complete path of file to write to
	 */
	public void writeAtr(String atr) {
		writeTree(dataModel.getAtrHeaderInfo(), atr);
	}

	/**
	 * write out gtr to file
	 * @param gtr complete path of file to write to
	 */
	public void writeGtr(String gtr) {
		writeTree(dataModel.getGtrHeaderInfo(), gtr);
	}

	/**
	 * write out the data array to file
	 * @param cdt path of file to write to
	 */
	public void writeCdt(String cdt) {
		// Get the Gene header info
		HeaderInfo geneHeaderInfo = dataModel.getGeneHeaderInfo();
		// Get the Array header info
		HeaderInfo arrayHeaderInfo = dataModel.getArrayHeaderInfo();
		// Get the data
		DataMatrix dataMatrix = dataModel.getDataMatrix();

		// Set up the writer
		DataMatrixWriter writer = new DataMatrixWriter(dataMatrix, geneHeaderInfo, arrayHeaderInfo);

		try {
			String spool = cdt + ".spool";
			writer.write(cdt);
			File f = new File(spool);
			f.renameTo(new File(cdt));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Error writing " + cdt +" " + e, "Save Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * write out HeaderInfo of tree to file
	 * @param info HeaderInfo to write out
	 * @param filePath complete path of file to write to
	 */
	private void writeTree(HeaderInfo info, String file) {
		HeaderInfoWriter writer = new HeaderInfoWriter(info);
		try {
			String spool = file + ".spool";
			writer.write(file);
			File f = new File(spool);
			if (f.renameTo(new File(file))) {
				info.setModified(false);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Error writing " + file +" " + e, "Save Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}	
}

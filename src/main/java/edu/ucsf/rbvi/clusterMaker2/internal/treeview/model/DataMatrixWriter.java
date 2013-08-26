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
public class DataMatrixWriter {
	DataMatrix matrix;
	HeaderInfo geneHeader;
	HeaderInfo arrayHeader;

	public DataMatrixWriter(DataMatrix matrix, HeaderInfo geneHeader, HeaderInfo arrayHeader) {
		this.matrix = matrix;
		this.geneHeader = geneHeader;
		this.arrayHeader = arrayHeader;
	}

	/**
	 * write out the data array to file
	 * @param cdt path of file to write to
	 */
	public void write(String cdt) throws IOException {
		FileWriter out = new FileWriter(cdt);

		// Get the number of Genes
		int nGenes = matrix.getNumRow();
		// Get the number of experimental values
		int nExpr = matrix.getNumCol();

		// Write out the header data
		writeGeneHeader(out);
	
		String[] arrayNames = arrayHeader.getNames();
		// Write out the array header data
		for (int header = 0; header < arrayNames.length; header++) {
			writeArrayHeader(out, arrayNames[header], geneHeader.getNames().length);
		}
	
		for (int row = 0; row < nGenes; row++) {
			writeDataRow(out, row);
		}

		out.flush();
		out.close();
	}

	/**
 	 * write out the gene header
 	 * @param out output stream
 	 */
	private void writeGeneHeader(FileWriter out) throws IOException {
		// Write out the Gene header names, followed by the AID's
		String[] geneNames = geneHeader.getNames();
		for (int i = 0; i < geneNames.length; i++) {
			out.write(geneNames[i]+"\t");
		}

		writeArrayHeader(out, "AID", -1);
	}

	/**
 	 * write out an array header
 	 * @param out output stream
 	 * @param header the header to output
 	 * @param spacers the number of blank columns between the header name and the data
 	 */
	private void writeArrayHeader(FileWriter out, String header, int spacers) throws IOException {
		if (spacers >= 0) {
			out.write(header);
			for (int i = 0; i < spacers; i++) out.write("\t");
		}

		for (int i = 0; i < arrayHeader.getNumHeaders()-1; i++) {
			out.write(arrayHeader.getHeader(i, header)+"\t");
		}
		out.write(arrayHeader.getHeader(arrayHeader.getNumHeaders()-1, header)+"\n");
	}

	/**
 	 * write out a row of the data matrix
 	 * @param out output stream
 	 * @param row the row number we're outputting
 	 */
	private void writeDataRow(FileWriter out, int row) throws IOException {
		// Output the headers
		String[] geneNames = geneHeader.getNames();
		for (int i = 0; i < geneNames.length; i++) {
			out.write(geneHeader.getHeader(row,geneNames[i])+"\t");
		}

		// Now, output the data
		for (int col = 0; col < matrix.getNumCol()-1; col++) {
			out.write(matrix.getValue(col, row)+"\t");
		}
		out.write(matrix.getValue(matrix.getNumCol()-1, row)+"\n");
	}
}

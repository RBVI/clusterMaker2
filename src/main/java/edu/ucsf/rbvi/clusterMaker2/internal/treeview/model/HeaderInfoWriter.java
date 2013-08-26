/*
 * Created on Mar 7, 2005
 *
 * Copyright Alok Saldnaha, all rights reserved.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;

import java.io.FileWriter;
import java.io.IOException;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;

/**
 * class that write out header info to flat file.
 */
public class HeaderInfoWriter {
	private HeaderInfo headerInfo;
	/**
	 * @param atrHeaderInfo headerInfo to write out
	 */
	public HeaderInfoWriter(HeaderInfo atrHeaderInfo) {
		headerInfo = atrHeaderInfo;
	}

	/**
	 * @param atr file to write to
	 * @throws IOException
	 */
	public void write(String atr) throws IOException {
		FileWriter out = null;
		try {
			out = new FileWriter(atr);
			// first, the header.
			String [] names = headerInfo.getNames();
			if (names.length > 0) out.write(names[0]);
			for (int i =1;i < names.length; i++) {
				out.write("\t");
				out.write(names[i]);
			}
			out.write("\n");
			int rows = headerInfo.getNumHeaders();
			for (int row = 0; row < rows; row++) {
				String [] headers = headerInfo.getHeader(row);
				if (headers.length > 0) out.write(headers[0]);
				for (int i =1;i < headers.length; i++) {
					out.write("\t");
					if (headers[i] != null)
						out.write(headers[i]);
				}
				out.write("\n");
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

}

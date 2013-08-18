package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * This class is used to read a Cost-Matrix from a .cm-file.
 * 
 * @author Nils Kleinboelting
 */

public class CostMatrixReader {
	
	private int node_no = 0;
	private String[] ids = null;
	private Object[] values = null;
	private String cmPath = "";
	private double reductionCost = 0.0;
	
	/**
	 * Creates a CostMatrixReader object.
	 * 
	 * @param file The cost matrix file.
	 */
	public CostMatrixReader(File file) {
		try {
		cmPath = file.toString();
		FileReader fr = new FileReader(file); 
		BufferedReader br = new BufferedReader(fr); 
		String line;
		/* check whether reduced cost matrices are used (.rcm) or normal ones (.cm) */
		if(cmPath.endsWith(".rcm")){
//			this.isReduced=true;
			//first line contains accumulated costs for the reduction process
			line = br.readLine();
			this.reductionCost = Double.parseDouble(line.trim());
		}

		line = br.readLine(); // next line contains no of nodes.
		this.node_no = Integer.parseInt(line);
		this.ids = new String[this.node_no];
		//fill id-array:
		for(int i = 0; i < node_no; i++) {
			line = br.readLine();
			ids[i] = line;
		}
		//fill value-array:
		this.values = new Object[node_no-1];
		for(int i = 0; i < node_no-1; i++) {
			line = br.readLine();
			String[] stringValues = line.split("\t");
			float[] values2 = new float[stringValues.length]; 
			for (int j = 0; j < values2.length; j++) {
				values2[j] = Float.parseFloat(stringValues[j]);
			}
			this.values[i] = values2;
		}
		
		br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	/**
	 * Returns an instance of ConnectedComponent.
	 * 
	 * @return instance of ConnectedComponent
	 * @throws LayoutTypeException If an incorrect type for ICCEdges is given in TaskConfig.
	 */
	public ConnectedComponent getConnectedComponent(){
		//create ICCedges:
		ICCEdges ccEdges = TaskConfig.ccEdgesEnum.createCCEdges(node_no);
		for (int i = 0; i < values.length; i++) {
			float[] vals = (float[]) values[i];
			for (int j = 0; j < vals.length; j++) {
				ccEdges.setEdgeCost(i, j+i+1, vals[j]);
			}
		}
		ConnectedComponent comp = new ConnectedComponent(ccEdges, ids, cmPath);
			comp.setReductionCost(this.reductionCost);
		return comp;
	}
	
}

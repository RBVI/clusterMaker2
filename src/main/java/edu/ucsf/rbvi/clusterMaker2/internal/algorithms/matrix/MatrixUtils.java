package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.util.Arrays;
import java.util.Comparator;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

public class MatrixUtils {
	public static Integer[] indexSort(double[] tData,int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
	}

	public static Integer[] indexSort(int[] tData, int nVals) {
		Integer[] index = new Integer[nVals];
		for (int i = 0; i < nVals; i++) index[i] = i;
		IndexComparator iCompare = new IndexComparator(tData);
		Arrays.sort(index, iCompare);
		return index;
	}

	private static class IndexComparator implements Comparator<Integer> {
		double[] data = null;
		int[] intData = null;

		public IndexComparator(double[] data) { this.data = data; }

		public IndexComparator(int[] data) { this.intData = data; }

		public int compare(Integer o1, Integer o2) {
			if (data != null) {
				if (data[o1] < data[o2]) return -1;
				if (data[o1] > data[o2]) return 1;
				return 0;
			} else if (intData != null) {
				if (intData[o1] < intData[o2]) return -1;
				if (intData[o1] > intData[o2]) return 1;
				return 0;
			}
			return 0;
		}
	}
}


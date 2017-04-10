package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A simple package of utils that help working with arrays
 */

public class ArrayUtils {
	public static int [] range(int n) {
		return range(0, n);
	}

	public static int [] range(int a, int b) {
		if( b < a ) {
			throw new IllegalArgumentException("b has to be larger than a");
		}
		int val = a;
		int [] result = new int[b-a];
		for (int i = 0; i < (b-a); i++) {
			result[i] = val++;
		}
		return result;
	}


	public static int [] concatenate(int [] v1,int [] v2) {
		int [] result = new int[v1.length+v2.length];
		int index = 0;
		for (int i = 0; i < v1.length; i++, index++) {
			result[index] = v1[index];
		}
		for (int i = 0; i < v2.length; i++, index++) {
			result[index] = v2[i];
		}
		return result;
	}

	public static double mean(double [] vector) {
		double sum = Arrays.stream(vector).sum();
		return sum/vector.length;
	}

	public static double [] scalarInverse(double [] v1) {
		double [] vector = new double[v1.length];
		IntStream.range(0, v1.length).parallel()
			.forEach(i -> {
				vector[i] = 1/v1[i];
			});
		return vector;
	}

	public static double [] sqrt(double [] v1) {
		double [] vector = new double[v1.length];
		IntStream.range(0, v1.length).parallel()
			.forEach(i -> {
				vector[i] = Math.sqrt(v1[i]);
			});
		return vector;
	}

	public static String printArray(double[] vector) {
		String str = "";
		for (int i = 0; i < vector.length; i++) {
			str += ""+vector[i]+" ";
		}
		return str;
	}

}

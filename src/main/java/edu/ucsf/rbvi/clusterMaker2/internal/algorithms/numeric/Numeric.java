package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Numeric provides utility functions for working with numbers.
 * @author djh.shih
 *
 */
public class Numeric {
	
	private final static Random rand;
	static {
		rand = new Random();
	}
	
	/**
	 * Find the mean. Missing (null) values are ignored.
	 * @param a array
	 * @return mean
	 */
	public static Double mean(Double[] a) {
		Double t = new Double(0);
		int n = 0;
		for (Double v: a) {
			if (v != null) {
				t += v;
				++n;
			}
		}
		return t / new Double(n);
	}
	
	public static double mean(double[] a) {
		double t = 0;
		int n = 0;
		for (double v: a) {
			t += v;
			++n;
		}
		return t / n;
	}
	
	/**
	 * Find the median.
	 * Missing values are not handled.
	 * @param a array
	 * @return median
	 */
	public static Double median(Double[] a) {
		int n = a.length;
		if (n % 2 == 0) {
			// even number of elements; average middle elements
			Double x = select(a, n/2 - 1);
			Double y = select(a, n/2);
			return (x + y) / 2;
		} else {
			// odd number of elements: return middle element
			return select(a, n/2);
		}
	}
	
	public static double median(double[] a) {
		int n = a.length;
		if (n % 2 == 0) {
			// even number of elements; average middle elements
			double x = select(a, n/2 - 1);
			double y = select(a, n/2);
			return (x + y) / 2;
		} else {
			// odd number of elements: return middle element
			return select(a, n/2);
		}
	}
	
	/**
	 * Find the ith order statistic.
	 * If multiple elements tie for ith order statistic, an arbitrary one is chosen.
	 * Missing values are not handled.
	 * average time: O(n)
	 * @param a array
	 * @param i index
	 * @return {@code i}th ranked element
	 */
	public static Double select(Double[] a, int i) {
		return select(a, i, 0, a.length);
	}
	
	public static double select(double[] a, int i) {
		return select(a, i, 0, a.length);
	}
	
	/**
	 * @param b begin index
	 * @param e end index (one past last element)
	 */
	private static Double select(Double[] a, int i, int b, int e) {
		int n = e - b;
		if (n == 1) return a[b];
		
		// choose pivot p from @a uniformly at random
		// and partition @a around p
		int j = partition(a, rand.nextInt(n), b, e);
		
		if (j == i) {
			// convert index of pivot from local to global index and return answer
			return a[b+j];
		} else if (j > i) {
			// recursion on first half
			return select(a, i, b, b+j);
		} else {
			// (j < i): recursion on second half
			++j;
			return select(a, i-j, b+j, e);
		}
	}
	
	private static double select(double[] a, int i, int b, int e) {
		int n = e - b;
		if (n == 1) return a[b];
		
		// choose pivot p from @a uniformly at random
		// and partition @a around p
		int j = partition(a, rand.nextInt(n), b, e);
		
		if (j == i) {
			// convert index of pivot from local to global index and return answer
			return a[b+j];
		} else if (j > i) {
			// recursion on first half
			return select(a, i, b, b+j);
		} else {
			// (j < i): recursion on second half
			++j;
			return select(a, i-j, b+j, e);
		}
	}
	
	/**
	 * Partition array around pivot indexed by k
	 * @param a array
	 * @param k index of pivot (local index)
	 * @return new index of pivot (local index)
	 */
	private static int partition(Double[] a, int k, int b, int e) {
		Double t;
		// convert k from local to global index
		k += b;
		
		// move pivot to the first position
		t = a[b]; a[b] = a[k]; a[k] = t;
		
		// use first element as pivot
		Double p = a[b];
		
		// i demarcates the boundary between partition "< p" and partition "> p"
		// i indexes the first element of "> p"
		int i = b + 1;
		for (int j = b + 1; j < e; ++j) {
			if (a[j] < p) {
				// swap element with the leftmost element in partition "> p"
				t = a[i]; a[i] = a[j]; a[j] = t;
				// increment i to include the new element in partition "< p"
				++i;
			}
		}
		
		// move pivot to middle, swapping with rightmost element in partition "< p"
		k = i - 1;
		t = a[b]; a[b] = a[k]; a[k] = t;
		return k - b;
	}
	
	private static int partition(double[] a, int k, int b, int e) {
		double t;
		// convert k from local to global index
		k += b;
		
		// move pivot to the first position
		t = a[b]; a[b] = a[k]; a[k] = t;
		
		// use first element as pivot
		double p = a[b];
		
		// i demarcates the boundary between partition "< p" and partition "> p"
		// i indexes the first element of "> p"
		int i = b + 1;
		for (int j = b + 1; j < e; ++j) {
			if (a[j] < p) {
				// swap element with the leftmost element in partition "> p"
				t = a[i]; a[i] = a[j]; a[j] = t;
				// increment i to include the new element in partition "< p"
				++i;
			}
		}
		
		// move pivot to middle, swapping with rightmost element in partition "< p"
		k = i - 1;
		t = a[b]; a[b] = a[k]; a[k] = t;
		return k - b;
	}
	
	
	/**
	 * Pearson correlation.
	 * @param x data array
	 * @param y data array
	 * @return correlation between x and y
	 */
	public static double correlation(double[] x, double[] y) {
		int n = x.length;
		if (n != y.length) {
			throw new IllegalArgumentException("x and y must have the same length");
		}
		
		// prepare calculation for mean and standard deviation
		Summary xs = new Summary();
		Summary ys = new Summary();
		double dotp = 0.0;
		for (int i = 0; i < n; ++i) {
			xs.add( x[i] );
			ys.add( y[i] );
			dotp += x[i] * y[i];
		}
		
		double nd = (double)n;
		
		return (dotp - nd * xs.mean() * ys.mean()) / (nd * xs.sd() * ys.sd());
	}

	/**
	 * Make an integer array with ordered elements in a defined range.
	 * @param start start value
	 * @param end end value (exclusive)
	 * @param step interval size
	 * @return
	 */
	public static int[] range(int start, int end, int step) {
		int n = (end - start) / step;
		int[] a = new int[n];
		int i = 0;
		for (int x = start; x < end; x+=step) {
			a[i++] = x;
		}
		return a;
	}
	
	
	// private class pairing key and and value
	// abandon generic here and hard-code types, since arrays of generics do not work well in Java!
	private static class KeyValuePair {
		public double key;
		public int value;
		
		public KeyValuePair(double key, int value) {
			this.key = key;
			this.value = value;
		}
	}
	
	// private class comparator for sorting key-value pairs
	private static class KeyValuePairAscendingComparator implements Comparator<KeyValuePair> {
		public int compare(KeyValuePair a, KeyValuePair b) {
			return (a.key < b.key) ? -1 : 1;
		}
	}
	
	private static class KeyValuePairDescendingComparator implements Comparator<KeyValuePair> {
		public int compare(KeyValuePair a, KeyValuePair b) {
			return (a.key < b.key) ? 1 : -1;
		}
	}
	
	/**
	 * Get the sorted order of the data. 
	 * @param x data
	 * @param descending whether to sort in descending (reverse) order
	 * @return sorted order
	 */
	public static int[] order(double[] x, boolean descending) {
		int n = x.length;
		
		// set up key-values pairs with data elements as keys, and index as values
		KeyValuePair[] pairs = new KeyValuePair[n];
		for (int i = 0; i < n; ++i) {
			pairs[i] = new KeyValuePair(x[i], i);
		}
		
		// sort pairs based on key values
		if (descending) {
			Arrays.sort(pairs, new KeyValuePairDescendingComparator());
		} else {
			Arrays.sort(pairs, new KeyValuePairAscendingComparator());
		}
		
		// copy over sorted index
		int[] ord = new int[n];
		for (int i = 0; i < n; ++i) {
			ord[i] = pairs[i].value;
		}
		
		return ord;
	}
	
	public static int[] order(double[] x) {
		return order(x, false);
	}
	
	
	public static void printArray(Double[] a) {
		for (int i = 0; i < a.length; ++i) {
			System.out.print(a[i].toString() + " ");
		}
		System.out.println();
	}
	
	public static void printArray(double[] a) {
		for (int i = 0; i < a.length; ++i) {
			System.out.print(String.valueOf(a[i]) + " ");
		}
		System.out.println();
	}
	
}

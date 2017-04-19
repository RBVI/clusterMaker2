package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;


public enum DistanceMetric {

	VALUE_IS_CORRELATION("None -- attributes are correlations"),
	VALUE_IS_DISTANCE("None -- attributes are distances"),
	BRAYCURTIS("Bray-Curtis distance"),
	CANBERRA("Canberra distance"),
	CHEBYSHEV("Chebyshev distance"),
	CITYBLOCK("City-block (Manhattan) distance"),
	EUCLIDEAN("Euclidean distance"),
	EUCLIDEANSQ("Euclidean squared distance"),
	KENDALLS_TAU("Kendall's tau"),
	MINKOWSKI("Minkowsky distance (p=3)"),
	CORRELATION("Pearson correlation"),
	ABS_CORRELATION("Pearson correlation, absolute value"),
	SPEARMANS_RANK("Spearman's rank correlation"),
	UNCENTERED_CORRELATION("Uncentered correlation"),
	ABS_UNCENTERED_CORRELATION("Uncentered correlation, absolute value"),
	// WMINKOWSKI("Weighted Minkowsky distance"),
	;

	private String name;

	DistanceMetric(String name) {
		this.name = name;
	}
	
	public static List<DistanceMetric> getDistanceMetricList(){
		
		List<DistanceMetric> distanceMetricList = new ArrayList<DistanceMetric>();
		
		distanceMetricList.add(VALUE_IS_CORRELATION);
		distanceMetricList.add(BRAYCURTIS);
		distanceMetricList.add(CANBERRA);
		distanceMetricList.add(CITYBLOCK);
		distanceMetricList.add(CHEBYSHEV);
		distanceMetricList.add(EUCLIDEAN);
		distanceMetricList.add(EUCLIDEANSQ);
		distanceMetricList.add(KENDALLS_TAU);
		distanceMetricList.add(MINKOWSKI);
		distanceMetricList.add(CORRELATION);
		distanceMetricList.add(ABS_CORRELATION);
		distanceMetricList.add(SPEARMANS_RANK);
		distanceMetricList.add(UNCENTERED_CORRELATION);
		distanceMetricList.add(ABS_UNCENTERED_CORRELATION);
		
		return distanceMetricList;
	}
	
	public String toString() {
		return this.name;
	}

	public double getMetric(Matrix data1, Matrix data2,
	                        int index1, int index2) {
		return getMetric(data1, data2, null, index1, index2);
	}

	public double getMetric(Matrix data1, Matrix data2, double[] weights,
	                        int index1, int index2) {
		switch (this) {
			case BRAYCURTIS:
				return brayCurtisMetric(data1, data2, weights, index1, index2);
			case CANBERRA:
				return canberraMetric(data1, data2, weights, index1, index2);
			case CHEBYSHEV:
				return chebyshevMetric(data1, data2, weights, index1, index2);
			case EUCLIDEAN:
				return Math.sqrt(euclidMetric(data1, data2, weights, index1, index2));
			case EUCLIDEANSQ:
				return euclidMetric(data1, data2, weights, index1, index2);
			case CITYBLOCK:
				return cityblockMetric(data1, data2, weights, index1, index2);
			case KENDALLS_TAU:
				return kendallMetric(data1, data2, weights, index1, index2);
			case MINKOWSKI:
				return minkowskiMetric(data1, data2, weights, index1, index2);
			case CORRELATION:
				return correlationMetric(data1, data2, weights, index1, index2);
			case ABS_CORRELATION:
				return acorrelationMetric(data1, data2, weights, index1, index2);
			case UNCENTERED_CORRELATION:
				return ucorrelationMetric(data1, data2, weights, index1, index2);
			case ABS_UNCENTERED_CORRELATION:
				return uacorrelationMetric(data1, data2, weights, index1, index2);
			case SPEARMANS_RANK:
				return spearmanMetric(data1, data2, weights, index1, index2);
			case VALUE_IS_CORRELATION:
				return (1-data1.doubleValue(index1, index2));
			case VALUE_IS_DISTANCE:
				return data1.doubleValue(index1, index2);
		}
		return euclidMetric(data1, data2, weights, index1, index2);
	}

	// Distance metric calculations
	private double euclidMetric(Matrix data1, Matrix data2, double[] weights, 
	                            int index1, int index2) {
		double result = 0.0;
		double tweight = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double weight = getWeight(weights, i);
				double term = data1.doubleValue(index1, i) - data2.doubleValue(index2, i);
				result += weight*term*term;
				tweight += weight;
			}
		}
		if (tweight == 0.0) return 0;
		return (result/tweight);
	}

	private double cityblockMetric(Matrix data1, Matrix data2, double[] weights, 
	                                      int index1, int index2) {
		double result = 0.0;
		double tweight = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double weight = getWeight(weights, i);
				double term = data1.doubleValue(index1, i) - data2.doubleValue(index2, i);
				result = result + weight*Math.abs(term);
				tweight += weight;
			}
		}
		if (tweight == 0.0) return 0;
		return (result/tweight);
	}

	private double correlationMetric(Matrix data1, Matrix data2, double[] weights, 
	                                        int index1, int index2) {
		double result = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		double tweight = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double w = getWeight(weights, i);
				double term1 = data1.doubleValue(index1, i);
				double term2 = data2.doubleValue(index2, i);
				sum1 += w*term1;
				sum2 += w*term2;
				result += w*term1*term2;
				denom1 += w*term1*term1;
				denom2 += w*term2*term2;
				tweight += w;
			}
		}
		if (tweight == 0.0) return 0;
		result -= sum1 * sum2 / tweight;
		denom1 -= sum1 * sum1 / tweight;
		denom2 -= sum2 * sum2 / tweight;
		if (denom1 <= 0) return 1;
		if (denom2 <= 0) return 1;
		result = result / Math.sqrt(denom1*denom2);
		return (1.0 - result);
	}

	private double acorrelationMetric(Matrix data1, Matrix data2, double[] weights, 
	                                         int index1, int index2) {
		double result = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		double tweight = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double term1 = data1.doubleValue(index1, i);
				double term2 = data2.doubleValue(index2, i);
				double w = getWeight(weights, i);
				sum1 += w*term1;
				sum2 += w*term2;
				result += w*term1*term2;
				denom1 += w*term1*term1;
				denom2 += w*term2*term2;
				tweight += w;
			}
		}
		if (tweight == 0.0) return 0;
		result -= sum1 * sum2 / tweight;
		denom1 -= sum1 * sum1 / tweight;
		denom2 -= sum2 * sum2 / tweight;
		if (denom1 <= 0) return 1;
		if (denom2 <= 0) return 1;
		result = Math.abs(result) / Math.sqrt(denom1*denom2);
		return (1.0 - result);
	}

	private double ucorrelationMetric(Matrix data1, Matrix data2, double[] weights, 
	                                         int index1, int index2) {
		double result = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		boolean flag = false;

		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1,i) && data2.hasValue(index2,i)) {
				double term1 = data1.doubleValue(index1,i);
				double term2 = data2.doubleValue(index2,i);
				double w = getWeight(weights, i);
				result += w*term1*term2;
				denom1 += w*term1*term1;
				denom2 += w*term2*term2;
				flag = true;
			}
		}
		if (!flag) return 0.0;
		if (denom1 == 0) return 1;
		if (denom2 == 0) return 1;
		result = result / Math.sqrt(denom1*denom2);
		return (1.0 - result);
	}

	private double uacorrelationMetric(Matrix data1, Matrix data2, double[] weights, 
	                                          int index1, int index2) {
		double result = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		boolean flag = false;

		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1,i) && data2.hasValue(index2,i)) {
				double term1 = data1.doubleValue(index1,i);
				double term2 = data2.doubleValue(index2,i);
				double w = getWeight(weights, i);
				result += w*term1*term2;
				denom1 += w*term1*term1;
				denom2 += w*term2*term2;
				flag = true;
			}
		}
		if (!flag) return 0.0;
		if (denom1 == 0) return 1;
		if (denom2 == 0) return 1;
		result = Math.abs(result) / Math.sqrt(denom1*denom2);
		return (1.0 - result);
	}

	private double spearmanMetric(Matrix data1, Matrix data2, double[] weights, 
	                                     int index1, int index2) {
		double result = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		double[] rank1 = data1.getRank(index1);
		double[] rank2 = data2.getRank(index2);

		if (rank1 == null || rank2 == null)
			return 0.0;

		double avgrank = 0.5*(rank1.length-1);

		for (int i = 0; i < rank1.length; i++) {
			double value1 = rank1[i];
			double value2 = rank2[i];
			result += value1 * value2;
			denom1 += value1 * value1;
			denom2 += value2 * value2;
		}
		result /= rank1.length;
		denom1 /= rank1.length;
		denom2 /= rank1.length;
		result -= avgrank * avgrank;
		denom1 -= avgrank * avgrank;
		denom2 -= avgrank * avgrank;
		if (denom1 <= 0) return 1;
		if (denom2 <= 0) return 1;
		result = result / Math.sqrt(denom1*denom2);
		return (1.0 - result);
	}

	private double kendallMetric(Matrix data1, Matrix data2, double[] weights, 
	                                    int index1, int index2) {
		int con = 0;
		int dis = 0;
		int exx = 0;
		int exy = 0;
		boolean flag = false;
		double denomx;
		double denomy;
		double tau;
		for (int i = 0; i < data1.nColumns(); i++) {
			for (int j = 0; j < i; j++) {
				if (data1.hasValue(index1, j) && data2.hasValue(index2, j)) {
					double x1 = data1.doubleValue(index1, i);
					double x2 = data1.doubleValue(index1, j);
					double y1 = data2.doubleValue(index2, i);
					double y2 = data2.doubleValue(index2, j);
					if (x1 < x2 && y1 < y2) con++;
					if (x1 > x2 && y1 > y2) con++;
					if (x1 < x2 && y1 > y2) dis++;
					if (x1 > x2 && y1 < y2) dis++;
					if (x1 == x2 && y1 != y2) exx++;
					if (x1 != x2 && y1 == y2) exy++;
					flag = true;
				}
			}
		}
		if (!flag) return 0.0;
		denomx = con + dis + exx;
		denomy = con + dis + exy;
		if (denomx == 0) return 1;
		if (denomy == 0) return 1;
		tau = (con-dis)/Math.sqrt(denomx*denomy);
		return 1.-tau;
	}

	private double brayCurtisMetric(Matrix data1, Matrix data2, double[] weights,
	                                     int index1, int index2) {
		double sumdiff = 0.0;
		double sumsum = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double w = getWeight(weights, i);
				double v1 = data1.doubleValue(index1, i)*w;
				double v2 = data1.doubleValue(index2, i)*w;
				sumdiff += Math.abs(v1-v2);
				sumsum += Math.abs(v1) + Math.abs(v2);
			}
		}
		return sumdiff / sumsum;
	}

	private double canberraMetric(Matrix data1, Matrix data2, double[] weights,
	                                     int index1, int index2) {
		double dist = 0.0;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double w = getWeight(weights, i);
				double v1 = data1.doubleValue(index1, i)*w;
				double v2 = data1.doubleValue(index2, i)*w;
				dist += Math.abs(v1-v2)/(Math.abs(v1)+Math.abs(v2));
			}
		}
		return dist;
	}

	private double chebyshevMetric(Matrix data1, Matrix data2, double[] weights,
	                                     int index1, int index2) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double w = getWeight(weights, i);
				double v1 = data1.doubleValue(index1, i)*w;
				double v2 = data1.doubleValue(index2, i)*w;
				max = Math.max(Math.abs(v1-v2),max);
			}
		}
		return max;
	}

	private double minkowskiMetric(Matrix data1, Matrix data2, double[] weights,
	                                     int index1, int index2) {
		double dist = 0.0;
		double p = 3.0; // How do we pass this??
		for (int i = 0; i < data1.nColumns(); i++) {
			if (data1.hasValue(index1, i) && data2.hasValue(index2, i)) {
				double w = getWeight(weights, i);
				double v1 = data1.doubleValue(index1, i)*w;
				double v2 = data1.doubleValue(index2, i)*w;
				dist += Math.pow(Math.abs(v1-v2),p);
			}
		}
		return Math.pow(dist, 1/p);
	}


	private double getWeight(double[] weights, int index) {
		if (weights != null && weights.length < index)
			return weights[index];
		return 1.0;
	}

}

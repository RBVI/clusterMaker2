package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft;

import java.util.HashMap;
import java.util.Random;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans.KMeansContext;

public class RunFFT extends AbstractKClusterAlgorithm{

	Random random = null;
	FFTContext context;

	public RunFFT(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
            TaskMonitor monitor, FFTContext context, AbstractClusterAlgorithm parentTask) {
		super(network, weightAttributes, metric, monitor, parentTask);
		this.context = context;
	}

	@Override
	public int kcluster(int nClusters, int nIterations, CyMatrix matrix, 
			DistanceMetric metric, int[] clusterID) {

		random = null;
		int nelements = matrix.nRows();
		int ifound = 1;

		int[] tclusterid = new int[nelements];

		int[] saved = new int[nelements];

		int[] mapping = new int[nClusters];
		int[] counts = new int[nClusters];

		HashMap<Integer,Integer> centers = new HashMap<Integer,Integer>();

		double error = Double.MAX_VALUE;

		if (monitor != null)
			monitor.setProgress(0);


		for (int i = 0; i < nelements; i++) 
			clusterID[i] = 0;

		// the first center
		Random randomGenerator = new Random();
		centers.put(0,randomGenerator.nextInt(nelements));

		//now find the remaining centers
		for (int i = 1; i < nClusters; i++){
			int y = getMaxMin(centers,matrix);
			centers.put(i, y);
		}

		// assign clusters now
		int k = centers.get(0);
		for(int i = 0; i <nelements; i++){
			double distance;

			if (i == k){
				clusterID[i] = 0;
				continue;
			}

			distance = metric.getMetric(matrix, matrix, i, k);
			clusterID[i] = k;

			for (int j = 1; j < nClusters; j++){
				double tdistance;

				if (i == centers.get(j)){
					clusterID[i] = j;
					continue;
				}

				tdistance = metric.getMetric(matrix, matrix, i, centers.get(j));
				if (tdistance < distance) 
				{ 
					distance = tdistance;
					clusterID[i] = j;
				}
			}
		}


		return ifound;
	}

	public int getMaxMin(HashMap<Integer,Integer> centers, CyMatrix matrix){
		int y = 0;
		int nelements = matrix.nRows();
		int numC = centers.size();
		int k = centers.get(0);
		double maxD = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < nelements; i++){
			double minD;
			if (centers.containsValue(i)) continue;

			minD = metric.getMetric(matrix, matrix, i, k);

			if (numC > 1){
				for (int j = 1; j < numC; j++){
					double tminD = metric.getMetric(matrix, matrix, i, centers.get(j));

					if (tminD < minD) 
					{ 
						minD = tminD;
					}
				}
			}

			if (minD > maxD){
				maxD = minD;
				y = i;
			}
		}


		return y;
	}

	/**
	 * This routine generates a random number between 0 and n inclusive, following
	 * the binomial distribution with probability p and n trials. The routine is
	 * based on the BTPE algorithm, described in:
	 * 
	 * Voratas Kachitvichyanukul and Bruce W. Schmeiser:
	 * Binomial Random Variate Generation
	 * Communications of the ACM, Volume 31, Number 2, February 1988, pages 216-222.
	 * 
	 * @param p The probability of a single event.  This should be less than or equal to 0.5.
	 * @param n The number of trials
	 * @return An integer drawn from a binomial distribution with parameters (p, n).
	 */

	private int binomial (int n, double p) {
		double q = 1 - p;
		if (n*p < 30.0) /* Algorithm BINV */
		{ 
			double s = p/q;
			double a = (n+1)*s;
			double r = Math.exp(n*Math.log(q)); /* pow() causes a crash on AIX */
			int x = 0;
			double u = uniform();
			while(true)
			{ 
				if (u < r) return x;
				u-=r;
				x++;
				r *= (a/x)-s;
			}
		}
		else /* Algorithm BTPE */
		{ /* Step 0 */
			double fm = n*p + p;
			int m = (int) fm;
			double p1 = Math.floor(2.195*Math.sqrt(n*p*q) -4.6*q) + 0.5;
			double xm = m + 0.5;
			double xl = xm - p1;
			double xr = xm + p1;
			double c = 0.134 + 20.5/(15.3+m);
			double a = (fm-xl)/(fm-xl*p);
			double b = (xr-fm)/(xr*q);
			double lambdal = a*(1.0+0.5*a);
			double lambdar = b*(1.0+0.5*b);
			double p2 = p1*(1+2*c);
			double p3 = p2 + c/lambdal;
			double p4 = p3 + c/lambdar;
			while (true)
			{ /* Step 1 */
				int y;
				int k;
				double u = uniform();
				double v = uniform();
				u *= p4;
				if (u <= p1) return (int)(xm-p1*v+u);
				/* Step 2 */
				if (u > p2)
				{ /* Step 3 */
					if (u > p3)
					{ /* Step 4 */
						y = (int)(xr-Math.log(v)/lambdar);
						if (y > n) continue;
						/* Go to step 5 */
						v = v*(u-p3)*lambdar;
					}
					else
					{
						y = (int)(xl+Math.log(v)/lambdal);
						if (y < 0) continue;
						/* Go to step 5 */
						v = v*(u-p2)*lambdal;
					}
				}
				else
				{
					double x = xl + (u-p1)/c;
					v = v*c + 1.0 - Math.abs(m-x+0.5)/p1;
					if (v > 1) continue;
					/* Go to step 5 */
					y = (int)x;
				}
				/* Step 5 */
				/* Step 5.0 */
				k = Math.abs(y-m);
				if (k > 20 && k < 0.5*n*p*q-1.0)
				{ /* Step 5.2 */
					double rho = (k/(n*p*q))*((k*(k/3.0 + 0.625) + 0.1666666666666)/(n*p*q)+0.5);
					double t = -k*k/(2*n*p*q);
					double A = Math.log(v);
					if (A < t-rho) return y;
					else if (A > t+rho) continue;
					else
					{ /* Step 5.3 */
						double x1 = y+1;
						double f1 = m+1;
						double z = n+1-m;
						double w = n-y+1;
						double x2 = x1*x1;
						double f2 = f1*f1;
						double z2 = z*z;
						double w2 = w*w;
						if (A > xm * Math.log(f1/x1) + (n-m+0.5)*Math.log(z/w)
						      + (y-m)*Math.log(w*p/(x1*q))
						      + (13860.-(462.-(132.-(99.-140./f2)/f2)/f2)/f2)/f1/166320.
						      + (13860.-(462.-(132.-(99.-140./z2)/z2)/z2)/z2)/z/166320.
						      + (13860.-(462.-(132.-(99.-140./x2)/x2)/x2)/x2)/x1/166320.
						      + (13860.-(462.-(132.-(99.-140./w2)/w2)/w2)/w2)/w/166320.)
							continue;
						return y;
					}
				}
				else
				{ /* Step 5.1 */
					int i;
					double s = p/q;
					double aa = s*(n+1);
					double f = 1.0;
					for (i = m; i < y; f *= (aa/(++i)-s));
					for (i = y; i < m; f /= (aa/(++i)-s));
					if (v > f) continue;
					return y;
				}
			}
		}
	}

	private double uniform() {
		if (random == null) {
			// Date date = new Date();
			// random = new Random(date.getTime());
			// Use an unseeded random so that our silhouette results are comparable
			random = new Random();
		}
		return random.nextDouble();
	}

}

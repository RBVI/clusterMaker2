package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

public class GeometricClusteringConfig {

	// ---------------- CONFIG VARIABLES - SINGLE-LINKAGING ---------------- //

	public static double minDistance = 0.01;
	public static double maxDistance = 5;
	public static double stepsize = 0.01;
	public static double stepsizeFactor = 0.01;

	// --------------------------------------------------------------------------------------

	// ----------------------- CONFIG VARIABLES - K-MEANS ----------------------

	public static int kLimit = 30;
	public static int maxInitStartConfigs = 1;

	// --------------------------------------------------------------------------------------

	/**
	 * This method loads every necessary parameters for single linkage clustering
	 * from the given ConfigFile
	 * 
	 * @param rb
	 *            The PropertyResourceBundle object.
	 */
	public static void initSLCFromConfigFile(PropertyResourceBundle rb)
			throws MissingResourceException {

		minDistance = Double.parseDouble(rb.getString("slc.minDistance").trim());
		maxDistance = Double.parseDouble(rb.getString("slc.maxDistance").trim());
		stepsize = Double.parseDouble(rb.getString("slc.stepsize").trim());
		stepsizeFactor = Double.parseDouble(rb.getString("slc.stepsizeFactor").trim());

	}
	
	/**
	 * This method
	 * @param rb
	 * @throws MissingResourceException
	 */
	public static void initKmeansFromConfigFile(PropertyResourceBundle rb) 
			throws MissingResourceException{
		
		kLimit = Integer.parseInt(rb.getString("km.maxK").trim());
		maxInitStartConfigs = Integer.parseInt(rb.getString("km.maxInitStartConfigs"));
		
	}
	
}

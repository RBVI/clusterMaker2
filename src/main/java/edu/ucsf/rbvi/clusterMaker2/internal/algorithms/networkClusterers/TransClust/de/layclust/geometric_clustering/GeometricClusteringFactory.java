/* 
* Created on 16. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ConfigFile;

/**
 * This class contains several static methods that create the correct objects for
 * the different geometric clustering algorithms as well as enum type classes that do 
 * most of the job. When a new algorithm that implements {@link IGeometricClusterer} 
 * is created by a developer, these methods need to be updated!
 * 
 * @author Sita Lange
 *
 */
public class GeometricClusteringFactory {
	
	/**
	 * enum type for {@link IGeometricClusterer} implementations.
	 * 
	 * @author sita
	 */
	public enum EnumGeometricClusteringClass {
		SINGLE_LINKAGE_CLUSTERING("SingleLinkageClusterer", 0),
		KMEANS_CLUSTERING("KmeansClusterer", 1);
		
		// ===============================//
		// ADD ADDITIONAL GEOMETRIC CLUSTERERS HERE!!   //
		// ===============================//
		
		private final String classname;
		private final int intvalue;
		
		EnumGeometricClusteringClass(String classname, int intvalue){
			this.classname = classname;
			this.intvalue = intvalue;
		}
		
		public String getClassname() {return classname;}
		public int getIntvalue() {return intvalue;}
		
		/**
		 * Initialises the correct IGeometricClusterer implementation according to type.
		 * @return The correct {@link IGeometricClusterer} implementation.
		 */
		public IGeometricClusterer createGeometricClusterer(){
			if(intvalue == 0){ return new SingleLinkageClusterer(); } 
			else if(intvalue == 1){ return new KmeansClusterer(); }		
			// ===============================//
			// ADD ADDITIONAL GEOMETRIC CLUSTERERS HERE!!   //
			// ===============================//		
			else return null;
		}
		
		/**
		 * Gets all the class names for the geometric clustering algorithms and returns
		 * these in a String array.
		 * @return Array containing all existing class names of the {@link IGeometricClusterer}
		 *  implementation.
		 */
		public static String[] getClassnames(){
			EnumGeometricClusteringClass[] values = EnumGeometricClusteringClass.values();
			String[] classnames = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				classnames[i] = values[i].getClassname();
			}
			return classnames;
		}
	}

	/**
	 * This method takes the given class name and returns the internal int value for this
	 * class. 
	 * @param className Class name of the geometric clustering implementation.
	 * @return Enum Enumeration type for the geometric clustering implementation.
	 * @throws InvalidTypeException If The class name does not exist or has not been bound into the program correctly.
	 */
	public static EnumGeometricClusteringClass getClustererEnumByClass(String className) 
		throws InvalidTypeException{
		
		if(className.equals(EnumGeometricClusteringClass.SINGLE_LINKAGE_CLUSTERING.
				getClassname())){
			return EnumGeometricClusteringClass.SINGLE_LINKAGE_CLUSTERING;
		} else if (className.equals(EnumGeometricClusteringClass.KMEANS_CLUSTERING.
				getClassname())){
			return EnumGeometricClusteringClass.KMEANS_CLUSTERING;
		
		// ===============================//
		// ADD ADDITIONAL GEOMETRIC CLUSTERERS HERE!!   //
		// ===============================//
		
		} else {
			throw new InvalidTypeException("GeometricClusteringFactory: This geometric clustering class " +
					"does not exist: "+className+".\nOr it has not been bound into the program correctly.");
		}	
	}

	/** 
	 * Writes the parameters for SingleLinkageClusterer and KmeansClusterer to the given 
	 * ConfigFile.
	 * @param confile The ConfigFile to write to.
	 */
	public static void printParametersToConfig(ConfigFile confile) {
		
		/* print single linkage clustering parameters */
		confile.printSubHeader("SINGLE LINKAGE CLUSTERING");
		confile.printParameter("slc.minDistance", ""+GeometricClusteringConfig.minDistance);
		confile.printParameter("slc.maxDistance", ""+GeometricClusteringConfig.maxDistance);
		confile.printParameter("slc.stepsize", ""+GeometricClusteringConfig.stepsize);
		confile.printParameter("slc.stepsizeFactor", ""+GeometricClusteringConfig.stepsizeFactor);
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
		
		/* print k-means clustering parameters */
		confile.printSubHeader("K-MEANS CLUSTERING");
		confile.printParameter("km.maxK", ""+GeometricClusteringConfig.kLimit);
		confile.printParameter("km.maxInitStartConfigs", ""+GeometricClusteringConfig.maxInitStartConfigs);
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
	}
}

/* 
* Created on 4. November 2007
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;


/**
 *  This class contains static methods that create the correct objects for
 * the different post processing algorithms. When a new algorithm that implements
 * {@link IPostProcessing} is created by a developer, these methods need to be updated!
 * 
 * @author sita
 *
 */
public class PostProcessingFactory {
	
	/**
	 * enum type for {@link IPostProcessing} implementations.
	 * @author sita
	 *
	 */
	public enum EnumPostProcessingClass{
		PP_REARRANGE_AND_MERGE_BEST("PP_RearrangeAndMergeBest", 0),
		PP_DIVIDE_AND_RECLUSTER("PP_DivideAndRecluster", 1),
		PP_DIVIDE_AND_RECLUSTER_RECURSIVELY("PP_DivideAndReclusterRecursively", 2);
		
		// ===============================//
		// ADD ADDITIONAL POST PROCESSORS HERE!!             //
		// ===============================//
		
		private final String classname;
		private final int intvalue;
		
		EnumPostProcessingClass(String classname, int intvalue){
			this.classname = classname;
			this.intvalue = intvalue;
		}
		
		public String getClassname() {return classname;}
		public int getIntvalue() {return intvalue;}
		
		/**
		 * Initialises the correct IPostProcessing implementation according to type.
		 * @return The correct {@link IPostProcessing} implementation.
		 */
		public IPostProcessing createPostProcessor(){
			if(intvalue == 0){ return new PP_RearrangeAndMergeBest(); } 
			else if(intvalue == 1){ return new PP_DivideAndRecluster(); }
			else if(intvalue == 2){ return new PP_DivideAndReclusterRecursively();}
			// ===============================//
			// ADD ADDITIONAL POST PROCESSORS HERE!!             //
			// ===============================//	
			else return null;
		}
		
		/**
		 * Gets all the class names for the post processing algorithms and returns
		 * these in a String array.
		 * @return Array containing all existing class names of the {@link IPostProcessing}
		 *  implementations.
		 */
		public static String[] getClassnames(){
			EnumPostProcessingClass[] values = EnumPostProcessingClass.values();
			String[] classnames = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				classnames[i] = values[i].getClassname();
			}
			return classnames;
		}
		
	}
	
	/**
	 * Gets the respective enum type according to the given class name.
	 * @param className Name of the class implementing the wanted algorithm/method
	 * @return The enum type representing the wanted implementation.
	 * @throws InvalidTypeException If the class name does not exist or has not been correctly incorporated into the program.
	 */
	public static EnumPostProcessingClass getPostProcessorEnumByClass(String className) throws InvalidTypeException{
		if(className.equals(EnumPostProcessingClass.
				PP_REARRANGE_AND_MERGE_BEST.getClassname())){
			return EnumPostProcessingClass.PP_REARRANGE_AND_MERGE_BEST;
//		} else if (className.equals(EnumPostProcessingClass.POSTPROCESSING_TOBI.getClassname())){
//			return EnumPostProcessingClass.POSTPROCESSING_TOBI;
		} else if (className.equals(EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY.getClassname())){
			return EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY;
		} else if (className.equals(EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER.getClassname())){
			return EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER;
		}
		
		// ===============================//
		// ADD ADDITIONAL POST PROCESSORS HERE!!             //
		// ===============================//
		
		else {
			throw new InvalidTypeException("PostProcessingFactory: This post processor class " +
					"does not exist: "+className+".\nOr it has not been bound into the program properly");
		}
	}
}

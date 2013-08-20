/* 
* Created on 11. December 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;

/**
 *  This class contains static methods that create the correct objects for
 * the different parameter training algorithms. When a new algorithm that implements
 * {@link IParameterTraining} is created by a developer, these methods need to be updated!
 * 
 * @author Sita Lange
 *
 */
public class ParameterTrainingFactory {
	
	public enum EnumParameterTrainingClass{
		PARAMETERTRAINER_SE("ParameterTraining_SE", 0);
		
		// ===============================//
		// ADD ADDITIONAL POST PROCESSORS HERE!!             //
		// ===============================//
		
		
			private final String classname;
			private final int intvalue;
			
			EnumParameterTrainingClass(String classname, int intvalue){
				this.classname = classname;
				this.intvalue = intvalue;
			}
			
			public String getClassname() {return classname;}
			public int getIntvalue() {return intvalue;}
			
			/**
			 * Initialises the correct IParameterTraining implementation according to type.
			 * @return The correct {@link IParameterTraining} implementation.
			 */
			public IParameterTraining createParameterTrainer(){
				if(intvalue == 0){ return new ParameterTraining_SE(); } 	
				// ===============================//
				// ADD ADDITIONAL POST PROCESSORS HERE!!             //
				// ===============================//	
				else return null;
			}
			
			/**
			 * Gets all the class names for the parameter training algorithms and returns
			 * these in a String array.
			 * @return Array containing all existing class names of the {@link IParameterTraining} implementation.
			 */
			public static String[] getClassnames(){
				EnumParameterTrainingClass[] values = EnumParameterTrainingClass.values();
				String[] classnames = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					classnames[i] = values[i].getClassname();
				}
				return classnames;
			}
			
		}
	
	/**
	 * This method takes the given class name of a parameter training implementation and
	 * returns the internal enum type for this class.
	 * @param className Class name of the {@link IParameterTraining} implementation.
	 * @return The enum type for the implementation.
	 * @throws InvalidTypeException If the given class has not been implemented or has not been bound into the program correctly.
	 */
	public static EnumParameterTrainingClass getParameterTrainingEnumByClass(String 
			className) throws InvalidTypeException{
		if(className.equals(EnumParameterTrainingClass.PARAMETERTRAINER_SE.
				getClassname())){
			return EnumParameterTrainingClass.PARAMETERTRAINER_SE;
		}

		// ===============================//
		// ADD ADDITIONAL POST PROCESSORS HERE!!             //
		// ===============================//
		
		else {
			throw new InvalidTypeException("ParameterTrainingFactory: This parameter training class " +
					"has not been implemented or has not been bound in the program correctly: "+className);
		}
	}
}

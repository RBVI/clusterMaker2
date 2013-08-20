/* 
 * Created on 18. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusterTrainingTask;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.GeneralParameterTraining;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * Implements a simple evolutionary parameter training on the complete input, which is either
 * one file or a directory. The actual training works the same way as the general training for all
 * {@link ConnectedComponent} at once as in {@link GeneralParameterTraining}.
 * 
 * Simple description:
 * The number of generations and the number of configurations per generations are
 * taken from the config class. The initial generation consists of only random parameter
 * configurations and 10 more than the given number of configurations per generation.
 * The 10 best configurations are saved in a list of 'best parameter configurations'.
 * Then for each generation that should be trained the following make-up of parameters 
 * is used:
 * 1. First parameters object is created by using the mean value of each of the previously collected
 * best parameter configurations.
 * 2. The rest is divided into thirds.
 * 		a) A random combination of the previously found best parameter configurations
 * 		b) A random combination of the previously found best parameter configurations and
 * 			additionally new random parameter values.
 * 		c) A complete random set of new parameter configurations within given bounds.
 * 
 * Then each generation is started. For each parameter configuration in the generation,
 * clustering occurs with the given input configurations and for all input cost matrices. The
 * clustering score is added to a total and this is set as the score in the parameters object.
 * Then, according to this score, the set of parameter objects for one generation is sorted.
 * 
 * After each generation has been trained, the best half of the parameters are added
 * to the list of 'best parameter configurations'. Also it is checked if the training should be terminated.
 * This is done by looking at the scores. The training terminates when all scores are the
 * highest, allowing for only two configurations to be below this top score.
 * @author Sita Lange
 * 
 */
public class ParameterTraining_SE implements IParameterTraining {

	private static Logger log = Logger.getLogger(ParameterTraining_SE.class
			.getName());

	/* type of layouter that is being used */
	private LayoutFactory.EnumLayouterClass layouterEnum = null;

	/* size of each generationg */
	private int generationSize = -1;

	/* number of generations to carry out */
	private int noOfGenerations = -1;

	private ParametersComparator paramComparator = null;
	
	private int noOfThreads; //no of threads to use for training if this feature should be turned on.

	private Semaphore maxThreadSemaphore;
	private ArrayList<Thread> allThreads;
	
	/*
	 * previous best parameter configuration this means only one instance should
	 * be created for the parameter training of one dataset.
	 */
	private IParameters bestPreviousIParameters = null;

	private Vector<IParameters> bestConfigs = null;

	// private IParameters[] generationParameterSet = null;

	public void initialise(LayoutFactory.EnumLayouterClass layouterEnum,
			int generationSize, int noOfGenerations) {
		this.layouterEnum = layouterEnum;
		this.generationSize = generationSize;
		this.noOfGenerations = noOfGenerations;
		this.paramComparator = new ParametersComparator();
		this.bestConfigs = new Vector<IParameters>();
		
		/* check how many threads are left over to see if it is possible to use threads at all
		 *  and set noOfThreads to a minimum of 1 */
		this.noOfThreads = TaskConfig.maxNoThreads;
		if(this.noOfThreads ==0 || this.noOfThreads ==1){
			TaskConfig.useThreads = false;
//			TaskConfig.useThreadsForParameterTraining = false;
			this.noOfThreads = 1;
		}
	}

	public IParameters run(ConnectedComponent cc) {

		boolean terminateTraining = false;

		/*
		 * initialise positions of the cc - the same initial positions are used
		 * for all training rounds
		 */
		ILayoutInitialiser li = layouterEnum.createLayoutInitialiser();
		li.initLayoutInitialiser(cc);
		li.run();

		/* run initial generation */
		IParameters[] initialGeneration = createInitialParameterGeneration();
		runOneGeneration(initialGeneration, cc, 0);
		terminateTraining = terminateTraining(initialGeneration);
		/* add the best 10 random configs to the bestConfigs collection Vector */
		for (int i = 0; i < 10; i++) {//TODO
			this.bestConfigs.add(initialGeneration[i]);
		}
		// System.out.println("terminate training? "+terminateTraining);

		/* run all following generations */
		IParameters[] generation;
		for (int i = 1; i <= this.noOfGenerations; i++) {
			if (terminateTraining) {
				break;
			}

			generation = createParameterGeneration();
			runOneGeneration(generation, cc, i);
			terminateTraining = terminateTraining(generation);
			for (int j = 0; j < this.generationSize / 2; j++) {
				this.bestConfigs.add(generation[j]);
			}
		}

		/* convert best configurations vector to array */
		IParameters[] bestConfigsArray = new IParameters[bestConfigs.size()];
		for (int i = 0; i < bestConfigs.size(); i++) {
			bestConfigsArray[i] = bestConfigs.get(i);
		}
		/*
		 * sort the IParameters array according to their score and return the
		 * best parameter set.
		 */
		Arrays.sort(bestConfigsArray, this.paramComparator);
		this.bestPreviousIParameters = bestConfigsArray[0];
		
		
		bestConfigsArray[0].setScore(0.0);
		return bestConfigsArray[0];

	}

	private IParameters[] createInitialParameterGeneration() {
		// create enough random configurations to start with
		int initialSize = this.generationSize + 10;
		IParameters[] paramsGen = new IParameters[initialSize];
		IParameters param;
		/* get parameters from config */
		param = layouterEnum.createIParameters();
		param.readParametersFromConfig();
		paramsGen[0] = param;
		/*
		 * get the best parameter configuration for the previous training round
		 * if it exists.
		 */
		if (this.bestPreviousIParameters != null) {
			paramsGen[1] = bestPreviousIParameters;
		} else {
			param = layouterEnum.createIParameters();
			param.createRandomConfiguration();
			paramsGen[1] = param;
		}
		for (int i = 2; i < initialSize; i++) {
			param = layouterEnum.createIParameters();
			param.createRandomConfiguration();
			paramsGen[i] = param;
		}
		return paramsGen;
	}

	private IParameters[] createParameterGeneration() {
		IParameters[] paramsGen = new IParameters[this.generationSize];
		IParameters param;

		/* convert best configurations vector to array */
		IParameters[] bestConfigsArray = new IParameters[bestConfigs.size()];
		for (int i = 0; i < bestConfigs.size(); i++) {
			bestConfigsArray[i] = bestConfigs.get(i);
		}

		/* add mean of best configs */
		param = layouterEnum.createIParameters();
		param.combineConfigurationsRandomly(bestConfigsArray);
		paramsGen[0] = param;

		int currentPosition = 1;
		int third = (this.generationSize - 1) / 3;

		/* add combinations of the best configurations for first third */
		for (int i = currentPosition; i < currentPosition + third; i++) {
			param = layouterEnum.createIParameters();
			param.combineConfigurationsRandomly(bestConfigsArray);
			paramsGen[i] = param;
		}
		currentPosition += third;

		/* add combinations of best half plus new random parameters */
		for (int i = currentPosition; i < currentPosition + third; i++) {
			param = layouterEnum.createIParameters();
			param.combineParametersRandomlyAndGetNewRandom(bestConfigsArray);
			paramsGen[i] = param;
		}
		currentPosition += third;

		/* fill the rest with random configuratons */
		for (int i = currentPosition; i < this.generationSize; i++) {
			param = layouterEnum.createIParameters();
			param.createRandomConfiguration();
			paramsGen[i] = param;
		}
		return paramsGen;
	}

	/**
	 * Sorts the parameters according to their score and determines if the
	 * training should be terminated. If almost all parameter configurations
	 * have the same (best) score, except for 2, then the parameter training can
	 * be terminated. This means the best possible configuration has been found,
	 * or that it doesn't matter much which parameter values are used, the
	 * connected component is always clustered correctly (min score). Important
	 * is that this method is called after the training has been carried out for
	 * this generation - otherwise all scores are 0.
	 * 
	 * @param params
	 *            All parameter configurations of one generation.
	 * @return True if the training should be terminated, otherwise false to
	 *         carry on.
	 */
	private boolean terminateTraining(IParameters[] params) {
		Arrays.sort(params, this.paramComparator);
		double bestScore = params[0].getScore();
		if (bestScore == 0) {
			return false;
		}
		for (int i = 1; i < params.length - 2; i++) {
			if (params[i].getScore() > bestScore) {
				return false;
			}
		}
		return true;
	}

	private void runOneGeneration(IParameters[] generationParameterSet,
			ConnectedComponent cc, int gen) {
		int size = generationParameterSet.length;

//		try {
			// collect semaphores
//			List<Semaphore> parallelSem = new ArrayList<Semaphore>();
			for (int i = 0; i < size; i++) {

				/* copy the cc (ConnectedComponent) - shares same resources! */
				ConnectedComponent newCC = cc.copy();

				/* sets a new positions array for the copy */
				newCC.setCCPositions(cc.copyCCPositions());

				/* start clustering */
				ClusterTrainingTask clusteringTask = new ClusterTrainingTask(
						newCC, generationParameterSet[i], layouterEnum);

				/*check whether threads are being used and do things related to this */
				
//				if(TaskConfig.useThreads){
//					Semaphore semaphore = new Semaphore(1, true);
//					clusteringTask.setSemaphore(semaphore);
//					parallelSem.add(semaphore);
//					Thread t = new Thread(clusteringTask);
//					clusteringTask.setMaxThreadSemaphore(maxThreadSemaphore, this.allThreads, t);
//					t.start();
//				}else{
					clusteringTask.run(); 
//				}
			}

			/* wait until all threads are done! */
//			if (TaskConfig.useThreads) {
//
//				/*
//				 * For each semaphore it is tested if a permit can be aquired.
//				 * This forces the main program thread to wait until all threads
//				 * are finished.
//				 */
//				for (Semaphore sem : parallelSem) {
//					sem.acquire();
//				}
//			}
//
//		} catch (InterruptedException e) {
//			/*
//			 * Exception created by Semaphore.aquire() - if the thread is
//			 * interrupted
//			 */
//			log.severe(e.getMessage());
//			e.printStackTrace();
//		}
	}

	/**
	 * Sets the semaphore which tracks the maximum number of parallel threads running at
	 * one time. Also sets the list of all running threads.
	 * @param semaphore The Semaphore with the number of permits equals the max no. of parallel threads.
	 * @param allThreads The list with all running threads.
	 */
	public void setMaxThreadSemaphoreAndThreadsList(Semaphore semaphore, ArrayList<Thread> allThreads) {
		this.maxThreadSemaphore = semaphore;
		this.allThreads = allThreads;
	}

}

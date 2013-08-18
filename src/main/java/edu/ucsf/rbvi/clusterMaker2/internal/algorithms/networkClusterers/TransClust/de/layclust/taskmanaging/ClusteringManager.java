package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.CostMatrixReader;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.IParameterTraining;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ClusterFile;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.InfoFile;

/**
 * This class manages the complete clustering process. It reads the input, creates
 * the connected components and runs the clustering for each input cost matrix.
 * It starts parameter training if necessary and controls the number of threads that
 * can be used at one time. It also keeps track of the total clustering costs.
 * 
 * @author Sita Lange
 *
 */
public class ClusteringManager {
	
	private static Logger log = Logger.getLogger(ClusteringManager.class.getName());
	
	private String cmPath;
	private IParameters[] layouterParameters = null;
//	private ArrayList<ConnectedComponent> connectedComponents = null;
	private ArrayList<File> connectedComponents = null;
	private ArrayList<Thread> allThreads;
	private int noOfThreads;
	
	private static double totalScoreSum = 0; // total score for all cc
	
	public ClusteringManager(String cmPath){
		this.cmPath = cmPath;
		
		String configString = TaskConfig.printConfiguration().toString();
		if(TaskConfig.mode==TaskConfig.CLUSTERING_MODE||TaskConfig.mode==TaskConfig.GENERAL_TRAINING_MODE){
			log.info(configString);
			InfoFile.appendToProjectDetails(configString);
		}

		
		/* check how many threads are left over to see if it is possible to use threads at all
		 *  and set noOfThreads to a minimum of 1 */
		this.noOfThreads = TaskConfig.maxNoThreads;
		if(this.noOfThreads ==0 || this.noOfThreads ==1){
			TaskConfig.useThreads = false;
//			TaskConfig.useThreadsForParameterTraining = false;
			this.noOfThreads = 1;
		}
//		if(TaskConfig.useThreads){
//			log.info("Using threads with a maximum of "+this.noOfThreads+" running parallel");
//			this.allThreads = new ArrayList<Thread>();
//		}
		
	}
	
	/**
	 * Sets transitive connected components file if in input directory. Finds all cost matrix files
	 * given and creates a new {@link ConnectedComponent} instance for each and adds them to
	 * the list of ConnectedComponents.
	 * 
	 * Also initialises the {@link IParameters} from the input configuration.
	 * @throws InvalidInputFileException 
	 *
	 */
	public void initParametersAndCCs() throws InvalidInputFileException{
		
		
		if(TaskConfig.mode==TaskConfig.CLUSTERING_MODE||TaskConfig.mode==TaskConfig.GENERAL_TRAINING_MODE){
//			this.connectedComponents = new ArrayList<ConnectedComponent>();
			this.connectedComponents = new ArrayList<File>();
			TaskConfig.transitiveConnectedComponents = null;
			
			/* read the input file or directory */
			File cmFile = new File(this.cmPath);
			if(cmFile.isDirectory()){
				log.finer("Input is a directory!");
				
				/* get all files in the directory */
				File[] files = cmFile.listFiles();
				
				/* check boolean whether cm files exist in directory */
				boolean noCostMatrices = true; 
				boolean noTCCfile = true;
				
				for(int i = 0; i < files.length; i++){
					String filePath = files[i].toString();
					/* find tcc file in directory */
					if(filePath.endsWith(".tcc") || filePath.endsWith(".rtcc")){
						noTCCfile = false;
						TaskConfig.transitiveConnectedComponents = filePath;
						log.info("Transitive connected components file: "+filePath);
						InfoFile.appendToProjectDetails("Transitive connected component file: "+filePath);
						
					}
					
					/* find cm files*/
					if(files[i].toString().endsWith(".cm") || files[i].toString().endsWith(".rcm")){
						noCostMatrices = false; //cm files exist	
						connectedComponents.add(files[i]);
						// create the connected component (cc) object and add to list
//						CostMatrixReader cmReader = new CostMatrixReader(files[i]);
//						ConnectedComponent cc = cmReader.getConnectedComponent();
//						connectedComponents.add(cc);
					}
					
				}

				if(noCostMatrices){
					if(noTCCfile){
						throw new InvalidInputFileException("There are no cost matrix " +
							"files in the input directory and also no transitive connected components file, " +
							"or check whether the file extensions equal .cm, .rcm, or .tcc");
					}
				}
				
			} else{
				
				/* only one cost matrix file is given */
				log.finer("One cm file given");

				 /* only one cost matrix as input - start clustering process */
				if(!cmFile.toString().endsWith(".tcc")){
					
					// create the connected component (cc) object and add to list
//					CostMatrixReader cmReader = new CostMatrixReader(cmFile);
//					ConnectedComponent cc = cmReader.getConnectedComponent();
//					connectedComponents.add(cc);
					connectedComponents.add(cmFile);
				} else {
					if(cmFile.toString().endsWith(".tcc")){
						TaskConfig.transitiveConnectedComponents = cmFile.toString();
						log.info("Only a transitive connected component file is given: "+cmFile.toString());
						InfoFile.appendToProjectDetails("Only a transitive connected component file is given: "
								+cmFile.toString()+". Therefore NO CLUSTERING IS PERFORMED, just the the " +
										"clusters from the TCC file are written into the clusters file.");
					} else {
						throw new InvalidInputFileException("Either the input cost matrix is of " +
							"wrong file type. The file extension should be \".cm\" or \".rcm\"," +
							"or in the given TCC file is of the wrong type and should be \".tcc\".");		
					}
				}
			}	
		}
		
		
		
		/* initialise parameters from config */
//		LayoutFactory.EnumLayouterClass[] layouterEnumTypes = TaskConfig.layouterEnumTypes;
		layouterParameters = new IParameters[TaskConfig.layouterEnumTypes.length];
		for(int i=0;i<TaskConfig.layouterEnumTypes.length;i++){			

				IParameters param = TaskConfig.layouterEnumTypes[i].createIParameters();
				param.readParametersFromConfig();
				layouterParameters[i] = param;
		
		}
		
		if(this.connectedComponents==null){
			
		}
	}

	/**
	 * Runs the clustering with the given configurations in the config class: {@link TaskConfig}.
	 * Clusters each {@link ConnectedComponent} separately and waits until all are done. 
	 * Differes between the modes clustering and general training. Creates a Config file if
	 * the training mode is used.
	 * @throws InvalidInputFileException If the file/directory given produces an error.
	 * @throws InvalidTypeException An incorrect method implementation was given, or some
	 * other error occured with this.
	 */
	public void runClustering() throws InvalidInputFileException, InvalidTypeException {
			
		/* initialise ClusterFile if in clustering mode */
		ClusterFile clusterFile = null;
		if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
			log.fine("Running clustering in clustering mode!");
			clusterFile = new ClusterFile();
			clusterFile.instantiateFile(TaskConfig.clustersPath);
			clusterFile.printPreProcessingClusters(TaskConfig.transitiveConnectedComponents);
			/* check whether connectedComponents has been initialised */
			if(this.connectedComponents==null ){
				if(TaskConfig.transitiveConnectedComponents==null){
					log.warning("Incorrect use of the ClusteringManager, the connected components list" +
						"hadn't been initialised. Called method to initialise this and the parameters from " +
						"the config. Or only a TCC file was given and no connected components.");
					this.initParametersAndCCs();			
				} else {
					log.info("No cost matrices were given, just a transitive connected components file, which" +
							"is converted to a clusters file. NO CLUSTERING IS PERFORMED!");
					InfoFile.appendToProjectDetails("No cost matrices were given, just a transitive connected components file, which" +
							"is converted to a clusters file. NO CLUSTERING IS PERFORMED!");
				}
			}
		} 
		
		
		
		

		if(this.connectedComponents != null){
		
		/* go through cc list and start training for each and control thread use */
		ArrayList<Semaphore> allSemaphores = new ArrayList<Semaphore>();
		Semaphore maxThreadSemaphore = new Semaphore(TaskConfig.maxNoThreads, true);
		for(int i=0;i<this.connectedComponents.size();i++){
			Semaphore semaphore = new Semaphore(1);
			allSemaphores.add(semaphore);
			long time = System.currentTimeMillis();
			CostMatrixReader cmReader = new CostMatrixReader(this.connectedComponents.get(i));
			ConnectedComponent cc = cmReader.getConnectedComponent();
			runClusteringForOneConnectedComponent(cc, clusterFile, semaphore, maxThreadSemaphore, time);					
		}

		/* wait for all clustering tasks to finish */
		for (Semaphore s : allSemaphores) {
			try {
				s.acquire();
			} catch (InterruptedException e) {
				log.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		}

		if(clusterFile!=null)
			clusterFile.closeFile();
		
		
		/* END OF CLUSTERING */	 
		
		log.info("Clustering scores sum: "+totalScoreSum);
		if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
				InfoFile.appendLnProjectResults("Total sum of clustering scores for given input: "+TaskUtility.round(totalScoreSum, 2));
		}
		/* set score to IParameters objects for general training mode */
		if(TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
			log.fine("Setting parameters score for training mode!");
			for (IParameters parameter : this.layouterParameters) {
				parameter.setScore(totalScoreSum);			
				
			}
			
		}
		totalScoreSum = 0;
	}
	
	/**
	 * Runs clustering for one {@link ConnectedComponent} and sets the total score to
	 * the parameters if in the general training mode.
	 * @param cc The connected component object.
	 * @param clusterFile The clusters file (null if in general training mode)
	 * @param semaphore The Semaphore to give to the clustering task to keep track of it.
	 * @param time 
	 * @throws InvalidInputFileException 
	 */
	public void runClusteringForOneConnectedComponent(ConnectedComponent cc, 
			ClusterFile clusterFile, Semaphore semaphore, Semaphore maxThreadSemaphore, long time) 
			throws InvalidInputFileException{
		
//		long time = System.currentTimeMillis();
		
		/* check whether layouterParameters has been initialised */
		if(this.layouterParameters==null){
			if(TaskConfig.mode==TaskConfig.CLUSTERING_MODE||TaskConfig.mode==TaskConfig.GENERAL_TRAINING_MODE) log.warning("Incorrect use of the ClusteringManager, the layouter parameters list" +
					"hadn't been initialised. Called method to initialise this and the connected components from " +
					"the config");

			this.initParametersAndCCs();
		}
		
		/* if in clustering mode and layout parameter training is used, then
		 * create the parameters using the parameter training for each individual 
		 * connected component
		 */
//TODO nicht sicher ob ich das if rausnehmen kann
//		if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
			if(TaskConfig.doLayoutParameterTraining&&!TaskConfig.greedy){
				for(int i=0;i<this.layouterParameters.length;i++){
					/* start parameter training for the cc */				
					IParameterTraining paramTrain = TaskConfig.parameterTrainingEnum.createParameterTrainer();
					paramTrain.initialise(TaskConfig.layouterEnumTypes[i], 
							TaskConfig.noOfParameterConfigurationsPerGeneration,
							TaskConfig.noOfGenerations);
					paramTrain.setMaxThreadSemaphoreAndThreadsList(maxThreadSemaphore, this.allThreads);
					IParameters bestparam = paramTrain.run(cc);
					log.fine("PARAMETER TRAINING RESULT\n: "+cc.getCcPath()+"\n"+bestparam.toString());
					this.layouterParameters[i] = bestparam;
				}				
			}
//		}
		
		/* run clustering with the previously determined parameters */
		ClusteringTask clusterTask = new ClusteringTask(cc, this.layouterParameters,
				TaskConfig.layouterEnumTypes, clusterFile);
		clusterTask.setTime(time);
		
//		if(!TaskConfig.doLayoutParameterTraining&&TaskConfig.useThreads){
//			clusterTask.setSemaphore(semaphore);
//			Thread t = new Thread(clusterTask);
//			clusterTask.setMaxThreadSemaphore(maxThreadSemaphore, allThreads, t);			
//			t.start();
//		}else{
			clusterTask.run(); 
//		}
		
	}

	/**
	 * @return the connectedComponents
	 */
	public ArrayList<File> getConnectedComponents() {
		return connectedComponents;
	}

	/**
	 * @param connectedComponents the connectedComponents to set
	 */
	public void setConnectedComponents(
			ArrayList<File> connectedComponents) {
		this.connectedComponents = connectedComponents;
	}

	/**
	 * @return the layouterParameters
	 */
	public IParameters[] getLayouterParameters() {
		return layouterParameters;
	}

	/**
	 * @param layouterParameters the layouterParameters to set
	 */
	public void setLayouterParameters(IParameters[] layouterParameters) {
		this.layouterParameters = layouterParameters;
	}
	
	/**
	 * This method adds one clustering score to the total clustering score for
	 * the whole directory. 
	 * 
	 * @param score The score to be added to the total score.
	 */
	public static synchronized void addClusteringScoreToSum(double score){
		totalScoreSum += score;
	}

	/**
	 * @return the totalScoreSum
	 */
	public double getTotalScoreSum() {
		return totalScoreSum;
	}

	/**
	 * @param totalScoreSum the totalScoreSum to set
	 */
	public void setTotalScoreSum(double totalScoreSum) {
		ClusteringManager.totalScoreSum = totalScoreSum;
	}
	
	/**
	 * Stops all currently running threads that have been started in runOneConnectedComponent.
	 *
	 */
	@SuppressWarnings("deprecation")
	public void stopAllRunningThreads(){
		if(this.allThreads!=null){
			for (Thread t : this.allThreads) {
				if(t.isAlive()){
					t.stop();
				}
			}
		}
	}
}

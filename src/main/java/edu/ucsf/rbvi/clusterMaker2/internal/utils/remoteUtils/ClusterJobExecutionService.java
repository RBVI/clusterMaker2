package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJob;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobDataService;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Leiden.LeidenCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;

/**
 * The main interface to the RBVI network cluster REST service.  The
 * general URL format for the execution is:
 * 	http://www.rbvi.ucsf.edu/clusterService/submit?algorithm=alg&param1=param&param2=param&...
 * followed by a JSON data set with all of the edges:
 * {
 * 	edges: [
 * 		{ source: SUID, target: SUID, sourceName: name, targetName: name, attrName: value }
 * 	]
 * }
 * where attrName is the name of the attribute to be used for the cluster algorithm.  
 * This should immediately return with a job id.
 *
 * To check the status of a job:
 * 	http://www.rbvi.ucsf.edu/clusterService/check?jobId=jobId
 *
 * To cancel a job:
 * 	http://www.rbvi.ucsf.edu/clusterService/cancel?jobId=jobId
 *
 * To get the completed data:
 * 	http://www.rbvi.ucsf.edu/clusterService/fetch?jobId=jobId
 *
 * The return
 * value is a very simple JSON:
 * {
 * 	nodes: [
 * 		{ node: SUID, clusterNumber: value }
 * 	]
 * }
 */

public class ClusterJobExecutionService implements CyJobExecutionService {
	static final Logger logger = Logger.getLogger(CyUserLog.NAME);
	static final String COMMAND = "command";
	static final String ERROR = "errorMessage";
	static final String JOBID = "job_id";
	static final String STATUS = "jobStatus";
	static final String STATUS_MESSAGE = "message";
	static final String SUBMIT = "submit";
	final ClusterJobDataService dataService;
	final CyJobManager cyJobManager; //responsible for managing all the running ClusterJobs: polls the changes in the Status, calls some methods in this class
	final CyServiceRegistrar cyServiceRegistrar;
	

	public enum Command {
		CANCEL("cancel"),
		SUBMIT("submit"),
		FETCH("fetch"),
		CHECK("check");

		String text;
		Command(String text) {
			this.text = text;
		}
		public String toString() { return text; }
	}

	//constructor
	public ClusterJobExecutionService(CyJobManager manager, CyServiceRegistrar registrar) {
		cyJobManager = manager;
		cyServiceRegistrar = registrar;
		dataService = new ClusterJobDataService(cyServiceRegistrar);
	}

	@Override
	public CyJobDataService getDataService() { return dataService; }

	//create a CLusterjob
	@Override
	public CyJob createCyJob(String name) {
		return new ClusterJob(name, null, this, dataService, null, null);
	}

	@Override
	public String getServiceName() { return "ClusterJobExecutionService"; }

	//checks whether the CyJob is a ClusterJob and cancels it, returns the status of the job
	@Override 
	public CyJobStatus cancelJob(CyJob job) {
		System.out.println("Canceling the job!");
		if (job instanceof ClusterJob) {
			JSONObject obj = handleCommand((ClusterJob)job, Command.CANCEL, null);
			return getStatus(obj, null);
		}
		return new CyJobStatus(Status.ERROR, "CyJob is not a ClusterJob");
	}

	//returns the status of the CLusterJob
	@Override
	public CyJobStatus checkJobStatus(CyJob job) {
		if (job instanceof ClusterJob) {
			JSONObject result = handleCommand((ClusterJob)job, Command.CHECK, null);
			return getStatus(result, null);
		}
		return new CyJobStatus(Status.ERROR, "CyJob is not a ClusterJob");
	}

	//checks if the CyJob is a ClusterJob, fetches the serialized data (JSON) using DataService methods, puts JSON information in the ClusterJob and returns the status
	@Override
	public CyJobStatus executeJob(CyJob job, String basePath, Map<String, Object> configuration, //configuration comes from network data
	                              CyJobData inputData) {
		if (!(job instanceof ClusterJob))
			return new CyJobStatus(Status.ERROR, "CyJob is not a ClusterJob"); //error message if not clusterjob

		ClusterJob clJob = (ClusterJob)job; //converts CyJob into ClusterJob
		Map<String, String> queryMap = convertConfiguration(configuration); //converts configuration into Map<String, String>

		String serializedData = dataService.getSerializedData(inputData); //gets serialized data (JSON) using dataService
		System.out.println("Serialized data in execution service: " + serializedData); 
		queryMap.put("inputData", serializedData.toString()); //...and puts it into queryMap as key: "inputData", value: String of the data
		queryMap.put(COMMAND, Command.SUBMIT.toString()); //puts key: COMMAND, value: SUBMIT in the queryMap --> queryMap has two keys
		
		JSONParser parser = new JSONParser();
		JSONObject jsonData = null;
		try {
			jsonData = (JSONObject) parser.parse(serializedData);
		} catch (ParseException e1) {
			System.out.println("Data to JSONObject conversion failed: " + e1.getMessage());
		}
		
		Object value = null;
		String jobName = (String) clJob.getClusterData().get("shortName");
		try {
			value = RemoteServer.postFile(RemoteServer.getServiceURI(jobName), jsonData);
		} catch (Exception e) {
			System.out.println("Error in postFile method: " + e.getMessage());
		}
		System.out.println("JSON Job ID: " + value);
		
		if (value == null) 
			return new CyJobStatus(Status.ERROR, "Job submission failed!");
		JSONObject json = (JSONObject) value;
		if (!json.containsKey(JOBID)) {
			System.out.println("JSON returned: "+json.toString());
			return new CyJobStatus(Status.ERROR, "Server didn't return an ID!");
		}

		System.out.println("JSONObject postFile(): " + json);
		
		String jobId = json.get(JOBID).toString(); //gets the job ID from the JSON Object
		clJob.setJobId(jobId); //...and sets it to the ClusterJob 
		System.out.println("ClusterJob jobID: " + clJob.getJobId());
		//everything above this is to get the job ID from the JSON jobID repsonse from postFile() and put it in the ClusterJob object
		
		clJob.setBasePath(basePath); //...and also sets the basePath to the Cluster Job
		System.out.println("ClusterJob BasePath: " + clJob.getBasePath());
		
		//getting status
		int waitTime = 20;
		CyJobStatus status = checkJobStatus(clJob);
		for (int i = 0; i < waitTime; i += 5) {
		    if (jobDone(status)) return status;

		    try {
		        TimeUnit.SECONDS.sleep(5);
		    } catch (InterruptedException e) {
		        return new CyJobStatus(Status.ERROR, "Interrupted");
		    }
		    
		    status = checkJobStatus(clJob);
		}
		
		return status;
	}
	
	private boolean jobDone(CyJobStatus status) {
	    CyJobStatus.Status st = status.getStatus();
	    if (st == Status.FINISHED ||
	      st == Status.CANCELED ||
	      st == Status.FAILED ||
	      st == Status.TERMINATED ||
	      st == Status.PURGED)
	        return true;
	    return false;
	}
 
	
	//fetches JSON object, deserializes the data and puts it to CyJobData
	@Override
	public CyJobStatus fetchResults(CyJob job, CyJobData data) {
		if (job instanceof ClusterJob) {
			
			ClusterJob clusterJob = (ClusterJob) job;
			//handleCommand gives whatever HttpGET gives.
		    JSONObject result = handleCommand(clusterJob, Command.FETCH, null); //handles command FETCH --> argMap is null --> JSON object runs the command
		    System.out.println("fetchResults() JSONObject result: " + result);
			
			// Get the unserialized data, dataService deserializes the data (the JSON object), CyJobData is basically a HashMap
		    CyJobData newData = dataService.deserialize(result); 
		    System.out.println("CyJobData results: " + newData.getAllValues());
			
			// Merge it in, move the information from newData to data
		    for (String key: newData.keySet()) {
		    	data.put(key, newData.get(key));
		    }
			
		    System.out.println("fetchResults() CyJobData: " + data.getAllValues());

		    Map<String, Object> clusterData = clusterJob.getClusterData().getAllValues();
		    System.out.println("fetchResults() ClusterJob: " + clusterData);
		    
		    // HERE I should take the results from UMAP {embedding=[["Node","x","y"],["YKR026C",12.655669212341309,13.004707336425781]....], "status" : "done"}
		    // and make the scatter plot that needs the coordinates and the node name
		    // make another method for dimensionality reduction? one that would see what form the data is ? either CLUSTER or DIMENSIONALITY --> different processing
		    // OR make an if sentence to this method?
			
			String clusterAttributeName = (String) clusterData.get("clusterAttributeName");
			CyNetwork network = (CyNetwork) clusterData.get("network");
			ClusterManager clusterManager = (ClusterManager) clusterData.get("clusterManager");
			Boolean createGroups = (Boolean) clusterData.get("createGroups");
			String group_attr = (String) clusterData.get("group_attr");
			List<String> params  = (List<String>) clusterData.get("params");
			String shortName = (String) clusterData.get("shortName");
			
			// if we are running a dimensionality reduction algorithm, now UMAP only
			if (shortName.equals("umap")) {
				JSONArray embedding = (JSONArray) data.get("embedding");
				int size = embedding.size(); 
        
				CyNode[] nodes = new CyNode[size-1]; 
				double[][] coordinates = new double[size-1][2];
				
				for (int i = 1; i < size; i++) {
					JSONArray nodeData = (JSONArray) embedding.get(i);
					String nodeName = (String) nodeData.get(0);
					
					for (CyNode cyNode : network.getNodeList()) {// getting the cyNode object with the name of the node
						if (network.getRow(cyNode).get(CyNetwork.NAME, String.class).equals(nodeName)) {
							nodes[i-1] = cyNode;

						}
					} 
					
					double x = (Double) nodeData.get(1); 
					double y = (Double) nodeData.get(2); 
					coordinates[i-1][0] = x;
					coordinates[i-1][1] = y;
				}

				
				CyTable nodeTable = network.getDefaultNodeTable();
				nodeTable.createColumn("newmap_x", Double.class, false);
				nodeTable.createColumn("newmap_y", Double.class, false);
				
				for (int i = 0; i < nodes.length; i++) {
				   if (nodes[i] != null) {
					   network.getRow(nodes[i]).set("newmap_x", coordinates[i][0]);
					   System.out.println("X value from the table : " + network.getRow(nodes[i]).get("newmap_x", Double.class));
					   network.getRow(nodes[i]).set("newmap_y", coordinates[i][1]);
					   System.out.println("Y value from the table : " + network.getRow(nodes[i]).get("newmap_y", Double.class));
				   }
				}
				
					// If we want to use ScatterPlotDialog
				
				//ClusterManager manager = (ClusterManager) clusterData.get("manager");
				//ScatterPlotDialog scatter = new ScatterPlotDialog(manager, "UMAP scatterplot", null, nodes, coordinates);
				//System.out.println("scatter plot : " + scatter);
				
			} else { // for clustering algorithms
				List<NodeCluster> nodeClusters = createClusters(data, clusterAttributeName, network); //move this to remote utils
				System.out.println("NodeClusters: " + nodeClusters);
		
				AbstractNetworkClusterer.createGroups(network, nodeClusters, group_attr, clusterAttributeName, 
						clusterManager, createGroups, params, shortName);
			}
			
			CyJobStatus resultStatus = getStatus(result, null);
			if (resultStatus == null)
				return new CyJobStatus(Status.FINISHED, "Data fetched"); //returns status FINISHED if succesfull

		}
		return new CyJobStatus(Status.ERROR, "CyJob is not a ClusterJob"); //if not a clusterjob
	}
	
	
	//returns a list of NodeCluster objects that have a number and a list of nodes belonging to it
	public static List<NodeCluster> createClusters(CyJobData data, String clusterAttributeName, CyNetwork network) {
		JSONArray partitions = (JSONArray) data.get("partitions");
		
		List<NodeCluster> nodeClusters = new ArrayList<>();
		int i = 1; //each cluster is assigned a number
		for (Object partition : partitions) {
			List<String> cluster = (ArrayList<String>) partition;
			List<CyNode> cyNodes = new ArrayList<>();
			for (String nodeName : cluster) {
				for (CyNode cyNode : network.getNodeList())
					if (network.getRow(cyNode).get(CyNetwork.NAME, String.class).equals(nodeName)) {
						cyNodes.add(cyNode);
					}
			}

			NodeCluster nodeCluster = new NodeCluster(i, cyNodes);
			nodeClusters.add(nodeCluster);
			i++;
		}
		return nodeClusters;
	}

	@Override
	public CyJob restoreJobFromSession(CySession session, File sessionFile) {
		CyJob job = null;
		try {
			FileReader reader = new FileReader(sessionFile);
			CyJobData sessionData = dataService.deserialize(reader);
			job = getCyJob(sessionData.get("name").toString(), 
			               sessionData.get("path").toString(),
										 sessionData.get("JobId").toString());
			job.setPollInterval((Integer)sessionData.get("pollInterval"));
			String handlerClass = sessionData.get("jobMonitor").toString();
			if (!handlerClass.equals(ClusterJobHandler.class.getCanonicalName())) {
				cyJobManager.associateMonitor(job, handlerClass, -1);
			}
		} catch (FileNotFoundException fnf) {
			logger.error("Unable to read session file!");
		}

		return job;
	}

	//getter, creates new ClusterJob, puts this as the ExecutionService, the same DataService as in this, returns the new clusterjob
	private CyJob getCyJob(String name, String basePath, String jobId) {
		return new ClusterJob(name, basePath, this, dataService, null, jobId);
	}

	@Override
	public void saveJobInSession(CyJob job, File sessionFile) {
		// Create a JSON object for our ClusterJob
		CyJobData sessionData = dataService.getDataInstance();
		sessionData.put("name", job.getJobName());
		sessionData.put("JobId", job.getJobId());
		sessionData.put("path", job.getPath());
		sessionData.put("pollInterval", job.getPollInterval());
		sessionData.put("jobMonitor", job.getJobMonitor().getClass().getCanonicalName());
		String data = dataService.getSerializedData(sessionData).toString();
		try {
			FileWriter writer = new FileWriter(sessionFile);
			writer.write(data);
			writer.close();
		} catch (IOException ioe) {
			logger.error("Unable to save job "+job.getJobId()+" in session!");
		}
	}


	
	//compare f ex "done" and map that to the status ENUM
	//added return new CyJobStatus
	private CyJobStatus getStatus(JSONObject obj, String message) {
		if (obj.containsKey(STATUS)) {
			Status status = Status.UNKNOWN;
			if (obj.get(STATUS).equals("done")) {
				status = Status.FINISHED;
			} else if (obj.get(STATUS).equals("running")) {
				status = Status.RUNNING;
			}
			// Did we get any information about our status?
			if (obj.containsKey(STATUS_MESSAGE)) {
				if (message == null || message.length() == 0)
					message = (String)obj.get(STATUS_MESSAGE);
				return new CyJobStatus(status, message);
			}
			return new CyJobStatus(status, message);
		} else if (obj.containsKey(ERROR)) {
			return new CyJobStatus(Status.ERROR, (String)obj.get(ERROR));
		}
		return null;
	}

	private JSONObject handleCommand(ClusterJob job, Command command, Map<String, String> argMap) { //argMap contains COMMAND and a JOB ID
		if (argMap == null)
			argMap = new HashMap<>();

		argMap.put(COMMAND, command.toString());
		argMap.put(JOBID, job.getJobId());
	    System.out.println("handleCommand argmap: " + argMap);
		
		JSONObject response = null;
		
		if (command == Command.CHECK) {
			try {
				response = RemoteServer.fetchJSON(job.getBasePath() + "status/" + job.getJobId(), command);
			} catch (Exception e) {
				System.out.println("Exception in fetchJSON: " + e.getMessage());
			}
			
		} else if (command == Command.FETCH) {
			try {
				response = RemoteServer.fetchJSON(job.getBasePath() + "fetch/" + job.getJobId(), command);
			} catch (Exception e) {
				System.out.println("Exception in fetchJSON: " + e.getMessage());
			}
		}
		
		System.out.println("FetchJSON: " + response + "/nmessage: ");
		return response;
	}

	//turns Map<String, Object> into Map<String, String>
	private Map<String, String> convertConfiguration(Map<String, Object> config) {
		Map<String, String> map = new HashMap<>();
		if (config == null || config.size() == 0)
			return map;
		for (String key: config.keySet()) {
			map.put(key, config.get(key).toString());
		}
		return map;
	}
	
	public CyNetwork getNetwork() {
		return null;
	}
}


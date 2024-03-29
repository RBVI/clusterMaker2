package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobExecutionService.Command;


public class RemoteServer {

	static private String LOCAL_PATH = "http://localhost:8000/";
	static private String PROD_PATH = "https://webservices.rbvi.ucsf.edu/rest/api/v1/";
	
		
	static public String getBasePath() {
		return PROD_PATH;
	}
	
	    // service is the shortname of the algorithm
		//make the choice between different clustering algorithms/servers, this works
	static public String getServiceURI(String service) {
		return PROD_PATH + "service/" + service;

	}

  static public String getServiceURIWithArgs(String service, Map<String, String> args) {
    String uri = getServiceURI(service) + '?';
    for (String key: args.keySet()) {
      uri += key+"="+args.get(key)+'&';
    }
    return uri.substring(0, uri.length()-1);
  }


		//sends the data
		//you should use a JSONParser to parse the reply and return that.
		//the URI is the whole URI of the service
		//returns the status code and JSONObject (JobID)
	static public JSONObject postFile(String uri, JSONObject jsonData) throws Exception {
		// System.out.println("Posting on: " + uri);
		CloseableHttpClient httpClient = HttpClients.createDefault();  //client = browser --> executes in the default browser of my computer?
		
		CloseableHttpResponse response = getPOSTresponse(uri, jsonData, httpClient);
		// System.out.println("HttpPOST response: " + response.toString());
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200 && statusCode != 202) {
			return null;
		}
			
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); // it's just a single string that contains a JSON representation of the job id.
		
		JSONParser parser = new JSONParser();
		JSONObject jsonJobID = (JSONObject) parser.parse(reader); //parses the string of jobID into JSONObject
		
		
		return jsonJobID;
	}
	
	static private CloseableHttpResponse getPOSTresponse(String uri, JSONObject jsonData, CloseableHttpClient httpClient) throws Exception {
		HttpPost httpPost = new HttpPost(uri);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create(); //builds the entity from the JSON data, entity= entire request/response w/o status/request line
		builder.addTextBody("data", jsonData.toString());
		HttpEntity entity = builder.build();
		httpPost.setEntity(entity); //posts the entity
		CloseableHttpResponse response = httpClient.execute(httpPost); //is this the jobID by itself or does it have to be called with some method?
		
		return response;
	}
	
	//this returns the results as JSON
	//CyJobExcService has a checkStatus() and gets results: translate to CyJobStatus that is an ENUM
	//replace the handle command with an appropriate command of remote server
	//parse the json
	static public JSONObject fetchJSON(String uri, Command command) throws Exception {
		System.out.println("Fetching JSON from: " + uri);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();  //client = browser --> executes in the default browser of my computer?
		HttpGet httpGet = new HttpGet(uri);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		
		System.out.println("HttpGET response: " + response.toString());
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200 && statusCode != 202) {
			System.out.println("Status code not 200!");
			return null;
		}
		HttpEntity entity = response.getEntity();
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
		
    JSONObject json = null;
		if (command == Command.CHECK) {
      System.out.println("Creating parser");
			JSONParser parser = new JSONParser();
      System.out.println("Parsing data");
      try {
			json = (JSONObject) parser.parse(reader); 
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("Status: "+json.toString());
      if (json.containsKey("status")) {
        json.put("jobStatus", json.get("status"));
      }
      /*
			String line = "";
			Object message = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("fetchJSON: "+line);
				json.put("jobStatus", line);
				if (json.containsKey("message")) {
					message = json.get("message");
					json.put("message: ", message);
				} else {
					json.put("message", line);
				}
			}
      */
		} else if (command == Command.FETCH) {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(reader); 
		}

		return json; //= dictionary, take it and poll from this the status key Map<Key, value> and for key status there is some answer, JSON similar to xml but easier
	}



}
